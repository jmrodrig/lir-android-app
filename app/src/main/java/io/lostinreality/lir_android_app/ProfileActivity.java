package io.lostinreality.lir_android_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jose on 28/07/16.
 */
public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback, ChooseStoryFormatDialogFragment.NoticeDialogListener {
    private User user;
    private TextView userFulNameLabel, userNoOfStoriesLabel, userNoFollowersLabel, userStoriesListLabel, nostorieswarningview;
    private CircleImageView userAvatarPicture;
    private ImageView changeuseravatarbutton;
    private Button followUserButton, storiesswitchbutton, mapswitchbutton;
    private MenuItem actionlogoutitem, actioncreateitem;
    private ProfileStoryListAdapter userstorieslistadapter;
    private ProfileStoryListAdapter bookmarkedstorieslistadapter;
    private ListView userstoriesListView;
    private ListView bookmarkedstoriesListView;
    private LinearLayout bookmarkedstoriesviewgroup, userstoriesviewgroup, profileLayout,profileStoriesLayout,profiledetailsLayout;
    private ProgressBar loadstoriesProgressBar;
    private ProfileActivity _this = this;
    private SwipeRefreshLayout profileViewParentLayout;
    private GoogleMap mMap;
    private RelativeLayout mapcontainerlayout;
    private ScrollView profilescroller;
    private List<Story> lastFeedStories;
    private LatLngBounds lastFeedStoriesbounds;

    private static final int PICK_IMAGE_REQUEST = 7001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userFulNameLabel = (TextView) findViewById(R.id.user_name_label);
        userNoOfStoriesLabel = (TextView) findViewById(R.id.no_created_stories_label);
        userNoFollowersLabel = (TextView) findViewById(R.id.no_followers_label);
        userAvatarPicture = (CircleImageView) findViewById(R.id.profile_avatar_picture_view);
        userStoriesListLabel = (TextView) findViewById(R.id.user_stories_list_label);
        userstoriesListView = (ListView) findViewById(R.id.user_stories_list);
        userstoriesviewgroup = (LinearLayout) findViewById(R.id.user_stories_viewgroup);
        bookmarkedstoriesviewgroup = (LinearLayout) findViewById(R.id.bookmarked_stories_viewgroup);
        bookmarkedstoriesListView = (ListView) findViewById(R.id.bookmarked_stories_list);
        followUserButton = (Button) findViewById(R.id.follow_user_button);
        changeuseravatarbutton = (ImageView) findViewById(R.id.change_user_avatar_button);
        nostorieswarningview = (TextView) findViewById(R.id.no_stories_warning_view);
        profileLayout = (LinearLayout) findViewById(R.id.profile_layout_view);
        profiledetailsLayout = (LinearLayout) findViewById(R.id.profile_details_layout_view);
        profileStoriesLayout = (LinearLayout) findViewById(R.id.profile_stories_layout);
        loadstoriesProgressBar = (ProgressBar) findViewById(R.id.loading_stories_progress_bar);
        profileViewParentLayout = (SwipeRefreshLayout) findViewById(R.id.profile_view_parent);
        mapcontainerlayout = (RelativeLayout) findViewById(R.id.map_fragment_container);
        storiesswitchbutton = (Button) findViewById(R.id.stories_switch_button);
        mapswitchbutton = (Button) findViewById(R.id.map_switch_button);
        profilescroller = (ScrollView) findViewById(R.id.profile_view_scroller);
        lastFeedStories = new ArrayList<>();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(_this);

        profileViewParentLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (user != null)
                    user.loadStories(_this);
                else
                    profileViewParentLayout.setRefreshing(false);
            }
        });

        storiesswitchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap(false);
            }
        });

        mapswitchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap(true);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Long userid = getIntent().getLongExtra("user_id",-1);
        if (userid == -1) {
            user = new User();
            user.loadData(_this);
            getSupportActionBar().setTitle(" Your profile");
        } else {
            user = new User();
            user.setNumberId(userid);
            user.setPublicprofile(true);
            user.loadData(_this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ll.latitude, ll.longitude), (float) Constants.DEFAULT_ZOOM));
        if (Utils.mayRequestLocation(_this,findViewById(R.id.map_fragment_container)))
            mMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_ab_menu, menu);
        actionlogoutitem = (MenuItem) menu.findItem(R.id.action_logout);
        actioncreateitem = (MenuItem) menu.findItem(R.id.action_create);
        showMenuItems();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                try {
                    LoginManager.getInstance().logOut();
                } catch (Exception e) {
                    // do nothing
                }
                User.deleteCurrentUserFromDevice(getBaseContext());
                Intent logoutintent = new Intent(getBaseContext(), LauncherActivity.class);
                logoutintent.putExtra("user_logged_out",true);
                startActivity(logoutintent);
                finish();
                return true;
            case R.id.action_create:
                DialogFragment dialog = new ChooseStoryFormatDialogFragment();
                dialog.show(getSupportFragmentManager(), "ChooseStoryFormatDialogFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogSingleStoryClick(DialogFragment dialog) {
        Intent intent = new Intent(getBaseContext(), CreateSingleStoryActivity.class);
        intent.putExtra("current_location", (Parcelable) Utils.getLastLocation(_this));
        startActivity(intent);
        dialog.dismiss();
    }

    @Override
    public void onDialogOpenStoryClick(DialogFragment dialog) {
        Intent intent = new Intent(getBaseContext(), CreateOpenStoryActivity.class);
        intent.putExtra("current_location", (Parcelable) Utils.getLastLocation(_this));
        startActivity(intent);
        dialog.dismiss();
    }


    public void onProfileLoadFinished() {
        setLayout();
    }

    public void setLayout() {
        if (user.isPublic()) {
            getSupportActionBar().setTitle(" " + user.getFullName() + "'s profile");
            userStoriesListLabel.setText(user.getFullName() + "'s stories");
            bookmarkedstoriesviewgroup.setVisibility(View.GONE);
            changeuseravatarbutton.setVisibility(View.GONE);
            if (user.getCurrentUserFollows()) {
                followUserButton.setText(getString(R.string.profile_follow_button_unfollow));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    followUserButton.setBackground(getDrawable(R.drawable.opaque_button_dark));
                    followUserButton.setTextColor(getColor(R.color.colorContrast));
                } else {
                    followUserButton.setBackground(getResources().getDrawable(R.drawable.opaque_button_dark));
                    followUserButton.setTextColor(getResources().getColor(R.color.colorContrast));
                }
            } else {
                followUserButton.setText(getString(R.string.profile_follow_button_follow));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    followUserButton.setTextColor(getColor(R.color.colorSecondary));
                    followUserButton.setBackground(getDrawable(R.drawable.transparent_button_blue));
                } else {
                    followUserButton.setTextColor(getResources().getColor(R.color.colorSecondary));
                    followUserButton.setBackground(getResources().getDrawable(R.drawable.transparent_button_blue));
                }
            }
            followUserButton.setVisibility(View.VISIBLE);
        } else {
            userStoriesListLabel.setText(getString(R.string.profile_user_stories_label_created_stories));
            followUserButton.setVisibility(View.GONE);
            changeuseravatarbutton.setVisibility(View.VISIBLE);
            showMenuItems();
        }

        userNoFollowersLabel.setText(user.getNoOfFollowers() + " " + getString(R.string.profile_no_followers_label));
        followUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = User.getCurrentUser(_this);
                if (currentUser != null)
                    currentUser.sendFollow(_this, user.getNumberId());
            }
        });

        userFulNameLabel.setText(user.getFullName());
        userNoOfStoriesLabel.setText(user.getNoOfStories() + " " + getString(R.string.profile_no_stories_label));
        Glide.with(getBaseContext()).load(user.getAvatarUrl()).dontAnimate().placeholder(R.drawable.placeholder_avatar).into(userAvatarPicture);
        if (!user.isPublic()) {
            changeuseravatarbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.mayRequestMedia(_this, profileLayout)) {
                        Intent intent = new Intent();
                        // Show only images, no videos or anything else
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // Always show the chooser (if there are multiple options available)
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                }
            });
        }
        profileLayout.setVisibility(View.VISIBLE);
        profileLayout.animate().alpha(1f).setDuration(300).start();
        user.loadStories(_this);
    }

    public void onUserStoriesLoadFinished(List<Story> createdstories,List<Story> savedstories, Boolean isFresh) {
        if (_this.isDestroyed()) return;
        lastFeedStories.clear();
        lastFeedStories.addAll(createdstories);
        lastFeedStories.addAll(savedstories);
        profileViewParentLayout.setRefreshing(false);
        if (createdstories.size() + savedstories.size() == 0) {
            userstoriesviewgroup.setVisibility(View.GONE);
            bookmarkedstoriesviewgroup.setVisibility(View.GONE);
            nostorieswarningview.setVisibility(View.VISIBLE);
        } else {
            userstoriesviewgroup.setVisibility(View.VISIBLE);
            bookmarkedstoriesviewgroup.setVisibility(View.VISIBLE);
            nostorieswarningview.setVisibility(View.GONE);
            loadUserStoriesOnListAdapters(createdstories, !isFresh);
            loadUserSavedStoriesOnListAdapters(savedstories, !isFresh);
            createdstories.addAll(savedstories);
            populateMapMarkers(lastFeedStories);
        }
        //profileStoriesLayout.animate().alpha(1f).setDuration(300).start();
        showMap(false);
        showProgress(false, profileStoriesLayout, loadstoriesProgressBar);
    }

    public void onPublicProfileStoriesLoadFinished(List<Story> slist) {
        if (_this.isDestroyed()) return;
        lastFeedStories = slist;
        profileViewParentLayout.setRefreshing(false);
        if (slist.size() == 0) {
            userstoriesviewgroup.setVisibility(View.GONE);
            nostorieswarningview.setVisibility(View.VISIBLE);
        } else {
            userstoriesviewgroup.setVisibility(View.VISIBLE);
            nostorieswarningview.setVisibility(View.GONE);
            userstorieslistadapter = new ProfileStoryListAdapter(this, R.layout.profile_story_list_row, slist,false);
            userstoriesListView.setAdapter(userstorieslistadapter);
            Utils.expandListViewOnParentView(userstoriesListView);
            populateMapMarkers(lastFeedStories);
        }
        showMap(false);
        showProgress(false,profileStoriesLayout,loadstoriesProgressBar);
        //profileStoriesLayout.animate().alpha(1f).setDuration(300).start();
    }

    public void onConnectionRestablished() {
        if (user != null) {
            user.loadData(_this);
        }
    }

    public void loadUserStoriesOnListAdapters(List<Story> createdstories, Boolean offline) {
        if (createdstories.size() > 0) {
            userstorieslistadapter = new ProfileStoryListAdapter(this, R.layout.profile_story_list_row, createdstories, offline);
            userstoriesListView.setAdapter(userstorieslistadapter);
            Utils.expandListViewOnParentView(userstoriesListView);
        } else {
            userstoriesviewgroup.setVisibility(View.GONE);
        }
    }

    public void loadUserSavedStoriesOnListAdapters(List<Story> savedstories, Boolean offline) {
        if (savedstories.size() > 0) {
            bookmarkedstorieslistadapter = new ProfileStoryListAdapter(this, R.layout.profile_story_list_row, savedstories, offline);
            bookmarkedstoriesListView.setAdapter(bookmarkedstorieslistadapter);
            Utils.expandListViewOnParentView(bookmarkedstoriesListView);
        } else {
            bookmarkedstoriesviewgroup.setVisibility(View.GONE);
        }
    }

    public void setUserFollowView(Boolean follows, Integer noFollowers) {
        userNoFollowersLabel.setText(noFollowers + " " + getString(R.string.profile_no_followers_label));
        if (follows) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                followUserButton.setBackground(getDrawable(R.drawable.opaque_button_dark));
                followUserButton.setTextColor(getColor(R.color.colorContrast));
            } else {
                followUserButton.setBackground(getResources().getDrawable(R.drawable.opaque_button_dark));
                followUserButton.setTextColor(getResources().getColor(R.color.colorContrast));
            }
            followUserButton.setText(getString(R.string.profile_follow_button_unfollow));
        } else {
            followUserButton.setText(getString(R.string.profile_follow_button_follow));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                followUserButton.setBackground(getDrawable(R.drawable.transparent_button_blue));
                followUserButton.setTextColor(getColor(R.color.colorSecondary));
            } else {
                followUserButton.setBackground(getResources().getDrawable(R.drawable.transparent_button_blue));
                followUserButton.setTextColor(getResources().getColor(R.color.colorSecondary));
            }
        }
    }

    private class ProfileStoryListAdapter extends ArrayAdapter<Story> {
        private List<Story> stories;
        private Context context;
        private Boolean offlinemode;

        public ProfileStoryListAdapter(Context ctx, int textViewResourceId, List<Story> stories, Boolean offline) {
            super(ctx, textViewResourceId, stories);
            this.stories = stories;
            this.context = ctx;
            this.offlinemode = offline;
        }

        public View getView(int position, View converView, ViewGroup parent) {
            View v = converView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.profile_story_list_row, null);
            }
            final Story story = stories.get(position);
            TextView titlelabel = (TextView) v.findViewById(R.id.story_title_label);
            TextView summarylabel = (TextView) v.findViewById(R.id.story_summary_label);
            TextView locationlabel = (TextView) v.findViewById(R.id.story_location_label);
            TextView authorlabel = (TextView) v.findViewById(R.id.story_author_label);
            TextView likeslabel = (TextView) v.findViewById(R.id.story_likes_label);
            TextView bookmarkslabel = (TextView) v.findViewById(R.id.story_bookmarks_label);
            TextView viewslabel = (TextView) v.findViewById(R.id.story_views_label);
            TextView publishStateView = (TextView) v.findViewById(R.id.publish_state_label);
            ImageView thumbnailPicture = (ImageView) v.findViewById(R.id.story_thumbnail_picture);
            CircleImageView authoravatarpicture = (CircleImageView) v.findViewById(R.id.story_author_avatar_picture);
            LinearLayout locationbanner = (LinearLayout) v.findViewById(R.id.location_banner_view);
            LinearLayout statsbanner = (LinearLayout) v.findViewById(R.id.story_stats_view);
            TextView storestatelabel = (TextView) v.findViewById(R.id.store_state_label);

            if (story.getFormat() == Constants.STORY_FORMAT_SINGLE) {
                summarylabel.setText(story.getSummary());
                summarylabel.setVisibility(View.VISIBLE);
                titlelabel.setVisibility(View.GONE);
            } else {
                summarylabel.setVisibility(View.GONE);
                if (story.getTitle() == null || story.getTitle().length() == 0) {
                    titlelabel.setText("no title");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        titlelabel. setTextAppearance(R.style.italicTextStyle);
                    } else {
                        titlelabel.setTextAppearance(getContext(), R.style.italicTextStyle);
                    }
                    titlelabel.setVisibility(View.VISIBLE);
                } else if (story.getFormat() == Constants.STORY_FORMAT_SINGLE) {
                    titlelabel.setVisibility(View.GONE);
                } else {
                    titlelabel.setText(story.getTitle());
                    titlelabel.setVisibility(View.VISIBLE);
                }
            }

            if (story.getLocation() == null || story.getLocation().name.length() == 0) {
                locationlabel.setText("no location");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    locationlabel.setTextColor(getColor(R.color.colorContrast));
                    locationbanner.setBackground(getDrawable(R.drawable.location_banner_no_location));
                } else {
                    locationlabel.setTextColor(getResources().getColor(R.color.colorContrast));
                    locationbanner.setBackground(getResources().getDrawable(R.drawable.location_banner_no_location));
                }
            } else {
                locationlabel.setText(story.getLocation().name);
                TypedArray bannerbg = context.getResources().obtainTypedArray(R.array.location_banner_color_array);
                long index = story.getId() % bannerbg.length();
                locationbanner.setBackground(bannerbg.getDrawable((int) index));
                bannerbg.recycle();
            }

            authorlabel.setText(story.getAuthor().getFullName());

            Glide.with(context).load(Story.getImagePath(context,story.getThumbnail()))
                    .placeholder(R.drawable.placeholder_picture)
                    .fitCenter()
                    .into(thumbnailPicture);
            Glide.with(context).load(story.getAuthor().getAvatarUrl()).dontAnimate().placeholder(R.drawable.placeholder_avatar).into(authoravatarpicture);

            if (!user.isPublic()) {
                switch (story.getPublished()) {
                    case Constants.DRAFT:
                        publishStateView.setText(context.getString(R.string.main_story_list_publish_draft));
                        break;
                    case Constants.PUBLISH_WITH_EVERYONE:
                        publishStateView.setText(context.getString(R.string.main_story_list_publish_everyone));
                        break;
                    case Constants.PUBLISH_WITH_FOLLOWERS:
                        publishStateView.setText(context.getString(R.string.main_story_list_publish_followers));
                        break;
                    case Constants.PUBLISH_PRIVATELY:
                        publishStateView.setText(context.getString(R.string.main_story_list_publish_private));
                        break;
                }
            } else {
                publishStateView.setVisibility(View.GONE);
            }

            if (story.getPublished() > 0) {
                statsbanner.setVisibility(View.VISIBLE);
                likeslabel.setText(story.getNoLikes().toString());
                bookmarkslabel.setText(story.getNoOfSaves().toString());
                viewslabel.setText(story.getNOViews().toString());
            } else
                statsbanner.setVisibility(View.GONE);

            if (story.getId() > Story.TEMP_DEVICE_STORY_ID_INDEX)  {
                storestatelabel.setText("in device only");
                storestatelabel.setVisibility(View.VISIBLE);
            } else if (story.isStoredOnDevice(context)) {
                storestatelabel.setText("in device");
                storestatelabel.setVisibility(View.VISIBLE);
            } else {
                storestatelabel.setVisibility(View.GONE);
            }

            if (offlinemode && !story.isStoredOnDevice(context)) {
                v.setAlpha(0.5f);
            } else if (story.getPublished() != 0) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i;
                        if (story.getFormat() == Constants.STORY_FORMAT_SINGLE)
                            i = new Intent(context, ReadSingleStoryActivity.class);
                        else
                            i = new Intent(context, ReadStoryActivity.class);
                        i.putExtra("story_item", (Parcelable) story);
                        context.startActivity(i);
                    }
                });
            } else {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i;
                        if (story.getFormat() == Constants.STORY_FORMAT_OPEN)
                            i = new Intent(context, CreateOpenStoryActivity.class);
                        else
                            i = new Intent(context, CreateSingleStoryActivity.class);
                        i.putExtra("storyid", story.getId());
                        context.startActivity(i);
                    }
                });
            }
            return v;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            String filePath = Utils.getFilePathFromUri(this,data.getData());
            if (filePath != null)
                user.uploadUserAvatar(this,filePath,userAvatarPicture);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, final View view, final View progressview) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            view.setVisibility(show ? View.GONE : View.VISIBLE);
            view.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressview.setVisibility(show ? View.VISIBLE : View.GONE);
            progressview.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressview.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressview.setVisibility(show ? View.VISIBLE : View.GONE);
            view.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showMap(Boolean show) {
        if (show) {
            int mapheight = profilescroller.getHeight() - profiledetailsLayout.getHeight();
            int mapwidth = profilescroller.getWidth();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mapheight);
            mapcontainerlayout.setLayoutParams(params);
            profileStoriesLayout.setVisibility(View.GONE);
            mapcontainerlayout.setVisibility(View.VISIBLE);
            profileViewParentLayout.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mapswitchbutton.setBackground(getDrawable(R.drawable.profile_border_selected_button));
                storiesswitchbutton.setBackground(getDrawable(R.drawable.profile_border_left_button));
            } else {
                mapswitchbutton.setBackground(getResources().getDrawable(R.drawable.profile_border_selected_button));
                storiesswitchbutton.setBackground(getResources().getDrawable(R.drawable.profile_border_left_button));
            }
            if (lastFeedStories != null && lastFeedStories.size() > 0 && lastFeedStoriesbounds != null) {
                int padding = 100;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(lastFeedStoriesbounds,mapwidth,mapheight, padding));
            } else {
                float zoom = (float) Constants.DEFAULT_ZOOM;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            }
        } else {
            profileStoriesLayout.setVisibility(View.VISIBLE);
            mapcontainerlayout.setVisibility(View.GONE);
            profileViewParentLayout.setEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mapswitchbutton.setBackground(getDrawable(R.drawable.profile_border_right_button));
                storiesswitchbutton.setBackground(getDrawable(R.drawable.profile_border_selected_button));
            } else {
                mapswitchbutton.setBackground(getResources().getDrawable(R.drawable.profile_border_right_button));
                storiesswitchbutton.setBackground(getResources().getDrawable(R.drawable.profile_border_selected_button));
            }
        }

    }

    private void populateMapMarkers(List<Story> list) {
        LatLngBounds.Builder boundsbuilder = new LatLngBounds.Builder();
        TypedArray markersicons = getResources().obtainTypedArray(R.array.markers_array);
        mMap.clear();
        int markerscount = 0;
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
                markerscount++;
                boundsbuilder.include(l);
            }
        }
        if (markerscount > 0)
            lastFeedStoriesbounds = boundsbuilder.build();

    }

    private void showMenuItems() {
        try {
            if (!user.isPublic()) {
                actionlogoutitem.setVisible(true);
                actioncreateitem.setVisible(true);
            } else {
                actionlogoutitem.setVisible(false);
                actioncreateitem.setVisible(false);
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        }
    }
}
