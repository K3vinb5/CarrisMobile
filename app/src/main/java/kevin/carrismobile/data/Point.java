package kevin.carrismobile.data;

public class Point {

    double shape_pt_lat;
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
