package kevin.carrismobile.data;

import java.io.Serializable;

public class Path implements Serializable {

    private final String id;
    private int stop_sequence;
    private boolean allow_pickup;
    private boolean allow_drop_off;
    private float distance_delta;
    private Stop stop;

    public Path(String id, int stop_sequence, boolean allow_pickup, boolean allow_drop_off, float distance_delta) {
        this.id = id;
        this.stop_sequence = stop_sequence;
        this.allow_pickup = allow_pickup;
        this.allow_drop_off = allow_drop_off;
        this.distance_delta = distance_delta;
    }

    public String getId() {
        return id;
    }

    public int getStop_sequence() {
        return stop_sequence;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public Stop getStop() {
        return stop;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Path){
            return ((Path)obj).getId().equals(this.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return stop_sequence + " - " + getStop().toString();
    }
}
