package kevin.carrismobile.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.carrismobile.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kevin.carrismobile.api.Api;
import kevin.carrismobile.data.Carreira;
import kevin.carrismobile.data.Direction;
import kevin.carrismobile.data.Schedule;
import kevin.carrismobile.data.Stop;
import kevin.carrismobile.gui.CustomMarkerInfoWindow;
import kevin.carrismobile.gui.TextDrawable;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.adaptors.StopImageListAdaptor;

public class RouteDetailsFragment extends Fragment {

    //private static SharedPreferences mPrefs;
    static MapView map;
    Button routeStopDetails;
    Button searchButton;
    EditText editText;
    TextView textView;//
    ImageView routeImageView;
    Toolbar routeDetailsToolbar;
    TextView pathListText;//
    TextView schedulesListText;//
    ListView stopView;//
    ListView scheduleView;//
    Spinner spinner;//
    ImageView loadingImage;

    public static String currentCarreiraId = null;
    public static boolean uiIsVisible = false;//
    public static int currentStopIndex = 0;//
    public static int currentDirectionIndex = 0;//
    public static Carreira currentCarreira = null;//
    public static List<Stop> stopList = new ArrayList<>();
    static StopImageListAdaptor stopImageListAdaptor;
    public static List<Direction> directionList = new ArrayList<>();//
    static ArrayAdapter<Direction> directionArrayAdapter = null;
    public static List<Schedule> scheduleList = new ArrayList<>();//
    static ArrayAdapter<Schedule> scheduleArrayAdapter = null;
    public AlertDialog dialog;
    public AlertDialog stopAdded;
    public AlertDialog routeAdded;
    public AlertDialog routeDeleted;
    public boolean connected;
    public static List<Marker> markerList = new ArrayList<>();//


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.route_details_fragment, container, false);

        //mPrefs = getActivity().getSharedPreferences("RouteDetailsFragmentd", MODE_PRIVATE);

        routeStopDetails = v.findViewById(R.id.routeStopDetails);
        searchButton = v.findViewById(R.id.searchButton);
        editText = v.findViewById(R.id.editText);
        map = v.findViewById(R.id.mapview);
        textView = v.findViewById(R.id.textView);
        pathListText = v.findViewById(R.id.pathListText);
        schedulesListText = v.findViewById(R.id.schedulesListText);
        stopView = v.findViewById(R.id.pathView);
        scheduleView = v.findViewById(R.id.scheduleView);
        spinner = v.findViewById(R.id.spinner);
        routeImageView = v.findViewById(R.id.routeImageView);
        loadingImage = v.findViewById(R.id.loadingImage);
        routeDetailsToolbar = v.findViewById(R.id.routeDetailsToolbar);

        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet");
        stopAdded = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem adicionada à Lista de Favoritos", "A Paragem Selecionada foi adicionada com sucesso á Lista de Paragens Favoritas");
        routeAdded = MyCustomDialog.createOkButtonDialog(getContext(), "Carreira adicionada à Lista de Favoritos", "A Carreira foi adicionada com sucesso á Lista de Carreiras Favoritas");
        routeDeleted = MyCustomDialog.createOkButtonDialog(getContext(), "Carreira removida da Lista de Favoritos", "A Carreira foi removida com sucesso da Lista de Carreiras Favoritas");

        loadingImage.setScaleX(0.3f);
        loadingImage.setScaleY(0.3f);
        schedulesListText.setVisibility(View.INVISIBLE);
        pathListText.setVisibility(View.INVISIBLE);
        map.setVisibility(View.INVISIBLE);
        routeStopDetails.setVisibility(View.INVISIBLE);
        textView.setText("Insira o número da sua Carreira\ne pressione Atualizar");

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(TileSourceFactory.OpenTopo);

        map.setMultiTouchControls(true);
        map.getController().setCenter(new GeoPoint(38.73329737648646, -9.14096412687648));
        map.getController().setZoom(13.0);
        map.invalidate();

        setSpinnerSetItemsSelectedListener();
        setStopViewItemsSelectedListener();
        setScheduleViewItemsSelectedListener();

        routeStopDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop currentStop = stopList.get(currentStopIndex);
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                        mainActivity.openFragment(stopDetailsFragment, 0, true);
                        stopDetailsFragment.loadNewStop(currentStop.getStopID()+"");
                    }
                });
                thread.start();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadCarreiraFromApi(editText.getText().toString());
                    }
                });
                thread.start();
            }
        });
            //Click Button
        return v;
    }

    private void setSpinnerSetItemsSelectedListener(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentDirectionIndex = adapterView.getSelectedItemPosition();
                        stopList.clear();
                        if (currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList().get(0).getStop().getScheduleList() == null){
                            try{
                                currentCarreira.updateSchedulesOnStopOnGivenDirectionAndStop(currentDirectionIndex, 0);
                            }catch (Exception ignore){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.show();
                                    }
                                });
                                connected = false;
                            }
                        }
                        List<Stop> toAdd = new ArrayList<>();
                        currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList().forEach(path -> toAdd.add(path.getStop()));
                        assert toAdd.size() > 0;
                        stopList.addAll(toAdd);
                        currentStopIndex = 0;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopImageListAdaptor = new StopImageListAdaptor(getActivity(), stopList);
                                stopView.setAdapter(stopImageListAdaptor);
                                //TODO not sure what the id is
                                stopView.performItemClick(getView(), 0, getId());
                            }
                        });
                    }
                });
                thread.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    private void setStopViewItemsSelectedListener(){
        stopView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentStopIndex = i;
                        if(stopList.size() == 0){
                            List<Stop> toAdd = new ArrayList<>();
                            currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList().forEach(path -> toAdd.add(path.getStop()));
                            assert toAdd.size() > 0;
                            stopList.addAll(toAdd);
                        }
                        Stop currentStop = stopList.get(currentStopIndex);
                        try{
                            currentCarreira.updateSchedulesOnStopOnGivenDirectionAndStop(currentDirectionIndex, currentStopIndex);
                        }catch (Exception ignore){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();
                                }
                            });
                            connected = false;
                        }
                        double[] coordinates = currentStop.getCoordinates();
                        scheduleList.clear();
                        scheduleList.addAll(currentStop.getScheduleList());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scheduleArrayAdapter.notifyDataSetChanged();
                                map.getController().animateTo(new GeoPoint(coordinates[0], coordinates[1]));
                                Thread thread1 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                routeStopDetails.setVisibility(View.VISIBLE);

                                            }
                                        });
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(5000);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                routeStopDetails.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    }
                                });
                                thread1.start();
                            }
                        });
                    }
                });
                thread.start();
            }
        });
    }

    private void setScheduleViewItemsSelectedListener(){
        scheduleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop currentStop = stopList.get(currentStopIndex);
                        double[] coordinates = currentStop.getCoordinates();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                map.getController().animateTo(new GeoPoint(coordinates[0], coordinates[1]));
                            }
                        });
                    }
                });
                thread.start();
            }
        });
    }


    private void startWaitingAnimation(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routeDetailsToolbar.getMenu().getItem(1).setIcon(R.drawable.baseline_star_border_24);
                stopView.setVisibility(View.INVISIBLE);
                scheduleView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                map.setVisibility(View.INVISIBLE);
                loadingImage.setVisibility(View.VISIBLE);
                pathListText.setVisibility(View.INVISIBLE);
                schedulesListText.setVisibility(View.INVISIBLE);
                routeImageView.setVisibility(((View.INVISIBLE)));
                routeStopDetails.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);

                RotateAnimation rotate = new RotateAnimation(0, 360 * 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(20000);
                rotate.setRepeatCount(5);
                rotate.setInterpolator(new LinearInterpolator());
                loadingImage.startAnimation(rotate);
            }
        });
    }

    private void stopWaitingAnimation(Carreira finalCarreira, RouteFavoritesFragment fragment, GeoPoint point){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(finalCarreira.getName());

                uiIsVisible = true;

                if (loadingImage.getAnimation() != null){
                    loadingImage.getAnimation().cancel();
                }
                loadingImage.setVisibility(View.INVISIBLE);

                textView.setVisibility(View.VISIBLE);
                scheduleView.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
                stopView.setVisibility(View.VISIBLE);
                pathListText.setVisibility(View.VISIBLE);
                schedulesListText.setVisibility(View.VISIBLE);
                routeImageView.setVisibility(View.VISIBLE);

                changeCarreiraDrawable(finalCarreira);
                changeCarreiraDrawable(finalCarreira);

                scheduleArrayAdapter = new ArrayAdapter<Schedule>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, scheduleList);
                stopImageListAdaptor = new StopImageListAdaptor(getActivity(), stopList);
                directionArrayAdapter = new ArrayAdapter<Direction>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, directionList);
                stopView.setAdapter(stopImageListAdaptor);
                scheduleView.setAdapter(scheduleArrayAdapter);
                spinner.setAdapter(directionArrayAdapter);

                if (fragment.containsCarreira(currentCarreira.getRouteId())) {
                    routeDetailsToolbar.getMenu().getItem(1).setIcon(R.drawable.baseline_star_24);
                }else{
                    routeDetailsToolbar.getMenu().getItem(1).setIcon(R.drawable.baseline_star_border_24);
                }
                map.setVisibility(View.VISIBLE);
                map.getController().setCenter(point);
                map.getController().setZoom(17.0);
                map.invalidate();
                updateMarkers(stopList, map, getActivity());
            }
        });
    }

    public void loadCarreiraFromApi(String carreiraId){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LOADING ROUTE", "THREAD STARTED");
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception ignored) {
                }
                //Loading Screen
                startWaitingAnimation();
                Carreira carreira = null;
                try {
                    carreira = Api.getCarreira(carreiraId);
                    carreira.init();
                    connected = true;
                }catch (Exception e ){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                            if (loadingImage.getAnimation() != null){
                                loadingImage.getAnimation().cancel();
                            }
                            loadingImage.setVisibility(View.INVISIBLE);
                        }
                    });
                    connected = false;
                    return;
                }
                currentDirectionIndex = 0;
                //carreira.updatePathsOnSelectedDirection(currentDirectionIndex);
                currentCarreira = carreira;
                currentCarreiraId = carreira.getRouteId();
                currentStopIndex = 0;
                stopList.clear();
                List<Stop> toAdd = new ArrayList<>();
                carreira.getDirectionList().get(currentDirectionIndex).getPathList().forEach(path -> toAdd.add(path.getStop()));
                assert toAdd.size() > 0;
                stopList.addAll(toAdd);
                double[] coordinates = stopList.get(currentStopIndex).getCoordinates();
                GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
                directionList = carreira.getDirectionList();
                //assert stopList.get(currentStopIndex).getScheduleList() != null;
                if(stopList.get(currentStopIndex).getScheduleList() != null){
                    scheduleList.addAll(stopList.get(currentStopIndex).getScheduleList());
                }

                MainActivity activity = (MainActivity)getActivity();
                RouteFavoritesFragment fragment = (RouteFavoritesFragment) activity.routeFavoritesFragment;

                Carreira finalCarreira = carreira;

                stopWaitingAnimation(finalCarreira, fragment, point);
            }
        });
        thread.start();
    }

    public void loadCarreira(Carreira carreira){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startWaitingAnimation();
                try{
                    carreira.updateSchedulesOnStopOnGivenDirectionAndStop(currentDirectionIndex, 0);
                }catch (Exception ignore){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                    connected = false;

                }
                currentCarreira = carreira;
                currentCarreiraId = carreira.getRouteId();
                currentStopIndex = 0;
                List<Stop> toAdd = new ArrayList<>();
                carreira.getDirectionList().get(currentDirectionIndex).getPathList().forEach(path -> toAdd.add(path.getStop()));
                stopList.addAll(toAdd);
                double[] coordinates = stopList.get(currentStopIndex).getCoordinates();
                GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
                directionList = carreira.getDirectionList();
                scheduleList.addAll(stopList.get(currentStopIndex).getScheduleList());

                MainActivity activity = (MainActivity)getActivity();
                RouteFavoritesFragment fragment = (RouteFavoritesFragment) activity.routeFavoritesFragment;

                Carreira finalCarreira = carreira;
                stopWaitingAnimation(finalCarreira, fragment, point);

            }
        });
        thread.start();
    }

    private static void updateMarkers(List<Stop> stopList, MapView map, Activity activity){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }

        for (Stop s : stopList){
            double[] coordinates = s.getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_stop_logo, null);
            marker.setIcon(d);
            markerList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Stop Id: " + s.getStopID() + "\nLocality: " + s.getLocality() + "\nMunicipality: " + s.getMunicipality_name();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, s.getTts_name(), descrption);
            marker.setInfoWindow(miw);
            map.getOverlays().add(marker);
        }
    }
    public String getCurrentCarreiraId() {
        return currentCarreiraId;
    }

    public void addCurrentRouteToFavorites(){
        MainActivity mainActivity = (MainActivity) getActivity();
        RouteFavoritesFragment fragment = (RouteFavoritesFragment) mainActivity.routeFavoritesFragment;
        if (fragment.containsCarreira(currentCarreira.getRouteId())){
            routeDetailsToolbar.getMenu().getItem(1).setIcon(R.drawable.baseline_star_border_24);
            fragment.removeStopFromFavorites(currentCarreiraId);
            //mainActivity.openRouteFavoritesFragment(true);
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    routeDeleted.show();
                }
            });
        }else{
            //Carreira not in favorites List
            routeDetailsToolbar.getMenu().getItem(1).setIcon(R.drawable.baseline_star_24);
            fragment.addCarreiraToFavorites(currentCarreira);
            //mainActivity.openRouteFavoritesFragment(true);
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    routeAdded.show();
                }
            });
        }
    }

    public void changeCarreiraDrawable(Carreira carreira){
        Drawable imageDrawable = getImageId(carreira.getColor());
        Drawable textDrawable = new TextDrawable(getActivity().getResources(), carreira.getRouteId(), 20);
        LayerDrawable finalDrawable = new LayerDrawable(new Drawable[] {imageDrawable, textDrawable});

        finalDrawable.setLayerSize(0,routeImageView.getWidth(), routeImageView.getHeight());
        finalDrawable.setLayerGravity(1, Gravity.CENTER_VERTICAL);

        routeImageView.setImageDrawable(finalDrawable);
    }

    private Drawable getImageId(String string){
        if (string.equals("#ED1944")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_ed1944, null);
        }else if (string.equals("#C61D23")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_c61d23, null);
        }else if (string.equals("#BB3E96")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_bb3e96, null);
        }else if (string.equals("#3D85C6")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_3d85c6, null);
        }else if (string.equals("#2A9057")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_2a9057, null);
        }else if (string.equals("#FDB71A")){
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_fdb71a, null);
        }else{
            return ResourcesCompat.getDrawable(getResources(), R.drawable.color_00b8b0, null);
        }
    }

    public MapView getMap() {
        return map;
    }
}