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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import api.Api;
import data_structure.Stop;

public class StopsBackgroundThread extends Thread{

    private List<Stop> currentToAddList = new ArrayList<>();
    private GeoPoint currentFixedPoint = new GeoPoint(0d,0d); //current updated location
    private GeoPoint lastFixedPoint = new GeoPoint(0d, 0d);// last known location (1 iteration behind)
    public Lock lock = new ReentrantLock();
    private boolean firstRunExecuted = true;
    private boolean needUpdating = false;

    @Override
    public void run() {
        int index = 0;
        List<Stop> stopList;
        try {
            stopList = Api.getStopList();
        }catch (Exception e){
            //TODO use Shared Preferences here to reduce internet connection need
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
                            try {
                                TimeUnit.MILLISECONDS.sleep(4000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            currentFixedPoint = fragment.getCurrentLocation();
                            lastFixedPoint = currentFixedPoint;
                        }else{
                            lastFixedPoint = currentFixedPoint;
                            currentFixedPoint = fragment.getCurrentLocation();
                            needUpdating = currentFixedPoint.getLatitude() != lastFixedPoint.getLatitude() || currentFixedPoint.getLongitude() != lastFixedPoint.getLongitude();
                        }
                        MapView map = fragment.getMap();
                        List<Stop> stopToAddList = new ArrayList<>();
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                double[] coordinates = new double[]{currentFixedPoint.getLatitude(),currentFixedPoint.getLongitude()};
                                if (stopList == null){
                                    return;
                                }
                                for (Stop stop : stopList){
                                    StopThread stopThread = new StopThread(coordinates,stop.getCoordinates(), stopToAddList, stop);
                                    stopThread.start();
                                }
                                Log.d("STOP BACKGROUND THREAD", "STOPS CLOSE " + stopToAddList.size());
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Thread thread2 = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                fragment.updateStopsMarkersList(stopToAddList);
                                            }
                                        });
                                        needUpdating = needUpdating || map.getOverlays().size() < 3;
                                        if (needUpdating){
                                            thread2.start();
                                            Log.d("STOP BACKGROUND THREAD", "MAP UPDATED");
                                            firstRunExecuted = false;
                                            needUpdating = false;
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
                TimeUnit.MILLISECONDS.sleep(7000);
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
    public Lock getLock() {
        return lock;
    }
}
