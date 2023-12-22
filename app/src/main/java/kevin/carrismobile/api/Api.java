package kevin.carrismobile.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.data.Bus;
import kevin.carrismobile.data.Carreira;
import kevin.carrismobile.data.CarreiraBasic;
import kevin.carrismobile.data.Direction;
import kevin.carrismobile.data.RealTimeSchedule;
import kevin.carrismobile.data.Stop;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Api {
    private String line;
    public static final String CARREIRAURL = "https://api.carrismetropolitana.pt/lines/";
    public static final String CARREIRALISTURL = "https://api.carrismetropolitana.pt/lines";
    public static final String DIRECTIONURL = "https://api.carrismetropolitana.pt/patterns/";
    public static final String REALTIMESTOPURL = "https://api.carrismetropolitana.pt/stops/";
    public static final String REALTIMELISTSTOPURL = "https://api.carrismetropolitana.pt/stops";

    public static final String BUSREALTIMESTOPURL = "https://api.carrismetropolitana.pt/vehicles";

    public static String getJson(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String output = "";
        try {
            output = client.newCall(request).execute().body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static Direction getDirection(String pattern){
        try {
            Gson gson = new Gson();
            return gson.fromJson(getJson(DIRECTIONURL + pattern), Direction.class);

        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    public static Carreira getCarreira(String id){
        try {
            Gson gson = new Gson();
            Carreira carreira = gson.fromJson(getJson(CARREIRAURL + id), Carreira.class);
            return carreira;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public static List<RealTimeSchedule> getRealTimeStops(String stopId) throws SocketTimeoutException {
        try {
            Gson gson = new Gson();
            JsonArray jsonArray = new JsonParser().parse(getJson(REALTIMESTOPURL + stopId + "/realtime")).getAsJsonArray();
            List<RealTimeSchedule> rts = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray){
                rts.add(gson.fromJson(jsonElement, RealTimeSchedule.class));
            }
            return rts;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public static List<Bus> getBusFromLine(String carreiraID){
        try {
            Gson gson = new Gson();
            List<Bus> busList = new ArrayList<>();
            //List<String> vehicleIds = new ArrayList<>();
            //Carreira carreira = getCarreira(carreiraID);
            //List<Path> pathList = carreira.getDirectionList().get(0).getPathList();

            /*for (Direction direction: carreira.getDirectionList()) {
                for (Path stop : direction.getPathList()) {
                    JsonArray jsonArray = new JsonParser().parse(getJson(REALTIMESTOPURL + stop.getStop().getStopID() + "/realtime")).getAsJsonArray();
                    for (JsonElement jsonElement : jsonArray) {
                        RealTimeSchedule rs = gson.fromJson(jsonElement, RealTimeSchedule.class);
                        if (!vehicleIds.contains(rs.getVehicle_id()) && rs.getTrip_id().split("_")[0].equals(carreiraID)) {
                            vehicleIds.add(rs.getVehicle_id());
                        }
                    }
                }
            }*/

            JsonArray jsonBusArray = new JsonParser().parse(getJson(BUSREALTIMESTOPURL)).getAsJsonArray();

            for (JsonElement jsonElement : jsonBusArray){
                Bus bus = gson.fromJson(jsonElement, Bus.class);
                if (bus.getTrip_id() != null){
                    if (bus.getTrip_id().split("_")[0].equals(carreiraID)){
                        busList.add(bus);
                    }
                }
            }

            return busList;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }

    }

    public static CarreiraBasic getCarreiraBasic(String id){
        try {
            Gson gson = new Gson();
            CarreiraBasic carreira = gson.fromJson(getJson(CARREIRAURL + id), CarreiraBasic.class);
            return carreira;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public static List<CarreiraBasic> getCarreiraBasicList(){
        try {
            Gson gson = new Gson();
            JsonArray jsonCarreiraArray = new JsonParser().parse(getJson(CARREIRALISTURL)).getAsJsonArray();
            List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
            for (JsonElement jsonElement : jsonCarreiraArray){
                CarreiraBasic carreira = gson.fromJson(jsonElement, CarreiraBasic.class);
                carreiraBasicList.add(carreira);
            }
            return carreiraBasicList;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public static List<Stop> getStopList(){
        try {
            Gson gson = new Gson();
            JsonArray jsonCarreiraArray = new JsonParser().parse(getJson(REALTIMELISTSTOPURL)).getAsJsonArray();
            List<Stop> stopList = new ArrayList<>();
            for (JsonElement jsonElement : jsonCarreiraArray){
                Stop stop = gson.fromJson(jsonElement, Stop.class);
                stopList.add(stop);
            }
            return stopList;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getLinesFromStop(String stopId){
        try{
            Gson gson = new Gson();
            Stop stop = gson.fromJson(getJson(REALTIMESTOPURL + stopId), Stop.class);
            return stop.getLinesObject();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    public static Stop getStopFromId(String stopId){
        try{
            Gson gson = new Gson();
            Stop stop = gson.fromJson(getJson(REALTIMESTOPURL + stopId), Stop.class);
            return stop;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*public static void main(String[] args) {
        List<Bus> busList = getBusFromLine("2823");
        for (Bus bus : busList){
            System.out.println(bus + "\n");
        }
    }*/
}
