package com.taxi.conner.finaltaxiproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This page displays the expected route that the journey will take. A splash screen outlining the
 * details of the journey will also be displayed from which the user can confirm their transaction
 */
public class JourneyRouteActivity extends AppCompatActivity implements
        OnMapReadyCallback, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMyLocationButtonClickListener {

    Context context;

    private GoogleMap mMap;
    private Button btnFindRoute, btnContinue;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private double totalDistance;
    private double totalCost;
    private String approxTime;
    private ArrayList<Double> startEndLatLong = new ArrayList<>();
    String finalLocation, finalDestination;
    String taxiLat, taxiLong;
    PlaceAutocompleteFragment autocompleteLocation, autocompleteDestination;

    String taxiTime;

    int i;

    private static final String TAG = "JourneyRouteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_journey_route_mob);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //edtLocation = (AutoCompleteTextView) findViewById(R.id.edtLocation);
        //edtDestination = (AutoCompleteTextView) findViewById(R.id.edtDestination);

        //Places autocomplete - Location
        autocompleteLocation = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_location);
        autocompleteLocation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Toast.makeText(getApplicationContext(),place.getName(),Toast.LENGTH_SHORT).show();
                finalLocation = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        AutocompleteFilter ukFilter = new AutocompleteFilter.Builder().setTypeFilter(Place.TYPE_COUNTRY).setCountry("UK").build();
        autocompleteLocation.setFilter(ukFilter);

        //Places autocomplete - Destination
        autocompleteDestination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Toast.makeText(getApplicationContext(),place.getName(),Toast.LENGTH_SHORT).show();
                finalDestination = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        autocompleteDestination.setFilter(ukFilter);

        //Button for calculating the route
        btnFindRoute = (Button) findViewById(R.id.btnFindRoute);
        btnFindRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        btnContinue = (Button) findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!startEndLatLong.isEmpty()) {
                    confirmationDialog(v);
                } else {
                    Toast.makeText(getApplicationContext(), "Please create a route!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(User.getInstance() == null) { // If account does not exist, create new empty one
            User.getInstance().logout();
        }
        if(User.getInstance().isLoggedout()) {
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng plymouthUni = new LatLng(50.374843, -4.140362);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(plymouthUni));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    //Finds location closest to device location
    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Location currLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currLat = currLocation.getLatitude();
        double currLong = currLocation.getLongitude();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currLat, currLong, 1);
            StringBuilder stringBuilder = new StringBuilder();
            if(addresses.size() > 0) {
                Address address = addresses.get(0);
                stringBuilder.append(address.getAddressLine(0));
                stringBuilder.append(" ");
                stringBuilder.append(address.getAddressLine(1));
            }
            String currAddress = stringBuilder.toString();
            autocompleteLocation.setText("20");
            autocompleteLocation.setText("");
            autocompleteLocation.setText(currAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendRequest() {
        if(finalLocation == null || finalLocation == "") {
            Toast.makeText(this, "Please enter your location!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(finalDestination == null || finalDestination == "") {
            Toast.makeText(this, "Please enter a destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, finalLocation, finalDestination, "customer").execute();

            //Retr
            JourneyRouteActivity.JourneyRouteSub journeyRouteSub = new JourneyRouteActivity.JourneyRouteSub();
            journeyRouteSub.execute("get_taxi_location");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void calcTime() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(taxiLat), Double.parseDouble(taxiLong))).title("Taxi Driver").icon(BitmapDescriptorFactory.fromAsset("taxi.png")));
        try {
            new DirectionFinder(this, taxiLat, taxiLong, startEndLatLong.get(0).toString(), startEndLatLong.get(1).toString(), "taxi").execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void confirmationDialog(final View v) {
        totalCost = 4 + (0.7*totalDistance/1000);
        BigDecimal result = new BigDecimal(totalCost).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalCost = result.doubleValue();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Confirmation");
        alert.setMessage("The total distance is: " + totalDistance/1000 + "KM" + "\nTotal Cost: £" + totalCost
        + "\nApproximate Duration: " + approxTime + "\nApprox Time Until Taxi Arrival: " + taxiTime);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    createRoute(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void createRoute(View v) throws IOException {

        totalCost = 4 + (0.7*totalDistance/1000);

        String type = "create_route";
        int customer_id = User.getInstance().getCustomerID();
        int taxi_id = 1;
        double cost = totalCost;
        //String rating = null; // Done at a later stage by user
        //String review = null; // Done at a later stage by user
        double departure_lat = this.startEndLatLong.get(0);
        double departure_long = this.startEndLatLong.get(1);
        double destination_lat = this.startEndLatLong.get(2);
        double destination_long = this.startEndLatLong.get(3);
        String departure_address = finalLocation;
        String destination_address = finalDestination;

        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, customer_id, taxi_id, cost, departure_lat, departure_long,
                destination_lat, destination_long, departure_address, destination_address, currentTime);
    }

    //Clear Map of markers and routes
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

    public void onTimeFound(List<Route> routes, ArrayList<Double> latLong) {
        progressDialog.dismiss();
        for(Route route: routes) {
            taxiTime = route.duration.text;
        }
    }

    //Draws the decoded poly-lines from Google Directions onto the map
    @Override
    public void onDirectionFinderSuccess(List<Route> routes, ArrayList<Double> latLong) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        totalDistance = 0;
        approxTime = "";

        startEndLatLong = latLong;

        //Stored in an arraylist as it allows for way points in the journey to be implemented
        for(Route route: routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            approxTime = route.duration.text;
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

    // This subclass retreives the latitude/longitude of the taxi driver to calcuate distance until pickup
    // Currently on uses single taxi driver in database
    private class JourneyRouteSub extends AsyncTask<Object, Void, String> {
        String methodType;

        @Override
        protected String doInBackground(Object... params) {

            String type = (String) params[0];
            methodType = type;
            int taxi_id = 1;
            //String history_url = "http://10.0.2.2/project/get_taxi_driver_location.php";
            String history_url = "http://192.168.0.14/project/route_history.php";
            if (type.equals("get_taxi_location")) {
                try {
                    URL url = new URL(history_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("taxi_id", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(taxi_id), "UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String result = "";
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return result;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (methodType == "get_taxi_location") {
                if (result != null) {
                    try {
                        JSONObject root = new JSONObject(result);
                        JSONArray objectArray = root.getJSONArray("result");
                        JSONObject currentRecord = objectArray.getJSONObject(0);
                        taxiLat = currentRecord.getString("location_lat");
                        taxiLong = currentRecord.getString("location_long");

                        ArrayList<String> output = new ArrayList<>();
                        output.add(taxiLat);
                        output.add(taxiLong);


                        calcTime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}