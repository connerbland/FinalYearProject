package com.taxi.conner.finaltaxiproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class HistoryItemMapActivity extends AppCompatActivity implements
        OnMapReadyCallback, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private double totalDistance;
    private ArrayList<Double> startEndLatLong = new ArrayList<>();
    double departureLat, departureLong, destinationLat, destinationLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_history_item_map_mob);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        departureLat = b.getDouble("departureLat");
        departureLong = b.getDouble("departureLong");
        destinationLat = b.getDouble("destinationLat");
        destinationLong = b.getDouble("destinationLong");
        String departureAddress = b.getString("departureAddress");
        String destinationAddress = b.getString("destinationAddress");
        double cost = b.getDouble("cost");
        String costStr = "£" + Double.toString(cost);
        int rating = b.getInt("rating");
        String ratingStr = Integer.toString(rating);
        if (ratingStr == "" || ratingStr == null || ratingStr == "0") {
            ratingStr = "No Rating";
        } else {
            ratingStr = "Rating: " + ratingStr + "/5";
        }
        String review = b.getString("review");
        if (review == "" || review == null) {
            review = "No Review";
        }

        EditText dspFrom = (EditText) findViewById(R.id.dspFrom);
        dspFrom.append(departureAddress);
        dspFrom.setKeyListener(null);

        EditText dspTo = (EditText) findViewById(R.id.dspTo);
        dspTo.append(destinationAddress);
        dspTo.setKeyListener(null);

        EditText dspCost = (EditText) findViewById(R.id.dspCost);
        dspCost.append(costStr);
        dspCost.setKeyListener(null);

        EditText dspReview = (EditText) findViewById(R.id.dspReview);
        dspReview.append(review);
        dspReview.setKeyListener(null);

        EditText dspRating = (EditText) findViewById(R.id.dspRating);
        dspRating.append(ratingStr);
        dspRating.setKeyListener(null);

        try {
            new DirectionFinder(this, Double.toString(departureLat), Double.toString(departureLong),
                    Double.toString(destinationLat), Double.toString(destinationLong), "customer").execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //LatLng plymouthUni = new LatLng(50.374843, -4.140362);
        LatLng routeStart = new LatLng(departureLat, departureLong);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(routeStart));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.", "Finding direction..!", true);
        if(originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if(destinationMarkers != null) {
            for(Marker marker : destinationMarkers) {
                marker.remove();
            }
        }
        if(polylinePaths != null) {
            for(Polyline polyline: polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes, ArrayList<Double> latLong) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        totalDistance = 0;

        startEndLatLong = latLong;

        //Stored in an arraylist as it allows for way points in the journey to be implemented
        for(Route route: routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            //routeDistance = route.distance.getDistance(); // gets distance in metres
            //double cost = route.getFinalCost(routeDistance);

            originMarkers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromAsset("letter_s.png")).title(route.startAddress).position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromAsset("letter_f.png")).title(route.endAddress).position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));

            totalDistance = totalDistance + route.distance.value;
        }
    }

    @Override
    public void onTimeFound(List<Route> routes, ArrayList<Double> latLong) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main_page) {
            Intent i = new Intent(getApplicationContext(), JourneyRouteActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_route_history) {
            Intent i = new Intent(getApplicationContext(), RouteHistoryActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(getApplicationContext(), UserSettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            User.getInstance().logout();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
