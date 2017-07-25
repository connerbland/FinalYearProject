package com.taxi.conner.finaltaxiproject;

/**
 * Created by Conner on 26/03/2017.
 */

public class Distance {
    public String text;
    public int value;

    public Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public double getDistance() {
        return value;
    }
}
