package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project1.helper.mqttHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    mqttHelper mqttConnect;
    TextView tv_x_value;
    TextView tv_y_value;
    TextView tv_crash_pos;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_x_value = findViewById(R.id.tv_x_value);
        tv_y_value = findViewById(R.id.tv_y_value);
        tv_crash_pos = findViewById(R.id.tv_crash_pos);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        startMqtt();
    }

    private void startMqtt(){
        mqttConnect = new mqttHelper(getApplicationContext());
        mqttConnect.setCallback(new MqttCallback() {
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
            tv_crash_pos.setText("TERJADI TABRAKAN");

            LatLng carPosition = new LatLng(getLat,getLong);

                mMap.addMarker(new MarkerOptions().position(carPosition).title("Crash"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(carPosition));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        //LatLng sydney = new LatLng(-34, 151);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttConnect.unregisterResources();
        mqttConnect.close();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
