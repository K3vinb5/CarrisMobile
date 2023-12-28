package kevin.carrismobile.api;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.carrismobile.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import kevin.carrismobile.data.Carreira;
import kevin.carrismobile.data.CarreiraBasic;
import kevin.carrismobile.data.Direction;
import kevin.carrismobile.data.Path;
import kevin.carrismobile.data.RealTimeSchedule;
import kevin.carrismobile.data.Schedule;
import kevin.carrismobile.data.Stop;
import kevin.carrismobile.data.Trip;

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
        if (!Offline.mPrefsStopTimes.contains(STOP_TIMES_KEY)){
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
        }
    }

    public static List<CarreiraBasic> getCarreiraList(){
        boolean currentEnd = false;
        boolean idFound = true;
        boolean skipHeader = true;
        //attributes
        Map<String, Integer> CarreiraIdIndex= new HashMap<>();
        List<String> seenDirections = new ArrayList<>();
        List<CarreiraBasic> carreiraList = new ArrayList<>();
        String[] lastLine;
        String carreiraId = null;
        String long_name = "";
        String color = "color_cp";
        List<String> patternList = new ArrayList<>();
        List<Direction> directionList = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(Offline.mPrefsRoutes.getString(ROUTES_KEY, null));
            while (scanner.hasNextLine()) {
                // nextLine[] is an array of values from the line
                String[] nextLine = scanner.nextLine().split(",");
                if (skipHeader){
                    skipHeader = false;
                    continue;
                }
                if(nextLine.length == 1){
                    continue;
                }
                if (carreiraId != null){
                    List<String> temp = Arrays.asList(nextLine[3].split(" - "));
                    temp.sort(String::compareTo);
                    String newID = temp.get(0).substring(0,2) + temp.get(1).substring(0,2);
                    currentEnd = !carreiraId.equals(newID);
                }
                if (currentEnd){
                    long_name= "";
                    seenDirections.clear();
                    for (int i = 0; i < directionList.size(); i++){
                        String currentDirection = directionList.get(i).getHeadsign();
                        if(i == directionList.size() - 1){
                            if (seenDirections.contains(currentDirection)){
                                long_name = long_name.substring(0, long_name.length() - 3);
                                continue;
                            }
                            long_name += currentDirection;
                        }else {
                            if (seenDirections.contains(currentDirection)){
                                continue;
                            }
                            long_name += currentDirection + " - ";
                        }
                        seenDirections.add(currentDirection);
                    }
                    CarreiraBasic currentCarreira = new CarreiraBasic(carreiraId, long_name, color);
                    //currentCarreira.setPatterns(patternList);
                    //currentCarreira.setDirectionList(directionList);
                    currentCarreira.setOnline(false);
                    currentCarreira.setAgency_id("1");
                    if(!carreiraList.contains(currentCarreira)){
                        carreiraList.add(currentCarreira);
                        CarreiraIdIndex.put(carreiraId, carreiraList.indexOf(currentCarreira));
                    }else{
                        CarreiraBasic cb = carreiraList.get(CarreiraIdIndex.get(carreiraId));
                        cb.setLong_name(cb.getLong_name() + " / " + long_name);
                    }
                    currentEnd = false;
                    idFound = true;
                }
                if (idFound) {
                    List<String> temp = Arrays.asList(nextLine[3].split(" - "));
                    temp.sort(String::compareTo);
                    String newID = temp.get(0).substring(0,2) + temp.get(1).substring(0,2);
                    carreiraId = newID;
                    patternList.clear();
                    directionList.clear();
                    idFound = false;
                }
                patternList.add(nextLine[1]);
                Direction direction = new Direction(nextLine[1], nextLine[3].split("- ")[1]);
                direction.setRouteId(carreiraId);
                directionList.add(direction);
            }
            scanner.close();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return carreiraList;
    }
    public static Carreira getCarreira(String id){
        boolean found = false;
        boolean first = true;
        //attributes
        Carreira carreira = null;
        String carreiraId = null;
        String long_name = "";
        String color = "color_cp";
        List<String> patternList = new ArrayList<>();
        List<Direction> directionList = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(Offline.mPrefsRoutes.getString(ROUTES_KEY, null));
            while (scanner.hasNextLine()) {
                // nextLine[] is an array of values from the line
                String[] nextLine = scanner.nextLine().split(",");
                if(nextLine.length == 1){
                    continue;
                }
                if (first){
                    first = false;
                    continue;
                }
                List<String> temp = Arrays.asList(nextLine[3].split(" - "));
                temp.sort(String::compareTo);
                String newID = temp.get(0).substring(0,2) + temp.get(1).substring(0,2);
                if (newID.equals(id)){
                    if (!found){
                        carreiraId = newID;
                        found = true;
                    }
                    patternList.add(nextLine[1]);
                    Direction direction = new Direction(nextLine[1], nextLine[3].split("- ")[1]);
                    direction.setRouteId(carreiraId);
                    directionList.add(direction);
                }
            }
            scanner.close();
            if (carreiraId != null){
                for (int i = 0; i < directionList.size(); i++){
                    if(i == patternList.size() - 1){
                        long_name += directionList.get(i).getHeadsign();
                    }else {
                        long_name += directionList.get(i).getHeadsign() + " / ";
                    }
                }
                carreira = new Carreira(long_name, carreiraId, color);
                carreira.setOnline(false);
                carreira.setAgency_id("1");
                carreira.setPatterns(patternList);
                carreira.setDirectionList(directionList);
                Direction direction = carreira.getDirectionList().get(0);
                addTripsToDirection(direction);
                Carreira finalCarreira = carreira;
                addStopsToDirection(direction, finalCarreira.getRouteId(), direction.getDirectionId(), direction.getHeadsign());
                for (Path path : direction.getPathList()) {
                    path.getStop().getOfflineRealTimeSchedules().sort(Comparator.comparing(RealTimeSchedule::getScheduled_arrival));
                    path.getStop().getScheduleList().sort(Comparator.comparing(Schedule::getArrival_time));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return carreiraId != null ? carreira : null;
    }

    public static void updateDirectionIndex(Carreira carreira, int directionIndex){
        Direction direction = carreira.getDirectionList().get(directionIndex);
        if (direction.getTrips().size() != 0){
            return;
        }
        addTripsToDirection(direction);
        addStopsToDirection(direction,carreira.getRouteId(), direction.getDirectionId(), direction.getHeadsign());
        for (Path path : direction.getPathList()) {
            path.getStop().getOfflineRealTimeSchedules().sort(Comparator.comparing(RealTimeSchedule::getScheduled_arrival));
            path.getStop().getScheduleList().sort(Comparator.comparing(Schedule::getArrival_time));
        }

    }

    private static void addTripsToDirection(Direction direction){
        try{
            Scanner scanner = new Scanner(Offline.mPrefsTrips.getString(TRIPS_KEY, null));
            while(scanner.hasNextLine()){
                String[] currentLine = scanner.nextLine().split(",");
                if (currentLine[0].equals(direction.getDirectionId())){
                    direction.addTrip(new Trip(currentLine[1], ""));
                }
            }
            scanner.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void addStopsToDirection(Direction direction, String lineID, String directionId, String directionHeadSign){
        List<String> targetLines = new ArrayList<>();
        List<String> stopLines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(Offline.mPrefsStops.getString(STOPS_KEY, null));
            while (scanner.hasNextLine()) {
                stopLines.add(scanner.nextLine());
            }
            scanner.close();
        }catch (Exception e) {
            Log.e("ERROR IN OFFLINE", e.getMessage());
        }
        List<String> tripIds = new ArrayList<>();
        direction.getTrips().forEach(trip -> tripIds.add(trip.getTripId()));
        try {
            Scanner scanner = new Scanner((Offline.mPrefsStopTimes.getString(STOP_TIMES_KEY, null)));
            while (scanner.hasNextLine()){
                String nextLine = scanner.nextLine();
                if (tripIds.contains(nextLine.split(",")[0])){
                    targetLines.add(nextLine);
                }
            }
            scanner.close();
            targetLines.sort(String::compareTo);
            Log.d("IMPORTANT", targetLines.size()+"");
        } catch (Exception e) {
            Log.e("ERROR IN OFFLINE", e.getMessage());
        }
        //Creates Paths using First trip
        for (String targetLine : targetLines) {
            String[] nextLine = targetLine.split(",");
            Path path = new Path(nextLine[3], Integer.parseInt(nextLine[4]), true, true, -1);
            if (direction.getPathList().contains(path)) {
                break;
            }
            direction.getPathList().add(path);
        }
        List<Thread> threadList = new ArrayList<>();
        //creates stops on paths based on the first trip
        for(int i = 0; i < direction.getPathList().size(); i++){
            int finalI = i;
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    Path path = direction.getPathList().get(finalI);
                    for (String stopLine : stopLines) {
                        String pathId = path.getId();
                        String[] nextLine = stopLine.split(",");
                        if (!pathId.equals(nextLine[0])){
                            continue;
                        }
                        Stop stop = new Stop(path.getId(), nextLine[2],nextLine[2], Double.parseDouble(nextLine[4]),Double.parseDouble(nextLine[5]),"Lisbon",-1,"Lisbon",-1,"Lisbon","-1","Lisbon",new ArrayList<>(),new ArrayList<>());
                        stop.init();
                        stop.setOnline(false);
                        stop.setAgency_id("1");
                        stop.getScheduleList().add(new Schedule(path.getId(), targetLines.get(finalI).split(",")[1], "-1"));
                        stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(lineID,lineID, directionId, pathId, directionHeadSign, path.getStop_sequence(), targetLines.get(finalI).split(",")[1],"-1"));
                        path.setStop(stop);
                        break;
                    }
                }
            }));

        }
        for (Thread thread:threadList){
            thread.start();
        }
        for(Thread thread : threadList){
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //updates the schedules for all the stops based on the trips
        for(int i = direction.getPathList().size(); i < targetLines.size(); i++){
            String[] nextLine = targetLines.get(i).split(",");
            if (nextLine.length == 1){
                continue;
            }
            int currentStopIndex = Integer.parseInt(nextLine[4]);
            if(currentStopIndex > direction.getPathList().size() - 1){
                continue;
            }
            updateStopOnPath(direction.getPathList().get(currentStopIndex), nextLine[1],lineID, directionId, directionHeadSign);
        }
    }
    private static void updateStopOnPath(Path path, String arrival_time, String lineId, String directionId, String directionHeadSign){
        Stop stop = path.getStop();
        if (stop == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            updateStopOnPath(path, arrival_time, lineId, directionId, directionHeadSign);
            return;
        }
        synchronized (stop.getScheduleList()){
            stop.getScheduleList().add(new Schedule(stop.getStopID(), arrival_time, "-1"));
        }
        synchronized (stop.getOfflineRealTimeSchedules()){
            stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(lineId,lineId, directionId, stop.getStopID()+"", directionHeadSign, path.getStop_sequence(), arrival_time,"-1"));
        }
    }

}
