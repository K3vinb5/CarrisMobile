package kevin.carrismobile.api;

import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.data.bus.Direction;
import kevin.carrismobile.data.bus.Path;
import kevin.carrismobile.data.bus.Point;
import kevin.carrismobile.data.bus.RealTimeSchedule;
import kevin.carrismobile.data.bus.Schedule;
import kevin.carrismobile.data.bus.Stop;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CarrisApi {

    public final static String GET_BUS_STOP_TIMES = " https://www.carris.pt/umbraco/Surface/Routes/GetBusStopTimes";
    public final static String GET_LINE = "https://www.carris.pt/viaje/carreiras/";
    public final static String GET_LIST = "https://carris.pt/umbraco/Surface/Routes/GetRoutes?Query=&VehicleTypeId=&ZoneId=";

    public static Carreira getCarreira(String name, String lineId, String color, String agency_id){
        String response = "";
        Gson gson = new Gson();
        Carreira carreiraOut = new Carreira(name, lineId, color);
        carreiraOut.setOnline(true);
        carreiraOut.setAgency_id("0");
        try {
            Document doc = Jsoup.connect(GET_LINE + lineId + "/").get();
            response = doc.body().html();
            Document doc1 = Jsoup.parse(response);
            Elements stopsContainers = doc1.select(".stops-wrapper");

            carreiraOut.initLists();
            int index = 0;
            for (Element stopsContainer : stopsContainers) {
                // Select the bus stop elements within the container
                Elements busStopElements = stopsContainer.select(".bus-stop");

                String firstStopName = "";
                String lastStopName = "";
                Direction currentDirection = new Direction(lineId+"_"+index,"");
                int stopIndex = 0;
                // Iterate through each bus stop element
                for (Element busStopElement : busStopElements) {
                    // Extract information from data attributes

                    String stopName = busStopElement.text();
                    String stopId = busStopElement.attr("data-stop-id");
                    String coordinates = busStopElement.attr("data-stop-coordinates");
                    Point geoLocation = gson.fromJson(coordinates, Point.class);
                    double latitude = geoLocation.getLat();
                    double longitude = geoLocation.getLon();
                    Path currentPath = new Path(stopId, stopIndex);
                    Stop currentStop = new Stop(stopId, stopName, latitude, longitude);
                    currentDirection.getPointList().add(new Point(latitude, longitude));
                    currentStop.setAgency_id("0");
                    currentStop.setOnline(true);
                    currentPath.setStop(currentStop);
                    currentDirection.getPathList().add(currentPath);
                    if (stopIndex == 0){
                        firstStopName = stopName;
                    }else if(stopIndex == busStopElements.size() - 1){
                        lastStopName = stopName;
                    }
                    stopIndex++;
                }
                currentDirection.setHeadsign(firstStopName + " -> " + lastStopName);
                carreiraOut.getDirectionList().add(currentDirection);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carreiraOut;
    }

    public static void updateDirectionAndStop(Carreira carreira, int directionIndex, int stopIndex){
        Stop stop = carreira.getDirectionList().get(directionIndex).getPathList().get(stopIndex).getStop();
        stop.getScheduleList().clear();
        stop.getOfflineRealTimeSchedules().clear();
        String urlDirection = null;
        if (carreira.getName().contains("Circulação")){
            urlDirection = "3";
        }else{
            urlDirection = (directionIndex + 1) + "";
        }
        String url = GET_BUS_STOP_TIMES+"?RouteNumber="+carreira.getRouteId()+"&Direction="+urlDirection+"&BusStopId="+stop.getStopID()+"&Date="+getCurrentFormattedTime();
        Log.d("CARRIS REALTIME", url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String html = "";
        try {
            html = client.newCall(request).execute().body().string();
            Document doc = Jsoup.parse(html);
            // Select the elements containing schedule information
            Elements scheduleElements = doc.select(".time-container-schedule-hour-minutes");
            // Iterate over the schedule elements
            for (Element scheduleElement : scheduleElements) {
                // Extract hour and minutes
                String hour = scheduleElement.select(".time-container-schedule-hour").text();
                String[] minutes = scheduleElement.select(".time-container-schedule-minutes").text().split(" ");
                hour = hour.substring(0,hour.length() - 1); //removes last character
                if (hour.length() == 1){
                    hour = "0" + hour; //adds one left zero if resulting string is one character long to keep standart
                }
                for (String minute : minutes) {
                    String toAdd;
                    if(!minute.equals("")){
                        toAdd = hour + ":" + minute + ":00";
                    }else{
                        toAdd = hour + ":00:00";
                    }
                    stop.getScheduleList().add(new Schedule(stop.getStopID(), toAdd, "-1"));
                    stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(carreira.getRouteId(), carreira.getRouteId(), "null", "null", "null", stopIndex, toAdd, "null"));
                }
            }


        } catch (Exception ignore) {
            Log.e("ERROR", ignore.getMessage());
        }
    }

    public static List<CarreiraBasic> getCarreiraBasicList(){
        List<CarreiraBasic> returnList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GET_LIST).build();
        String html;
        try{
            html = client.newCall(request).execute().body().string();
            Document doc = Jsoup.parse(html);
            Elements routeClasses = doc.select(".results-container");
            for (Element routeClass : routeClasses) {
                // Extract background color
                String bgColor = routeClass.select(".status").attr("style").replaceAll(".*background-color:\\s*([^;]+).*", "$1").trim().toUpperCase();

                // Extract route ID (text within the status div)
                String routeId = routeClass.select(".status").text().trim();

                // Extract route name
                String routeName = routeClass.select(".text-container").text().trim();
                CarreiraBasic carreiraBasic = new CarreiraBasic(routeId, routeName, bgColor, true);
                carreiraBasic.setAgency_id("0");
                returnList.add(carreiraBasic);
            }
        }catch (Exception e){
            Log.e("CARRIS API ERROR", "Message: " + e.getMessage());
        }
        return returnList;
    }

    public static String getCurrentFormattedTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return currentUTC.format(formatter);
        }
        return null;
    }
}
