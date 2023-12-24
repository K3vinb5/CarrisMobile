package kevin.carrismobile.api;

import android.os.Environment;
import android.util.Log;

import kevin.carrismobile.data.*;
import kevin.carrismobile.fragments.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Offline {
    //keys constants
    public final static String ROUTES_KEY = "key_routes";
    public final static String STOP_TIMES_KEY = "key_stop_times";
    public final static String TRIPS_KEY = "key_trips";
    public final static String STOPS_KEY = "key_stops";
    //TODO Deprecatred
    public final static String ROUTES_PATH = Environment.getDataDirectory().getAbsolutePath() + "/gtfs_carris/routes.txt";
    public final static String TRIPS_PATH = Environment.getDataDirectory().getAbsolutePath() + "/gtfs_carris/trips.txt";
    public final static String STOPS = Environment.getDataDirectory().getAbsolutePath() + "/gtfs_carris/stops.txt";
    public final static String STOP_TIMES = Environment.getDataDirectory().getAbsolutePath() + "/gtfs_carris/stop_times.txt";

    public static Carreira getCarreira(String id){
        boolean found = false;
        //attributes
        Carreira carreira = null;
        String carreiraId = null;
        String long_name = "";
        String color = "0x0000FF";
        List<String> patternList = new ArrayList<>();
        List<Direction> directionList = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(MainActivity.mPrefs.getString(ROUTES_KEY, null));
            while (scanner.hasNextLine()) {
                // nextLine[] is an array of values from the line
                String[] nextLine = scanner.nextLine().split(",");
                if (nextLine[3].split(" ")[0].equals(id)){
                    if (!found){
                        carreiraId = nextLine[3].split(" ")[0];
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
                carreira.setPatterns(patternList);
                carreira.setDirectionList(directionList);
                for (Direction direction : carreira.getDirectionList()) {
                    addTripsToDirection(direction);
                    addStopsToDirection(direction,carreira.getRouteId(), direction.getDirectionId(), direction.getHeadsign());

                }
                /*for(Direction direction: carreira.getDirectionList()){
                    for (Path path : direction.getPathList()) {
                        path.getStop().getOfflineRealTimeSchedules().sort(Comparator.comparing(RealTimeSchedule::getScheduled_arrival));
                        path.getStop().getScheduleList().sort(Comparator.comparing(Schedule::getArrival_time));
                    }
                }*/
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return carreiraId != null ? carreira : null;
    }

    public static List<CarreiraBasic> getCarreiraList(){
        boolean currentEnd = false;
        boolean idFound = true;
        boolean skipHeader = true;
        //attributes
        List<String> seenDirections = new ArrayList<>();
        List<CarreiraBasic> carreiraList = new ArrayList<>();
        String[] lastLine;
        String carreiraId = null;
        String long_name = "";
        String color = "0x0000FF";
        List<String> patternList = new ArrayList<>();
        List<Direction> directionList = new ArrayList<>();
        try{
            Scanner scanner = new Scanner(MainActivity.mPrefs.getString(ROUTES_KEY, null));
            while (scanner.hasNextLine()) {
                // nextLine[] is an array of values from the line
                String[] nextLine = scanner.nextLine().split(",");
                if (skipHeader){
                    skipHeader = false;
                    continue;
                }
                if (carreiraId != null){
                    currentEnd = !carreiraId.equals(nextLine[3].split(" ")[0]);
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
                    carreiraList.add(currentCarreira);
                    currentEnd = false;
                    idFound = true;
                }
                if (idFound) {
                    carreiraId = nextLine[3].split(" ")[0];
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
        carreiraList.sort(Comparator.comparing(CarreiraBasic::getId));
        return carreiraList;
    }

    private static void addTripsToDirection(Direction direction){
        try{
            Scanner scanner = new Scanner(MainActivity.mPrefs.getString(TRIPS_KEY, null));
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
        boolean begin = false;
        try {
            Scanner scanner = new Scanner(MainActivity.mPrefs.getString(STOPS_KEY, null));
            while (scanner.hasNextLine()) {
                stopLines.add(scanner.nextLine());
            }
            scanner.close();
        }catch (Exception e) {
                Log.e("ERROR IN OFFLINE", e.getMessage());
            }
        try {
            Scanner scanner = new Scanner((MainActivity.mPrefs.getString(STOP_TIMES_KEY, null)));
            while (scanner.hasNextLine()){
                if (!begin){
                    if (scanner.nextLine().split(",")[0].equals(direction.getTrips().get(0).getTripId())){
                        begin = true;
                        targetLines.add(scanner.nextLine()); //first line
                    }else{
                        continue;
                    }
                }
                if (scanner.nextLine().split(",")[0].equals(direction.getTrips().get(direction.getTrips().size() - 1).getTripId())){
                    targetLines.add(scanner.nextLine());
                    break;
                }
                targetLines.add(scanner.nextLine());
            }
            scanner.close();
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
        //creates stops on paths based on the first trip
        for(int i = 0; i < direction.getPathList().size() - 1; i++){
            int finalI = i;
            new Thread(new Runnable() {
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
                        stop.getScheduleList().add(new Schedule(path.getId(), targetLines.get(finalI).split(",")[1], "-1"));
                        stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(Integer.parseInt(lineID),lineID, directionId, pathId, directionHeadSign, path.getStop_sequence(), targetLines.get(finalI).split(",")[1],"-1"));
                        path.setStop(stop);
                        break;
                    }
                }
            }).start();

        }
        Thread last = new Thread(new Runnable() {
            @Override
            public void run() {
                Path path = direction.getPathList().get(direction.getPathList().size() - 1);
                for (String stopLine : stopLines) {
                    String pathId = path.getId();
                    String[] nextLine = stopLine.split(",");
                    if (!pathId.equals(nextLine[0])){
                        continue;
                    }
                    Stop stop = new Stop(path.getId(), nextLine[2],nextLine[2], Double.parseDouble(nextLine[4]),Double.parseDouble(nextLine[5]),"Lisbon",-1,"Lisbon",-1,"Lisbon","-1","Lisbon",new ArrayList<>(),new ArrayList<>());
                    stop.init();
                    stop.getScheduleList().add(new Schedule(path.getId(), targetLines.get(direction.getPathList().size()-1).split(",")[1], "-1"));
                    stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(Integer.parseInt(lineID),lineID, directionId, pathId, directionHeadSign, path.getStop_sequence(), targetLines.get(direction.getPathList().size() - 1).split(",")[1],"-1"));
                    path.setStop(stop);
                    break;
                }
            }
        });
        last.start();
        try {
            last.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //updates the schedules for all the stops based on the trips
        List<String> seenStop = new ArrayList<>();
        for(int i = direction.getPathList().size(); i < targetLines.size(); i++){
            String[] nextLine = targetLines.get(i).split(",");
            int currentStopIndex = Integer.parseInt(nextLine[4]);
            if(currentStopIndex > direction.getPathList().size() - 1){
                continue;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateStopOnPath(direction.getPathList().get(currentStopIndex), nextLine[1],lineID, directionId, directionHeadSign);
                }
            }).start();
        }
    }

    private static void addStopToPath(Path path, String arrival_time, String lineId, String directionId, String directionHeadSign){
        String pathId = path.getId();
        int debug = 0;
        try {
            Scanner scanner = new Scanner(MainActivity.mPrefs.getString(STOPS_KEY, null));
            while(scanner.hasNextLine()){
                String[] nextLine = scanner.nextLine().split(",");
                if (!pathId.equals(nextLine[0])){
                    continue;
                }
                Stop stop = new Stop(path.getId(), nextLine[2],nextLine[2], Double.parseDouble(nextLine[4]),Double.parseDouble(nextLine[5]),"Lisbon",-1,"Lisbon",-1,"Lisbon","-1","Lisbon",new ArrayList<>(),new ArrayList<>());
                stop.init();
                stop.getScheduleList().add(new Schedule(path.getId(), arrival_time, "-1"));
                stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(Integer.parseInt(lineId),lineId, directionId, pathId, directionHeadSign, path.getStop_sequence(), arrival_time,"-1"));
                path.setStop(stop);
            }
            scanner.close();
        } catch (Exception e) {
            Log.e("ERROR IN OFFLINE", e.getMessage());
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
            stop.getOfflineRealTimeSchedules().add(new RealTimeSchedule(Integer.parseInt(lineId),lineId, directionId, stop.getStopID()+"", directionHeadSign, path.getStop_sequence(), arrival_time,"-1"));
        }
    }


}
