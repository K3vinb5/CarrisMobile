package gui;

import android.util.Log;

import com.example.carrismobile.MainActivity;
import com.example.carrismobile.RealTimeFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import api.Api;
import data_structure.Bus;

public class BusBackgroundThread extends Thread{

    private Lock lock = new ReentrantLock();
    private boolean connected = false;


    public Lock getLock() {
        return lock;
    }

    @Override
    public void run() {
        int index= 0;

        while (true){
            try{
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MapView map = RealTimeFragment.map;
                        String currentText = RealTimeFragment.currentText;

                        List<Bus> listToAdd;
                        try{
                            listToAdd = Api.getBusFromLine(currentText);
                            if (listToAdd.size() > 0){
                                RealTimeFragment.busList.clear();
                                RealTimeFragment.busList.addAll(listToAdd);
                                RealTimeFragment.updateBusesUI();
                                RealTimeFragment.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (RealTimeFragment.checkBox.isChecked()){
                                            map.getController().animateTo(RealTimeFragment.markerBusList.get(RealTimeFragment.currentSelectedBus).getPosition(), 16.5, 2500L);
                                            map.invalidate();
                                        }
                                    }
                                });
                            }
                            connected = true;
                        }catch (Exception e){
                            connected = false;
                            MainActivity mainActivity = (MainActivity)RealTimeFragment.activity;
                            RealTimeFragment realTimeFragment = (RealTimeFragment) mainActivity.realTimeFragment;
                            realTimeFragment.showBackgroundThreadDialog();
                            return;
                        }
                        Log.println(Log.DEBUG, "BACKGROUND THREAD", getName() + "Ended");
                    }
                });

                Log.println(Log.DEBUG,"BACKGROUND CALLER", "Call" + index);
                index++;
                List<Marker> currentBusList = RealTimeFragment.markerBusList;
                TimeUnit.MILLISECONDS.sleep(5000);
                if (!RealTimeFragment.markerBusList.equals(currentBusList)){
                    for (Marker marker : currentBusList){
                        RealTimeFragment.map.getOverlays().remove(marker);
                    }
                }
                Log.println(Log.DEBUG, "BACKGROUND THREAD", thread.getName() + "STARTED");
                lock.lock();
                thread.start();
                lock.unlock();
            }catch (Exception e){
                Log.println(Log.DEBUG,"BACKGROUND CALLER", "INTERRUPT");
                break;
            }
        }
    }

}
