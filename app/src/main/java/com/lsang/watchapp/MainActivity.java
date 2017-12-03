package com.lsang.watchapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.os.AsyncTask;
import java.net.*;
import java.io.*;

public class MainActivity extends WearableActivity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getName();
    private static final int threshold_max = 70;
    private float prev_hr = 0;
    private float curr_hr = 0;
    private TextView mTextView1;
    private TextView mTextView2;
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;
    private BatteryManager mBatteryManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView1 = (TextView) findViewById(R.id.textView1);
        mTextView2 = (TextView) findViewById(R.id.textView2);
        mTextView1.setText("Heart Rate...");
        Log.i(TAG, "------- onCreate");
        // Enables Always-on
        setAmbientEnabled();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mBatteryManager = (BatteryManager)getSystemService(BATTERY_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, mHeartRateSensor, mSensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        curr_hr = sensorEvent.values[0];
        Log.d(TAG, "------- sensor event: " + sensorEvent.accuracy + " = " + curr_hr);
        mTextView1.setText("HR1: " + String.valueOf(curr_hr) + " bpm");
        int batLevel = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Log.d(TAG, "------- Batter Level = : " + batLevel);
        if (curr_hr >= threshold_max && prev_hr < threshold_max) {
            String url = "http://192.168.2.11:5000/hr?val=" + Math.round(curr_hr);
            new CallAPI().execute(url);
        }
        prev_hr = curr_hr;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "------- accuracy changed: " + i);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "------- Stopped");
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    public class CallAPI extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... url) {
            String result = "";
            try {
                URL obj = new URL(url[0]);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                int responseCode = con.getResponseCode();
                System.out.println("Sending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                result = response.toString();
                System.out.println(result);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            return result;
        }

        protected void onPostExecute(String result) {
            mTextView2.setText("Result: " + result);
        }
    }
}
