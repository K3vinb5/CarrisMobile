package data_structure;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import api.Api;

public class Carreira implements Serializable {

    private int id;
    private String long_name;
    private List<Direction> directionList;
    private List<String> patterns = new ArrayList<>();
    private List<String> routes = new ArrayList<>();

    public Carreira(String long_name, int id){
        this.long_name = long_name;
        this.id = id;
    }

    public String getName() {
        return long_name;
    }

    public int getRouteId() {
        return id;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void init(){
        directionList = new ArrayList<>();
        for (String pattern : patterns){
            directionList.add(Api.getDirection(pattern));
        }
    }

    public void updateSchedulesInStops(){
        for (Direction direction : this.getDirectionList()){
            for (Path path : direction.getPathList()){
                for (Trip trip : direction.getTrips()){
                    for (Schedule schedule : trip.getSchedules()){
                        if ( path.getStop().getStopID() == schedule.getStopId() /*&& !path.getStop().containsSchedule(schedule)*/){
                            path.getStop().getScheduleList().add(schedule);
                        }
                    }
                }
            }
        }
    }

    public void updatePathsOnSelectedDirection(int directionId){
        Direction currentDirection = this.getDirectionList().get(directionId);
        List <Path> currentPathList = currentDirection.getPathList();
        List <Trip> currentTripList = currentDirection.getTrips();
        for (int i = 0; i < currentPathList.size(); i++){
            currentPathList.get(i).getStop().init();
            List<Schedule> schedules = new ArrayList<>();
            for (int j = 0; j < currentTripList.size(); j++){
                schedules.add(currentTripList.get(j).getScheduleAtSelectedStop(i));
            }
            currentPathList.get(i).getStop().getScheduleList().addAll(schedules);
        }
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
        return new Carreira("Carreira", -1);
    }

    @Override
    public String toString() {
        return id + " - " + long_name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Carreira carreiraToCompare = (Carreira) obj;
        return this.getRouteId() == carreiraToCompare.getRouteId();
    }
}
