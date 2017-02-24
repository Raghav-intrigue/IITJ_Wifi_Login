package com.abhi.wifi_login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class Logged_In extends Activity {
    /**
     * Called when the activity is first created.
     */
    TextView tV,tV2;
    Button btn;
    String details, id, pwd,pass1;
    String result = "";
    boolean started = false;
    Handler handler;
    Runnable runnable;
    int log_out=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);

        Intent i = getIntent();
        // getting attached intent data
        details = i.getStringExtra("data");
        //Log.i("MyActivity1",details);

        try {
            JSONObject jObject = new JSONObject(details);
            id = jObject.getString("id");
            pwd = jObject.getString("pwd");
        } catch (JSONException e1) {
            e1.printStackTrace();
            // Toast.makeText(getBaseContext(), "No item Found",
            // Toast.LENGTH_LONG).show();
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        btn = (Button) findViewById(R.id.button2);
        tV = (TextView) findViewById(R.id.textView);
        tV2 = (TextView) findViewById(R.id.textView2);
        tV.setText("Logged In as ");
        tV2.setText(id);
        //int SPLASH_TIME_OUT = 60000;

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Connection_Detector cd = new Connection_Detector(getApplicationContext());
                if (cd.isConnectingToInternet())
                // true or false
                {
                    Logout_task();
                }
                else {
                    showAlertDialog(Logged_In.this,
                            "No Internet Connection",
                            "No internet connection.");
                }
            }
        });


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //new HttpAsyncTaskLogin().execute("https://10.0.1.254:4100/wgcgi.cgi");
                Login_task();
                if(started) {
                    start();
                }
            }
        };

        start();
        //Log.i("result1234", "after_start");
    }

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        //Log.i("result1234", "in_start");
        started = true;
        Connection_Detector cd1 = new Connection_Detector(getApplicationContext());
        if (cd1.isConnectingToInternet() && log_out==1)
        // true or false
        {
            Log.i("log_out=1", String.valueOf(log_out));
            handler.postDelayed(runnable, 60000);
        }
        else if(!cd1.isConnectingToInternet()){
            showAlertDialog(Logged_In.this,
                    "No Internet Connection",
                    "No internet connection.");
        }
        else
        {
            Log.i("log_out=0", String.valueOf(log_out));
        }

    }

    public void showAlertDialog(Context context, String title, String message) {
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
                            Toast.makeText(Logged_In.this, "Successfully Authenticated.", Toast.LENGTH_SHORT).show();
                            result = "Successfully Authenticated.";
                        }
                        else
                        {
                            Toast.makeText(Logged_In.this, "Invalid Credentials Provided.", Toast.LENGTH_SHORT).show();
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
                params.put("fw_username", id);
                params.put("fw_password", pwd);
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
            //Intent i = new Intent(Main_Activity.this, Logged_In.class);
            // sending data to new activity
            //i.putExtra("data", pass1);
            //startActivity(i);
        }
    }

    private void Logout_task(){

        // Tag used to cancel the request
        String req_tag = "POST_REQUEST";

        String url = "https://10.0.1.254:4100/wgcgi.cgi";

        //final ProgressDialog pDialog = new ProgressDialog(this);
        //pDialog.setMessage("Loading...");
        //pDialog.show();

        StringRequest strReq = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d(TAG, response.toString());
                        //pDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                       // pDialog.hide();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Logout", "Logout");
                params.put("action", "fw_logon");
                params.put("fw_logon_type", "logout");
                return params;
            }
        };

        // Adding request to request queue
        RequestQueue mRequestQueue;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        strReq.setTag(req_tag);
        mRequestQueue.add(strReq);

        Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();
        log_out=0;
        Intent i = new Intent(Logged_In.this, Main_Activity.class);
        startActivity(i);

    }
}
