package com.example.carrismobile;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import api.Api;
import data_structure.Carreira;
import data_structure.Direction;
import data_structure.Path;
import data_structure.Schedule;
import data_structure.Stop;
import gui.CustomMarkerInfoWindow;
import kevin.carrismobile.adaptors.MyCustomDialog;

public class RouteDetailsFragment extends Fragment {

    private static SharedPreferences mPrefs;
    static MapView map;
    Button button;
    Button favoriteButton;
    Button routeStopDetails;
    TextView textView;//
    TextView pathListText;//
    TextView schedulesListText;//
    EditText editText;
    ListView pathView;//
    ListView scheduleView;//
    Spinner spinner;//
    ImageView loadingImage;

    public static String currentCarreiraId = null;
    public static boolean uiIsVisible = false;//
    public static int currentPathIndex = 0;//
    public static int currentDirectionIndex = 0;//
    public static Carreira currentCarreira = null;//
    public static List<Path> pathList = new ArrayList<>();//
    static ArrayAdapter<Path> pathAdapter = null;
    public static List<Direction> directionList = new ArrayList<>();//
    static ArrayAdapter<Direction> directionArrayAdapter = null;
    public static List<Schedule> scheduleList = new ArrayList<>();//
    static ArrayAdapter<Schedule> scheduleArrayAdapter = null;
    public AlertDialog dialog;
    public AlertDialog stopAdded;
    public AlertDialog routeAdded;
    public boolean connected;
    public static List<Marker> markerList = new ArrayList<>();//


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.route_details_fragment, container, false);

        mPrefs = getActivity().getSharedPreferences("RouteDetailsFragmentd", MODE_PRIVATE);

        button = v.findViewById(R.id.button);
        favoriteButton = v.findViewById(R.id.favoriteButton);
        routeStopDetails = v.findViewById(R.id.routeStopDetails);
        map = v.findViewById(R.id.mapview);
        textView = v.findViewById(R.id.textView);
        editText = v.findViewById(R.id.editText);
        pathListText = v.findViewById(R.id.pathListText);
        schedulesListText = v.findViewById(R.id.schedulesListText);
        pathView = v.findViewById(R.id.pathView);
        scheduleView = v.findViewById(R.id.scheduleView);
        spinner = v.findViewById(R.id.spinner);
        loadingImage = v.findViewById(R.id.loadingImage);

        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet");
        stopAdded = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem adicionada à Lista de Favoritos", "A Paragem Selecionada foi adicionada com sucesso á Lista de Paragens Favoritas");
        routeAdded = MyCustomDialog.createOkButtonDialog(getContext(), "Carreira adicionada à Lista de Favoritos", "A Carreira foi adicionada com sucesso á Lista de Carreiras Favoritas");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Loading Screen
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pathView.setVisibility(View.INVISIBLE);
                                scheduleView.setVisibility(View.INVISIBLE);
                                textView.setVisibility(View.INVISIBLE);
                                map.setVisibility(View.INVISIBLE);
                                loadingImage.setVisibility(View.VISIBLE);
                                pathListText.setVisibility(View.INVISIBLE);
                                schedulesListText.setVisibility(View.INVISIBLE);
                                favoriteButton.setVisibility((View.INVISIBLE));
                                routeStopDetails.setVisibility(View.INVISIBLE);

                                RotateAnimation rotate = new RotateAnimation(0, 360 * 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                rotate.setDuration(20000);
                                rotate.setRepeatCount(5);
                                rotate.setInterpolator(new LinearInterpolator());
                                loadingImage.startAnimation(rotate);
                            }
                        });
                        Carreira carreira = null;
                        try {
                            carreira = Api.getCarreira(editText.getText().toString());
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

                        carreira.updatePathsOnSelectedDirection(currentDirectionIndex);
                        currentCarreira = carreira;
                        currentCarreiraId = carreira.getRouteId();
                        currentPathIndex = 0;
                        double[] coordinates = carreira.getDirectionList().get(currentDirectionIndex).getPathList().get(currentPathIndex).getStop().getCoordinates();
                        GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
                        directionList = carreira.getDirectionList();
                        pathList.addAll(carreira.getDirectionList().get(currentDirectionIndex).getPathList());
                        scheduleList.addAll(pathList.get(currentPathIndex).getStop().getScheduleList());

                        scheduleArrayAdapter = new ArrayAdapter<Schedule>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, scheduleList);
                        pathAdapter = new ArrayAdapter<Path>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, pathList);
                        directionArrayAdapter = new ArrayAdapter<Direction>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, directionList);
                        Carreira finalCarreira = carreira;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(finalCarreira.getRouteId() + " - " + finalCarreira.getName());
                                uiIsVisible = true;
                                textView.setVisibility(View.VISIBLE);
                                scheduleView.setVisibility(View.VISIBLE);
                                pathView.setVisibility(View.VISIBLE);
                                pathListText.setVisibility(View.VISIBLE);
                                schedulesListText.setVisibility(View.VISIBLE);

                                pathView.setAdapter(pathAdapter);
                                scheduleView.setAdapter(scheduleArrayAdapter);
                                spinner.setAdapter(directionArrayAdapter);

                                map.setVisibility(View.VISIBLE);
                                map.getController().setCenter(point);
                                map.getController().setZoom(17.0);
                                map.invalidate();
                                updateMarkers(pathList, map, getActivity());

                                //Loading Screen
                                if (loadingImage.getAnimation() != null){
                                    loadingImage.getAnimation().cancel();
                                }
                                loadingImage.setVisibility(View.INVISIBLE);
                            }
                        });



                    }
                });
                thread.start();
            }
        });



        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        loadingImage.setScaleX(0.3f);
        loadingImage.setScaleY(0.3f);
        schedulesListText.setVisibility(View.INVISIBLE);
        pathListText.setVisibility(View.INVISIBLE);
        map.setVisibility(View.INVISIBLE);
        favoriteButton.setVisibility(View.INVISIBLE);
        routeStopDetails.setVisibility(View.INVISIBLE);
        textView.setText("Insira o número da sua Carreira\ne pressione Atualizar");

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setTileSource(TileSourceFactory.OpenTopo);

        map.setMultiTouchControls(true);
        //map.setVisibility(View.INVISIBLE);
        map.getController().setCenter(new GeoPoint(38.73329737648646, -9.14096412687648));
        map.getController().setZoom(13.0);
        map.invalidate();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentDirectionIndex = adapterView.getSelectedItemPosition();
                        pathList.clear();
                        currentCarreira.updatePathsOnSelectedDirection(currentDirectionIndex);
                        pathList.addAll(currentCarreira.getDirectionList().get(currentDirectionIndex).getPathList());
                        currentPathIndex = 0;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pathAdapter.notifyDataSetChanged();
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

        pathView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Path currentPath = pathList.get(i);
                        currentPathIndex = i;
                        double[] coordinates = currentPath.getStop().getCoordinates();
                        scheduleList.clear();
                        scheduleList.addAll(currentPath.getStop().getScheduleList());
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
                                                favoriteButton.setVisibility(View.VISIBLE);
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
                                                favoriteButton.setVisibility(View.INVISIBLE);
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

        scheduleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Path currentPath = pathList.get(currentPathIndex);
                        double[] coordinates = currentPath.getStop().getCoordinates();
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

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop stoptoAdd = pathList.get(currentPathIndex).getStop();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopFavoritesFragment fragment = (StopFavoritesFragment)mainActivity.stopFavoritesFragment;
                        fragment.addStopToFavorites(stoptoAdd);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopAdded.show();
                                //informs user of success;
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        routeStopDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop currentStop = pathList.get(currentPathIndex).getStop();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                        mainActivity.openstopDetailsFragment(true);
                        stopDetailsFragment.loadNewStop(currentStop.getStopID()+"");
                    }
                });
                thread.start();
            }
        });

        //Click Button
        return v;
    }

    public void loadCarreira(Carreira carreira){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                carreira.updatePathsOnSelectedDirection(currentDirectionIndex);
                currentCarreira = carreira;
                currentCarreiraId = carreira.getRouteId();
                currentPathIndex = 0;
                double[] coordinates = carreira.getDirectionList().get(currentDirectionIndex).getPathList().get(currentPathIndex).getStop().getCoordinates();
                GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
                directionList = carreira.getDirectionList();
                pathList.addAll(carreira.getDirectionList().get(currentDirectionIndex).getPathList());
                scheduleList.addAll(pathList.get(currentPathIndex).getStop().getScheduleList());

                scheduleArrayAdapter = new ArrayAdapter<Schedule>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, scheduleList);
                pathAdapter = new ArrayAdapter<Path>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, pathList);
                directionArrayAdapter = new ArrayAdapter<Direction>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, directionList);
                Carreira finalCarreira = carreira;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(finalCarreira.getRouteId() + " - " + finalCarreira.getName());
                        uiIsVisible = true;
                        textView.setVisibility(View.VISIBLE);
                        scheduleView.setVisibility(View.VISIBLE);
                        pathView.setVisibility(View.VISIBLE);
                        pathListText.setVisibility(View.VISIBLE);
                        schedulesListText.setVisibility(View.VISIBLE);

                        pathView.setAdapter(pathAdapter);
                        scheduleView.setAdapter(scheduleArrayAdapter);
                        spinner.setAdapter(directionArrayAdapter);

                        map.setVisibility(View.VISIBLE);
                        map.getController().setCenter(point);
                        map.getController().setZoom(17.0);
                        map.invalidate();
                        updateMarkers(pathList, map, getActivity());

                        //Loading Screen
                        if (loadingImage.getAnimation() != null){
                            loadingImage.getAnimation().cancel();
                        }
                        loadingImage.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        thread.start();
    }

    private static void updateMarkers(List<Path> pathList, MapView map, Activity activity){
        for (Marker marker : markerList){
            map.getOverlays().remove(marker);
        }

        for (Path path : pathList){
            double[] coordinates = path.getStop().getCoordinates();
            GeoPoint point = new GeoPoint(coordinates[0], coordinates[1]);
            Marker marker = new Marker(map);
            Drawable d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.map_stop_selected, null);
            marker.setIcon(d);
            markerList.add(marker);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            String descrption = "Stop Id: " + path.getStop().getStopID() + "\nLocality: " + path.getStop().getLocality() + "\nMunicipality: " + path.getStop().getMunicipality_name();
            MarkerInfoWindow miw= new CustomMarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map, path.getStop().getTts_name(), descrption);
            marker.setInfoWindow(miw);
            map.getOverlays().add(marker);
        }
    }
    private static void storeObject(String stringToSplit, String key){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.println(Log.DEBUG, "SPLIT STRING THREAD", "STARTED");
                    int maxTransactionSize = 800000;

                    int numberOfBytes = stringToSplit.getBytes().length;
                    Log.println(Log.DEBUG, "SPLIT STRING THREAD", "SIZE: " + numberOfBytes + " | SIZE STRING: " + stringToSplit.length() + " | " + (numberOfBytes < maxTransactionSize));


                    Log.println(Log.DEBUG, "SPLIT STRING THREAD", "SAVING");
                    mPrefs.edit().putInt(key + "Size", 1).commit();
                    mPrefs.edit().putString(key, stringToSplit).commit();
                    Log.println(Log.DEBUG, "SPLIT STRING THREAD", "FINISH");
                }catch (Exception e){
                    Log.println(Log.DEBUG, "SPLIT STRING THREAD", "INTERRUPTED\n\n" + e.getMessage());
                }
            }
        });
        thread.start();

    }

    private static Object loadObject(String key, Class klass){
        return new Gson().fromJson(mPrefs.getString(key, null), klass);
    }

    public EditText getEditText() {
        return editText;
    }

    public Button getButton() {
        return button;
    }

    public String getCurrentCarreiraId() {
        return currentCarreiraId;
    }

    public void addCurrentRouteToFavorites(){
        MainActivity mainActivity = (MainActivity) getActivity();
        RouteFavoritesFragment fragment = (RouteFavoritesFragment) mainActivity.routeFavoritesFragment;
        fragment.addCarreiraToFavorites(currentCarreira);
        mainActivity.openRouteDetailsFragment(false);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routeAdded.show();
            }
        });
    }

}