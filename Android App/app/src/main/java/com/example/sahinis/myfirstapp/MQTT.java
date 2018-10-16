package com.example.sahinis.myfirstapp;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MQTT {

    public MqttAndroidClient mqttAndroidClient;
    private boolean connected;
    private boolean allowedToPublish;

    private TextView log;

    final String serverUri = "tcp://192.168.1.";
    final String publishTopic = "accel_data";
    final String subscriptionTopic = "android_rpc";
    private final String username = "mqtt-test";
    private final char[] password ={'m','q','t','t','-','t','e','s','t'};

    public MQTT(Context context, android.widget.TextView log){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, MqttClient.generateClientId(), new MemoryPersistence());

        this.log = log;
        log.setText("Status: Not Connected, provide IP \n(\t~ ex: 192.168.1.X)");

        allowedToPublish=false;

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String msg = mqttMessage.toString();
                Log.w("Mqtt", msg);
                if(msg.equals("send")){
                    allowedToPublish = true ;
                }else{
                    Log.w("Mqtt", "FALSE");
                    allowedToPublish = false;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void connect(String server){

        if (mqttAndroidClient.isConnected()){
            return;
        }

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{"tcp://" + server});
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password);

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "Successful connection" );
                    log.setText("Status: Connected");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect"  + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
            mqttAndroidClient.close();
        }
    }

    public void disconnect(){
        try {
            mqttAndroidClient.disconnect();
            Log.w("Mqtt", "Disconnected");
            log.setText("Status: Not Connected");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(float x, float y, float z) throws JSONException, UnsupportedEncodingException {
        if (mqttAndroidClient.isConnected() && allowedToPublish) {
            try {

                JSONObject payload = new JSONObject();
                payload.put("X Axis", String.format("%f", x));
                payload.put("Y Axis", String.format("%f", y));
                payload.put("Z Axis", String.format("%f", z));

                MqttMessage message = new MqttMessage(payload.toString().getBytes());
                Log.w("Mqtt", "Publishing...");
                mqttAndroidClient.publish(publishTopic, message);

            } catch (MqttException ex) {
                System.err.println("Exception whilst publishing");
                ex.printStackTrace();
            }
        }else if(mqttAndroidClient.isConnected() && !allowedToPublish){
            Log.w("Mqtt", "Not alowed, can not publish");
        }else{
            Log.w("Mqtt", "Not connected, can not publish");
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
}