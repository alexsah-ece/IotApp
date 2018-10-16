package com.example.sahinis.myfirstapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.hardware.*;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView x, y, z, log;
    private EditText ip;
    private Button button;

    private MQTT mqtt;
    private String brokerIP;
    private static final String TAG = "Alex";
    private static final int SAMPLING_DELAY = 500000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mqtt = new MQTT(getApplicationContext(), (TextView)findViewById(R.id.logText));

        ip = (EditText) findViewById(R.id.ipInput);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brokerIP = ip.getText().toString();
                ip.setText(" ");
                Log.w("Mqtt", brokerIP);
                mqtt.connect(brokerIP);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final float x1, y1, z1;
        x1 = event.values[0];
        y1 = event.values[1];
        z1 = event.values[2];
        Log.d(TAG, "X:" + x1 + "\tY:" + y1 + "\tZ:" + z1);
        try {

            mqtt.publish(x1, y1, z1);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        x = (TextView) findViewById(R.id.tX);
        y = (TextView) findViewById(R.id.tY);
        z = (TextView) findViewById(R.id.tZ);
        x.setText(String.format("%f", event.values[0]));
        y.setText(String.format("%f", event.values[1]));
        z.setText(String.format("%f", event.values[2]));
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SAMPLING_DELAY);
        //mqtt.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
        //mqtt.disconnect();
    }
}


