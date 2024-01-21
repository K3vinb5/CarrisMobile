package kevin.carrismobile.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.data.bus.Bus;
import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.data.bus.Direction;
import kevin.carrismobile.data.bus.Point;
import kevin.carrismobile.data.bus.RealTimeSchedule;
import kevin.carrismobile.data.bus.Shape;
import kevin.carrismobile.data.bus.Stop;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CarrisMetropolitanaApi {
    /**
     * API endpoint URL for retrieving information about a specific bus route.
     */
    public static final String CARREIRAURL = "https://api.carrismetropolitana.pt/lines/";
    /**
     * API endpoint URL for retrieving a list of all bus routes.
     */
    public static final String CARREIRALISTURL = "https://api.carrismetropolitana.pt/lines";
    /**
     * API endpoint URL for retrieving pattern (direction) information.
     */
    public static final String DIRECTIONURL = "https://api.carrismetropolitana.pt/patterns/";
    /**
     * API endpoint URL for retrieving real-time information about a specific bus stop.
     */
    public static final String REALTIMESTOPURL = "https://api.carrismetropolitana.pt/stops/";
    /**
     * API endpoint URL for retrieving a list of all bus stops.
     */
    public static final String REALTIMELISTSTOPURL = "https://api.carrismetropolitana.pt/stops";
    /**
     * API endpoint URL for retrieving information about the shape of a bus route.
     */
    public static final String SHAPELISTURL = "https://api.carrismetropolitana.pt/shapes/";
    /**
     * API endpoint URL for retrieving real-time information about all buses.
     */
    public static final String BUSREALTIMESTOPURL = "https://api.carrismetropolitana.pt/vehicles";
    /**
     * Performs an HTTP GET request to the specified URL and returns the response as a JSON string.
     *
     * @param url The URL to which the GET request is made.
     * @return A JSON string containing the response from the specified URL.
     */
    public static String getJson(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String output = "";
        try {
            Response response = client.newCall(request).execute();
            output = response.body().string();
            response.close();
        } catch (Exception e) {
            Log.e("CARRIS METROPOLITANA API", "Message :" + e.getMessage());
        }
        return output;
    }
    /**
     * Retrieves and parses direction information for a specific pattern associated with a route.
     *
     * @param pattern         The pattern ID for which direction information is requested.
     * @param routeId         The ID of the route associated with the pattern.
     * @param directionIndex  Index representing the direction within the route.
     * @return A {@link Direction} object containing information about the specified direction.
     */
    public static Direction getDirection(String pattern, String routeId, int directionIndex){
        try {
            Gson gson = new Gson();
            Direction direction = gson.fromJson(getJson(DIRECTIONURL + pattern), Direction.class);
            direction.setRouteId(routeId);
            direction.setDirectionIndexInRoute(directionIndex);
            return  direction;

        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Retrieves a list of geographic points associated with a specific shape ID.
     *
     * @param shape_id The ID of the shape for which points are requested.
     * @return A list of {@link Point} objects representing geographical points.
     */
    public static List<Point> getPoints(String shape_id){
        try{
            Shape shape = new Gson().fromJson(getJson(SHAPELISTURL + shape_id), Shape.class);
            return shape.getPoints();
        }catch (Exception ignore){}
        return null;
    }
    /**
     * Retrieves and parses information for a specific bus route (Carreira) based on the given ID.
     *
     * @param id The ID of the bus route (Carreira).
     * @return A {@link Carreira} object containing information about the specified bus route.
     */
    public static Carreira getCarreira(String id){
        try {
            Gson gson = new Gson();
            Carreira carreira = gson.fromJson(getJson(CARREIRAURL + id), Carreira.class);
            carreira.setOnline(true);
            carreira.setAgency_id("-1");
            return carreira;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Retrieves real-time schedule information for a specific bus stop.
     *
     * @param stopId The ID of the bus stop.
     * @return A list of {@link RealTimeSchedule} objects representing real-time schedule information for the specified bus stop.
     * @throws SocketTimeoutException If a timeout occurs during the HTTP request.
     */
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
    /**
     * Retrieves a list of buses currently active on a specific bus route.
     *
     * @param carreiraID The ID of the bus route for which buses are requested.
     * @return A list of {@link Bus} objects representing buses on the specified bus route.
     */
    public static List<Bus> getBusFromLine(String carreiraID){
        try {
            Gson gson = new Gson();
            List<Bus> busList = new ArrayList<>();

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
    /**
     * Retrieves a list of basic information about all bus routes (Carreiras).
     *
     * @return A list of {@link CarreiraBasic} objects containing basic information about each bus route.
     */
    public static List<CarreiraBasic> getCarreiraBasicList(){
        try {
            Gson gson = new Gson();
            JsonArray jsonCarreiraArray = new JsonParser().parse(getJson(CARREIRALISTURL)).getAsJsonArray();
            List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
            for (JsonElement jsonElement : jsonCarreiraArray){
                CarreiraBasic carreira = gson.fromJson(jsonElement, CarreiraBasic.class);
                carreira.setOnline(true);
                carreira.setAgency_id("-1");
                carreiraBasicList.add(carreira);
            }
            return carreiraBasicList;
        }catch (Exception e ){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Retrieves a list of bus stops with basic information.
     *
     * @return A list of {@link Stop} objects containing basic information about each bus stop.
     */
    public static List<Stop> getStopList(){
        try {
            Gson gson = new Gson();
            JsonArray jsonCarreiraArray = gson.fromJson(getJson(REALTIMELISTSTOPURL), JsonArray.class);
            //Log.d("CARRIS METROPOLITANA API DEBUG", "SIZE :" + jsonCarreiraArray.size());
            List<Stop> stopList = new ArrayList<>();
            for (JsonElement jsonElement : jsonCarreiraArray){
                Stop stop = gson.fromJson(jsonElement, Stop.class);
                stop.setOnline(true);
                stop.setAgency_id("-1");
                stopList.add(stop);
            }
            return stopList;
        }catch (Exception e ){
            Log.e("ERROR CARRIS METROPOLITANA API", "Message :" + e.getMessage());
            return null;
        }
    }
    /**
     * Retrieves a list of bus lines associated with a specific bus stop.
     *
     * @param stopId The ID of the bus stop.
     * @return A list of strings representing bus lines associated with the specified bus stop.
     */
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
    /**
     * Retrieves detailed information about a specific bus stop based on the stop ID.
     *
     * @param stopId The ID of the bus stop.
     * @return A {@link Stop} object containing detailed information about the specified bus stop.
     */
    public static Stop getStopFromId(String stopId){
        try{
            Gson gson = new Gson();
            Stop stop = gson.fromJson(getJson(REALTIMESTOPURL + stopId), Stop.class);
            stop.setOnline(true);
            stop.setAgency_id("-1");
            return stop;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
