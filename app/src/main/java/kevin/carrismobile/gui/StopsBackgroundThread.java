package kevin.carrismobile.gui;

import android.util.Log;

import kevin.carrismobile.api.RealCarrisApi;
import kevin.carrismobile.fragments.StopsMapFragment;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.data.bus.Stop;

public class StopsBackgroundThread extends Thread{

    private List<Stop> currentToAddList = new ArrayList<>();
    private GeoPoint currentPoint = new GeoPoint(0d,0d); //current updated location
    private GeoPoint lastFixedPoint = new GeoPoint(0d, 0d);// last known location
    public static Lock lock = new ReentrantLock();
    public List<Stop> stopList = new ArrayList<>();
    private boolean firstRunExecuted = true;
    private boolean needUpdating = false;
    private final StopsMapFragment fragment;

    public StopsBackgroundThread(StopsMapFragment fragment) {
        this.fragment = fragment;
    }
    public void initStopList(){
        try{
            stopList.addAll(CarrisMetropolitanaApi.getStopList());
            stopList.addAll(RealCarrisApi.getStopList());
        }catch (Exception e){
            Log.e("ERROR IN STOP MAP", "Message :" + e.getMessage());
        }
    }

    @Override
    public void run() {
        initStopList();
        int index = 0;
        while(true){
            if (stopList.isEmpty()){
                initStopList();
            }
            try{
                if (index > 0){
                    TimeUnit.MILLISECONDS.sleep(7000);
                    if (fragment.isHidden()){
                        continue;
                    }
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Stop> stopToAddList = new ArrayList<>();
                        GeoPoint point = fragment.getCurrentLocation();
                        currentPoint.setCoords(point.getLatitude(), point.getLongitude());
                        needUpdating = calcCrow(lastFixedPoint, currentPoint) > 0.5d;
                        if (stopList == null){
                            return;
                        }
                        if (needUpdating){
                            Log.d("STOP BACKGROUND THREAD", "UPDATING...");
                            double[] coordinates = new double[]{currentPoint.getLatitude(), currentPoint.getLongitude()};
                            Log.d("STOP BACKGROUND THREAD", "CURRENT COORDINATES: " + currentPoint.getLatitude() + " , " + currentPoint.getLongitude());
                            for (Stop stop : stopList){
                                if (calcCrowFromCoordinates(coordinates, stop.getCoordinates()) < 1d) {
                                    stopToAddList.add(stop);
                                }
                            }
                            Log.d("STOP BACKGROUND THREAD", "UPDATING " + stopToAddList.size() + " STOPS\nFrom " + stopList.size());
                            needUpdating = false;
                            lastFixedPoint.setLatitude(currentPoint.getLatitude());
                            lastFixedPoint.setLongitude(currentPoint.getLongitude());
                            fragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("STOP BACKGROUND THREAD", "UPDATED GUI");
                                    fragment.updateStopsMarkersList(stopToAddList);
                                }
                            });
                        }
                    }
                }).start();
                Log.println(Log.DEBUG,"STOP BACKGROUND THREAD", "CALL" + index);
                index++;
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

    private double calcCrowFromCoordinates(double[] coordinates1, double[] coordinates2) {
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
    public static Lock getLock() {
        return lock;
    }
}
