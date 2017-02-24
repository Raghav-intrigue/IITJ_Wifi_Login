package com.abhi.wifi_login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class Main_Activity extends Activity {

    Button btn;
    EditText eT, eT2;
    CheckBox cB;
    String id, pwd;
    String pass1;
    User_Info user;
    String result="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.button);
        eT = (EditText) findViewById(R.id.editText);
        eT2 = (EditText) findViewById(R.id.editText2);
        cB = (CheckBox) findViewById(R.id.checkBox);

        loadSavedPreferences();
        // show location button click event
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Connection_Detector cd = new Connection_Detector(getApplicationContext());
                id = eT.getText().toString().trim();
                pwd = eT2.getText().toString().trim();
                if (id.equals("")) {
                    Toast.makeText(Main_Activity.this,
                            "Name Field is empty", Toast.LENGTH_SHORT).show();
                } else if (pwd.equals("")) {
                    Toast.makeText(Main_Activity.this,
                            "Contact Field is empty", Toast.LENGTH_SHORT).show();
                } else if (cd.isConnectingToInternet())
                // true or false
                {
                    savePreferences("CheckBox_Value", cB.isChecked());
                    if (cB.isChecked()) {
                        savePreferences("saved_id", eT.getText().toString());
                        savePreferences("saved_pwd", eT2.getText().toString());
                    }
                    //new HttpAsyncTask().execute("https://10.0.1.254:4100/wgcgi.cgi");
                } else {
                    showAlertDialog(Main_Activity.this,
                            "No Internet Connection",
                            "No internet connection.", false);
                }
            }
        });

    }

    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean checkBoxValue = sharedPreferences.getBoolean("CheckBox_Value", false);
        String saved_id = sharedPreferences.getString("saved_id", "");
        String saved_pwd = sharedPreferences.getString("saved_pwd", "");
        if (checkBoxValue) {
            cB.setChecked(true);
        } else {
            cB.setChecked(false);
        }

        eT.setText(saved_id);
        eT2.setText(saved_pwd);
    }

    private void savePreferences(String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        // alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void Login_task(){

        // Tag used to cancel the request
        String req_tag = "POST_REQUEST";

        String url = "https://10.0.1.254:4100/wgcgi.cgi";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest strReq = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG, response.toString());
                        pDialog.hide();
                        if(response.toString().contains("successfully authenticated"))
                        {
                            Toast.makeText(Main_Activity.this, "Successfully Authenticated.", Toast.LENGTH_SHORT).show();
                            result = "Successfully Authenticated.";
                        }
                        else
                        {
                            Toast.makeText(Main_Activity.this, "Invalid Credentials Provided.", Toast.LENGTH_SHORT).show();
                            result = "Invalid Credentials Provided.";
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        pDialog.hide();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("fw_username", user.getId());
                params.put("fw_password", user.getPwd());
                params.put("fw_domain", "gpra.in");
                params.put("submit", "Login");
                params.put("action", "fw_logon");
                params.put("fw_logon_type", "logon");
                params.put("redirect", "");
                params.put("lang", "en-US");
                return params;
            }
        };

        // Adding request to request queue
        RequestQueue mRequestQueue;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        strReq.setTag(req_tag);
        mRequestQueue.add(strReq);

        if(result.contains("Successfully")) {
            try {
                JSONObject pass = new JSONObject();
                pass.accumulate("id", id);
                pass.accumulate("pwd", pwd);
                pass1 = pass.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent i = new Intent(Main_Activity.this, Logged_In.class);
            // sending data to new activity
            i.putExtra("data", pass1);
            startActivity(i);
        }
    }


}