package kevin.carrismobile.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.adaptors.StopImageListAdaptor;
import kevin.carrismobile.api.Api;
import kevin.carrismobile.data.Bus;
import kevin.carrismobile.data.Carreira;
import kevin.carrismobile.data.Direction;
import kevin.carrismobile.data.Path;
import kevin.carrismobile.data.Point;
import kevin.carrismobile.data.Stop;
import kevin.carrismobile.gui.BusBackgroundThread;
import kevin.carrismobile.gui.CustomMarkerInfoWindow;
import kevin.carrismobile.custom.MyCustomDialog;

public class RealTimeFragment extends Fragment {

    public static MapView map;
    public static Activity activity;
    Button button;
    Button nextButton;
    public static TextView textView;
    Button previousButton;
    EditText editText;
    public static CheckBox checkBox;
    public BusBackgroundThread backgroundThread = new BusBackgroundThread();
    public boolean backgroundThreadStarted = false;
    public static List<Bus> busList = new ArrayList<>();
    public static List<Path> pathList = new ArrayList<>();
    public static List<Marker> markerList = new ArrayList<>();
    public static List<Marker> markerBusList = new ArrayList<>();
    public static int currentSelectedBus = 0;
    public int currentDirectionIndex = 0;
    public boolean connected = false;
    public Polyline line = null;
    public AlertDialog dialog;
    public AlertDialog backgroundDialog;
    public AlertDialog noCurrentBusesDialog;
    public static Carreira currentCarreira;
    public static String currentText = "";

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
        map.setTileSource(TileSourceFactory.OpenTopo);

