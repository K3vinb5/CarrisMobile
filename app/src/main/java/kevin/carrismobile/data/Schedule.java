package kevin.carrismobile.data;
import java.io.Serializable;
import java.sql.Time;

public class Schedule implements Serializable {

    private int stop_id;
    private String arrival_time;
    private String travel_time;

    public Schedule(int stop_id, String arrival_time, String travel_time) {
        this.stop_id = stop_id;
        this.arrival_time = arrival_time;
        this.travel_time = travel_time;
    }

    public int getScheduleStopId() {
        return stop_id;
    }

    public int getStopId() {
        return stop_id;
    }

    @Override
    public String toString() {
        return arrival_time;
    }
}
