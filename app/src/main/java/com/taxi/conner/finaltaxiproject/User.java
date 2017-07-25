package com.taxi.conner.finaltaxiproject;

/**
 * Created by Conner on 22/03/2017.
 */

/**
 * Class that defines a 'User' object
 */
public class User {

    public int customerID = -1; //Not logged in
    public String forename;
    public String surname;
    public String emailAddress;
    public String address;
    public String city;
    public String postcode;
    public String phoneNumber;

    /**
     * Singleton for creating a global instance of user after login
     */
    private static User instance = new User();
    public static User getInstance() { return instance; }

    private User() {}

    public void login(int id, String forename, String surname, String email, String address,
                      String city, String postcode, String phoneNumber) {
        this.customerID = id;
        this.forename = forename;
        this.surname = surname;
        this.emailAddress = email;
        this.address = address;
        this.city = city;
        this.postcode = postcode;
        this.phoneNumber = phoneNumber;
    }

    public void logout() {
        this.customerID = -1;
        this.forename = null;
        this.surname = null;
        this.emailAddress = null;
        this.address = null;
        this.city = null;
        this.postcode = null;
        this.phoneNumber = null;
    }

    public boolean isLoggedout() {
        if(customerID == -1)
            return true;
        else
            return false;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
