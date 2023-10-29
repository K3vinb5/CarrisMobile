package gui;

import android.util.Log;
import android.widget.TextView;

import com.example.carrismobile.MainActivity;
import com.example.carrismobile.RealTimeFragment;
import com.example.carrismobile.StopsMapFragment;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import api.Api;
import data_structure.Stop;

public class StopsBackgroundThread extends Thread{

    private List<Stop> currentToAddList = new ArrayList<>();
    private boolean firstRunExecuted = true;

    @Override
    public void run() {
        int index = 0;
        List<Stop> stopList;
        try {
            stopList = Api.getStopList();
        }catch (Exception e){
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
                        double[] currentCoordinates = new double[]{fragment.getCurrentLocation().getLatitude(),fragment.getCurrentLocation().getLongitude()};
                        List<Stop> stopToAddList = new ArrayList<>();
                        Thread thread1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (Stop stop : stopList){
                                    StopThread stopThread = new StopThread(currentCoordinates,stop.getCoordinates(), stopToAddList, stop);
                                    stopThread.start();
                                }
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragment.updateStopsMarkersList(stopToAddList);
                                    }
                                });
                            }
                        });
                        if (Math.random() < 0.25 || firstRunExecuted){
                            thread1.start();
                            firstRunExecuted = false;
                        }
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragment.updateMarkers();
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
}
