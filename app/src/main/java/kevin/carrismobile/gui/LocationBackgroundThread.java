package kevin.carrismobile.gui;

import android.os.strictmode.ExplicitGcViolation;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.concurrent.TimeUnit;

import kevin.carrismobile.fragments.StopsMapFragment;

public class LocationBackgroundThread extends Thread{

    private StopsMapFragment fragment;
    private GeoPoint currentLocation = new GeoPoint(0,0);
    public LocationBackgroundThread(StopsMapFragment fragment) {
        this.fragment = fragment;
    }
    @Override
    public void run() {
        int index = 0;
        while (true) {
            try {
                if (index > 1) {
                    TimeUnit.MILLISECONDS.sleep(200);
                    if (fragment.isHidden()) {
                        continue;
                    }
                }
                fragment.getLastLocation();
                currentLocation.setLatitude(fragment.getCurrentLocation().getLatitude());
                currentLocation.setLatitude(fragment.getCurrentLocation().getLongitude());
                fragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.updateCurrentLocationMarker();
                    }
                });
                index++;
            }catch (Exception e){
                Log.e("ERROR IN LOCATION THREAD", "MESSAGE: " + e.getMessage());
                break;
            }
        }
    }
}
