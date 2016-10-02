package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by jose on 11/06/16.
 */
public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Boolean areaSelectionMode;
    private EditText locationnameET;
    private Location currentLocation;
    private Location sectionLocation;
    private Location storyLocation;
    private ImageView mapSightIV;
    private Boolean isUpdateLocation;
    private int[] cursorposition;
    private Activity _this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);


        locationnameET = (EditText) findViewById(R.id.location_name_edit);
        mapSightIV = (ImageView) findViewById(R.id.map_sight);

        if (getIntent().getExtras() != null) {
            currentLocation = (Location) getIntent().getParcelableExtra("current_location");
            sectionLocation = (Location) getIntent().getParcelableExtra("section_location");
            storyLocation = (Location) getIntent().getParcelableExtra("story_location");
            isUpdateLocation = getIntent().getExtras().getBoolean("update_location");
            cursorposition = getIntent().getExtras().getIntArray("cursor_position");
        }

        Switch mapmodeswitch = (Switch) findViewById(R.id.map_mode_switch);
        if (mapmodeswitch != null) {
            mapmodeswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    else
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            });
        }

        CheckBox selectionmodecheckbox = (CheckBox) findViewById(R.id.area_mode_checkbox);
        if (selectionmodecheckbox != null) {
            selectionmodecheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    areaSelectionMode = isChecked;
                    if (isChecked)
                        mapSightIV.setImageResource(R.drawable.map_sight_area);
                    else
                        mapSightIV.setImageResource(R.drawable.map_sight_dot);
                }
            });
        }

        if (storyLocation != null) {
            locationnameET.setText(storyLocation.name);
            locationnameET.setSelection(locationnameET.getText().length());
        } else if (sectionLocation != null) {
            locationnameET.setText(sectionLocation.name);
            locationnameET.setSelection(locationnameET.getText().length());
        }

        areaSelectionMode = isAreaSelectionMode();
        if (areaSelectionMode) {
            selectionmodecheckbox.setChecked(areaSelectionMode);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Utils.mayRequestLocation(_this,findViewById(R.id.map_fragment_container)))
            mMap.setMyLocationEnabled(true);

        Location l;
        if (storyLocation != null) {
            l = storyLocation;
        } else if (sectionLocation != null) {
            l = sectionLocation;
        } else if (currentLocation != null) {
            l = currentLocation;
        } else {
            l = new Location(Constants.DEFAULT_LATITUDE,
                    Constants.DEFAULT_LONGITUDE,
                    getString(R.string.location_name_hint),
                    Constants.DEFAULT_RADIUS,
                    Constants.DEFAULT_ZOOM);
        }
        LatLng latlng = new LatLng(l.latitude, l.longitude);
        if (isUpdateLocation) {
            if (areaSelectionMode)
                mMap.addCircle(new CircleOptions().center(latlng)
                        .radius(l.radius)
                        .strokeColor(Color.argb(80,0,0,0))
                        .strokeWidth(5)
                );
            else
                mMap.addMarker(new MarkerOptions().position(latlng).title(l.name));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, l.zoom.floatValue()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_location_ab_menu, menu);
        if (isUpdateLocation) {
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(true);
        }
        return true;
    }

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
            case R.id.action_confirm:
                Location resultlocation;
                if (sectionLocation != null)
                    resultlocation = sectionLocation;
                else if (storyLocation != null)
                    resultlocation = storyLocation;
                else
                    resultlocation = new Location();
                resultlocation.setLocation(getLocationFromMap());
                Intent resultIntent = new Intent();
                resultIntent.putExtra("location", (Parcelable) resultlocation);
                resultIntent.putExtra("cursor_position", cursorposition);
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            case R.id.action_delete:
                Location lnull = null;
                Intent deleteIntent = new Intent();
                deleteIntent.putExtra("location", (Parcelable) lnull);
                deleteIntent.putExtra("cursor_position", getIntent().getExtras().getIntArray("cursor_position"));
                setResult(RESULT_OK, deleteIntent);
                finish();
                return true;
            case android.R.id.home:
                Intent upIntent = new Intent();
                setResult(RESULT_CANCELED, upIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Location getLocationFromMap() {
        Location l;
        LatLng latLng = mMap.getCameraPosition().target;
        Float zoom = mMap.getCameraPosition().zoom;
        String locationname = locationnameET.getText().toString();
        if (areaSelectionMode) {
            Double radius = computeRadius();
            l = new Location(latLng.latitude,latLng.longitude,locationname,radius,zoom.doubleValue());
        } else {
            l = new Location(latLng.latitude,latLng.longitude,locationname,0.0,zoom.doubleValue());
        }
        return l;
    }

    private Boolean isAreaSelectionMode() {
        if (sectionLocation != null && sectionLocation.radius > 0) {
            return true;
        }
        return false;
    }

    private Double computeRadius() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        Double eastToWestDistance = Utils.distanceBetweenCoordinates(
                0.0,
                bounds.northeast.longitude,
                0.0,
                bounds.southwest.longitude,
                0.0,
                0.0
        );
        return Constants.LOCATION_AREA_RADIUS_PIX / ((Integer) mapFragment.getView().getMeasuredWidth()).doubleValue() * eastToWestDistance;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_PLACE_AUTOCOMPLETE:
                switch (resultCode) {
                    case RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        if (place.getViewport() != null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(),0));
                        else
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),17f));
                        locationnameET.setText(place.getName());
                        locationnameET.setSelection(locationnameET.getText().length());
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

    //TODO: remover localização

    //TODO: map sight and area

    //TODO: toogle point selection or zone selection

    //TODO: my location button
}
