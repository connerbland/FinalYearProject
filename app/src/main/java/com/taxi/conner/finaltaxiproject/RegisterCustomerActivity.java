package com.taxi.conner.finaltaxiproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RegisterCustomerActivity extends AppCompatActivity {

    private EditText edtForename, edtSurname, edtEmailAddress, edtAddress, edtCity,
            edtPostcode, edtPhoneNumber, edtPassword;
    private String forename, surname, emailAddress, address, city, postcode, phoneNumber, password, salt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        edtForename = (EditText)findViewById(R.id.edtForename);
        edtSurname = (EditText)findViewById(R.id.edtSurname);
        edtEmailAddress = (EditText)findViewById(R.id.edtEmailAddress);
        edtAddress = (EditText)findViewById(R.id.edtAddress);
        edtCity = (EditText)findViewById(R.id.edtCity);
        edtPostcode = (EditText)findViewById(R.id.edtPostcode);
        edtPhoneNumber = (EditText)findViewById(R.id.edtPhoneNumber);
        edtPassword = (EditText)findViewById(R.id.edtPassword);

        Button btnLogin = (Button) findViewById(R.id.btnContinue);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailsValidation(v);
            }
        });

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }

    public void DetailsValidation(View view) {
        InputValidationHelper inputValidationHelper = new InputValidationHelper();
        forename = edtForename.getText().toString();
        if(!inputValidationHelper.isValidName(forename)) {
            Toast.makeText(this, "Forename Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        surname = edtSurname.getText().toString();
        if(!inputValidationHelper.isValidName(surname)) {
            Toast.makeText(this, "Surname Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        emailAddress = edtEmailAddress.getText().toString();
        if(!inputValidationHelper.isValidEmail(emailAddress)) {
            Toast.makeText(this, "Email Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        address = edtAddress.getText().toString();
        if(!inputValidationHelper.isValidAddress(address)) {
            Toast.makeText(this, "Address Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        city = edtCity.getText().toString();
        if(!inputValidationHelper.isValidCity(city)) {
            Toast.makeText(this, "City Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        postcode = edtPostcode.getText().toString();
        if(!inputValidationHelper.isValidPostcode(postcode)) {
            Toast.makeText(this, "Postcode Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        phoneNumber = edtPhoneNumber.getText().toString();
        if(!inputValidationHelper.isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Phone Number Invalid", Toast.LENGTH_LONG).show();
            return;
        }
        password = edtPassword.getText().toString();
        if(!inputValidationHelper.isValidPassword(password)) {
            Toast.makeText(this, "Password Invalid", Toast.LENGTH_LONG).show();
            return;
        }

        // Generate a random salt for customer and use to hash password
        CryptographyHelper cryptographyHelper = new CryptographyHelper();
        salt = cryptographyHelper.createSalt();
        password = cryptographyHelper.hashPasswordWithSalt(password, salt);

        // Check that the email address currently in system or not
        String type = "check_email_available";
        RegisterCustomerSub registerCustomerSub = new RegisterCustomerSub();
        registerCustomerSub.execute(emailAddress);
    }

    /**
     * After all data is valid, create account
     */
    public void Register() {
        String type = "register";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, forename, surname, emailAddress, address, city, postcode,
                phoneNumber, password, salt);
    }

    private class RegisterCustomerSub extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            //String check_email_available_url = "http://10.0.2.2/project/check_email_available.php";
            String check_email_available_url = "http://192.168.0.14/project/check_email_available.php";
                try {
                    String email_address = (String) params[0];
                    URL url = new URL(check_email_available_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("email_address", "UTF-8") + "=" + URLEncoder.encode(email_address, "UTF-8");
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contentEquals("Email Unavailable")) {
                Toast.makeText(getApplicationContext() ,"Email Unavailable", Toast.LENGTH_LONG).show();
            } else if (result.contentEquals("Email Available")) {
                Register();
            }
        }
    }
}
