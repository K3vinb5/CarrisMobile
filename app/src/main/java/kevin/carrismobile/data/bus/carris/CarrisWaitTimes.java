package kevin.carrismobile.data.bus.carris;

import androidx.annotation.NonNull;

public class CarrisWaitTimes {

    private String routeId;
    private String routeName;
    private String destinantionName;
    private String waitingTime;

    public CarrisWaitTimes(String routeId, String routeName, String destinantionName, String waitingTime) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.destinantionName = destinantionName;
        this.waitingTime = waitingTime;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getDestinantionName() {
        return destinantionName;
    }

    public void setDestinantionName(String destinantionName) {
        this.destinantionName = destinantionName;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }

    @NonNull
    @Override
    public String toString() {
        return this.routeId + ": " + this.destinantionName + " - " + this.waitingTime;
    }
}
