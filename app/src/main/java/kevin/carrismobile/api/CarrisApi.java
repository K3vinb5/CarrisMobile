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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.data.bus.Direction;
import kevin.carrismobile.data.bus.Path;
import kevin.carrismobile.data.bus.Point;
import kevin.carrismobile.data.bus.RealTimeSchedule;
import kevin.carrismobile.data.bus.Schedule;
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.data.bus.carris.CarrisWaitTimes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CarrisApi {
    /**
     * API endpoint URL for retrieving bus stop times.
     */
    public final static String GET_BUS_STOP_TIMES = " https://www.carris.pt/umbraco/Surface/Routes/GetBusStopTimes";
    /**
     * API endpoint URL for retrieving information about a specific bus line.
     */
    public final static String GET_LINE = "https://www.carris.pt/viaje/carreiras/";
    /**
     * API endpoint URL for retrieving a list of all bus routes.
     */
    public final static String GET_LIST = "https://carris.pt/umbraco/Surface/Routes/GetRoutes?Query=&VehicleTypeId=&ZoneId=";
    /**
     * API endpoint URL for retrieving next routes at a bus stop.
     */
    public final static String GET_STOP_INFO = "https://carris.pt/umbraco/Surface/BusStops/GetNextRoutesAtStop";
    /**
     * API endpoint URL for retrieving nearest bus stops.
     */
    public final static String GET_NEAR_STOPS = "https://carris.pt/umbraco/Surface/BusStops/GetNearestBusStops";

    /**
     * Retrieves detailed information about a specific bus route based on the provided name and line ID.
     *
     * @param name    The name of the bus route.
     * @param lineId  The ID of the bus route.
     * @return A {@link Carreira} object containing detailed information about the specified bus route.
     */
    public static Carreira getCarreira(String name, String lineId){
        String response = "";
        Gson gson = new Gson();
        Carreira carreiraOut = new Carreira(name, lineId, "#000000");
        carreiraOut.setOnline(true);
        carreiraOut.setAgency_id("0");
        try {
            Document doc = Jsoup.connect(GET_LINE + lineId + "/").get();
            response = doc.body().html();
            Document doc1 = Jsoup.parse(response);
            Elements stopsContainers = doc1.select(".stops-wrapper");
            Elements shapeContainers = doc1.select("div.shape-holder");
            Element button = doc.selectFirst("button.variant");
            if (button != null) {
                String color = button.attr("style").replaceAll(".*background-color:(#\\w+);.*", "$1");
                if (color.length() == 7){
                    carreiraOut.setColor(color.toUpperCase());
                }
            }else{
                Element div = doc.selectFirst("div.variant");
                if(div != null){
                    String color = div.attr("style").replaceAll(".*background-color:(#\\w+);.*", "$1");
                    if (color.length() == 7){
                        carreiraOut.setColor(color.toUpperCase());
                    }
                }
            }
            List<List<Point>> pointsList = new ArrayList<>();
            carreiraOut.initLists();
            int index = 0;
            for (Element shape : shapeContainers){
                String geojsonData = shape.attr("data-itinerary-geojson");
                if (!geojsonData.isEmpty()) {
                    pointsList.add(parseGeoJson(geojsonData));
                }
            }
            //Log.d("DEBUG Carris Api", "Number of directions :" + pointsList.size());
            for (Element stopsContainer : stopsContainers) {
                // Select the bus stop elements within the container
                Elements busStopElements = stopsContainer.select(".bus-stop");

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
                    currentStop.setAgency_id("0");
                    currentStop.setOnline(true);
                    currentPath.setStop(currentStop);
                    currentDirection.getPathList().add(currentPath);
                    if(stopIndex == busStopElements.size() - 1){
                        lastStopName = stopName;
                    }
                    stopIndex++;
                }
                currentDirection.getPointList().addAll(pointsList.get(index));
                currentDirection.setHeadsign(lastStopName);
                carreiraOut.getDirectionList().add(currentDirection);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carreiraOut;
    }
    /**
     * Parses GeoJSON data and returns a list of geographic points.
     *
     * @param geoJson GeoJSON data to parse.
     * @return A list of {@link Point} objects representing geographic points.
     */
    public static List<Point> parseGeoJson(String geoJson) {
        List<Point> points = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)\\]");
        Matcher matcher = pattern.matcher(geoJson);

        while (matcher.find()) {
            double lon = Double.parseDouble(matcher.group(1));
            double lat = Double.parseDouble(matcher.group(2));
            points.add(new Point(lat, lon));
        }

        return points;
    }
    /**
     * Updates schedule information for a specific bus stop within a given bus route and direction and stop.
     *
     * @param carreira        The bus route.
     * @param directionIndex  The index of the direction within the bus route.
     * @param stopIndex       The index of the stop within the direction.
     */
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
        //Log.d("CARRIS REALTIME", url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        String html = "";
        try {
            Response response = client.newCall(request).execute();
            html = response.body().string();
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
            response.close();
        } catch (Exception ignore) {
            Log.e("ERROR", ignore.getMessage());
        }
    }
    /**
     * Retrieves a list of basic information about all bus routes.
     *
     * @return A list of {@link CarreiraBasic} objects containing basic information about each bus route.
     */
    public static List<CarreiraBasic> getCarreiraBasicList(){
        List<CarreiraBasic> returnList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GET_LIST).build();
        String html;
        try{
            Response response = client.newCall(request).execute();
            html = response.body().string();
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
                response.close();
            }
        }catch (Exception e){
            Log.e("CARRIS API ERROR", "Message: " + e.getMessage());
        }
        return returnList;
    }
    /**
     * Retrieves wait times for buses at a specific bus stop.
     *
     * @param stopId   The ID of the bus stop.
     * @param stopName The name of the bus stop.
     * @return A list of {@link CarrisWaitTimes} objects representing wait times for buses at the specified stop.
     */
    public static List<CarrisWaitTimes> getStopWaitTimes(String stopId, String stopName){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GET_STOP_INFO + "?StopId=" + stopId + "&StopName=" + stopName).build();
        List<CarrisWaitTimes> carrisWaitTimes = new ArrayList<>();
        String html;
        try {
            Response response = client.newCall(request).execute();
            html = response.body().string();
            Document doc = Jsoup.parse(html);

            // Extract Routes
            Elements routeElements = doc.select(".next-routes-route");

            for (Element routeElement : routeElements) {
                // Extract Route Information
                String routeId = routeElement.select(".info-number").text().trim();
                String routeName = routeElement.select(".detail-text-name").text().trim();
                String routeDest = routeElement.select(".detail-text-dest").text().trim();
                String routeWaiting = routeElement.select(".next-routes-route-time div").first().text().trim();
                carrisWaitTimes.add(new CarrisWaitTimes(routeId, routeName, routeDest, routeWaiting.toLowerCase()));
            }
            response.close();
        }catch (Exception e){
            Log.e("CARRIS API ERROR", "Message: " + e.getMessage());
        }
        return carrisWaitTimes;
    }
    /**
     * Gets the current formatted time for use in API requests.
     *
     * @return A formatted string representing the current date in the "yyyy-MM-dd" format.
     */
    public static String getCurrentFormattedTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime currentUTC = LocalDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return currentUTC.format(formatter);
        }
        return null;
    }
}
