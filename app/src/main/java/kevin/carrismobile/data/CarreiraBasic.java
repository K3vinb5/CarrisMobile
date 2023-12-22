package kevin.carrismobile.data;

import androidx.annotation.NonNull;

public class CarreiraBasic {

    private String id;
    private String long_name;
    private String color;

    public CarreiraBasic(String id, String long_name, String color) {
        this.id = id;
        this.long_name = long_name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLong_name() {
        return long_name;
    }

    public String getColor() {
        return color;
    }

    public static CarreiraBasic getExample(){
        return new CarreiraBasic("0", "Example", "#ED1944");
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public static CarreiraBasic newCarreiraBasicFromCarreira(Carreira carreira){
        return new CarreiraBasic(carreira.getRouteId(), carreira.getName(), carreira.getColor());
    }

    @NonNull
    @Override
    public String toString() {
        return this.getId() + " - " + this.getLong_name();
    }
}
