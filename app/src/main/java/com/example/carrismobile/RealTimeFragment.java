package com.example.carrismobile;

import static android.content.Context.INPUT_METHOD_SERVICE;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class RealTimeFragment extends Fragment {

    public static MapView map;
    public static Activity activity;
    Button button;
    EditText editText;
    public BusBackgroundThread backgroundThread = new BusBackgroundThread();
    public boolean backgroundThreadStarted = false;
    public static List<Bus> busList = new ArrayList<>();
    public static List<Path> pathList = new ArrayList<>();
    public static List<Marker> markerList = new ArrayList<>();
    public static List<Marker> markerBusList = new ArrayList<>();
    public int currentSelectedBus = 0;
    public int currentSelectedDirection = 0;
    public static String currentText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.realtime_fragment, container, false);

        activity = getActivity();
        map = v.findViewById(R.id.mapview);
        button = v.findViewById(R.id.button2);
        editText = v.findViewById(R.id.editText2);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(TileSourceFactory.OpenTopo);
        map.setMultiTouchControls(true);
        //map.setVisibility(View.INVISIBLE);
        map.getController().setCenter(new GeoPoint(38.73329737648646, -9.14096412687648));
        map.getController().setZoom(13.0);
        map.invalidate();
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
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

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateMarkers(pathList, map, getActivity());
                                updateBuses(busList, map, getActivity());
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
        return v;
    }

    public void openRouteDeitalActivity() {
        backgroundThread.interrupt();
        Intent intent = new Intent(getActivity(), RouteDetailsFragment.class);
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

}