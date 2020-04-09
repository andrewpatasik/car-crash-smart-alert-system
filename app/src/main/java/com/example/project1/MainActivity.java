package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    Button button;
    MqttAndroidClient client;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.connect_mqtt);

        startMqtt();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = "test";
                int Qos = 1;
                try {
//                        final MqttMessage msg = new MqttMessage();
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
                            // The subscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards
                            Log.d("MQTT", "Re-subscribed");

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    protected void startMqtt(){
        String clientId = MqttClient.generateClientId();
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        client = new MqttAndroidClient(MainActivity.this, "tcp://192.168.0.106:1883",
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this,"connected",Toast.LENGTH_LONG).show();
                    Log.d("MQTT", "onSuccess");

                    // Now, try to publish a message
//                            String msg = "Test connection from UI";
//                            try
//                            {
//                                MqttMessage message = new MqttMessage();
//                                message.setQos(1);
//                                message.setPayload(msg.getBytes());
//                                client.publish("test", message);
//                            }
//                            catch (MqttException e)
//                            {
//                                Log.d(getClass().getCanonicalName(), "Publish failed with reason code = " + e.getReasonCode());
//                            }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this,"not connected",Toast.LENGTH_LONG).show();
                    Log.d("MQTT", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            startMqtt();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
            textView.setText(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}
