package kevin.carrismobile.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.gui.CustomMarkerInfoWindow;
import kevin.carrismobile.gui.LocationBackgroundThread;
import kevin.carrismobile.gui.StopsBackgroundThread;
import kevin.carrismobile.custom.MyCustomDialog;

public class StopsMapFragment extends Fragment {

    public MapView map;
    public CompassOverlay compassOverlay;
    public TextView textView;
    public LocationManager mLocationManager;
    public Button buttonCenter;
    public Button favoritarButton;
    public Button stopDetailsButton;
    public Stop currentStop;
    AlertDialog stopAdded;
    static List<Marker> markerList = Collections.synchronizedList(new ArrayList<Marker>());
    private List<Marker> stopsMarkerList = Collections.synchronizedList(new ArrayList<Marker>());
    GeoPoint currentLocation = new GeoPoint(0d,0d);
    StopsBackgroundThread backgroundThread;
    LocationBackgroundThread locationBackgroundThread;
    FusedLocationProviderClient fusedLocationProviderClient;
    int LOCATION_REFRESH_TIME = 1000; // 1 times per second update
    int LOCATION_REFRESH_DISTANCE = 1; // 1 meters to update
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

        map.getController().setCenter(currentLocation);
        map.getController().setZoom(18f);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(16d);
        map.setMaxZoomLevel(20d);
        compassOverlay = new CompassOverlay(getContext(), map);
        compassOverlay.enableCompass();
        setUpLocationManager();
        backgroundThread = new StopsBackgroundThread(StopsMapFragment.this);
        backgroundThread.start();
        locationBackgroundThread = new LocationBackgroundThread(StopsMapFragment.this);
        locationBackgroundThread.start();
        buttonCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.getController().animateTo(currentLocation, 17d, 1500L);
                Log.d("CURRENT ORIENTATION: " , "ORIENTATION: " + compassOverlay.getOrientation());
                Log.d("CURRENT LOCATION: ", "LOCATION: " + getCurrentLocation().getLatitude()+" , " + getCurrentLocation().getLongitude());
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

    public void setUpLocationManager(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            if (location == null){
                return;
            }
            currentLocation.setCoords(location.getLatitude(), location.getLongitude());
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            setUpLocationManager();
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            synchronized (currentLocation) {
                currentLocation.setCoords(location.getLatitude(), location.getLongitude());
            }
        }
    };
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