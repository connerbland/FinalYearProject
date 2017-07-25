package com.taxi.conner.finaltaxiproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Conner on 26/03/2017.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> routes, ArrayList<Double> latLong);
    void onTimeFound(List<Route> routes, ArrayList<Double> latLong);
}
