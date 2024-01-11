package kevin.carrismobile.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import kevin.carrismobile.data.metro.MetroStop;
import okhttp3.*;
import okhttp3.internal.http.RealResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetroApi {

    /*public final static String TOKEN = "";
    public final static String BASE_URL = "https://api.metrolisboa.pt:8243/estadoServicoML/1.0.1/";

    public final static String ALL_LINES_STATE_URL = "estadoLinha/todos";
    public final static String ALL_STATIONS_INFO = "infoEstacao/todos";
    public final static String ALL_INFO_DESTINY = "infoDestinos/todos";
    public final static String ALL_WAITING_TIME_STATIONS = "tempoEspera/Estacao/todos";

    public final static String LINE_STATE = "estadoLinha/";
    public final static String STATION_INFO = "infoEstacao/";
    public final static String LINE_WAITING_TIME = "tempoEspera/Linha/";
    public final static String STATION_WAITING_TIME = "tempoEspera/Estacao/";

    public final static String LINE_INFO_INTERVALS_DAY_HOUR = "infoIntervalos/";*/

    public final static String[] lines = new String[]{"Azul", "Amarela", "Vermelha", "Verde"};

    /*public static String getJson(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(BASE_URL + url).addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + TOKEN).build();
        String output = "";
        try {
            output = client.newCall(request).execute().body().string();
        } catch (Exception ignore) {}
        return output;
    }

    public static MetroStop getMetroStop(String stop_id) {
        Gson gson = new Gson();
        JsonArray responseArray = null;
        String jsonString = getJson(STATION_INFO + stop_id);
        JsonElement jsonResponse = gson.fromJson(jsonString, JsonElement.class);
        if (jsonResponse.isJsonObject()) {
            JsonObject jsonObject = jsonResponse.getAsJsonObject();
            responseArray = jsonObject.getAsJsonArray("resposta");
            return gson.fromJson(responseArray.get(0), MetroStop.class);
        }
        return null;
    }

    public static List<MetroStop> getMetroStops() {
        List<MetroStop> out = new ArrayList<>();
        Gson gson = new Gson();
        JsonArray responseArray = null;
        String jsonString = getJson(ALL_STATIONS_INFO);
        JsonElement jsonResponse = gson.fromJson(jsonString, JsonElement.class);
        if (jsonResponse.isJsonObject()) {
            JsonObject jsonObject = jsonResponse.getAsJsonObject();
            responseArray = jsonObject.getAsJsonArray("resposta");
            for (JsonElement jsonElement : responseArray){
                out.add(gson.fromJson(jsonElement, MetroStop.class));
            }
            return out;
        }
        return null;
    }*/

    public static MetroStop getMetroStopArrivals(String stop_id) {
        OkHttpClient client = new OkHttpClient();
        JsonObject json = new JsonObject();
        JsonObject variables = new JsonObject();

        json.addProperty("operationName", "Station");
        variables.addProperty("id", stop_id);
        json.add("variables", variables);
        json.addProperty("query", "query Station($id: String!) { station(id: $id) { name lines lat lon waitTimes { destination { id name lines __typename } arrivalTimes { timeLeft __typename } live __typename } __typename } }");
        RequestBody formBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.proximometro.pt/graphql")
                .post(formBody)
                .build();
        String out = "";
        MetroStop stop = null;
        Gson gson = new Gson();
        try {
            out = client.newCall(request).execute().body().string();
            JsonObject object = gson.fromJson(out, JsonObject.class);
            JsonObject stationData = gson.fromJson(gson.toJson(object.get("data").getAsJsonObject().get("station")), JsonObject.class);
            Log.d("DEBUG API", stationData+"");
            stop = gson.fromJson(stationData, MetroStop.class);
        } catch (Exception e) {
            Log.e("API ERROR", e.getMessage()+"");
        }
        stop.setStop_id(stop_id);
        return stop;
    }

    public static List<MetroStop> getStops(String line){
        OkHttpClient client = new OkHttpClient();
        JsonObject json = new JsonObject();
        JsonObject variables = new JsonObject();

        json.addProperty("operationName", "Line");
        variables.addProperty("id", line);
        json.add("variables", variables);
        json.addProperty("query", "query Line($id: LineID!) { line(id: $id) { stations { id name lines __typename } __typename } }");
        RequestBody formBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.proximometro.pt/graphql")
                .post(formBody)
                .build();
        String out ="";
        List<MetroStop> stopList = new ArrayList<>();
        Gson gson = new Gson();
        try {
            out = client.newCall(request).execute().body().string();
            JsonObject object = gson.fromJson(out, JsonObject.class);
            JsonArray responseArray = gson.fromJson(gson.toJson(object.get("data").getAsJsonObject().get("line").getAsJsonObject().get("stations")), JsonArray.class);
            for (JsonElement element : responseArray){
                stopList.add(gson.fromJson(element, MetroStop.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stopList;
    }

    public static List<MetroStop> getAllStops(){
        List<MetroStop> stopList = new ArrayList<>();
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < lines.length; i++){
            int finalI = i;
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    List<MetroStop> toAdd = getStops(lines[finalI]);
                    synchronized (stopList){
                        stopList.addAll(toAdd);
                    }
                }
            }));
        }
        for (int i = 0; i < lines.length; i++){
            threadList.get(i).start();
        }
        try {
         for (int i = 0; i < lines.length; i++){
             threadList.get(i).join();
        }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stopList;
    }


}
