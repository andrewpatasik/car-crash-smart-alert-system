package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    TextView tv_x_value;
    TextView tv_y_value;
    Button button;
    MqttAndroidClient client;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_x_value = findViewById(R.id.tv_x_value);
        tv_y_value = findViewById(R.id.tv_y_value);
        button = findViewById(R.id.button_sub);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        startMqtt();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = "finalProject/MPU";
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
            byte[] msg = message.getPayload();
            JSONObject jsonmsg = new JSONObject(new String(message.getPayload()));
            JSONObject getData = jsonmsg.getJSONObject("data");
            String getX = getData.getString("X-Axis");
            String getY = getData.getString("Y-Axis");
            Double getLat = getData.getDouble("Lat");
            Double getLong = getData.getDouble("Long");

            tv_x_value.setText(getX);
            tv_y_value.setText(getY);

            LatLng sydney = new LatLng(getLat,getLong);

                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        //LatLng sydney = new LatLng(-34, 151);

    }
}
