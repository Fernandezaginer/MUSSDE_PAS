package com.example.pas_proyecto;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.content.DialogInterface;



public class MainScreen extends AppCompatActivity {


    Button bt_connect, bt_open_current_location, bt_nearest_location, bt_open_ranking;
    TextView tv_status, tv_location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        Utilities.start_gps(this);

        bt_connect = findViewById(R.id.bt_connect);
        bt_open_current_location = findViewById(R.id.bt_open_current_location);
        bt_nearest_location = findViewById(R.id.bt_nearest_lost_obj_locations);
        bt_open_ranking = findViewById(R.id.bt_open_ranking);
        tv_status = findViewById(R.id.textView_status);
        tv_location = findViewById(R.id.textView_location);


        // Connect device
        bt_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BT.connect_device(MainScreen.this);


                Handler handler1 = new Handler();
                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        if(BT.disconnected && BT.connected){
                            set_textview_lost();
                            BT.connected = false;
                        }
                        else if((!BT.disconnected) && BT.connected) {
                            set_textview_connected();
                        }
                        handler1.postDelayed(this, 100);
                    }
                };
                handler1.post(runnable1);

            }
        });

        // Open current location
        bt_open_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_object_location(Utilities.get_latitude(MainScreen.this), Utilities.get_longitude(MainScreen.this));
            }
        });

        // Open nearest police station
        bt_nearest_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_near_police_locations(Utilities.get_latitude(MainScreen.this), Utilities.get_longitude(MainScreen.this));
            }
        });

        // Open ranking
        bt_open_ranking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreen.this, Ranking.class);
                startActivity(intent);
            }
        });

        // Location callback update:
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String text_view = "Ubicación: " + String.format("%.5f", Utilities.get_latitude(MainScreen.this)) + ", " + String.format("%.5f", Utilities.get_longitude(MainScreen.this));
                tv_location.setText(text_view);
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(runnable);


    }



    public void set_textview_connected(){
        tv_status.setTextColor(Color.GREEN);
        tv_status.setTypeface(null, Typeface.BOLD);
        tv_status.setText("Conectado");
    }



    public void set_textview_lost(){
        tv_status.setTextColor(Color.RED);
        tv_status.setTypeface(null, Typeface.BOLD);
        tv_status.setText("Objeto perdido!!");

        AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
        builder.setTitle("Alert");
        builder.setMessage("Objeto perdido!!");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 1000};
        VibrationEffect effect = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            effect = VibrationEffect.createWaveform(pattern, -1);
            vibrator.vibrate(effect);
        }

    }



    private void open_object_location(double latitude, double longitude){
        Intent intent = new Intent(this, LocateObject.class);
        Bundle bundle = new Bundle();
        bundle.putDoubleArray("Location", new double[]{latitude, longitude});
        intent.putExtras(bundle);
        startActivity(intent);
    }



    private void open_near_police_locations(double latitude, double longitude){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<Object> result = Utilities.get_lost_object_offices(latitude, longitude, 15000);
            handler.post(() -> {
                Intent intent = new Intent(this, LocatePoliceStations.class);
                Bundle bundle = new Bundle();
                bundle.putDoubleArray("Location", (double[]) result.get(1));
                bundle.putStringArrayList("names", (ArrayList<String>) result.get(0));
                intent.putExtras(bundle);
                startActivity(intent);
            });
        });
    }


}

