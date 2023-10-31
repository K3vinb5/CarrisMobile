package data_structure;

import androidx.annotation.NonNull;

public class CarreiraBasic {

    private int id;
    private String long_name;
    private String color;

    public CarreiraBasic(int id, String long_name, String color) {
        this.id = id;
        this.long_name = long_name;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLong_name() {
        return long_name;
    }

    public String getColor() {
        return color;
    }

    public static CarreiraBasic getExample(){
        return new CarreiraBasic(0, "Example", "#ED1944");
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public static CarreiraBasic newCarreiraBasicFromCarreira(Carreira carreira){
        return new CarreiraBasic(carreira.getRouteId(), carreira.getName(), "000000");
    }

    @NonNull
    @Override
    public String toString() {
        return this.getId() + " - " + this.getLong_name();
    }
}
