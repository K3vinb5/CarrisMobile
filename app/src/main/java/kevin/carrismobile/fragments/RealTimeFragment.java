package kevin.carrismobile.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.carrismobile.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.adaptors.StopImageListAdaptor;
import kevin.carrismobile.api.CarrisApi;
import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.api.Offline;
import kevin.carrismobile.api.RealCarrisApi;
import kevin.carrismobile.data.bus.Bus;
import kevin.carrismobile.data.bus.Carreira;
import kevin.carrismobile.data.bus.Direction;
import kevin.carrismobile.data.bus.Path;
import kevin.carrismobile.data.bus.Point;
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.gui.BusBackgroundThread;
import kevin.carrismobile.gui.CustomMarkerInfoWindow;
import kevin.carrismobile.custom.MyCustomDialog;

public class RealTimeFragment extends Fragment {

    public static MapView map;
    public static Activity activity;
    Button button;
    public static Button nextButton;
    public static TextView textView;
    public static Button previousButton;
    EditText editText;
    public static CheckBox checkBox;
    public BusBackgroundThread backgroundThread;
    public Lock lock = new ReentrantLock();
    public boolean backgroundThreadStarted = false;
    public static List<Bus> busList = Collections.synchronizedList(new ArrayList<Bus>());
    public static List<Path> pathList = Collections.synchronizedList(new ArrayList<Path>());
    public static List<Marker> markerList = Collections.synchronizedList(new ArrayList<Marker>());
    public static List<Marker> markerBusList = Collections.synchronizedList(new ArrayList<Marker>());
    public static int currentSelectedBus = 0;
    public int currentDirectionIndex = 0;
    public boolean connected = false;
    public Polyline line = null;
    public AlertDialog dialog;
    public AlertDialog backgroundDialog;
    public AlertDialog noCurrentBusesDialog;
    public static Carreira currentCarreira;
    public static String currentLine = "";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.realtime_fragment, container, false);

        activity = getActivity();
        map = v.findViewById(R.id.mapview);
        button = v.findViewById(R.id.button2);
        previousButton = v.findViewById(R.id.previousButton);
        nextButton = v.findViewById(R.id.nextButton);
        editText = v.findViewById(R.id.editText2);
        textView = v.findViewById(R.id.textViewRealTimeFragment);
        checkBox = v.findViewById(R.id.checkBox);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(SettingsFragment.getCurrentTileProvider(getContext()));

        map.setMultiTouchControls(true);
        map.getController().setCenter(new GeoPoint(38.73329737648646d, -9.14096412687648d));
        map.getController().setZoom(13.0);
        map.invalidate();
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), map);
        compassOverlay.enableCompass();
        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet.\nPode também haver um problema com os servidores da Carris Metropolitana");
        backgroundDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Error in background thread");
        noCurrentBusesDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não existe nenhum autocarro dessa carreira em circulação neste preciso momento");
        setButtonListener();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lock.lock();
                if (currentSelectedBus < busList.size() - 1){
                    currentSelectedBus ++;
                    updateTextView();
                    updateDirectionIndex();
                    updateMarkers(currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList(), map);
                    updateBusesUI();
                    Log.println(Log.DEBUG, "Button", "Current Bus: " + currentSelectedBus);
                    getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //textView.setText((currentSelectedBus + 1) + "/" + busList.size() + "\n" + busList.get(currentSelectedBus).getStatus());
                                GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                                if (checkBox.isChecked()){
                                    map.getController().animateTo(point, 16.5, 2500L);
                                }
                                map.invalidate();
                            }
                        });
                }
                lock.unlock();
            }

        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lock.lock();
                if (currentSelectedBus > 0){
                    currentSelectedBus --;
                    updateTextView();
                    updateDirectionIndex();
                    updateMarkers(currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList(), map);
                    updateBusesUI();
                    Log.println(Log.DEBUG, "Button", "Current Bus: " + currentSelectedBus);
                    getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                                if (checkBox.isChecked()){
                                    map.getController().animateTo(point, 16.5, 2500L);
                                }
                                map.invalidate();
                            }
                        });
                }
                lock.unlock();
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()){
                    GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                    map.getController().animateTo(point, 16.5, 2500L);
                }else{
                    map.getController().stopAnimation(false);
                }
            }
        });

        return v;
    }

    private void setButtonListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception ignored) {

                }
                loadNewRoute(editText.getText().toString(), false);
            }
        });
    }

    public void loadNewRoute(String line, boolean isThread) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                List<Bus> listToAdd;
                Carreira carreira = null;
                if (!isThread){
                    currentLine = line;
                }
                try{
                    String agencyId = Offline.agencyServiceMap.get(currentLine) != null ? Offline.agencyServiceMap.get(currentLine) : "-1";
                    if (agencyId.equals("0")){
                        listToAdd = RealCarrisApi.getBusFromLine(currentLine);
                        if (noRunningBuses(listToAdd)) {
                            lock.unlock();
                            return;
                        }
                        listToAdd.sort(Comparator.comparing(Bus::getVehicleId));
                        if (!isThread){
                            carreira = CarrisApi.getCarreira("", currentLine);
                            if(backgroundThreadStarted){
                                backgroundThread.setCarreira(carreira);
                            }
                        }else if(backgroundThreadStarted){
                            carreira = backgroundThread.getCarreira();
                        }
                        List<Direction> carreiraDirectionList = carreira.getDirectionList();
                        for (Bus b : listToAdd){
                            switch (b.getPattern_id()) {
                                case "ASC":
                                    b.setPattern_name(carreiraDirectionList.get(0).getHeadsign());
                                    break;
                                case "DESC":
                                    b.setPattern_name(carreiraDirectionList.get(1).getHeadsign());
                                    break;
                                case "CIRC":
                                    b.setPattern_name(carreiraDirectionList.get(0).getHeadsign());
                                    break;
                            }
                        }
                    }else{
                        listToAdd = CarrisMetropolitanaApi.getBusFromLine(currentLine);
                        if (noRunningBuses(listToAdd)) {
                            lock.unlock();
                            return;
                        }
                        listToAdd.sort(Comparator.comparing(Bus::getVehicleId));
                        if(!isThread){
                            carreira = CarrisMetropolitanaApi.getCarreira(currentLine);
                            carreira.init();
                            if(backgroundThreadStarted){
                                backgroundThread.setCarreira(carreira);
                            }
                        }else if (backgroundThreadStarted){
                            carreira = backgroundThread.getCarreira();
                        }
                        for (Bus b : listToAdd){
                            for(Direction d : carreira.getDirectionList()){
                                if (d.isCorrectHeadsign(b.getPattern_id())){
                                    b.setPattern_name(d.getHeadsign());
                                }
                            }
                        }
                    }
                    currentCarreira = carreira;
                    if (!isThread){
                        currentDirectionIndex = 0;
                        currentSelectedBus = 0;
                    }
                    List<Path> pathListToAdd = currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList();
                    busList.clear();
                    pathList.clear();
                    busList.addAll(listToAdd);
                    pathList.addAll(pathListToAdd);
                    connected = true;
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                    connected = false;
                    Log.e("REALTIME TRACKING", "Exception Caught: \n" + e.getMessage());
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isThread){
                            updateTextView();
                            updateDirectionIndex();
                            updateMarkers(pathList, map);
                        }
                        updateBusesUI();
                        if (!isThread){
                            GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                            map.getController().animateTo(point, 16.5, 1500L);
                        }else{
                            if (checkBox.isChecked()){
                                map.getController().animateTo(markerBusList.get(currentSelectedBus).getPosition(), 16.5, 2500L);
                            }
                        }
                    }
                });
                lock.unlock();

                if(!backgroundThreadStarted && connected){
                    backgroundThread = new BusBackgroundThread(RealTimeFragment.this, currentCarreira);
                    backgroundThreadStarted = true;
                    backgroundThread.start();
                }
            }
        }).start();
    }

    private boolean noRunningBuses(List<Bus> listToAdd) {
        if (listToAdd.size() == 0){
            Log.e("REALTIME TRACKING", "Bus List Invalid (Empty)");
            connected = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    noCurrentBusesDialog.show();
                }
            });
            return true;
        }else{
            Log.d("REALTIME TRACKING", "Bus List Valid");
        }
        return false;
    }

    private void updateDirectionIndex() {
        int newIndex = 0;
        String directionId = busList.get(currentSelectedBus).getPattern_id();
        if (!currentCarreira.getAgency_id().equals("0")){
            for (Direction direction : currentCarreira.getDirectionList()){
                if(direction.getDirectionId().equals(directionId)){
                    currentDirectionIndex = newIndex;
                }
                newIndex++;
            }
        }else{
            String direction = busList.get(currentSelectedBus).getPattern_id();
            if (direction.equals("ASC")){
                currentDirectionIndex = 0;
            }
            else if (direction.equals("DESC")){
                currentDirectionIndex = 1;
            }
            else if (direction.equals("CIRC")){
                currentDirectionIndex = 0;
            }
        }

    }

    private void updateMarkers(List<Path> pathList, MapView map){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }
        markerList.clear();
        List<GeoPoint> geoPointList = new ArrayList<>();
        for (Path path : pathList){
            Stop s = path.getStop();
            double[] coordinates = s.getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            geoPointList.add(point);
            Marker marker = new Marker(map);
            Drawable d = StopImageListAdaptor.getImageId(s.getFacilities(), s.getTts_name(), s.getAgency_id(), getActivity());
            marker.setIcon(d);
            markerList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Stop Id: " + s.getStopID() + "\nLocality: " + s.getLocality() + "\nMunicipality: " + s.getMunicipality_name();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, s.getTts_name(), descrption);
            marker.setInfoWindow(miw);
        }
        if (line != null){
            map.getOverlays().remove(line);
        }
        line = new Polyline(map, true, false);
        if (currentCarreira.isOnline()){
            Direction currentDirection = currentCarreira.getDirectionList().get(currentDirectionIndex);
            List<Point> pointList = currentDirection.getPointList();
            pointList.forEach(point -> line.addPoint(new GeoPoint(point.getLat(), point.getLon())));
            String hexCode;
            try {
                hexCode = currentCarreira.getColor().substring(1);

            }catch (Exception ignore){
                hexCode = "000000";
            }
            int resultRed = Integer.valueOf(hexCode.substring(0, 2), 16);
            int resultGreen = Integer.valueOf(hexCode.substring(2, 4), 16);
            int resultBlue = Integer.valueOf(hexCode.substring(4, 6), 16);
            line.setColor(Color.rgb(resultRed, resultGreen, resultBlue));
            line.setWidth(7.5f);
            line.setInfoWindow(null);
            map.getOverlays().add(line);
            markerList.forEach(marker -> map.getOverlays().add(marker));
        }else{
            geoPointList.forEach(line::addPoint);
            map.getOverlays().add(line);
            markerList.forEach(marker -> map.getOverlays().add(marker));
        }
    }

    public static void updateBuses(List<Bus> busList, MapView map, Activity activity){

        for (Marker marker : markerBusList){
            map.getOverlays().remove(marker);
        }
        markerBusList.clear();
        int index = 0;
        for (Bus bus : busList){
            double[] coordinates = bus.getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d;
            if (index!=currentSelectedBus){
                d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.cm_bus, null);
            }else{
                d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.cm_bus_regular, null);
            }
            RotateDrawable d1 = new RotateDrawable();
            d1.setDrawable(d);
            d1.setFromDegrees(0f);
            d1.setToDegrees((float)bus.getHeading());
            d1.setLevel(10000);
            marker.setIcon(d1.getCurrent());
            markerBusList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Vehicle Id: " + bus.getVehicleId() + "\nSpeed: " + bus.getSpeed() + "\nPattern Id: " + bus.getPattern_id();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, "Bus " + bus.getVehicleId(), descrption);
            marker.setOnMarkerClickListener(new BusClickListener(miw));
            map.getOverlays().add(marker);
            index++;
        }
    }

    public static void updateBusesUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (busList){
                    updateBuses(busList, map, activity);
                }
            }
        });
    }

    public static void updateTextView(){
        textView.setText((currentSelectedBus + 1) + "/" + busList.size() + " - " + busList.get(currentSelectedBus).getStatus() + "\n" + busList.get(currentSelectedBus).getPattern_name());
    }

    public MapView getMap() {
        return map;
    }

    static class BusClickListener implements Marker.OnMarkerClickListener{
        MarkerInfoWindow miw;
        public BusClickListener(MarkerInfoWindow miw){
            this.miw=miw;
        }
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            int index = 0;
            marker.closeInfoWindow();
            for (Marker busMarker : markerBusList) {
                if (busMarker.equals(marker)){
                    if (currentSelectedBus != index){
                        currentSelectedBus = index + 1;
                        previousButton.performClick();
                    }
                    break;
                }
                index++;
            }
            marker.setInfoWindow(miw);
            marker.showInfoWindow();
            return false;
        }
    }
}