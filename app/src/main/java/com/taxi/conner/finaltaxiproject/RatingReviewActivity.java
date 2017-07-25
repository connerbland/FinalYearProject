package com.taxi.conner.finaltaxiproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class RatingReviewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private EditText edtReview;
    private Button btnContinue;
    Spinner spnRating;
    ArrayAdapter<Integer> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_rating_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        edtReview = (EditText) findViewById(R.id.edtReview);

        btnContinue = (Button) findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(v);
            }
        });

        spnRating = (Spinner) findViewById(R.id.spnRating);
        Integer[] listRatings = new Integer[]{1,2,3,4,5};
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,listRatings);
        spnRating.setAdapter(adapter);
    }

    public void submit(View v) {
        String type = "add_rating_review";
        Bundle b = getIntent().getExtras();
        int customer_id = b.getInt("customerID");
        int taxi_id = b.getInt("taxiID");
        double cost = b.getDouble("cost");
        double departure_lat = b.getDouble("departureLat");
        double departure_long = b.getDouble("departureLong");
        double destination_lat = b.getDouble("destinationLat");
        double destination_long = b.getDouble("destinationLong");
        String datetime = b.getString("datetime");
        int rating = (Integer)spnRating.getSelectedItem();
        String review = edtReview.getText().toString();
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type,customer_id,taxi_id,cost,departure_lat,departure_long,
                destination_lat,destination_long,rating,review,datetime);
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
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
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
}
