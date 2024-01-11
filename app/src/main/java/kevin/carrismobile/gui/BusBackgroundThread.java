package kevin.carrismobile.gui;

import android.util.Log;

import androidx.fragment.app.Fragment;

import kevin.carrismobile.fragments.MainActivity;
import kevin.carrismobile.fragments.RealTimeFragment;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.data.bus.Bus;
import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.Direction;

public class BusBackgroundThread extends Thread{

    private Lock lock = new ReentrantLock();
    private boolean connected = false;
    private final Fragment fragment;

    public BusBackgroundThread(Fragment fragment) {
        this.fragment = fragment;
    }


    public Lock getLock() {
        return lock;
    }

    @Override
    public void run() {
        int index= 0;
        while (true){
            if (index > 1){
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (fragment.isHidden()){
                continue;
            }
            try{
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MapView map = RealTimeFragment.map;
                        String currentText = RealTimeFragment.currentText;

                        List<Bus> listToAdd;
                        try{
                            listToAdd = CarrisMetropolitanaApi.getBusFromLine(currentText);
                            Carreira carreira = RealTimeFragment.currentCarreira;
                            if (listToAdd.size() > 0){
                                List<Direction> carreiraDirectionList = carreira.getDirectionList();
                                for (Bus b : listToAdd){
                                    for(Direction d : carreiraDirectionList){
                                        if (d.isCorrectHeadsign(b.getPattern_id())){
                                            b.setPattern_name(d.getHeadsign());
                                        }
                                    }
                                }
                                synchronized (RealTimeFragment.busList){
                                    RealTimeFragment.busList.clear();
                                    RealTimeFragment.busList.addAll(listToAdd);
                                }
                                RealTimeFragment.updateBusesUI();
                                RealTimeFragment.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (RealTimeFragment.checkBox.isChecked()){
                                            RealTimeFragment.updateTextView();
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
                            Log.d("BACKGROUND THREAD", "Exception Caught: " + e.getMessage());
                            return;
                        }
                        Log.println(Log.DEBUG, "BACKGROUND THREAD", getName() + "Ended");
                    }
                });

                Log.println(Log.DEBUG,"BACKGROUND CALLER", "Call" + index);
                index++;
                synchronized (RealTimeFragment.markerBusList){
                    List<Marker> currentBusList = RealTimeFragment.markerBusList;
                    if (!RealTimeFragment.markerBusList.equals(currentBusList)){
                        for (Marker marker : currentBusList){
                            RealTimeFragment.map.getOverlays().remove(marker);
                        }
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

    public synchronized void lockLock(){
        lock.lock();
    }
    public synchronized void unlockLock(){
        lock.unlock();
    }

}
