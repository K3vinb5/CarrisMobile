package kevin.carrismobile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Direction implements Serializable {

    private int directionIndexInRoute;
    private String routeId;
    private double heuristic; //used in journey Algorithm

    private String id;
    private String headsign;
    private String shape_id;
    private List<Path> path = new ArrayList<>();
    private List<Trip> trips = new ArrayList<>();

    public Direction(String id, String headsign){
        this.id = id;
        this.headsign = headsign;
    }

    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public int getDirectionIndexInRoute() {
        return directionIndexInRoute;
    }

    public void setDirectionIndexInRoute(int directionIndexInRoute) {
        this.directionIndexInRoute = directionIndexInRoute;
    }
    public void addTrip(Trip trip){
        trips.add(trip);
    }
    public double getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(double heuristic) {
        if(this.heuristic == 0){
            this.heuristic = 999999;
        }
        if(heuristic < this.heuristic){
            this.heuristic = heuristic;
        }
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

    public String getShape_id() {
        return shape_id;
    }

    @Override
    public String toString() {
        return headsign + " - " + id;
    }
}
