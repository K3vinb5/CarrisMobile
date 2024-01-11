package kevin.carrismobile.gui;

import java.util.List;

import kevin.carrismobile.data.bus.Stop;

public class StopThread extends Thread{

    private double lat1, lon1, lat2, lon2;
    private List<Stop> stopList;
    private Stop stop;
    public StopThread(double[] coordinates1, double[] coordinates2, List<Stop> stopList, Stop stop) {
        this.lat1 = coordinates1[0];
        this.lon1 = coordinates1[1];
        this.lat2 = coordinates2[0];
        this.lon2 = coordinates2[1];
        this.stopList = stopList;
        this.stop = stop;
    }

    @Override
    public void run() {
        double distance = this.calcCrow();
        //less than 1km I'm hoping
        if (distance < 1.5d){
            stopList.add(stop);
        }
    }

    public double calcCrow()
    {
        float R = 6371; // km
        double dLat = toRad(lat2-lat1);
        double dLon = toRad(lon2-lon1);
        double radlat1 = toRad(lat1);
        double radlat2 = toRad(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(radlat1) * Math.cos(radlat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public static double toRad(double d)
    {
        return d * Math.PI / 180;
    }
}
