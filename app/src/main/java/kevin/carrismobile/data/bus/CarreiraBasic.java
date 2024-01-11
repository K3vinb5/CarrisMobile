package kevin.carrismobile.data.bus;

import androidx.annotation.Nullable;

public class CarreiraBasic {

    private String id;
    private String agency_id;
    private boolean online;
    private String long_name;
    private String color;

    public CarreiraBasic(String id, String long_name, String color, boolean isOnline) {
        this.id = id;
        this.long_name = long_name;
        this.color = color;
        this.online = isOnline;
    }

    public String getRouteId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLong_name() {
        return long_name;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }
    public String getAgency_id() {
        return agency_id;
    }
    public boolean isOnline() {
        return online;
    }
    public String getColor() {
        return color;
    }
    public static CarreiraBasic getExample(){
        return new CarreiraBasic("0", "Example", "#ED1944", false);
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public static CarreiraBasic newCarreiraBasicFromCarreira(Carreira carreira){
        return new CarreiraBasic(carreira.getRouteId(), carreira.getName(), carreira.getColor(), carreira.isOnline());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof CarreiraBasic){
            return ((CarreiraBasic)obj).getRouteId().equals(this.getRouteId());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getRouteId() + " - " + this.getLong_name();
    }
}
