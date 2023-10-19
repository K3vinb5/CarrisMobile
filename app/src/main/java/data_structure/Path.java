package data_structure;

import java.io.Serializable;

public class Path implements Serializable {

    private final int id;
    private int stop_sequence;
    private boolean allow_pickup;
    private boolean allow_drop_off;
    private float distance_delta;
    private Stop stop;

    public Path(int id, int stop_sequence, boolean allow_pickup, boolean allow_drop_off, float distance_delta) {
        this.id = id;
        this.stop_sequence = stop_sequence;
        this.allow_pickup = allow_pickup;
        this.allow_drop_off = allow_drop_off;
        this.distance_delta = distance_delta;
    }

    public Stop getStop() {
        return stop;
    }

    @Override
    public String toString() {
        return stop_sequence + " - " + getStop().toString();
    }
}
