package kevin.carrismobile.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.carrismobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kevin.carrismobile.adaptors.StopImageListAdaptor;
import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.gui.CustomMarkerInfoWindow;
import kevin.carrismobile.gui.LocationBackgroundThread;
import kevin.carrismobile.gui.StopsBackgroundThread;
import kevin.carrismobile.custom.MyCustomDialog;

public class StopsMapFragment extends Fragment {

    public MapView map;
    public CompassOverlay compassOverlay;
    public TextView textView;
    public Button buttonRefresh;
    public Button buttonCenter;
    public Button favoritarButton;
    public Button stopDetailsButton;
    public Stop currentStop;
    AlertDialog stopAdded;
    public boolean isFocused = true;
    static List<Marker> markerList = Collections.synchronizedList(new ArrayList<Marker>());
    private List<Marker> stopsMarkerList = Collections.synchronizedList(new ArrayList<Marker>());
    GeoPoint currentLocation = new GeoPoint(0d,0d);
    StopsBackgroundThread backgroundThread;
    LocationBackgroundThread locationBackgroundThread;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final float[] gravity = new float[3];
    private static final float[] geomagnetic = new float[3];
    private static float azimuth = 0.0f;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stops_map_fragment, container, false);

        map = v.findViewById(R.id.mapviewStops);
        textView = v.findViewById(R.id.textViewStops);
        buttonCenter = v.findViewById(R.id.imageButtonCenter);
        favoritarButton = v.findViewById(R.id.favoritar);
        stopDetailsButton = v.findViewById(R.id.stopDetails);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        stopAdded = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem adicionada à Lista de Favoritos", "A Paragem Selecionada foi adicionada com sucesso á Lista de Favoritos de Paragens");

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(SettingsFragment.getCurrentTileProvider(getContext()));

        getLastLocation();
        map.getController().setCenter(currentLocation);
        map.getController().setZoom(18f);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(16d);
        map.setMaxZoomLevel(20d);
        compassOverlay = new CompassOverlay(getContext(), map);
        compassOverlay.enableCompass();
        backgroundThread = new StopsBackgroundThread(StopsMapFragment.this);
        backgroundThread.start();
        locationBackgroundThread = new LocationBackgroundThread(StopsMapFragment.this);
        locationBackgroundThread.start();
        buttonCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFocused = true;
                getLastLocation();
                Log.d("DEBUG HEADING", "HEADING :" + getFacingDirection());
                map.getController().animateTo(currentLocation, 17d, 1500L);
                Log.d("CURRENT ORIENTATION: " , "ORIENTATION: " + compassOverlay.getOrientation());
            }
        });

        favoritarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop stoptoAdd = currentStop;
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopFavoritesFragment fragment = (StopFavoritesFragment)mainActivity.stopFavoritesFragment;
                        fragment.addStopToFavorites(stoptoAdd);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopAdded.show();
                                //informs user of success;
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        stopDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        assert mainActivity != null;
                        if (currentStop.isOnline()){
                            if (currentStop.getAgency_id().equals("-1")){
                                StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                                mainActivity.openFragment(stopDetailsFragment, 0, true);
                                stopDetailsFragment.loadNewStop(currentStop.getStopID());
                            }else if(currentStop.getAgency_id().equals("0")){
                                CarrisStopDetailsFragment carrisStopDetailsFragment = (CarrisStopDetailsFragment)mainActivity.carrisStopDetailsFragment;
                                mainActivity.openFragment(carrisStopDetailsFragment, 0, true);
                                carrisStopDetailsFragment.loadCarrisStop(currentStop);
                            }
                        }else{
                            StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                            mainActivity.openFragment(stopDetailsFragment, 0, true);
                            stopDetailsFragment.loadNewOfflineStop(currentStop);
                        }
                    }
                });
                thread.start();
            }
        });
        //temp
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //buttonCenter.performClick();
            }
        });

        return v;
    }

    public void getLastLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location!=null && isFocused){
                        //gets updated every 50m you walk so it is not necessary to compute new close stops everytime there is a tiny change
                        currentLocation.setLatitude(location.getLatitude());
                        currentLocation.setLongitude(location.getLongitude());
                    }
                }
            });
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    public double getFacingDirection() {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(getContext().SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (accelerometer != null && magnetometer != null) {
            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, 3);
                    } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3);
                    }

                    float[] rotationMatrix = new float[9];
                    boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);

                    if (success) {
                        float[] orientationValues = new float[3];
                        SensorManager.getOrientation(rotationMatrix, orientationValues);
                        azimuth = (float) Math.toDegrees(orientationValues[0]);

                        // Adjust the azimuth to be in the range [0, 360)
                        azimuth = (azimuth + 360) % 360;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Do nothing for now
                }
            };

            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

            // Sleep for a short duration to allow sensors to stabilize
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Unregister the listener to stop receiving sensor updates
            sensorManager.unregisterListener(sensorEventListener);
        }

        // Calculate the facing direction in degrees where 0 represents north
        double facingDirection = (azimuth + 360) % 360;

        // Adjust the facing direction to be in the range [0.0, 360.0)
        return facingDirection;
    }

    public void updateCurrentLocationMarker(){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }
        markerList.clear();
        Marker marker = new Marker(map);
        Drawable d = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.current_location, null);
        RotateDrawable d1 = new RotateDrawable();
        d1.setDrawable(d);
        d1.setFromDegrees(0f);
        d1.setToDegrees(compassOverlay.getOrientation());
        d1.setLevel(10000);
        marker.setIcon(d1.getCurrent());
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
            Drawable d = StopImageListAdaptor.getImageId(stop.getFacilities(), stop.getTts_name(), stop.getAgency_id(), getActivity());
            marker.setIcon(d);
            stopsMarkerList.add(marker);
            marker.setPosition(new GeoPoint(stop.getCoordinates()[0], stop.getCoordinates()[1]));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String description = "Municipality: " + stop.getMunicipality_name() + "\nLocality: " + stop.getLocality();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, stop.getTts_name(), description);
            marker.setInfoWindow(miw);
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    currentStop = stop;
                    favoritarButton.setVisibility(View.VISIBLE);
                    stopDetailsButton.setVisibility(View.VISIBLE);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TimeUnit.MILLISECONDS.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            favoritarButton.setVisibility(View.INVISIBLE);
                            stopDetailsButton.setVisibility(View.INVISIBLE);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                        marker.getInfoWindow().close();
                                    }
                            });
                        }
                    });
                    thread.start();
                    marker.showInfoWindow();
                    mapView.getController().animateTo(marker.getPosition());
                    return true;
                }
            });
            map.getOverlays().add(marker);
            map.invalidate();
        }
    }

    private double calcCrow(double[] coordinates1, double[] coordinates2)
    {
        float R = 6371; // Radius of earth in km
        double dLat = toRad(coordinates2[0] - coordinates1[0]);
        double dLon = toRad(coordinates2[1] - coordinates1[1]);
        double radlat1 = toRad(coordinates1[0]);
        double radlat2 = toRad(coordinates2[0]);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(radlat1) * Math.cos(radlat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private static double toRad(double d)
    {
        return d * Math.PI / 180;
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

    public boolean isFocused() {
        return isFocused;
    }
}