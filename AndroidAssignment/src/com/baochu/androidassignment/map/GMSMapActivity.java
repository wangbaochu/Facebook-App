package com.baochu.androidassignment.map;

import com.baochu.assignment.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class GMSMapActivity extends FragmentActivity {

    private GoogleMap mMap;
    private static final LatLng GOLDEN_GATE_BRIDGE = new LatLng(37.828891,-122.485884);
    private static final LatLng APPLE = new LatLng(37.3325004578, -122.03099823);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gms_map);

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap == null) {
            Toast.makeText(this, "Google Maps not available", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Toast menuToast = Toast.makeText(this, "Please click menu to see more", Toast.LENGTH_LONG);
        menuToast.setGravity(Gravity.BOTTOM, 20, 20);
        menuToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.gms_map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_sethybrid:
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            break;

        case R.id.menu_showtraffic:
            mMap.setTrafficEnabled(true);
            break;

        case R.id.menu_zoomin:
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            break;

        case R.id.menu_zoomout:
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
            break;

        case R.id.menu_gotolocation:
            CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(GOLDEN_GATE_BRIDGE) // Sets the center of the map to
            // Golden Gate Bridge
            .zoom(17)                   // Sets the zoom
            .bearing(90) // Sets the orientation of the camera to east
            .tilt(30)    // Sets the tilt of the camera to 30 degrees
            .build();    // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    cameraPosition));
            break;

        case R.id.menu_addmarker:
            mMap.addMarker(new MarkerOptions()
            .position(GOLDEN_GATE_BRIDGE)
            .title("Golden Gate Bridge")
            .snippet("San Francisco")
            .icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_launcher)));
            break;

        case R.id.menu_getcurrentlocation:
            // ---get your current location and display a blue dot---
            mMap.setMyLocationEnabled(true);

            break;

        case R.id.menu_lineconnecttwopoints:
            //---add a marker at Apple---
            mMap.addMarker(new MarkerOptions()
            .position(APPLE)
            .title("Apple")
            .snippet("Cupertino")
            .icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE)));

            //---draw a line connecting Apple and Golden Gate Bridge---
            mMap.addPolyline(new PolylineOptions().add(GOLDEN_GATE_BRIDGE, APPLE).width(5).color(Color.RED));
            break;
        }
        return true;
    }

}
