package kevin.carrismobile.api;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.data.bus.Bus;
import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.Stop;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RealCarrisApi {

    static String apiKey = "79d6dca94a5c5719c636b99e36e62dfe03d368432dd04ee0ad5dfa2d7cb07f57";
    static String token = "";
    static String refreshToken = "";
    static double expires = 0.0;
    public static String[] types = new String[]{"apikey", "refresh"};
    public final static String VEHICLE_STATUSES = "https://gateway.carris.pt/gateway/xtranpassengerapi/api/v2.10/vehicleStatuses";
    public final static String STOPS = "https://gateway.carris.pt/gateway/xtranpassengerapi/api/v2.10/busstops";
    public final static String SIGN = "https://gateway.carris.pt/gateway/authenticationapi/authorization/sign";
    private static SharedPreferences mPrefs;

    public static void init(Activity activity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPrefs = activity.getSharedPreferences("RealCarrisApi", MODE_PRIVATE);
                token = (String)loadObject("token", String.class);
                refreshToken = (String)loadObject("refreshToken", String.class);
                if (token == null || refreshToken == null){
                    try{
                        updateToken();
                        storeObject(new Gson().toJson(token), "token");
                        storeObject(new Gson().toJson(refreshToken), "refreshToken");
                    }catch (Exception e){
                        Log.e("REAL CARRIS API ERROR", "Message :" + e.getMessage());
                    }
                }
            }
        }).start();

    }
    public static List<Bus> getBusFromLine(String carreiraID){
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(VEHICLE_STATUSES + "/routeNumber/" + carreiraID).addHeader("Authorization", "Bearer " + token).addHeader("Accept", "application/json").build();
        Response response;
        String json;
        List<Bus> busList = new ArrayList<>();
        try{
            response = client.newCall(request).execute();
            int responseCode = response.code();
            if (responseCode != 200){
                response.close();
                refreshToken();
                Request newRequest = new Request.Builder().url(VEHICLE_STATUSES + "/routeNumber/" + carreiraID).addHeader("Authorization", "Bearer " + token).addHeader("Accept", "application/json").build();
                response = client.newCall(newRequest).execute();
            }
            json = response.body().string();
            JsonArray jsonBusArray = new JsonParser().parse(json).getAsJsonArray();
            for (JsonElement jsonElement : jsonBusArray) {
                Bus bus = gson.fromJson(jsonElement, Bus.class);
                busList.add(bus);
            }
            response.close();
        }catch (Exception e){
            Log.e("REAL CARRIS API ERROR", "Message :" + e.getMessage());
        }
        return busList;
    }

    public static List<Stop> getStopList(){
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STOPS).addHeader("Authorization", "Bearer " + token).addHeader("Accept", "application/json").build();
        Response response;
        String json;
        List<Stop> stopList = new ArrayList<>();
        try {
            response = client.newCall(request).execute();
            int responseCode = response.code();
            if (responseCode != 200) {
                response.close();
                refreshToken();
                Request newRequest = new Request.Builder().url(STOPS).addHeader("Authorization", "Bearer " + token).addHeader("Accept", "application/json").build();
                response = client.newCall(newRequest).execute();
            }
            json = response.body().string();
            JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
            for (JsonElement jsonElement : jsonArray) {
                Stop stopToAdd = new Stop(jsonElement.getAsJsonObject().get("publicId").getAsString(), jsonElement.getAsJsonObject().get("name").getAsString(), jsonElement.getAsJsonObject().get("lat").getAsDouble(), jsonElement.getAsJsonObject().get("lng").getAsDouble());
                stopToAdd.setAgency_id("0");
                stopToAdd.setOnline(true);
                stopList.add(stopToAdd);
            }
            response.close();
        }catch (Exception e){
            Log.e("REAL CARRIS API ERROR", "Message :" + e.getMessage());
        }
        //Log.d("REAL CARRIS API DEBUG", "STOP LIST SIZE: " + stopList.size());
        return stopList;
    }

    private static void refreshToken(){
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        JsonObject query = new JsonObject();
        query.addProperty("token", refreshToken);
        query.addProperty("type", types[1]);
        RequestBody formBody = RequestBody.create(query.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SIGN).post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200){
                updateToken();
                return;
            }
            String json = response.body().string();
            JsonObject jsonResponse = gson.fromJson(json, JsonObject.class);
            token = jsonResponse.get("authorizationToken").getAsString();
            refreshToken = jsonResponse.get("refreshToken").getAsString();
            expires = jsonResponse.get("expires").getAsDouble();
            response.close();
        } catch (Exception e) {
            Log.e("ERROR REAL CARRIS API REFRESHING TOKEN", "Message :" + e.getMessage());
        }
    }

    private static void updateToken(){
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        JsonObject query = new JsonObject();
        query.addProperty("token", apiKey);
        query.addProperty("type", types[0]);
        RequestBody formBody = RequestBody.create(query.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SIGN).post(formBody).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            JsonObject jsonResponse = gson.fromJson(json, JsonObject.class);
            token = jsonResponse.get("authorizationToken").getAsString();
            refreshToken = jsonResponse.get("refreshToken").getAsString();
            expires = jsonResponse.get("expires").getAsDouble();
            response.close();
        } catch (Exception e) {
            Log.e("ERROR REAL CARRIS API UPDATING TOKEN", "Message :" + e.getMessage());
        }
    }

    public static String getCurrentFormattedTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return currentUTC.format(formatter);
        }
        return null;
    }

    private static void storeObject(String json, String key){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.println(Log.DEBUG, "STORE THREAD", "SAVING");
                    mPrefs.edit().putString(key, json).apply();
                    Log.println(Log.DEBUG, "STORE THREAD", "FINISH");
                }catch (Exception e){
                    Log.println(Log.ERROR, "STORE THREAD", "INTERRUPTED\n\n" + e.getMessage());
                }
            }
        });
        thread.start();
    }
    private static Object loadObject(String key, Class klass){
        return new Gson().fromJson(mPrefs.getString(key, null), klass);
    }

}
