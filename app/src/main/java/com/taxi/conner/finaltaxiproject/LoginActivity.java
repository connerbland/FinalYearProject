package com.taxi.conner.finaltaxiproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailAddress, edtPassword;
    String salt = null;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtEmailAddress = (EditText) findViewById(R.id.edtEmailAddress);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSalt();
            }
        });

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterCustomerActivity.class);
                startActivity(i);
            }
        });
    }

    public void getSalt() {
        LoginSub loginSub = new LoginSub();
        loginSub.execute(edtEmailAddress.getText().toString());
    }

    public void OnLogin() {
        String emailAddress = edtEmailAddress.getText().toString();
        CryptographyHelper cryptographyHelper = new CryptographyHelper();
        String password = cryptographyHelper.hashPasswordWithSalt(edtPassword.getText().toString(), salt);
        String type = "login";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        //ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait.", "Logging in..!", true);
        backgroundWorker.execute(type, emailAddress, password);
    }

    private class LoginSub extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            //String get_salt_url = "http://10.0.2.2/project/get_salt.php";
            String get_salt_url = "http://192.168.0.14/project/get_salt.php";
            try {
                String email_address = (String) params[0];
                URL url = new URL(get_salt_url);
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
            if (result.contentEquals("Account not found")) {
                Toast.makeText(getApplicationContext() ,"Email/Password Incorrect", Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONObject root = new JSONObject(result);
                    JSONArray objectArray = root.getJSONArray("result");
                    JSONObject saltJSON = objectArray.getJSONObject(0);
                    salt = saltJSON.getString("salt");
                    OnLogin();
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
}