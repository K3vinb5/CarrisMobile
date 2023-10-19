package com.example.carrismobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;

import api.Api;
import data_structure.Bus;
import data_structure.Carreira;
import data_structure.Path;
import gui.BusBackgroundThread;
import gui.CustomMarkerInfoWindow;

public class MainActivity2 extends AppCompatActivity {

    public static MapView map;
    Button button;
    EditText editText;
    public BusBackgroundThread backgroundThread = new BusBackgroundThread();
    public boolean backgroundThreadStarted = false;
    public static Activity activity;
    public static List<Bus> busList = new ArrayList<>();
    public static List<Path> pathList = new ArrayList<>();
    public static List<Marker> markerList = new ArrayList<>();
    public static List<Marker> markerBusList = new ArrayList<>();
    public int currentSelectedBus = 0;
    public int currentSelectedDirection = 0;
    public static String currentText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        activity = this;
        map = findViewById(R.id.mapview);
        button = findViewById(R.id.button2);
        editText = findViewById(R.id.editText2);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(TileSourceFactory.OpenTopo);
        map.setMultiTouchControls(true);
        //map.setVisibility(View.INVISIBLE);
        map.getController().setCenter(new GeoPoint(38.73329737648646, -9.14096412687648));
        map.getController().setZoom(13.0);
        map.invalidate();
        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentText = editText.getText().toString();
                        busList.clear();
                        pathList.clear();
                        List<Bus> listToAdd = Api.getBusFromLine(currentText);
                        Carreira carreira = Api.getCarreira(currentText);
                        carreira.init();
                        carreira.updatePathsOnSelectedDirection(currentSelectedDirection);
                        List<Path> pathListToAdd = carreira.getDirectionList().get(currentSelectedDirection).getPathList();

                        assert listToAdd != null;
                        assert pathListToAdd != null;
                        busList.addAll(listToAdd);
                        pathList.addAll(pathListToAdd);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateMarkers(pathList, map, activity);
                                updateBuses(busList, map, activity);
                                Log.println(Log.DEBUG, "BUS DEBUG", "UI UPDATED");
                            }
                        });

                    }
                });
                thread.start();
                if(!backgroundThreadStarted){
                    backgroundThread.start();
                    backgroundThreadStarted = true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            openRouteDeitalActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openRouteDeitalActivity() {
        backgroundThread.interrupt();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private static void updateMarkers(List<Path> pathList, MapView map, Activity activity){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }

        for (Path path : pathList){
            double[] coordinates = path.getStop().getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_bus_stop, null);
            marker.setIcon(d);
            markerList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Stop Id: " + path.getStop().getStopID() + "\nLocality: " + path.getStop().getLocality() + "\nMunicipality: " + path.getStop().getMunicipality_name();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, path.getStop().getTts_name(), descrption);
            marker.setInfoWindow(miw);
            map.getOverlays().add(marker);
        }
    }

    public static void updateBuses(List<Bus> busList, MapView map, Activity activity){
        for (Marker marker : markerBusList){
            map.getOverlays().remove(marker);
        }

        for (Bus bus : busList){
            double[] coordinates = bus.getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_bus_icon, null);
            marker.setIcon(d);
            markerBusList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Vehicle Id: " + bus.getVehicleId() + "\nSpeed: " + bus.getSpeed() + "\nPattern Id: " + bus.getPattern_id();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, "Bus " + bus.getId(), descrption);
            marker.setInfoWindow(miw);
            map.getOverlays().add(marker);
        }
    }

    public static void updateBusesUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateBuses(busList, map, activity);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Gson gson = new Gson();
        String busListJson = gson.toJson(busList);
        String pathListJson = gson.toJson(pathList);
        String markerListJson = gson.toJson(markerList);
        String markerBusListJson = gson.toJson(markerBusList);

        outState.putInt("key_currentSelectedBus", currentSelectedBus);
        outState.putInt("key_currentSelectedDirection", currentSelectedDirection);
        outState.putString("key_currentText", currentText);

        outState.putString("key_busListJson", busListJson);
        outState.putString("key_pathListJson", pathListJson);
        outState.putString("key_markerListJson", markerListJson);
        outState.putString("key_markerBusListJson", markerBusListJson);*/
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        /*Gson gson = new Gson();

        String busListJson = savedInstanceState.getString("key_busListJson");
        String pathListJson = savedInstanceState.getString("key_pathListJson");
        String markerListJson = savedInstanceState.getString("key_markerListJson");
        String markerBusListJson = savedInstanceState.getString("key_markerBusListJson");

        busList = gson.fromJson(busListJson, busList.getClass());
        pathList = gson.fromJson(pathListJson, pathList.getClass());
        markerList = gson.fromJson(markerListJson, markerList.getClass());
        markerBusList = gson.fromJson(markerBusListJson, markerBusList.getClass());

        currentSelectedBus = savedInstanceState.getInt("key_currentSelectedBus");
        currentSelectedDirection = savedInstanceState.getInt("key_currentSelectedDirection");
        currentText = savedInstanceState.getString("key_currentText");*/
    }
}