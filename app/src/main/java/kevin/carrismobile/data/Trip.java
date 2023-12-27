package kevin.carrismobile.data;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable {

    private String id;
    private String calendar_id;
    private List<Schedule> schedule = new ArrayList<>();

    public Trip(String id, String calendar_id){
        this.id = id;
        this.calendar_id = calendar_id;
    }

    public String getTripId() {
        return id;
    }

    public String getCalendar_id() {
        return calendar_id;
    }

    public List<Schedule> getSchedules() {
        return schedule;
    }

    public Schedule getScheduleAtSelectedStop(int stopSequence){
        return schedule.get(stopSequence);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String){
            return this.getTripId().equals(obj);
        }
        if (obj instanceof Trip){
            return this.getTripId().equals(((Trip)obj).getTripId());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getTripId();
    }
}
