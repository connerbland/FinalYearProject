package com.taxi.conner.finaltaxiproject;

/**
 * Created by Conner on 16/04/2017.
 */

public class HistoryItem {
    private int routeID, customer_id, taxi_id;
    private double cost;
    private int rating;
    private String review;
    private double departure_lat, departure_long, destination_lat, destination_long;
    private String departure_address, destination_address, datetime;

    public HistoryItem(int routeID, int customer_id, int taxi_id, double cost, int rating,
                       String review, double departure_lat, double departure_long,
                       double destination_lat, double destination_long, String departure_address,
                       String destination_address, String datetime) {
        this.routeID = routeID;
        this.customer_id = customer_id;
        this.taxi_id = taxi_id;
        this.cost = cost;
        this.rating = rating;
        this.review = review;
        this.departure_lat = departure_lat;
        this.departure_long = departure_long;
        this.destination_lat = destination_lat;
        this.destination_long = destination_long;
        this.departure_address = departure_address;
        this.destination_address = destination_address;
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        if(this.rating == 0) {
            return "From: " + departure_address + "\nTo: " + destination_address + "\nWhen: " + datetime;
            /*+ "\nCost: £" + Double.toString(cost) + "\nNo Rating " + "\nNo Review";*/
        } else if((this.rating > 0 && this.review == "") || (this.rating > 0 && this.review == null)) {
            return "From: " + departure_address + "\nTo: " + destination_address + "\nWhen: " + datetime;
                    /*+ "\nCost: £" + Double.toString(cost) + "\nYour Rating: " + Integer.toString(rating) + "/5" + "\nNo Review";*/
        } else {
            return "From: " + departure_address + "\nTo: " + destination_address + "\nWhen: " + datetime;
                    /*+ "\nCost: £" + Double.toString(cost) + "\nYour Rating: " + Integer.toString(rating) + "/5" + "\nYour Review: " + review;*/
        }
    }

    public double getDeparture_lat() {
        return departure_lat;
    }

    public double getDeparture_long() {
        return departure_long;
    }

    public double getDestination_lat() {
        return destination_lat;
    }

    public double getDestination_long() {
        return destination_long;
    }

    public double getCost() {
        return cost;
    }

    public int getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public String getDeparture_address() {
        return departure_address;
    }

    public String getDestination_address() {
        return destination_address;
    }

    public String getDatetime() {
        return datetime;
    }
}
