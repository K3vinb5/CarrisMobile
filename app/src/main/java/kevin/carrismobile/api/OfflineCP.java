package kevin.carrismobile.api;

import android.app.Activity;
import android.util.Log;

import com.example.carrismobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.data.bus.Direction;
import kevin.carrismobile.data.bus.Path;
import kevin.carrismobile.data.bus.RealTimeSchedule;
import kevin.carrismobile.data.bus.Schedule;
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.data.bus.Trip;
import kevin.carrismobile.data.train.CPStopBasic;

public class OfflineCP {

    public final static String ROUTES_KEY = "cp_key_routes";
    public final static String STOP_TIMES_KEY = "cp_key_stop_times";
    public final static String TRIPS_KEY = "cp_key_trips";
    public final static String STOPS_KEY = "cp_key_stops";

    public static void initCPOffline(Activity activity){
        if (!Offline.mPrefsStops.contains(STOPS_KEY)){
            Offline.copyResource(R.raw.cp_stops, STOPS_KEY,activity, Offline.mPrefsStops);
            Log.w("WARNING SHARED PREFERENCES", "Stops files not found");
        }else {
            Log.w("WARNING SHARED PREFERENCES", "Stops files found " + Offline.mPrefsStops.getString(STOPS_KEY, null).length());
        }
        /*if (!Offline.mPrefsStopTimes.contains(STOP_TIMES_KEY)){
            Offline.copyResource(R.raw.cp_stop_times, STOP_TIMES_KEY, activity, Offline.mPrefsStopTimes);
            Log.w("WARNING SHARED PREFERENCES", "Stop Times files not found");
        }else{
            Log.w("WARNING SHARED PREFERENCES", "Stops Times files found " + Offline.mPrefsStopTimes.getString(STOP_TIMES_KEY, null).length());
        }
        if (!Offline.mPrefsRoutes.contains(ROUTES_KEY)){
            Offline.copyResource(R.raw.cp_routes, ROUTES_KEY, activity, Offline.mPrefsRoutes);
            Log.w("WARNING SHARED PREFERENCES", "Routes files not found");
        }else{
            Log.w("WARNING SHARED PREFERENCES", "Routes files found " + Offline.mPrefsRoutes.getString(ROUTES_KEY, null).length());
        }
        if (!Offline.mPrefsTrips.contains(TRIPS_KEY)){
            Offline.copyResource(R.raw.cp_trips, TRIPS_KEY, activity, Offline.mPrefsTrips);
            Log.w("WARNING SHARED PREFERENCES", "Trips files not found");
        }else{
            Log.w("WARNING SHARED PREFERENCES", "Trips files found " + Offline.mPrefsTrips.getString(TRIPS_KEY, null).length());
        }*/
    }
    public static List<CPStopBasic> getStops(){
        List<CPStopBasic> out = new ArrayList<>();
        boolean first = true;
        try{
            Scanner scanner = new Scanner(Offline.mPrefsStops.getString(STOPS_KEY, null));
            while(scanner.hasNextLine()){
                String[] nextLine = scanner.nextLine().split(",");
                if (nextLine.length == 1){
                    continue;
                }
                if (first){
                    first = false;
                    continue;
                }
                out.add(new CPStopBasic(nextLine[0],nextLine[2],Double.parseDouble(nextLine[4]), Double.parseDouble(nextLine[5])));
            }
        }catch (Exception e){
            Log.e("ERROR", "Message: " + e.getMessage());
        }
        return out;
    }

}
