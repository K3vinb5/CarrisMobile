package kevin.carrismobile.data.bus;
import java.io.Serializable;
import java.sql.Time;

public class Schedule implements Serializable {

    private String stop_id;
    private String arrival_time;
    private String travel_time;

    public Schedule(String stop_id, String arrival_time, String travel_time) {
        this.stop_id = stop_id;
        this.arrival_time = arrival_time;
        this.travel_time = travel_time;
    }

    public String getScheduleStopId() {
        return stop_id;
    }
    public String getArrival_time() {
        return arrival_time;
    }
    public String getStopId() {
        return stop_id;
    }

    @Override
    public String toString() {
        return arrival_time;
    }
}
