package com.example.pas_proyecto;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LocatePoliceStations extends AppCompatActivity {

    MapView mapView;
    public static double latitude_mid = 0.0;
    public static double longitude_mid = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locate_police_stations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(getCacheDir());

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        double[] locations = bundle.getDoubleArray("Location");
        List<String> names = bundle.getStringArrayList("names");   // !!

        List<GeoPoint> points = new ArrayList<GeoPoint>() {};
        for(int i = 0; i < Objects.requireNonNull(locations).length; i+=2){
            points.add(new GeoPoint(locations[i], locations[1+i]));
            latitude_mid = latitude_mid + locations[i];
            longitude_mid = longitude_mid + locations[1+i];
        }

        mapView = findViewById(R.id.mapViewSimpleLoc);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        for (int i = 0; i < points.size(); i++){
            Marker marker = new Marker(mapView);
            marker.setPosition(points.get(i));
            assert names != null;
            marker.setTitle(String.valueOf(names.get(i)));
            mapView.getOverlays().add(marker);
        }
        latitude_mid = latitude_mid / (locations.length / 2);
        longitude_mid = longitude_mid / (locations.length / 2);
        mapView.getController().setZoom(12);
        mapView.getController().setCenter(new GeoPoint(latitude_mid, longitude_mid));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}