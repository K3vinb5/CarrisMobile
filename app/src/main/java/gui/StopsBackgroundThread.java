package gui;

import android.util.Log;

import com.example.carrismobile.MainActivity;
import com.example.carrismobile.RealTimeFragment;
import com.example.carrismobile.StopsMapFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import api.Api;
import data_structure.Stop;

public class StopsBackgroundThread extends Thread{

    private List<Stop> currentToAddList = new ArrayList<>();
    private GeoPoint currentFixedPoint = new GeoPoint(0d,0d);
    private boolean firstRunExecuted = true;
    private boolean wasUpdated = false;
    private boolean mapIsUpdated = true;

    @Override
    public void run() {
        int index = 0;
        List<Stop> stopList;
        try {
            stopList = Api.getStopList();
        }catch (Exception e){
            //TODO
            return;
        }
        while(true){
            try{
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity mainActivity = (MainActivity) RealTimeFragment.activity;
                        StopsMapFragment fragment = (StopsMapFragment) mainActivity.stopsMapFragment;
                        fragment.getLastLocation();
                        if (firstRunExecuted){
                            currentFixedPoint = fragment.getCurrentLocation();
                        }
                        MapView map = fragment.getMap();
                        List<Stop> stopToAddList = new ArrayList<>();
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (getUpdate()){
                                    currentFixedPoint = fragment.getCurrentLocation();
                                    mapIsUpdated = false;
                                }
                                double[] coordinates = new double[]{currentFixedPoint.getLatitude(),currentFixedPoint.getLongitude()};
                                if (stopList == null){
                                    return;
                                }
                                for (Stop stop : stopList){
                                    StopThread stopThread = new StopThread(coordinates,stop.getCoordinates(), stopToAddList, stop);
                                    stopThread.start();
                                }
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Thread thread2 = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                fragment.updateStopsMarkersList(stopToAddList);
                                            }
                                        });
                                        if (!mapIsUpdated || firstRunExecuted){
                                            thread2.start();
                                            firstRunExecuted = false;
                                            mapIsUpdated = true;
                                            currentFixedPoint = fragment.getCurrentLocation();
                                        }
                                    }
                                });
                            }
                        });
                        thread1.start();

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragment.updateCurrentLocationMarker();
                            }
                        });
                    }
                });
                Log.println(Log.DEBUG,"STOP BACKGROUND THREAD", "CALL" + index);
                thread.start();
                index++;
                TimeUnit.MILLISECONDS.sleep(5000);
            }catch (Exception e){
                Log.d("STOP BACKGROUND THREAD", "INTERRUPTED");
                break;
            }
        }
    }

    private double calcCrow(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        double[] coordinates1 = new double[]{geoPoint1.getLatitude(), geoPoint1.getLongitude()};
        double[] coordinates2 = new double[]{geoPoint2.getLatitude(), geoPoint2.getLongitude()};
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

    public void notifyUpdate(){
        wasUpdated = true;
    }

    private boolean getUpdate(){
        boolean update = wasUpdated;
        wasUpdated = false;
        return update;
    }
}
