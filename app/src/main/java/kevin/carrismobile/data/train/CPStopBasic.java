package kevin.carrismobile.data.train;

public class CPStopBasic {

    String node;
    String node_name;
    double lat;
    double lon;

    public CPStopBasic(String node, String node_name, double lat, double lon) {
        this.node = node;
        this.node_name = node_name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getNode_name() {
        return node_name;
    }

    public void setNode_name(String node_name) {
        this.node_name = node_name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
