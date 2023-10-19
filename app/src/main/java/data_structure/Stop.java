package data_structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import api.Api;

public class Stop implements Serializable {

    private int id;
    private String name;
    private String tts_name;
    private double lat;
    private double lon;
    private String locality;
    private int municipality_id;
    private String municipality_name;
    private int district_id;
    private String district_name;
    private String region_id;
    private String region_name;
    private List<Schedule> scheduleList;
    private boolean init = false;


    public Stop(int id, String name, String tts_name, double lat, double lon, String locality, int municipality_id, String municipality_name, int district_id, String district_name, String region_id, String region_name) {
        this.id = id;
        this.name = name;
        this.tts_name = tts_name;
        this.lat = lat;
        this.lon = lon;
        this.locality = locality;
        this.municipality_id = municipality_id;
        this.municipality_name = municipality_name;
        this.district_id = district_id;
        this.district_name = district_name;
        this.region_id = region_id;
        this.region_name = region_name;
    }

    public int getStopID() {
        return id;
    }

    public double[] getCoordinates(){
        double[] list = new double[2];
        list[0] = lat;
        list[1] = lon;
        return list;
    }

    public String getTts_name() {
        return tts_name;
    }

    public String getLocality() {
        return locality;
    }

    public String getMunicipality_name() {
        return municipality_name;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void init(){
        if (!init){
            init = true;
            scheduleList = new ArrayList<>();
        }
    }

    /*public int containsSchedule(Schedule schedule){
        int out = 0;
        for (Schedule sc : scheduleList){
            if (sc.getArrivalTime().toString().equals(schedule.getArrivalTime().toString())){
                out ++;
            }
        }
        return out;
    }*/

    public void saveStop(){

        try {
            FileOutputStream fos = new FileOutputStream("./saves/stops/" + this.getStopID() + ".bin");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            //TODO poput telling it went wrong
            throw new RuntimeException(e);
        }
    }

    public Stop createStopFromFile(File file){

        try{
            FileInputStream fos = new FileInputStream(file);
            ObjectInputStream obs = new ObjectInputStream(fos);
            return (Stop)obs.readObject();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " - " + locality;
    }
}
