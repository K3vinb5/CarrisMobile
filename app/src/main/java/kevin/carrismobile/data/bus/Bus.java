package kevin.carrismobile.data.bus;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Bus {
    @SerializedName(value = "id", alternate = {"plateNumber"})
    private String id;
    private double lat;
    private double previousLatitude;
    private double previousLongitude;
    @SerializedName(value = "lon", alternate = {"lng"})
    private double lon;
    private String speed;
    @SerializedName(value = "status", alternate = {"state"})
    private String status;
    private double heading;
    private String trip_id;
    @SerializedName(value = "pattern_id", alternate = {"direction"})
    private String pattern_id;
    private String pattern_name = "Not settedw";
    private String timestamp;

    public Bus(String id, double lat, double lon, String speed, double heading, String trip_id, String pattern_id, String timestamp) {
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
    public double getHeading() {
        if (heading == 0){
            return calculateHeading(new double[]{previousLatitude, previousLongitude}, new double[]{lat, lon});
        }
        return heading;
    }
    public static double calculateHeading(double[] prevCoordinates, double[] currentCoordinates) {
        double prevLatRad = Math.toRadians(prevCoordinates[0]);
        double prevLonRad = Math.toRadians(prevCoordinates[1]);
        double currentLatRad = Math.toRadians(currentCoordinates[0]);
        double currentLonRad = Math.toRadians(currentCoordinates[1]);

        double deltaLon = currentLonRad - prevLonRad;

        double y = Math.sin(deltaLon) * Math.cos(currentLatRad);
        double x = Math.cos(prevLatRad) * Math.sin(currentLatRad) - Math.sin(prevLatRad) * Math.cos(currentLatRad) * Math.cos(deltaLon);
        double headingRad = Math.atan2(y, x);

        double headingDegrees = Math.toDegrees(headingRad);
        headingDegrees = (headingDegrees + 360) % 360;

        return headingDegrees;
    }
    public String getSpeed() {
        if (speed == null){
            return "-1";
        }
        return speed;
    }

    public String getStatus() {
        return status;
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
    public String getTimestamp() {
        return timestamp;
    }
    @Override
    public String toString() {
        return id;
    }
}
