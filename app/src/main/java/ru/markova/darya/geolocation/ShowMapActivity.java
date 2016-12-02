package ru.markova.darya.geolocation;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;

public class ShowMapActivity extends AppCompatActivity implements OnMapReadyCallback{
    SupportMapFragment mapFragment;
    private double lon;
    private double lat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        lon = getIntent().getDoubleExtra("lastLocationLon", 39.2088823);
        lat = getIntent().getDoubleExtra("lastLocationLat", 51.6754966);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney =
                //new LatLng(-34, 151);
                new LatLng(lat,lon);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions().
                position(sydney).
                title("Marker in Voronezh"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 17.0f));
    }
}
