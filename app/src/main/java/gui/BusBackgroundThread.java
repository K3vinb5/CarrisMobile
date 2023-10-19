package gui;

import android.util.Log;

import com.example.carrismobile.MainActivity2;

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
                        MapView map = MainActivity2.map;
                        String currentText = MainActivity2.currentText;

                        MainActivity2.busList.clear();
                        List<Bus> listToAdd = Api.getBusFromLine(currentText);
                        MainActivity2.busList.addAll(listToAdd);
                        MainActivity2.updateBusesUI();
                        Log.println(Log.DEBUG, "BACKGROUND THREAD", getName() + "Ended");
                    }
                });

                Log.println(Log.DEBUG,"BACKGROUND CALLER", "Call" + index);
                index++;
                List<Marker> currentBusList = MainActivity2.markerBusList;
                TimeUnit.MILLISECONDS.sleep(10000);
                if (!MainActivity2.markerBusList.equals(currentBusList)){
                    for (Marker marker : currentBusList){
                        MainActivity2.map.getOverlays().remove(marker);
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
