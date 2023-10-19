package data_structure;

public class CarreiraBasic {

    private int id;
    private String long_name;

    public CarreiraBasic(int id, String long_name) {
        this.id = id;
        this.long_name = long_name;
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

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }
}
