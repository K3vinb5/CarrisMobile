package api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import data_structure.Bus;
import data_structure.Carreira;
import data_structure.CarreiraBasic;
import data_structure.Direction;
import data_structure.RealTimeSchedule;

public class Api {
    private String line;
    public static final String CARREIRAURL = "https://api.carrismetropolitana.pt/lines/";
    public static final String DIRECTIONURL = "https://api.carrismetropolitana.pt/patterns/";
    public static final String REALTIMESTOPURL = "https://api.carrismetropolitana.pt/stops/";
    public static final String BUSREALTIMESTOPURL = "https://api.carrismetropolitana.pt/vehicles";

    public static String getJson(String url){
        try {
            final Document document = Jsoup.connect(url).ignoreContentType(true).get();
            Gson gson = new Gson();
            return document.body().text();
        }catch (Exception e ){
            e.printStackTrace();
        }

        return null;
    }

    public static Direction getDirection(String pattern){
        try {
            Gson gson = new Gson();
            Direction direction = gson.fromJson(getJson(DIRECTIONURL + pattern), Direction.class);
            return  direction;
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

    public static List<RealTimeSchedule> getRealTimeStops(String stopId){
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

    /*public static void main(String[] args) {
        List<Bus> busList = getBusFromLine("2823");
        for (Bus bus : busList){
            System.out.println(bus + "\n");
        }
    }*/
}
