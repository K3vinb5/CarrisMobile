package kevin.carrismobile.data.bus;

import java.util.List;

public class Shape {

    String id;
    List<Point> points;

    public Shape(String id){
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }
}
