package kevin.carrismobile.data.bus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.api.CarrisMetropolitanaApi;

public class Stop implements Serializable {

    private String id;
    private String agency_id;
    private boolean online;
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
    private List<String> lines;
    private List<String> facilities;
    private List<RealTimeSchedule> realTimeSchedules = new ArrayList<>();
    private List<Schedule> scheduleList = new ArrayList<>();
    private boolean init = false;


    public Stop(String id, String name, String tts_name, double lat, double lon, String locality, int municipality_id, String municipality_name, int district_id, String district_name, String region_id, String region_name, List<String> lines, List<String> facilities) {
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
        this.lines = lines;
        this.facilities = facilities;
    }
    public Stop(String id, String name, double lat, double lon){
        this.id = id;
        this.name = name;
        this.tts_name = name;
        this.lat = lat;
        this.lon = lon;
        this.locality = "-1";
        this.municipality_id = -1;
        this.municipality_name = "Lisbon";
        this.district_id = -1;
        this.district_name = "Lisbon";
        this.region_id = "-1";
        this.region_name = "Lisbon";
        this.lines = new ArrayList<>();
        this.facilities = new ArrayList<>();
    }
    public String getStopID() {
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
    public boolean isOnline() {
        return online;
    }

    public String getAgency_id() {
        return agency_id;
    }

    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }

    public void setOnline(boolean online) {
        this.online = online;
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

    public String getLines(){
        if (lines == null){
            lines = CarrisMetropolitanaApi.getLinesFromStop(this.id+"");
        }
        StringBuilder sb = new StringBuilder();
        for (String s : getLinesObject()){
            sb.append("\n").append(s);
        }
        return sb.toString();
    }

    public List<String> getLinesObject(){
        List<String> out = new ArrayList<>();
        if (lines != null){
            return  lines;
        }else{
            return out;
        }
    }
    public List<RealTimeSchedule> getOfflineRealTimeSchedules(){
        return realTimeSchedules;
    }
    public List<RealTimeSchedule> getRealTimeSchedules(){
        if (realTimeSchedules!= null && !realTimeSchedules.isEmpty()){
            return  realTimeSchedules;
        }else{
            List<RealTimeSchedule> newRealTimeSchedulesList;
            try{
                String callId = id;
                while (callId.length() < 6){
                    callId = "0" + callId;
                }
                newRealTimeSchedulesList = CarrisMetropolitanaApi.getRealTimeStops(callId);
            }catch (Exception e){
                //Log.d("API ERROR", "ERROR RETRIEVING SCHEDULES");
                return new ArrayList<RealTimeSchedule>();
            }
            return  newRealTimeSchedulesList;
        }
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistrict_name() {
        return district_name;
    }


    @Override
    public String toString() {
        return name + " - " + locality;
    }

    @Override
    public boolean equals(Object obj) {
        Stop stopCompare = (Stop) obj;
        return this.getStopID().equals(stopCompare.getStopID());
    }
}
