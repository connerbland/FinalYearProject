package com.taxi.conner.finaltaxiproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;

/**
 * Created by Conner on 16/03/2017.
 */

/**
 *
 */
public class BackgroundWorker extends AsyncTask<Object, Void, String> {

    Context context;
    AlertDialog alertDialog;
    String methodType;
    private getTaskListener mListener = null;

    //Global variables need to store for 'Create a Route' -> 'Add Rating/Review'
    int intCustomerID, intTaxiID;
    double doubleCost, doubleDepartureLat, doubleDepartureLong, doubleDestinationLat, doubleDestinationLong;
    String strDatetime;

    public BackgroundWorker(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(Object... params) {
        String type = (String) params[0];
        methodType = type;

        //Desktop URLs
        //String login_url = "http://10.0.2.2/project/login_and_retrieve_details.php";
        //String register_url = "http://10.0.2.2/project/register.php";
        //String create_route_url = "http://10.0.2.2/project/create_route.php";
        //String rating_review_url = "http://10.0.2.2/project/add_rating_review.php";
        //String delete_profile_url = "http://10.0.2.2/project/delete_profile.php";

        //Mobile URLs
        String login_url = "http://192.168.0.14/project/login_and_retrieve_details.php";
        String register_url = "http://192.168.0.14/project/register.php";
        String create_route_url = "http://192.168.0.14/project/create_route.php";
        String rating_review_url = "http://192.168.0.14/project/add_rating_review.php";
        String delete_profile_url= "http://192.168.0.14/project/delete_profile.php";

        URL url = null;

        //All potential variable need intialising before due to assignment inside 'if' statements
        String email_address = "";
        String password = "";
        String forename = "";
        String surname = "";
        String address = "";
        String city = "";
        String postcode = "";
        String phone_number = "";
        int customer_id = 0;
        int taxi_id = 0;
        double cost = 0;
        double departure_lat = 0;
        double departure_long = 0;
        double destination_lat = 0;
        double destination_long = 0;
        String departure_address = "";
        String destination_address = "";
        int rating = 0;
        String review = "";
        String salt = "";
        String datetime = "";

        try {

            // Sets parameters based on task
            if (type.equals("login")) {
                email_address = (String) params[1];
                password = (String) params[2];
                url = new URL(login_url);
            } else if(type.equals("register")) {
                forename = (String) params[1];
                surname = (String) params[2];
                email_address = (String) params[3];
                address = (String) params[4];
                city = (String) params[5];
                postcode = (String) params[6];
                phone_number = (String) params[7];
                password = (String) params[8];
                salt = (String) params[9];
                url = new URL(register_url);
            } else if (type.equals("create_route")) {
                customer_id = (int) params[1];
                intCustomerID = customer_id;    // Used for Rating/Review
                taxi_id = (int) params[2];
                intTaxiID = taxi_id;
                cost = (double) params[3];
                BigDecimal bd = new BigDecimal(cost); // Convert to 2dp
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                cost = bd.doubleValue();
                doubleCost = cost;
                departure_lat = (double) params[4];
                doubleDepartureLat = departure_lat;
                departure_long = (double) params[5];
                doubleDepartureLong = departure_long;
                destination_lat = (double) params[6];
                doubleDestinationLat = destination_lat;
                destination_long = (double) params[7];
                doubleDestinationLong = destination_long;
                departure_address = (String) params[8];
                destination_address = (String) params[9];
                datetime = (String) params[10];
                strDatetime = datetime; //Since datetime is used, even identical routes will not cause sql problems
                url = new URL(create_route_url);
            } else if(type.equals("add_rating_review")) {
                customer_id = (int) params[1];
                taxi_id = (int) params[2];
                cost = (double) params[3];
                departure_lat = (double) params[4];
                departure_long = (double) params[5];
                destination_lat = (double) params[6];
                destination_long = (double) params[7];
                rating = (int) params[8];
                review = (String) params[9];
                datetime = (String) params[10];
                url = new URL(rating_review_url);
            } else if (type.equals("delete_profile")) {
                customer_id = (int) params[1];
                url = new URL(delete_profile_url);
            }

            //Opens all connections needed to create and send POST data
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = "";

            // Writes POST data dependent on task and variables needed
            if (type.equals("login")) {
                post_data = URLEncoder.encode("email_address", "UTF-8") + "=" + URLEncoder.encode(email_address, "UTF-8") + "&"
                        + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
            } else if(type.equals("register")) {
                post_data = URLEncoder.encode("forename", "UTF-8") + "=" + URLEncoder.encode(forename, "UTF-8")
                        + "&" + URLEncoder.encode("surname", "UTF-8") + "=" + URLEncoder.encode(surname, "UTF-8")
                        + "&" + URLEncoder.encode("email_address", "UTF-8") + "=" + URLEncoder.encode(email_address, "UTF-8")
                        + "&" + URLEncoder.encode("address", "UTF-8") + "=" + URLEncoder.encode(address, "UTF-8")
                        + "&" + URLEncoder.encode("city", "UTF-8") + "=" + URLEncoder.encode(city, "UTF-8")
                        + "&" + URLEncoder.encode("postcode", "UTF-8") + "=" + URLEncoder.encode(postcode, "UTF-8")
                        + "&" + URLEncoder.encode("phone_number", "UTF-8") + "=" + URLEncoder.encode(phone_number, "UTF-8")
                        + "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
                        + "&" + URLEncoder.encode("salt", "UTF-8") + "=" + URLEncoder.encode(salt, "UTF-8");
            } else if (type.equals("create_route")) {
                post_data = URLEncoder.encode("customer_id","UTF-8")+"="+URLEncoder.encode(Integer.toString(customer_id), "UTF-8")
                        +"&"+URLEncoder.encode("taxi_id","UTF-8")+"="+URLEncoder.encode(Integer.toString(taxi_id), "UTF-8")
                        +"&"+URLEncoder.encode("cost","UTF-8")+"="+URLEncoder.encode(Double.toString(cost), "UTF-8")
                        +"&"+URLEncoder.encode("departure_lat","UTF-8")+"="+URLEncoder.encode(Double.toString(departure_lat), "UTF-8")
                        +"&"+URLEncoder.encode("departure_long","UTF-8")+"="+URLEncoder.encode(Double.toString(departure_long), "UTF-8")
                        +"&"+URLEncoder.encode("destination_lat","UTF-8")+"="+URLEncoder.encode(Double.toString(destination_lat), "UTF-8")
                        +"&"+URLEncoder.encode("destination_long","UTF-8")+"="+URLEncoder.encode(Double.toString(destination_long), "UTF-8")
                        +"&"+URLEncoder.encode("departure_address","UTF-8")+"="+URLEncoder.encode(departure_address, "UTF-8")
                        +"&"+URLEncoder.encode("destination_address","UTF-8")+"="+URLEncoder.encode(destination_address, "UTF-8")
                        +"&"+URLEncoder.encode("datetime","UTF-8")+"="+URLEncoder.encode(datetime, "UTF-8");
            } else if(type.equals("add_rating_review")) {
                post_data = URLEncoder.encode("rating","UTF-8")+"="+URLEncoder.encode(Integer.toString(rating), "UTF-8")
                        +"&"+URLEncoder.encode("review","UTF-8")+"="+URLEncoder.encode(review, "UTF-8")
                        +"&"+URLEncoder.encode("customer_id","UTF-8")+"="+URLEncoder.encode(Integer.toString(customer_id), "UTF-8")
                        +"&"+URLEncoder.encode("taxi_id","UTF-8")+"="+URLEncoder.encode(Integer.toString(taxi_id), "UTF-8")
                        +"&"+URLEncoder.encode("cost","UTF-8")+"="+URLEncoder.encode(Double.toString(cost), "UTF-8")
                        +"&"+URLEncoder.encode("departure_lat","UTF-8")+"="+URLEncoder.encode(Double.toString(departure_lat), "UTF-8")
                        +"&"+URLEncoder.encode("departure_long","UTF-8")+"="+URLEncoder.encode(Double.toString(departure_long), "UTF-8")
                        +"&"+URLEncoder.encode("destination_lat","UTF-8")+"="+URLEncoder.encode(Double.toString(destination_lat), "UTF-8")
                        +"&"+URLEncoder.encode("destination_long","UTF-8")+"="+URLEncoder.encode(Double.toString(destination_long), "UTF-8")
                        +"&"+URLEncoder.encode("datetime","UTF-8")+"="+URLEncoder.encode(datetime, "UTF-8");
            } else if (type.equals("delete_profile")) {
                post_data = URLEncoder.encode("customer_id","UTF-8")+"="+URLEncoder.encode(Integer.toString(customer_id), "UTF-8");
            }

            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

            //Set the result as the 'echo'ed data from PHP
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
        return null;
    }

    @Override
    protected void onPreExecute() {
        alertDialog = new AlertDialog.Builder(context).create();
        if (methodType == "login")
            alertDialog.setTitle("Login Status");
        else if(methodType == "register")
            alertDialog.setTitle("Registration Status");
        else if(methodType == "view_history")
            alertDialog.setTitle("Refresh Status");
        else if(methodType == "create_route")
            alertDialog.setTitle("Create Route Status");
        else if(methodType == "add_rating_review")
            alertDialog.setTitle("Add Rating and/or Review Status");
        else if(methodType == "delete_profile")
            alertDialog.setTitle("Delete Account Status");
    }

    //Listener used by test cases
    public BackgroundWorker setListener(getTaskListener listener) {
        this.mListener = listener;
        return this;
    }

    //Interface used by test cases
    public static interface getTaskListener {
        public void onComplete(String result, Exception e);
    }

    @Override
    protected void onPostExecute(String result) {
        alertDialog.setMessage(result);

        if(result.contentEquals("Login failed")) {
            Toast.makeText(context,"Email/Password Incorrect", Toast.LENGTH_LONG).show();
        }

        if(result.contentEquals("Route creation successful")) {
            alertDialog.setMessage("Route Creation Successful");
            Intent intent = new Intent(context, RatingReviewActivity.class);
            Bundle b = new Bundle();
            b.putInt("customerID", intCustomerID);
            b.putInt("taxiID", intTaxiID);
            b.putDouble("cost", doubleCost);
            DecimalFormat dfPos = new DecimalFormat("#.######"); //Fixes bug causing large number of decimals. E.g. 45.564349999..
            dfPos.setRoundingMode(RoundingMode.CEILING);
            DecimalFormat dfNeg = new DecimalFormat("-#.######"); //Fixes bug causing large number of decimals. E.g. 45.564349999..
            dfNeg.setRoundingMode(RoundingMode.CEILING);
            if(doubleDepartureLat >= 0) {
                dfPos.format(doubleDepartureLat);
            } else {
                dfNeg.format(doubleDepartureLat);
            }
            b.putDouble("departureLat", doubleDepartureLat);
            if(doubleDepartureLong >= 0) {
                dfPos.format(doubleDepartureLong);
            } else {
                dfNeg.format(doubleDepartureLong);
            }
            b.putDouble("departureLong", doubleDepartureLong);
            if(doubleDestinationLat >= 0) {
                dfPos.format(doubleDestinationLat);
            } else {
                dfNeg.format(doubleDestinationLat);
            }
            b.putDouble("destinationLat", doubleDestinationLat);
            if(doubleDestinationLong >= 0) {
                dfPos.format(doubleDestinationLong);
            } else {
                dfNeg.format(doubleDestinationLong);
            }
            b.putDouble("destinationLong", doubleDestinationLong);
            b.putString("datetime", strDatetime);
            intent.putExtras(b);
            context.startActivity(intent);
            //context.startActivity(new Intent(context, RatingReviewActivity.class));
        }

        //After a user updates their account
        if(result.contentEquals("Record update successful")) {
            alertDialog.setMessage("Thank you for leaving for your feedback");
            Toast.makeText(context,"Thanks you for your feedback", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(context, JourneyRouteActivity.class));
        }

        if(result.contentEquals("Registration successful")) {
            alertDialog.setMessage("Account Created");
            Toast.makeText(context,"Registration Successful", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(context, LoginActivity.class));
        }

        if(result.contentEquals("Profile deletion successful")) {
            alertDialog.setMessage("Account Successfully Deleted");
            Toast.makeText(context,"Sorry to see you go", Toast.LENGTH_LONG).show();
            User.getInstance().logout();
            context.startActivity(new Intent(context, LoginActivity.class));
        }

        // After login successful
        if(methodType == "login" && !result.contentEquals("Login failed")) {
            //Use JSON data to log in user account details
            try {
                JSONObject root = new JSONObject(result);
                JSONArray objectArray = root.getJSONArray("result");
                JSONObject jsonObject = objectArray.getJSONObject(0);
                int loginCustomerID = jsonObject.getInt("customer_id");
                String loginForename = jsonObject.getString("forename");
                String loginSurname = jsonObject.getString("surname");
                String loginEmailAddress = jsonObject.getString("email_address");
                String loginAddress = jsonObject.getString("address");
                if(loginAddress == "null")
                    loginAddress = null;
                String loginCity = jsonObject.getString("city");
                String loginPhoneNumber = jsonObject.getString("phone_number");
                if(loginPhoneNumber == "null")
                    loginPhoneNumber = null;
                String loginPostcode = jsonObject.getString("postcode");
                if(loginPostcode == "null")
                    loginPostcode = null;

                User.getInstance().login(loginCustomerID, loginForename, loginSurname,
                        loginEmailAddress, loginAddress, loginCity, loginPostcode, loginPhoneNumber);

                context.startActivity(new Intent(context, JourneyRouteActivity.class));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}