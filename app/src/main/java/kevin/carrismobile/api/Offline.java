package kevin.carrismobile.api;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.carrismobile.R;

import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.CarreiraBasic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Offline {
    public static Map<String,String> agencyServiceMap;
    private static final String AGENCY_SERVICE_KEY = "key_agencyServiceMap";
    public static SharedPreferences mPrefsMaps;
    public static SharedPreferences mPrefsRoutes;
    public static SharedPreferences mPrefsStopTimes;
    public static SharedPreferences mPrefsTrips;
    public static SharedPreferences mPrefsStops;

    public static void init(Activity activity){
        mPrefsStops = activity.getSharedPreferences("OfflineStops", Context.MODE_PRIVATE);
        //mPrefsRoutes = activity.getSharedPreferences("OfflineRoutes", Context.MODE_PRIVATE);
        //mPrefsStopTimes = activity.getSharedPreferences("OfflineStopTimes", Context.MODE_PRIVATE);
        //mPrefsTrips = activity.getSharedPreferences("OfflineTrips", Context.MODE_PRIVATE);
        mPrefsMaps = activity.getSharedPreferences("OfflineAgencyMap", Context.MODE_PRIVATE);
        OfflineCP.initCPOffline(activity);
        if (!mPrefsMaps.contains(AGENCY_SERVICE_KEY)){
            copyResource(R.raw.agency_service, AGENCY_SERVICE_KEY,activity, mPrefsMaps);
            Log.w("WARNING SHARED PREFERENCES", "Agency files not found");
        }
        agencyServiceMap = new HashMap<>();
        Scanner scanner = new Scanner(mPrefsMaps.getString(AGENCY_SERVICE_KEY, null));
        while(scanner.hasNextLine()){
            String[] nextLine = scanner.nextLine().split(",");
            if (nextLine.length == 1){
                continue;
            }
            agencyServiceMap.put(nextLine[0], nextLine[1]);
        }
    }

    public static Carreira getCarreira(String id){
        String agency = agencyServiceMap.get(id);
        switch (agency) {
            default:
        }
        return null;
    }


    public static List<CarreiraBasic> getCarreiraList(){
        List<CarreiraBasic> returnList = new ArrayList<>();
        //returnList.addAll(OfflineCarris.getCarreiraList());
        //returnList.addAll(OfflineCP.getCarreiraList());
        //returnList.addAll(OfflineFerTagus.getCarreiraList());
        //returnList.addAll(OfflineMobiCascais.getCarreiraList());
        return returnList;
    }

    public static void updateDirectionIndex(Carreira carreira, int directionIndex){
        switch (carreira.getAgency_id()) {
            default:
        }
    }
    public static void copyResource(int resource, String key, Activity activity, SharedPreferences mPrefs){
        InputStream in = activity.getResources().openRawResource(resource);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            mPrefs.edit().putString(key, textBuilder.toString()).apply();
            in.close();
            Log.d("SUCCESS SAVING " + key, Integer.toString(textBuilder.toString().length()));
        }catch (Exception e){
            Log.e("ERROR SAVING " + key, e.getMessage());
        }
    }

}
