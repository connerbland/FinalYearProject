package com.taxi.conner.finaltaxiproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This page displays a list of the users previous journeys in a list view
 * Clicking an entry will access its details and draw it on a map
 */
public class RouteHistoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ArrayList<HistoryItem> routeHistory;
    private static ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(User.getInstance() == null) { // If account does not exist, create new empty one
            User.getInstance().logout();
        }
        if(User.getInstance().isLoggedout()) {
            Toast.makeText(getApplicationContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_route_history_mob);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        routeHistory = new ArrayList<>();
        list = (ListView) findViewById(R.id.list);

        int customerID = User.getInstance().getCustomerID();
        RouteHistorySub routeHistorySub = new RouteHistorySub();
        routeHistorySub.execute("view_history", customerID);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(RouteHistoryActivity.this, HistoryItemMapActivity.class);
                Bundle b = new Bundle();
                HistoryItem selectedItem = routeHistory.get(position);
                double departureLat = selectedItem.getDeparture_lat();
                double departureLong = selectedItem.getDeparture_long();
                double destinationLat = selectedItem.getDestination_lat();
                double destinationLong = selectedItem.getDestination_long();
                String departureAddress = selectedItem.getDeparture_address();
                String destinationAddress = selectedItem.getDestination_address();
                String datetime = selectedItem.getDatetime();
                double cost = selectedItem.getCost();
                int rating = selectedItem.getRating();
                String review = selectedItem.getReview();

                b.putDouble("departureLat", departureLat);
                b.putDouble("departureLong", departureLong);
                b.putDouble("destinationLat", destinationLat);
                b.putDouble("destinationLong", destinationLong);
                b.putString("departureAddress", departureAddress);
                b.putString("destinationAddress", destinationAddress);
                b.putString("datetime", datetime);
                b.putDouble("cost", cost);
                b.putInt("rating", rating);
                b.putString("review", review);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onHistoryFound() {
        ArrayAdapter<HistoryItem> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, this.routeHistory);
        list.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
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

    //Had to use private class over background worker due to issues involving static variables
    private class RouteHistorySub extends AsyncTask<Object, Void, String> {

        String methodType;

        @Override
        protected String doInBackground(Object... params) {
            String type = (String) params[0];
            methodType = type;
            //String history_url = "http://10.0.2.2/project/route_history.php";
            String history_url = "http://192.168.0.14/project/route_history.php";
            if (type.equals("view_history")) {
                try {
                    int customer_id = (int) params[1];
                    URL url = new URL(history_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("customer_id", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(customer_id), "UTF-8");
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
            if (methodType == "view_history") {
                if (result != null) {
                    try {
                        ArrayList<HistoryItem> history = new ArrayList<>();
                        JSONObject root = new JSONObject(result);
                        JSONArray objectArray = root.getJSONArray("result");
                        for (int i = 0; i < objectArray.length(); i++) {
                            JSONObject currentRecord = objectArray.getJSONObject(i);
                            int routeID = currentRecord.getInt("route_id");
                            int customerID = currentRecord.getInt("customer_id");
                            int taxiID = currentRecord.getInt("taxi_id");
                            double cost = currentRecord.getDouble("cost");
                            int rating = currentRecord.getInt("rating");
                            String review = currentRecord.getString("review");
                            double departureLat = currentRecord.getDouble("departure_lat");
                            double departureLong = currentRecord.getDouble("departure_long");
                            double destinationLat = currentRecord.getDouble("destination_lat");
                            double destinationLong = currentRecord.getDouble("destination_long");
                            String departureAddress = currentRecord.getString("departure_address");
                            String destinationAddress = currentRecord.getString("destination_address");
                            String datetime = currentRecord.getString("datetime");
                            HistoryItem currentItem = new HistoryItem(routeID, customerID, taxiID,
                                    cost, rating, review, departureLat, departureLong,
                                    destinationLat, destinationLong, departureAddress, destinationAddress, datetime);
                            history.add(i, currentItem);
                        }
                        routeHistory = history;
                        onHistoryFound();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //no records
                }
            }
        }
    }
}