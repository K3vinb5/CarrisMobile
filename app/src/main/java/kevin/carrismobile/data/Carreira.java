package kevin.carrismobile.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.List;
import kevin.carrismobile.api.*;
public class Carreira implements Serializable {

    private String id;
    public boolean online;
    private String long_name;
    private String color;
    private List<Direction> directionList;
    private List<String> patterns = new ArrayList<>();
    private List<String> routes = new ArrayList<>();

    public Carreira(String long_name, String id, String color){
        this.long_name = long_name;
        this.id = id;
        this.color = color;
    }

    public String getName() {
        return long_name;
    }
    public String getRouteId() {
        return id;
    }
    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }
    public List<String> getPatterns() {
        return patterns;
    }
    public List<String> getRoutes() {
        return routes;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public boolean isOnline() {
        return online;
    }
    public void setDirectionList(List<Direction> directionList) {
        this.directionList = directionList;
    }
    public void init(){
        directionList = new ArrayList<>();
        int index = 0;
        for (String pattern : patterns){
            directionList.add(Api.getDirection(pattern, id, index));
            index++;
        }
    }

    public void updateSchedulesOnStopOnGivenDirectionAndStop(int directionIndex, int stopIndex) throws IllegalStateException{
        Carreira currentCarreira = this;
        Direction currentDirection = currentCarreira.getDirectionList().get(directionIndex);
        //Log.d("UpdatePathsOnSelectedDirection was called", "Starting updatePathsOnSelectedDirection");
        Stop stopToUpdate = currentDirection.getPathList().get(stopIndex).getStop();
        stopToUpdate.init();
        List<Schedule> schedules = stopToUpdate.getScheduleList();
        List<RealTimeSchedule> realTimeSchedules = new ArrayList<>();
        realTimeSchedules.addAll(getSchedules(stopToUpdate.getStopID(), 0,5));
        //RouteDetails will make sure to catch the error (I hope so)
        realTimeSchedules.removeIf(realTimeSchedule -> !realTimeSchedule.getPattern_id().equals(currentDirection.getDirectionId()));
        //TODO travel_time not being assigned correct
        realTimeSchedules.forEach(realTimeSchedule -> schedules.add(new Schedule(stopToUpdate.getStopID(), realTimeSchedule.getScheduled_arrival(), "")));
        //Log.e("Stop Schedule added", schedules.toString());
    }

    public static List<RealTimeSchedule> getSchedules(String stopId, int attempt, int maxAttempt) throws IllegalArgumentException {
        if (attempt == maxAttempt){
            throw new IllegalArgumentException("Attempt has reached maxAttempt");
        }
        String newStopId = stopId+"";
        while(newStopId.length() < 6){
            newStopId = "0" + newStopId;
        }
        List<RealTimeSchedule> realTimeScheduleList = new ArrayList<>();
        try{
            realTimeScheduleList = Api.getRealTimeStops(newStopId);
        }catch (Exception e){
            return getSchedules(stopId, attempt + 1, maxAttempt);
        }
        return realTimeScheduleList;
    }

    public List<Direction> getDirectionList() {
        return directionList;
    }

    public void saveCarreira(){

        try {
            File file = new File("./saves/carreiras/" + this.getRouteId() + ".bin");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            //TODO poput telling it went wrong
            throw new RuntimeException(e);
        }
    }

    public static Carreira createStopFromFile(String path){
        try{
            FileInputStream fos = new FileInputStream(new File(path));
            ObjectInputStream obs = new ObjectInputStream(fos);
            return (Carreira) obs.readObject();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    /*public static void main(String[] args) {
        String path = "./saves/carreiras/2202.bin";
        Carreira carreira = Carreira.createStopFromFile(path);
    }*/

    public static Carreira generateCarreiraPlaceHolder(){
        return new Carreira("Carreira", "-1", "#000000");
    }
    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return id + " - " + long_name;
    }

    @Override
    public boolean equals(Object obj) {
        Carreira carreiraToCompare = (Carreira) obj;
        return this.getRouteId() == carreiraToCompare.getRouteId();
    }
}
