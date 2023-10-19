package data_structure;

public class RealTimeSchedule {

    private int line_id;
    private String route_id;
    private String pattern_id;
    private String trip_id;
    private String headsign;
    private int stop_sequence;
    private String scheduled_arrival;
    private String vehicle_id;

    public RealTimeSchedule(int line_id, String route_id, String pattern_id, String trip_id, String headsign, int stop_sequence, String scheduled_arrival, String vehicle_id) {
        this.line_id = line_id;
        this.route_id = route_id;
        this.pattern_id = pattern_id;
        this.trip_id = trip_id;
        this.headsign = headsign;
        this.stop_sequence = stop_sequence;
        this.scheduled_arrival = scheduled_arrival;
        this.vehicle_id = vehicle_id;
    }

    public int getLine_id() {
        return line_id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public String getPattern_id() {
        return pattern_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public String getHeadsign() {
        return headsign;
    }

    public int getStop_sequence() {
        return stop_sequence;
    }

    public String getScheduled_arrival() {
        return scheduled_arrival;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    @Override
    public String toString() {
        return  line_id + " - " + headsign + " - " + scheduled_arrival;
    }

}
