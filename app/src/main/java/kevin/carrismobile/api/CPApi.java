package kevin.carrismobile.api;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.train.CPStop;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CPApi {

    public final static  String NEXT_TRAINS = "https://www.cp.pt/sites/spring/station/trains";

    public static String getJson(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String output = "";
        try {
            Response response = client.newCall(request).execute();
            output = response.body().string();
        } catch (Exception ignore) {}
        return output;
    }

    public static List<CPStop> getStopArrivals(String stationId){
        List<CPStop> out = new ArrayList<>();
        Gson gson = new Gson();
        if (stationId.length() < 7){
            stationId = stationId.substring(0,2) + "0" + stationId.substring(2);
        }
        String jsonString = getJson(NEXT_TRAINS + "?stationId=" + stationId.substring(0,stationId.length() - 5)+"-"+stationId.substring(stationId.length() - 5));
        JsonArray responseArray= gson.fromJson(jsonString, JsonArray.class);
        if (responseArray == null){
            return new ArrayList<>();
        }
        for (JsonElement element : responseArray){
            out.add(gson.fromJson(element, CPStop.class));
        }
        out.removeIf(trip -> isFirstTimeSmaller(trip.getArrivalScheduledDateTime().substring(0,5), getCurrentFormattedTime()));
        for (CPStop cpStop : out) {
            if (cpStop.getArrivalScheduledDateTime().substring(0,2).equals("00")){
                cpStop.setArrivalScheduledDateTime("24"+cpStop.getArrivalScheduledDateTime().substring(2));
            }
        }
        out.sort(Comparator.comparing(CPStop::getArrivalScheduledDateTime));
        return out;
    }

    public static void updateDirectionAndStop(Carreira carreira){

    }

    public static String getCurrentFormattedTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return currentUTC.format(formatter);
        }
        return null;
    }
    private static boolean isFirstTimeSmaller(String time1, String time2) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalTime lt1 = LocalTime.parse(time1, formatter);
            LocalTime lt2 = LocalTime.parse(time2, formatter);

            // Subtract 30 minutes from the first time
            LocalTime lt1Minus30 = lt1.plus(30, ChronoUnit.MINUTES);

            // Compare the adjusted first time with the second time
            return lt1Minus30.isBefore(lt2);
        }
        return false;
    }

}
