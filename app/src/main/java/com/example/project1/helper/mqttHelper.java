package com.example.project1.helper;


import android.content.Context;
import android.util.Log;

import com.example.project1.MainActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class mqttHelper {
    public MqttAndroidClient client;
    String clientId = MqttClient.generateClientId();
    final String serverUri = "tcp://192.168.0.106:1883";
    final String topic = "finalProject/MPU";
    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

    public mqttHelper(Context context){
        client = new MqttAndroidClient(context,serverUri,
                clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        connect();
    }

    public void setCallback(MqttCallback callback) {
        client.setCallback(callback);
    }

    private void connect() {



        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                    Log.d("MQTT", "onSuccess");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    //Toast.makeText(MainActivity.this,"not connected",Toast.LENGTH_LONG).show();
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(){
        // Initialize Subscribe process

        int Qos = 1;
        try {
//          final MqttMessage msg = new MqttMessage();
            IMqttToken subToken = client.subscribe(topic,Qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.d("MQTT", "Subscribed");
//                                Log.d("MQTT", msg.toString());
//                                textView.setText(msg.toString());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    //Toast.makeText(MainActivity.this,"Failed to subscribe",Toast.LENGTH_LONG).show();
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d("MQTT", "Failed to subscribe");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void registerResources() {
    }

    public void unregisterResources() {
    }

    public void close() {
    }


}
