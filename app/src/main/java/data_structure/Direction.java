package data_structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Direction implements Serializable {

    private String id;
    private String headsign;
    private List<Path> path = new ArrayList<>();
    private List<Trip> trips = new ArrayList<>();

    public Direction(String id, String headsign){
        this.id = id;
        this.headsign = headsign;
    }

    public String getDirectionId() {
        return id;
    }

    public List<Path> getPathList() {
        return path;
    }

    public String getHeadsign() {
        return headsign;
    }

    public boolean isCorrectHeadsign(String pattern){
        return pattern.equals(id);
    }

    public List<Trip> getTrips() {
        return trips;
    }

    @Override
    public String toString() {
        return headsign + " - " + id;
    }
}
