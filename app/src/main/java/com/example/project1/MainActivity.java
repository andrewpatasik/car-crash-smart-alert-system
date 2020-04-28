package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
    TextView tv_crash_pos;
    GoogleMap mMap;
    LinearLayout main_view;
    TextView empty_view;


    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_view = findViewById(R.id.main_view);
        empty_view = findViewById(R.id.empty_view);
        tv_crash_pos = findViewById(R.id.tv_crash_pos);
        main_view.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        startMqtt();

        notificationManager = NotificationManagerCompat.from(this);
    }

    private void startMqtt(){
        mqttConnect = new mqttHelper(getApplicationContext());
        mqttConnect.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            byte[] msg = message.getPayload();
            JSONObject jsonmsg = new JSONObject(new String(message.getPayload()));
            JSONObject getData = jsonmsg.getJSONObject("data");
            String getX = getData.getString("X-Axis");
            String getY = getData.getString("Y-Axis");
            double getLat = getData.getDouble("Lat");
            double getLong = getData.getDouble("Long");

            updateView(getX,getY,getLat,getLong);
            tv_crash_pos.setText("TERJADI TABRAKAN");
            createNotification();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void updateView(String getX, String getY, double getLat, double getLong){
        final String tempGetX = getX;
        final String tempGetY = getY;
        final double tempGetLat = getLat;
        final double tempGetLong = getLong;

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    empty_view.setVisibility(View.GONE);
                    main_view.setVisibility(View.VISIBLE);

                    TextView tv_x_value;
                    TextView tv_y_value;

                    tv_x_value = findViewById(R.id.tv_x_value);
                    tv_y_value = findViewById(R.id.tv_y_value);

                    tv_x_value.setText(tempGetX);
                    tv_y_value.setText(tempGetY);

                    LatLng carPosition = new LatLng(tempGetLat,tempGetLong);

                    mMap.addMarker(new MarkerOptions().position(carPosition).title("Crash"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(carPosition));
                }
            });
        } catch (Exception ex) {
            Log.e("TAG", ex.getMessage());
        }

    }

    public void createNotification(){
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Notification")
                .setContentText("Crash Occured")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1,notification);
    }


    @Override
    public void onResume() {
        super.onResume();
    mqttConnect.registerResources();
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
