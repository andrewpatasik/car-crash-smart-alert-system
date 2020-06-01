package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.strictmode.IntentReceiverLeakedViolation;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.project1.activity.CreateDataActivity;
import com.example.project1.helper.DatabaseHelper;
import com.example.project1.helper.mqttHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    mqttHelper mqttConnect;
    GoogleMap mMap;
    LinearLayout main_view;
    TextView empty_view;
    ImageButton emergency_call;

    private NotificationManagerCompat notificationManager;
    private SQLiteDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        main_view = findViewById(R.id.main_view);
        empty_view = findViewById(R.id.empty_view);
        emergency_call = findViewById(R.id.btn_call);

        main_view.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        startMqtt();

        notificationManager = NotificationManagerCompat.from(this);

        emergency_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialNumberIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:085695771804"));
                startActivity(dialNumberIntent);
            }
        });

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
            final String getX = getData.getString("X-Axis");
            final String getY = getData.getString("Y-Axis");
            final double getLat = getData.getDouble("Lat");
            final double getLong = getData.getDouble("Long");
            Log.d("MQTT","payload: " + jsonmsg);

            String driverDetail = "Tobey Marshall";
            String carType = "Ford Shelby";

            //Insert to DB
            ContentValues cv = new ContentValues();
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_DRIVER, driverDetail);
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_CAR, carType);
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_X, getX);
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_Y, getY);
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_LAT, getLat);
            cv.put(DataTabrakan.TabrakanEntry.COLUMN_LONG, getLong);

            mDatabase.insert(DataTabrakan.TabrakanEntry.TABLE_NAME, null, cv);
            Log.d("MQTT","DB_TRANSACTION: " + cv);

            updateView(getX,getY,getLat,getLong);
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

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        final String time = format.format(calendar.getTime());

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    empty_view.setVisibility(View.GONE);
                    main_view.setVisibility(View.VISIBLE);

                    TextView tv_x_value;
                    TextView tv_y_value;
                    TextView tv_timestamp;

                    tv_x_value = findViewById(R.id.tv_x_value);
                    tv_y_value = findViewById(R.id.tv_y_value);
                    tv_timestamp = findViewById(R.id.tv_time);

                    tv_x_value.setText(tempGetX);
                    tv_y_value.setText(tempGetY);
                    tv_timestamp.setText(time);

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

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Notifikasi Tabrakan")
                .setContentText("Tabrakan telah teridentifikasi")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .build();

        notificationManager.notify(1,notification);
    }

    @Override
    public void onResume() {
        super.onResume();
        mqttConnect.registerResources(this);
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttConnect.unregisterResources(this);
        mqttConnect.close();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
