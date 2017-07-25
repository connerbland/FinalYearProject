package com.taxi.conner.finaltaxiproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class UserSettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText edtForename, edtSurname, edtEmailAddress, edtAddress, edtCity, edtPostcode, edtPhoneNumber;
    private Button btnUpdateAccount, btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_user_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        edtForename = (EditText) findViewById(R.id.edtForename);
        edtSurname = (EditText) findViewById(R.id.edtSurname);
        edtEmailAddress = (EditText) findViewById(R.id.edtEmailAddress);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        edtCity = (EditText) findViewById(R.id.edtCity);
        edtPostcode = (EditText) findViewById(R.id.edtPostcode);
        edtPhoneNumber = (EditText) findViewById(R.id.edtPhoneNumber);

        edtForename.setText(User.getInstance().getForename());
        edtSurname.setText(User.getInstance().getSurname());
        edtEmailAddress.setText(User.getInstance().getEmailAddress());
        edtAddress.setText(User.getInstance().getAddress());
        edtCity.setText(User.getInstance().getCity());
        edtPostcode.setText(User.getInstance().getPostcode());
        edtPhoneNumber.setText(User.getInstance().getPhoneNumber());

        btnUpdateAccount = (Button) findViewById(R.id.btnUpdate);
        btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccount();
            }
        });

        btnDeleteAccount = (Button) findViewById(R.id.btnDelete);
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeletion(v);
            }
        });
    }

    private void updateAccount() {
        InputValidationHelper inputValidationHelper = new InputValidationHelper();
        int customerID = User.getInstance().getCustomerID();
        String forename = edtForename.getText().toString();
        if(!inputValidationHelper.isValidName(forename)) {
            Toast.makeText(this, "Forename Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String surname = edtSurname.getText().toString();
        if(!inputValidationHelper.isValidName(surname)) {
            Toast.makeText(this, "Surname Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String emailAddress = edtEmailAddress.getText().toString();
        if(!inputValidationHelper.isValidEmail(emailAddress)) {
            Toast.makeText(this, "Email Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String address = edtAddress.getText().toString();
        if(!inputValidationHelper.isValidAddress(address)) {
            Toast.makeText(this, "Address Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String city = edtCity.getText().toString();
        if(!inputValidationHelper.isValidCity(city)) {
            Toast.makeText(this, "City Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String postcode = edtPostcode.getText().toString();
        if(!inputValidationHelper.isValidPostcode(postcode)) {
            Toast.makeText(this, "Postcode Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        String phoneNumber = edtPhoneNumber.getText().toString();
        if(!inputValidationHelper.isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Phone Number Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        UserSettingsSub userSettingsSub = new UserSettingsSub(this);
        userSettingsSub.execute("edit_profile", customerID, forename, surname, emailAddress,
                address, city, postcode, phoneNumber);
    }

    private void confirmDeletion(final View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Account Deletion");
        alert.setMessage("Are you sure you want to delete your account?");
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProfile(v);
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

    private void deleteProfile(View v) {
        int customer_id = User.getInstance().getCustomerID();
        String type = "delete_profile";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, customer_id);
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

    private class UserSettingsSub extends AsyncTask<Object, Void, String> {

        Context context;
        AlertDialog alertDialog;
        String methodType;

        public UserSettingsSub(Context ctx) {
            context = ctx;
        }

        @Override
        protected String doInBackground(Object... params) {
            String type = (String) params[0];
            methodType = type;
            //String edit_profile_url = "http://10.0.2.2/project/update_profile.php";
            String edit_profile_url = "http://192.168.0.14/project/update_profile.php";
            if(type.equals("edit_profile")) {
                try {
                int customer_id = (int) params[1];
                String forename = (String) params[2];
                String surname = (String) params[3];
                String email_address = (String) params[4];
                String address = (String) params[5];
                String city = (String) params[6];
                String postcode = (String) params[7];
                String phone_number = (String) params[8];
                URL url = new URL(edit_profile_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("customer_id", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(customer_id), "UTF-8")
                        +"&"+URLEncoder.encode("forename","UTF-8")+"="+URLEncoder.encode(forename, "UTF-8")
                        +"&"+URLEncoder.encode("surname","UTF-8")+"="+URLEncoder.encode(surname, "UTF-8")
                        +"&"+URLEncoder.encode("email_address","UTF-8")+"="+URLEncoder.encode(email_address, "UTF-8")
                        +"&"+URLEncoder.encode("address","UTF-8")+"="+URLEncoder.encode(address, "UTF-8")
                        +"&"+URLEncoder.encode("city","UTF-8")+"="+URLEncoder.encode(city, "UTF-8")
                        +"&"+URLEncoder.encode("postcode","UTF-8")+"="+URLEncoder.encode(postcode, "UTF-8")
                        +"&"+URLEncoder.encode("phone_number","UTF-8")+"="+URLEncoder.encode(phone_number, "UTF-8");
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
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Account Update Status");
        }

        @Override
        protected void onPostExecute(String result) {
            alertDialog.setMessage(result);
            Intent i = new Intent(getApplicationContext(), UserSettingsActivity.class);
            startActivity(i);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
