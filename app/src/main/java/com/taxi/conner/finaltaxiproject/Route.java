package com.taxi.conner.finaltaxiproject;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Conner on 26/03/2017.
 */

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public List<LatLng> points;

    // Hard coded route costs
    // Need to be editable during in-app for final product
    public double initialCost = 4;
    public double costPerMetre = 0.0006;

    public double getFinalCost(double distance) {
        double finalCost = (costPerMetre*distance)+initialCost;
        return finalCost;
    }
}