        map.setMultiTouchControls(true);
        map.getController().setCenter(new GeoPoint(38.73329737648646d, -9.14096412687648d));
        map.getController().setZoom(13.0);
        map.invalidate();
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet.\nPode também haver um problema com os servidores da Carris Metropolitana");
        backgroundDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Error in background thread");
        noCurrentBusesDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não existe nenhum autocarro dessa carreira em circulação neste preciso momento");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception ignored) {

                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (backgroundThreadStarted){
                            backgroundThread.lockLock();
                        }
                        currentText = editText.getText().toString();
                        List<Bus> listToAdd;
                        Carreira carreira;
                        try{
                            listToAdd = Api.getBusFromLine(currentText);
                            Log.d("REALTIME TRACKING", "Bus List Loaded " + listToAdd);
                            carreira = Api.getCarreira(currentText);
                            Log.d("REALTIME TRACKING", "Carreira Loaded " + carreira.getRouteId());
                            carreira.init();
                            assert carreira != null;
                            assert listToAdd != null;
                            //carreira.updatePathsOnSelectedDirection(currentDirectionIndex);
                            List<Direction> carreiraDirectionList = carreira.getDirectionList();
                            for (Bus b : listToAdd){
                                for(Direction d : carreiraDirectionList){
                                    if (d.isCorrectHeadsign(b.getPattern_id())){
                                        b.setPattern_name(d.getHeadsign());
                                    }
                                }
                            }
                            currentCarreira = carreira;
                            List<Path> pathListToAdd = currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList();
                            busList.clear();
                            pathList.clear();
                            busList.addAll(listToAdd);
                            pathList.addAll(pathListToAdd);
                            connected = true;
                            if (listToAdd.size() == 0){
                                Log.d("REALTIME TRACKING", "Bus List Invalid (Empty)");
                                connected = false;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        noCurrentBusesDialog.show();
                                    }
                                });
                                return;
                            }else{
                                Log.d("REALTIME TRACKING", "Bus List Valid");
                            }
                        }catch (Exception e){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();
                                }
                            });
                            connected = false;
                            Log.d("REALTIME TRACKING", "Exception Caught: \n" + e.getMessage());
                            return;
                        }

                        currentSelectedBus = 0;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateMarkers(pathList, map);
                                updateBuses(busList, map, getActivity());
                                updateTextView();
                                updateDirectionIndex();
                                Log.println(Log.DEBUG, "BUS DEBUG", "GUI UPDATED");
                                GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                                map.getController().animateTo(point, 16.5, 1500L);
                            }
                        });

                        if (backgroundThreadStarted){
                            backgroundThread.unlockLock();
                        }

                        if(!backgroundThreadStarted && connected){
                            backgroundThread.start();
                            backgroundThreadStarted = true;
                        }

                    }
                });
                thread.start();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backgroundThreadStarted)
                    backgroundThread.lockLock();
                if (currentSelectedBus < busList.size() - 1){
                    currentSelectedBus ++;
                    updateTextView();
                    updateDirectionIndex();
                    updateMarkers(currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList(), map);
                    Log.println(Log.DEBUG, "Button", "Current Bus: " + currentSelectedBus);
                    getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //textView.setText((currentSelectedBus + 1) + "/" + busList.size() + "\n" + busList.get(currentSelectedBus).getStatus());
                                GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                                map.getController().animateTo(point, 16.5, 2500L);
                                map.invalidate();
                            }
                        });
                }
                if(backgroundThreadStarted)
                    backgroundThread.unlockLock();
            }

        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(backgroundThreadStarted)
                    backgroundThread.lockLock();
                if (currentSelectedBus > 0){
                    currentSelectedBus --;
                    updateTextView();
                    updateDirectionIndex();
                    updateMarkers(currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList(), map);
                    Log.println(Log.DEBUG, "Button", "Current Bus: " + currentSelectedBus);
                    getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GeoPoint point = markerBusList.get(currentSelectedBus).getPosition();
                                map.getController().animateTo(point, 16.5, 2500L);
                                map.invalidate();
                            }
                        });
                }
                if(backgroundThreadStarted)
                    backgroundThread.unlockLock();
            }
        });

        return v;
    }

    private void updateDirectionIndex() {
        int newIndex = 0;
        String directionId = busList.get(currentSelectedBus).getPattern_id();
        for (Direction direction : currentCarreira.getDirectionList()){
            if(direction.getDirectionId().equals(directionId)){
                currentDirectionIndex = newIndex;
            }
            newIndex++;
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
            String hexCode = currentCarreira.getColor().substring(1);
            int resultRed = Integer.valueOf(hexCode.substring(0, 2), 16);
            int resultGreen = Integer.valueOf(hexCode.substring(2, 4), 16);
            int resultBlue = Integer.valueOf(hexCode.substring(4, 6), 16);
            line.setColor(Color.rgb(resultRed, resultGreen, resultBlue));
            line.setWidth(7.5f);
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
        for (Bus bus : busList){
            double[] coordinates = bus.getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.cm_bus_regular, null);
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
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, "Bus " + bus.getId(), descrption);
            marker.setInfoWindow(miw);
            map.getOverlays().add(marker);
        }
    }

    public static void updateBusesUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateBuses(busList, map, activity);
            }
        });
    }


    private static Drawable getRotateDrawable(Bitmap b, float angle, Activity activity) {
        final BitmapDrawable drawable = new BitmapDrawable(activity.getApplicationContext().getResources(), b) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.rotate(angle, b.getWidth() / 2f, b.getHeight() / 2f);
                super.draw(canvas);
                canvas.restore();
            }
        };
        return drawable;
    }

    private static Drawable getRotateDrawable2(final Drawable d, final float angle) {
        final Drawable[] arD = { d };
        return new LayerDrawable(arD) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.rotate(angle, d.getBounds().width() / 2, d.getBounds().height() / 2);
                super.draw(canvas);
                canvas.restore();
            }
        };
    }

    public void showBackgroundThreadDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                backgroundDialog.show();
            }
        });
    }

    public static void updateTextView(){
        textView.setText((currentSelectedBus + 1) + "/" + busList.size() + " - " + busList.get(currentSelectedBus).getStatus() + "\n" + busList.get(currentSelectedBus).getPattern_name());
    }

    public MapView getMap() {
        return map;
    }
}