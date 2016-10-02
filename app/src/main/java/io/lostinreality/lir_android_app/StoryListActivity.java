package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StoryListActivity extends AppCompatActivity implements OnMapReadyCallback, ChooseStoryFormatDialogFragment.NoticeDialogListener {
    private Location lastLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RecyclerView storyListView;
    private FrameLayout storyListOverlay;
    private RelativeLayout mapFramentContainer;
    private GoogleMap mMap;
    private StoryListAdapter storyListAdapter;
    private JsonArrayRequest loadStoriesRequest;
    private ImageView openCreateStoryActivityButton;
    private ImageView openProfileActivityButton;
    private ImageView mapListViewsSwitchButton;
    private User user;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Activity _this = this;
    private List<Story> lastStoriesFeed;
    private ProgressBar loadStoriesProgressBar;
    private Location mMapCurrentPosition;
    private SwipeRefreshLayout storylistviewparent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);


        storyListView = (RecyclerView) findViewById(R.id.story_list_view);
        storyListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        storyListView.setLayoutManager(llm);

        storyListAdapter = new StoryListAdapter(this);
        storyListView.setAdapter(storyListAdapter);

        mapFramentContainer = (RelativeLayout) findViewById(R.id.map_fragment_container);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);

        storyListOverlay = (FrameLayout) findViewById(R.id.story_list_overlay);
        storyListOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyListOverlay.animate().alpha(0f).setDuration(500);
                v.setVisibility(View.INVISIBLE);
                mapFramentContainer.setVisibility(View.GONE);
            }
        });

        sharedPref = getSharedPreferences("app_data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        openCreateStoryActivityButton = (ImageView) findViewById(R.id.open_create_story_button);
        openCreateStoryActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new ChooseStoryFormatDialogFragment();
                dialog.show(getSupportFragmentManager(), "ChooseStoryFormatDialogFragment");

            }
        });

        mapListViewsSwitchButton = (ImageView) findViewById(R.id.map_list_views_switch_button);
        mapListViewsSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFramentContainer.getVisibility() == View.VISIBLE) {
                    storyListOverlay.animate().alpha(0f).setDuration(500);
                    storyListOverlay.setVisibility(View.INVISIBLE);
                    mapFramentContainer.setVisibility(View.GONE);
                } else {
                    LayoutVisibilityAnimation layoutanim = new LayoutVisibilityAnimation(mapFramentContainer,View.VISIBLE);
                    layoutanim.setDuration(500);
                    layoutanim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            List<Story> list = getLastFeedStories();
                            LatLng latLng = mMap.getCameraPosition().target;
                            mMapCurrentPosition = new Location(latLng.latitude, latLng.longitude, getString(R.string.current_location), 0.0, Constants.DEFAULT_ZOOM);
                            if (list != null) {
                                Story fartheststory = getFarthestStoryFromList(list);
                                Location farthestdist = fartheststory.getLocation();
                                int padding = 100;
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getFitBounds(mMapCurrentPosition, farthestdist), padding));
                            } else {
                                float zoom = (float) Constants.DEFAULT_ZOOM;
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMapCurrentPosition.latitude, mMapCurrentPosition.longitude), zoom));
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    mapFramentContainer.startAnimation(layoutanim);
                    storyListOverlay.setVisibility(View.VISIBLE);
                    storyListOverlay.animate().alpha(.5f).setDuration(500);
                }

            }
        });


        openProfileActivityButton = (ImageView) findViewById(R.id.open_user_profile_button);
        openProfileActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        loadStoriesProgressBar = (ProgressBar) findViewById(R.id.load_stories_progress_bar);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(android.location.Location l) {
                // Called when a new location is found by the network location provider.
                if (getLastLocation() == null) {
                    lastLocation = new Location(l.getLatitude(), l.getLongitude(), getString(R.string.current_location), 0.0, Constants.DEFAULT_ZOOM);
                    loadStoriesFromServerWithLocation();
                } else {
                    lastLocation = new Location(l.getLatitude(), l.getLongitude(), getString(R.string.current_location), 0.0, Constants.DEFAULT_ZOOM);
                }
                setLastLocation(lastLocation);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(l.getLatitude(), l.getLongitude())));
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        storylistviewparent = (SwipeRefreshLayout) findViewById(R.id.story_list_view_parent);
        storylistviewparent.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadStoriesFromServerWithLocation();
                    }
                }
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        // Register the listener with the Location Manager to receive location updates
        if (Utils.mayRequestLocation(_this,storyListView))
            startLocationUpdates();
        if (getLastLocation() != null) {
            loadStoriesFromServerWithLocation();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Utils.mayRequestLocation(_this,storyListView))
            locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onDialogSingleStoryClick(DialogFragment dialog) {
        Intent intent = new Intent(getBaseContext(), CreateSingleStoryActivity.class);
        intent.putExtra("current_location", (Parcelable) getLastLocation());
        startActivity(intent);
        dialog.dismiss();
    }

    @Override
    public void onDialogOpenStoryClick(DialogFragment dialog) {
        Intent intent = new Intent(getBaseContext(), CreateOpenStoryActivity.class);
        intent.putExtra("current_location", (Parcelable) getLastLocation());
        startActivity(intent);
        dialog.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Location ll = getLastLocation();
        if (ll == null) return;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ll.latitude, ll.longitude), (float) Constants.DEFAULT_ZOOM));
        if (Utils.mayRequestLocation(_this,findViewById(R.id.map_fragment_container)))
            mMap.setMyLocationEnabled(true);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                ProgressBarAnimation anim = new ProgressBarAnimation(loadStoriesProgressBar, 0, 100);
                anim.setDuration(2100);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        LatLng latLng = mMap.getCameraPosition().target;
                        mMapCurrentPosition = new Location(latLng.latitude, latLng.longitude, getString(R.string.current_location), 0.0, Constants.DEFAULT_ZOOM);
                        loadStoriesFromServerWithLocation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                loadStoriesProgressBar.startAnimation(anim);
            }
        });
    }

    public void onConnectionRestablished() {
        loadStoriesFromServerWithLocation();
    }

    public void onStoryLiked(Integer nolikes, Boolean currentuserlikes, StoryListAdapter.StoryViewHolder holder) {
        holder.setNoLikesLabelView(nolikes,currentuserlikes);
    }

    public void onStoryBookmarked(Integer nosaves, Boolean currentuserbookmarked, StoryListAdapter.StoryViewHolder holder) {
        holder.setNoBookmarksLabelView(nosaves,currentuserbookmarked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_list_activity_ab_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                try {
                    Intent searchintent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
                    startActivityForResult(searchintent, Constants.REQUEST_CODE_PLACE_AUTOCOMPLETE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: handle error
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: handle error
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_PLACE_AUTOCOMPLETE:
                    switch (resultCode) {
                        case RESULT_OK:
                            final Place place = PlaceAutocomplete.getPlace(this, data);
                            final LatLng latlng = place.getLatLng();
                            LayoutVisibilityAnimation layoutanim = new LayoutVisibilityAnimation(mapFramentContainer,View.VISIBLE);
                            layoutanim.setDuration(500);
                            layoutanim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 0));
                                    mMapCurrentPosition = new Location(latlng.latitude, latlng.longitude, place.getName().toString(), 0.0, Constants.DEFAULT_ZOOM);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                            mapFramentContainer.startAnimation(layoutanim);
                            storyListOverlay.setVisibility(View.VISIBLE);
                            storyListOverlay.animate().alpha(.5f).setDuration(500);
                            break;
                        case RESULT_CANCELED:
                            // User cancelled action
                            break;
                        case PlaceAutocomplete.RESULT_ERROR:
                            Status status = PlaceAutocomplete.getStatus(this, data);
                            // TODO: Handle the error.
                            break;
                    }
                    break;


            }
        }
    }

    private void loadStoriesFromServerWithLocation() {
        if (mMapCurrentPosition != null)
            setLoadStoriesRequest(mMapCurrentPosition);
        else if (getLastLocation() != null)
            setLoadStoriesRequest(getLastLocation());
        else
            return;
        RequestQueue queue = RequestsSingleton.getInstance(this).getRequestQueue();
        queue.add(loadStoriesRequest);
    }

    private void setLoadStoriesRequest(Location location) {
        String url = Constants.LOAD_PUBLIC_FOLLOWING_AND_PRIVATE_STORIES_WITH_LOCATION_API_ENTRY + location.latitude + "/" + location.longitude + "/" + 0 + "/" + 20;
        loadStoriesRequest = new JsonArrayRequest(Request.Method.GET, url, null, new ResponseListener(), new ErrorListener(this));
    }


    /**
     * Runs when a JsonArrayRequest object successfully gets an response.
     */
    class ResponseListener implements Response.Listener<JSONArray> {
        @Override
        public void onResponse(JSONArray response) {
            Story[] jsonStory = new Gson().fromJson(response.toString(), Story[].class);
            ArrayList<Story> stories = new ArrayList<>(Arrays.asList(jsonStory));
            loadStoriesOnAdapter(stories);
            setLastFeedStories(stories);
            populateMapMarkers(stories);
            storylistviewparent.setRefreshing(false);
        }
    }

    class ErrorListener implements Response.ErrorListener {
        private Context context;
        public ErrorListener(Context ctx) {context = ctx;}
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof NoConnectionError) {
                Toast.makeText(context, getString(R.string.offline_message), Toast.LENGTH_LONG).show();
            } else {
                Utils.reattemptlogin(context);
                //Toast.makeText(context, "Couldn't load stories.", Toast.LENGTH_LONG).show();
            }
            List<Story> stories = getLastFeedStories();
            loadStoriesOnAdapter(stories);
            storylistviewparent.setRefreshing(false);
        }
    }

    private void loadStoriesOnAdapter(List<Story> slist) {
        if (slist == null) return;
        ArrayList<Story> filteredstories = new ArrayList<>();
        for (Story story : slist) {
            if (!story.isDummy() || story.getNoStories() > 0)
                filteredstories.add(story);
        }
        storyListAdapter.setStoryList(filteredstories);
        storyListAdapter.notifyDataSetChanged();
        return;
    }

    private void setLastLocation(Location l) {
        editor.putString("last_location", new Gson().toJson(l));
        editor.commit();
    }

    private Location getLastLocation() {
        if (lastLocation != null) {
            return lastLocation;
        } else {
            String llocationstring = sharedPref.getString("last_location",null);
            if (llocationstring != null) {
                return new Gson().fromJson(llocationstring, Location.class);
            }
        }
        return null;
    }

    private void setLastFeedStories(List<Story> storyList) {
        lastStoriesFeed = storyList;
        editor.putString("last_feed_stories", new Gson().toJson(storyList));
        editor.commit();
    }

    private List<Story> getLastFeedStories() {
        if (lastStoriesFeed != null) {
            return lastStoriesFeed;
        } else {
            String storyListstring = sharedPref.getString("last_feed_stories", null);
            if (storyListstring != null) {
                Story[] storyarray = new Gson().fromJson(storyListstring, Story[].class);
                return new LinkedList<Story>(Arrays.asList(storyarray));
            }
        }
        return null;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    private void startLocationUpdates() {
        if (Utils.mayRequestLocation(_this,storyListView)) {
            //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener,null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
    }

    private LatLngBounds getFitBounds(Location c, Location f) {

        double east2west = Math.abs(c.longitude - f.longitude);
        double north2south = Math.abs(c.latitude - f.latitude);

        LatLng north = new LatLng(c.latitude + north2south,c.longitude);
        LatLng south = new LatLng(c.latitude - north2south,c.longitude);
        LatLng east = new LatLng(c.latitude,c.longitude + east2west);
        LatLng west = new LatLng(c.latitude,c.longitude - east2west);

        LatLngBounds.Builder boundsbuilder = new LatLngBounds.Builder();
        boundsbuilder.include(north);
        boundsbuilder.include(south);
        boundsbuilder.include(east);
        boundsbuilder.include(west);
        LatLngBounds bounds = boundsbuilder.build();

        return bounds;
    }

    private Story getFarthestStoryFromList(List<Story> list) {
        for (int i = list.size()-1; i >= 0; i-- ) {
            Story st = list.get(i);
            if (!st.isDummy()) {
                return st;
            }
        }
        return null;
    }

    private void populateMapMarkers(List<Story> list) {
        TypedArray markersicons = getResources().obtainTypedArray(R.array.markers_array);
        mMap.clear();
        for (Story story : list) {
            if (story.getLocation() != null) {
                long index = story.getId() % markersicons.length();
                LatLng l = new LatLng(story.getLocation().latitude, story.getLocation().longitude);
                MarkerOptions markeroptions = new MarkerOptions().position(l)
                        .icon(BitmapDescriptorFactory.fromResource(markersicons.getResourceId((int) index, -1)))
                        .anchor(0.5f, 0.5f);
                if (story.getFormat() == Constants.STORY_FORMAT_OPEN)
                    markeroptions.title(story.getTitle()).snippet(story.getLocationName());
                else
                    markeroptions.title(story.getLocationName());
                mMap.addMarker(markeroptions);
            }
        }
    }
}
