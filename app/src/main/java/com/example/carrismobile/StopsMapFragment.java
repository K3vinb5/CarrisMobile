package com.example.carrismobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import data_structure.Path;
import data_structure.Stop;
import gui.CustomMarkerInfoWindow;
import gui.StopsBackgroundThread;

public class StopsMapFragment extends Fragment {

    public MapView map;
    public TextView textView;
    static List<Marker> markerList = new ArrayList<>();
    private List<Marker> stopsMarkerList = new ArrayList<>();
    GeoPoint currentLocation = new GeoPoint(0d, 0d);
    StopsBackgroundThread backgroundThread = new StopsBackgroundThread();
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stops_map, container, false);

        map = v.findViewById(R.id.mapviewStops);
        textView = v.findViewById(R.id.textViewStops);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(TileSourceFactory.OpenTopo);
        map.setMultiTouchControls(true);
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        getLastLocation();
        map.getController().setCenter(currentLocation);
        map.getController().setZoom(18f);
        map.setMinZoomLevel(16d);
        map.setMaxZoomLevel(20d);
        backgroundThread.start();

        return v;
    }

    public void getLastLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null){
                        currentLocation.setLatitude(location.getLatitude());
                        currentLocation.setLongitude(location.getLongitude());
                    }
                }
            });
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    public void updateMarkers(){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }
        markerList.clear();
        Marker marker = new Marker(map);
        Drawable d = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.baseline_person_24, null);
        marker.setIcon(d);
        markerList.add(marker);
        marker.setPosition(currentLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        map.getOverlays().add(marker);
        map.invalidate();
    }

    public void updateStopsMarkersList(List<Stop> listToAdd){
        for (Marker marker : stopsMarkerList){
            map.getOverlays().remove(marker);
        }
        stopsMarkerList.clear();
        for (Stop stop : listToAdd){
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.map_stop_selected, null);
            marker.setIcon(d);
            stopsMarkerList.add(marker);
            marker.setPosition(new GeoPoint(stop.getCoordinates()[0], stop.getCoordinates()[1]));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

            map.getOverlays().add(marker);
            map.invalidate();
        }
    }

    public MapView getMap() {
        return map;
    }

    public GeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public TextView getTextView() {
        return textView;
    }
}