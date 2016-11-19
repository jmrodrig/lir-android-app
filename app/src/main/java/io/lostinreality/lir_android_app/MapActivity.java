package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;

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
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location storylocation;
    private Activity _this = this;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FrameLayout mapfragmentcontainer;

    //TODO: add back button on action bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        mapfragmentcontainer = (FrameLayout) findViewById(R.id.map_fragment_container);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);

        storylocation = (Location) getIntent().getParcelableExtra("location");

        if (storylocation.name != null)
            getSupportActionBar().setTitle(storylocation.name);

        LinearLayout navigationbtn = (LinearLayout) findViewById(R.id.start_navigation_button);
        if (navigationbtn != null) {
            navigationbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + storylocation.latitude + "," + storylocation.longitude + "&mode=w");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }

        LinearLayout mapmodeswitch = (LinearLayout) findViewById(R.id.map_mode_switch);
        if (mapmodeswitch != null) {
            mapmodeswitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.setBackground(getDrawable(R.drawable.map_activity_border_right_selected_button));
                        } else {
                            v.setBackground(getResources().getDrawable(R.drawable.map_activity_border_right_selected_button));
                        }
                    } else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.setBackground(getDrawable(R.drawable.map_activity_border_right_button));
                        } else {
                            v.setBackground(getResources().getDrawable(R.drawable.map_activity_border_right_button));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Utils.mayRequestLocation(_this,findViewById(R.id.map_fragment_container)))
            mMap.setMyLocationEnabled(true);

        LatLng storylocationLatLng = new LatLng(storylocation.latitude, storylocation.longitude);
        if (false)
            mMap.addCircle(new CircleOptions().center(storylocationLatLng)
                                .radius(storylocation.radius)
                                .strokeColor(Color.argb(200, 0, 0, 0))
                                .fillColor(Color.argb(100, 0, 0, 0))
                                .strokeWidth(1)
                            );
        else
            mMap.addMarker(new MarkerOptions().position(storylocationLatLng).title(storylocation.name));
        fitMapView(storylocationLatLng);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fitMapView(final LatLng storylocation) {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(android.location.Location l) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(storylocation);
                builder.include(new LatLng(l.getLatitude(),l.getLongitude()));
                LatLngBounds bounds = builder.build();
                int padding = 500;
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                } catch (IllegalStateException e) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(storylocation));
                }
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        if (Utils.mayRequestLocation(_this,mapfragmentcontainer)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestSingleUpdate(locationManager.getBestProvider(criteria, true), locationListener, null);
            //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            //locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
    }
}
