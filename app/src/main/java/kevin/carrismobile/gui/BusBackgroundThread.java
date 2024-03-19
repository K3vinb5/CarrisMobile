package kevin.carrismobile.gui;

import android.util.Log;

import kevin.carrismobile.fragments.RealTimeFragment;

import java.util.concurrent.TimeUnit;

import kevin.carrismobile.data.bus.Carreira;

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
                Log.e("ERROR BACKGROUND", "Message :" + e.getMessage());
                break;
            }

            if (fragment.isHidden()) {
                continue;
            }
            fragment.loadNewRoute(null, true);
        }
    }

}
