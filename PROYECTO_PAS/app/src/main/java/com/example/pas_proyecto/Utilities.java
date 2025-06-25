package com.example.pas_proyecto;

import static androidx.core.app.ActivityCompat.requestPermissions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class Utilities {



    // ----------------- PERMISSION -------------------------


    public static final int REQUEST_CODE = 100;

    private static String[] getPermissions() {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        perms.add(Manifest.permission.BLUETOOTH);
        perms.add(Manifest.permission.BLUETOOTH_ADMIN);
        perms.add(Manifest.permission.BLUETOOTH_ADMIN);
        perms.add(Manifest.permission.INTERNET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_CONNECT);
            perms.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        return perms.toArray(new String[0]);
    }

    public static boolean checkAndRequestPermissions(Activity activity) {
        String[] permissions = getPermissions();
        List<String> notGranted = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(perm);
            }
        }
        if (!notGranted.isEmpty()) {
            requestPermissions(activity, notGranted.toArray(new String[0]), REQUEST_CODE);
            return false;
        }
        return true;
    }

    public static boolean handlePermissionResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode != REQUEST_CODE) return false;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permisos no requeridos, cerrando la app", Toast.LENGTH_LONG).show();
                activity.finishAffinity();
                return false;
            }
        }
        return true;
    }





    // --------------------- GPS -----------------------------

    static boolean started_gps = false;
    static double gps_latitude = 0.0;
    static double gps_longitude = 0.0;
    static final int DEFAULT_UPDATE_INTERVAL = 30;
    static final int FAST_UPDATE_INTERVAL = 5;
    static final int PERMISSIONS_FINE_LOCATION = 99;
    static FusedLocationProviderClient fusedLocationProviderClient;


    public static double get_latitude(Activity activity) {
        if (!started_gps) {
            start_gps(activity);
            started_gps = true;
        }
        else {
            updateGPS(activity);
        }
        return gps_latitude;
    }

    public static double get_longitude(Activity activity) {
        if (!started_gps) {
            start_gps(activity);
            started_gps = true;
        }
        else {
            updateGPS(activity);
        }
        return gps_longitude;
    }


    static void start_gps(Activity activity) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            return;
        }

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    gps_latitude = location.getLatitude();
                    gps_longitude = location.getLongitude();
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    static void updateGPS(Activity activity) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                    gps_latitude = location.getLatitude();
                    gps_longitude = location.getLongitude();
                } else {
                    Toast.makeText(activity, "Unable to get location. Make sure location is enabled.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }







    // ----------------- LOST OBJECTS API ---------------------

    public static List<Object> get_lost_object_offices(double lat, double lon, int radius) {
        ArrayList<String> names = new ArrayList<>();
        List<Double> coordsList = new ArrayList<>();

        String endpoint = "https://overpass-api.de/api/interpreter";
        String query =
                "[out:json][timeout:25];\n" +
                        "(\n" +
                        "  node[\"amenity\"=\"police\"](around:" + radius + "," + lat + "," + lon + ");\n" +
                        "  way[\"amenity\"=\"police\"](around:" + radius + "," + lat + "," + lon + ");\n" +
                        "  relation[\"amenity\"=\"police\"](around:" + radius + "," + lat + "," + lon + ");\n" +
                        ");\n" +
                        "out center;";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONObject json = new JSONObject(response.toString());
            JSONArray elements = json.getJSONArray("elements");

            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);

                double resLat = 0, resLon = 0;
                String name = "Unnamed Police Station";

                JSONObject tags = element.optJSONObject("tags");
                if (tags != null && tags.has("name")) {
                    name = tags.getString("name");
                }

                String type = element.getString("type");
                if ("node".equals(type)) {
                    resLat = element.optDouble("lat", 0);
                    resLon = element.optDouble("lon", 0);
                } else {
                    JSONObject center = element.optJSONObject("center");
                    if (center != null) {
                        resLat = center.optDouble("lat", 0);
                        resLon = center.optDouble("lon", 0);
                    }
                }

                if (resLat != 0 && resLon != 0) {
                    names.add(name);
                    coordsList.add(resLat);
                    coordsList.add(resLon);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching data: " +  e.getMessage());
        }

        double[] coordinates = new double[coordsList.size()];
        for (int i = 0; i < coordsList.size(); i++) {
            coordinates[i] = coordsList.get(i);
        }

        List<Object> result = new ArrayList<>();
        result.add(names);
        result.add(coordinates);
        return result;
    }



}

