package kevin.carrismobile.data.bus;

import com.google.gson.annotations.SerializedName;

public class Point {
    @SerializedName(value = "shape_pt_lat", alternate = {"lat"})
    double shape_pt_lat;
    @SerializedName(value = "shape_pt_lon", alternate = {"lng"})
    double shape_pt_lon;

    public Point(double lat, double lon){
        shape_pt_lat = lat;
        shape_pt_lon = lon;
    }
    public double getLat() {
        return shape_pt_lat;
    }

    public double getLon() {
        return shape_pt_lon;
    }
}
