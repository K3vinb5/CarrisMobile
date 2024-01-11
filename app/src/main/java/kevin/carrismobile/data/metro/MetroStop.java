package kevin.carrismobile.data.metro;

import java.util.Arrays;
import java.util.List;

public class MetroStop {
    private String id;
    private String name;
    private double lat;
    private double lon;
    private List<String> lines;
    private List<MetroWaitTime> waitTimes;

    public MetroStop(String id, String name, double lat, double lon, List<String> lines, List<MetroWaitTime> waitTimes) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.lines = lines;
        this.waitTimes = waitTimes;
    }

    public String getStop_id() {
        return id;
    }

    public void setStop_id(String stop_id) {
        this.id = stop_id;
    }

    public String getStop_name() {
        return name;
    }

    public void setStop_name(String stop_name) {
        this.name = stop_name;
    }

    public double getStop_lat() {
        return lat;
    }

    public void setStop_lat(double stop_lat) {
        this.lat = stop_lat;
    }

    public double getStop_lon() {
        return lon;
    }

    public void setStop_lon(double stop_lon) {
        this.lon = stop_lon;
    }

    public List<String> getLinhas() {
        return lines;
    }

    public List<MetroWaitTime> getWaitTimes() {
        return waitTimes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetroStop){
            return ((MetroStop)obj).getStop_id().equals(this.getStop_id());
        }
        return false;
    }

    @Override
    public String toString() {
        if (waitTimes!= null){
            return id + "-" + name + " -> " + lat + "," + lon + ":" + waitTimes;
        }
        return id + "-" + name;
    }
}
