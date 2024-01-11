package kevin.carrismobile.data.metro;

import java.util.List;

public class MetroWaitTime {
    MetroStop destination;
    boolean live;
    List<MetroArrivalTimes> arrivalTimes;

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public List<MetroArrivalTimes> getArrivalTimes() {
        return arrivalTimes;
    }

    public void setArrivalTimes(List<MetroArrivalTimes> arrivalTimes) {
        this.arrivalTimes = arrivalTimes;
    }

    public MetroStop getDestination() {
        return destination;
    }

    public void setDestination(MetroStop destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return destination + "->" + arrivalTimes;
    }
}
