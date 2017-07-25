package com.taxi.conner.finaltaxiproject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Conner on 17/04/2017.
 */

public class InputValidationHelper {

    public boolean isValidName(String input) {
        boolean result = true;
        String regex = "[a-zA-Z]+"; //Only letters
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidEmail(String input) {
        boolean result = true;
        String regex = "[a-zA-Z0-9!#$%&'*+-/=?^_`{|}~]+@{1}[a-z.]+"; //Unlimited valid characters + @ + unlimited lowercase characters and dots
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidAddress(String input) {
        boolean result = true;
        String regex = "[a-zA-Z0-9 ]+"; //At least one number or letter
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidCity(String input) {
        boolean result = true;
        String regex = "[a-zA-Z]+"; //At least one letter
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidPostcode(String input) {
        boolean result = true;
        String regex = "^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$"; //Only UK Postcode
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidPhoneNumber(String input) {
        boolean result = true;
        String regex = "[0]{1}[0-9]{10}"; //Start with '0' and 10 other numbers
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        ArrayList<String> pause = new ArrayList<>();
        if(matcher.matches() == (false)) {
            result = false;
        }
        return result;
    }

    public boolean isValidPassword(String input) {
        boolean result = true;
        //String regex = "^.{8,}$/"; //Minumum of 8 Characters
        //Pattern pattern = Pattern.compile(regex);
        //Matcher matcher = pattern.matcher(input);
        //if(matcher.matches() == (false)) {
        //    result = false;
        //}
        return result;
    }
}