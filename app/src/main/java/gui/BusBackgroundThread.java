package gui;

import android.util.Log;

import com.example.carrismobile.RealTimeFragment;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.concurrent.TimeUnit;

import api.Api;
import data_structure.Bus;

public class BusBackgroundThread extends Thread{

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

                        RealTimeFragment.busList.clear();
                        List<Bus> listToAdd = Api.getBusFromLine(currentText);
                        RealTimeFragment.busList.addAll(listToAdd);
                        RealTimeFragment.updateBusesUI();
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
                thread.start();
            }catch (Exception e){
                Log.println(Log.DEBUG,"BACKGROUND CALLER", "INTERRUPT");
                break;
            }
        }
    }

}
