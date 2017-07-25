package com.taxi.conner.finaltaxiproject;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Conner on 26/03/2017.
 */

/**
 * The direction finder does three different tasks.
 * Find the route using two addresses
 * Find the route using two pairs of lat/long coordinates
 * Find the route between the taxi driver and the customer to calculate arrival time
 */
public class DirectionFinder {
    private static final String GOOGLE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyCuKQALmxWzmQB75hFEkjDO4NsTymGmmU0";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String locationLat, locationLong, destinationLat, destinationLong;
    String type;
    private ArrayList<Double> startEndLatLong = new ArrayList<>();

    // Calculates route using addresses
    public DirectionFinder(DirectionFinderListener listener, String origin, String destination, String type) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.type = type;
    }

    //calculates route using coordinates - Also calucates distance between taxi and customers
    public DirectionFinder(DirectionFinderListener listener, String locationLat, String locationLong, String destinationLat, String destinationLong, String type) {
        this.listener = listener;
        this.locationLat = locationLat;
        this.locationLong = locationLong;
        this.destinationLat = destinationLat;
        this.destinationLong = destinationLong;
        this.type = type;
    }

    public void execute() throws UnsupportedEncodingException {
        if(type == "customer") { //Only clear map for route finding
            listener.onDirectionFinderStart();
        }
        new DownloadRawData().execute(createURL());
    }

    public String createURL() throws UnsupportedEncodingException {
        if(origin != null && destination != null) {
            String urlOrigin = URLEncoder.encode(origin, "utf-8");
            String urlDestination = URLEncoder.encode(destination, "utf-8");
            return GOOGLE_DIRECTIONS_URL + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
        } else if (locationLat != null && locationLong != null && destinationLat != null && destinationLong != null) {
            String urlLocationLat = URLEncoder.encode(locationLat, "utf-8");
            String urlLocationLong = URLEncoder.encode(locationLong, "utf-8");
            String urlDestinationLat = URLEncoder.encode(destinationLat, "utf-8");
            String urlDestinationLong = URLEncoder.encode(destinationLong, "utf-8");
            return GOOGLE_DIRECTIONS_URL +"origin="+urlLocationLat+","+urlLocationLong
                    +"&destination="+urlDestinationLat+","+urlDestinationLong+"&key="+GOOGLE_API_KEY;
        }
        return null;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String result = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                    buffer.append(line + "\n");
                }
                startEndLatLong = this.getStartEndLatLong(result);
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private ArrayList<Double> getStartEndLatLong(String strJSON) {
            ArrayList<Double> result = new ArrayList<>();
            JSONObject root = null;
            try {
                root = new JSONObject(strJSON);
                JSONArray routesArray = root.getJSONArray("routes");
                JSONObject routesObject = (JSONObject) routesArray.get(0);
                JSONArray legsArray = routesObject.getJSONArray("legs");
                JSONObject legsObject = (JSONObject) legsArray.get(0);
                JSONObject startLocation = legsObject.getJSONObject("start_location");
                JSONObject endLocation = legsObject.getJSONObject("end_location");
                double startLocationLat = startLocation.getDouble("lat");
                double startLocationLong = startLocation.getDouble("lng");
                double endLocationLat = endLocation.getDouble("lat");
                double endLocationLong = endLocation.getDouble("lng");
                result.add(startLocationLat);
                result.add(startLocationLong);
                result.add(endLocationLat);
                result.add(endLocationLong);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        private void parseJSon(String data) throws JSONException {
            if  (data == null)
                return;
            List<Route> routes = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for(int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                Route route = new Route();

                JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                JSONObject jsonLeg = jsonLegs.getJSONObject(i);
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                route.endAddress = jsonLeg.getString("end_address");
                route.startAddress = jsonLeg.getString("start_address");
                route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                route.points = decodePolyLine(overview_polylineJson.getString("points"));

                routes.add(route);

            }
            if(type == "taxi") { //Found route between customer and taxi, will find arrival time for taxi
                listener.onTimeFound(routes, startEndLatLong);
            } else { //Found route, will retrieve details and draw on map
                listener.onDirectionFinderSuccess(routes, startEndLatLong);
            }
        }

        //Decodes the Google Maps direction data so can be drawn using polylines on map
        private  List<LatLng> decodePolyLine(final String poly) {
            int polyLength = poly.length();
            int index = 0;
            List<LatLng> decoded = new ArrayList<>();
            int lat = 0;
            int lng = 0;

            while (index < polyLength) {
                int b;
                int shift = 0;
                int result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                decoded.add(new LatLng(
                        lat / 100000d, lng / 100000d
                ));
            }
            return decoded;
        }
    }
}