package kevin.carrismobile.gui;

import android.util.Log;

import kevin.carrismobile.api.Offline;
import kevin.carrismobile.api.RealCarrisApi;
import kevin.carrismobile.fragments.MainActivity;
import kevin.carrismobile.fragments.RealTimeFragment;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.data.bus.Bus;
import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.Direction;

public class BusBackgroundThread extends Thread{

    private boolean connected = false;
    private final RealTimeFragment fragment;
    private String line;
    private Carreira carreira;

    public BusBackgroundThread(RealTimeFragment fragment, Carreira carreira) {
        this.fragment = fragment;
        this.carreira = carreira;
    }

    public Carreira getCarreira() {
        return carreira;
    }

    public void setCarreira(Carreira carreira) {
        this.carreira = carreira;
    }

    @Override
    public void run() {
        while (true) {

            try {
                TimeUnit.MILLISECONDS.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (fragment.isHidden()) {
                continue;
            }
            fragment.loadNewRoute(null, true);
        }
    }

}
