package kevin.carrismobile.gui;

import android.util.Log;

import androidx.fragment.app.Fragment;

import kevin.carrismobile.api.RealCarrisApi;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.fragments.MainActivity;
import kevin.carrismobile.fragments.RealTimeFragment;
import kevin.carrismobile.fragments.StopsMapFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.data.bus.Stop;

public class StopsBackgroundThread extends Thread{

    private List<Stop> currentToAddList = new ArrayList<>();
    private GeoPoint currentFixedPoint = new GeoPoint(0d,0d); //current updated location
    private GeoPoint lastFixedPoint = new GeoPoint(0d, 0d);// last known location (1 iteration behind)
    public static Lock lock = new ReentrantLock();
    public List<Stop> stopList = new ArrayList<>();
    private boolean firstRunExecuted = true;
    private boolean needUpdating = false;
    private final StopsMapFragment fragment;

    public StopsBackgroundThread(StopsMapFragment fragment) {
        this.fragment = fragment;
    }
    public void initStopList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    stopList.addAll(CarrisMetropolitanaApi.getStopList());
                    stopList.addAll(RealCarrisApi.getStopList());
                }catch (Exception e){
                    Log.e("ERROR IN STOP MAP", "Message :" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void run() {
        initStopList();
        int index = 0;
        while(true){
            if (stopList == null){
                initStopList();
            }
            try{
                if (index > 1){
                    TimeUnit.MILLISECONDS.sleep(7000);
                    if (fragment.isHidden()){
                        continue;
                    }
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MapView map = fragment.getMap();
                        List<Stop> stopToAddList = new ArrayList<>();

                        fragment.getLastLocation();
                        currentFixedPoint.setLatitude(fragment.getCurrentLocation().getLatitude());
                        currentFixedPoint.setLongitude(fragment.getCurrentLocation().getLongitude());

                        needUpdating = calcCrow(lastFixedPoint, currentFixedPoint) > 0.5d;

                        if (stopList == null){
                            return;
                        }
                        if (needUpdating){
                            double[] coordinates = new double[]{currentFixedPoint.getLatitude(),currentFixedPoint.getLongitude()};
                            for (Stop stop : stopList){
                                if (calcCrowFromCoordinates(coordinates, stop.getCoordinates()) < 1d) {
                                    stopToAddList.add(stop);
                                }
                            }
                            needUpdating = false;
                            lastFixedPoint.setLatitude(currentFixedPoint.getLatitude());
                            lastFixedPoint.setLongitude(currentFixedPoint.getLongitude());
                            fragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
