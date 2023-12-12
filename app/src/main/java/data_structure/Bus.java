package data_structure;

import java.util.HashMap;
import java.util.Map;

public class Bus {
    private String id;
    private double lat;
    private double lon;
    private String speed;
    private String status;
    private double heading;
    private String trip_id;
    private String pattern_id;
    private String pattern_name = "Not settedw";
    private long timestamp;

    public Bus(String id, double lat, double lon, String speed, double heading, String trip_id, String pattern_id, long timestamp) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.heading = heading;
        this.trip_id = trip_id;
        this.pattern_id = pattern_id;
        this.timestamp = timestamp;
    }

    public String getVehicleId(){
        return this.id;
    }

    public String getPattern_id() {
        return pattern_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public double getLat(){
        return  lat;
    }

    public double getLon(){
        return lon;
    }

    public double getHeading() {
        return heading;
    }

    public int getIntHeading(){
        return (int)Math.round(heading);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpeed() {
        return speed;
    }

    public String getStatus() {
        return status;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setPattern_id(String pattern_id) {
        this.pattern_id = pattern_id;
    }

    public double[] getCoordinates(){
        double[] coordinates = new double[2];
        coordinates[0] = lat;
        coordinates[1] = lon;
        return coordinates;
    }

    public String getPattern_name() {
        return pattern_name;
    }

    public void setPattern_name(String pattern_name) {
        this.pattern_name = pattern_name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return id;
    }
}
