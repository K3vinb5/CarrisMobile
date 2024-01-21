package kevin.carrismobile.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.MessageFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import com.example.carrismobile.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ThunderforestTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.views.MapView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.tile_source.MyMapTilerTileSource;
import kevin.carrismobile.tile_source.MyThunderForestTileSource;
import kevin.carrismobile.tile_source.OpenStreetMapsTileSource;


public class SettingsFragment extends Fragment {
    private static SharedPreferences mPrefs;
    public List<String> keys;
    private static String selectedSwitch;
    public Switch openTopoSwitch;
    public Switch openStreetMapsSwitch;
    public Switch thunderForestSwitch;
    public Switch bingMapsSwitch;
    public Switch mapTilerSwitch;
    public Button resetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.settings_fragment, container, false);
        openTopoSwitch = v.findViewById(R.id.openTopoSwitch);
        thunderForestSwitch = v.findViewById(R.id.thunderForestSwitch);
        bingMapsSwitch = v.findViewById(R.id.bingMapsSwitch);
        mapTilerSwitch = v.findViewById(R.id.mapTilerSwitch);
        openStreetMapsSwitch = v.findViewById(R.id.openStreetMapsSwitch);
        resetButton = v.findViewById(R.id.resetPreferences);
        mPrefs = getActivity().getSharedPreferences("SettingsFragment", MODE_PRIVATE);
        selectedSwitch = (String)loadObject("key_settings", String.class);
        if (loadObject("key_settings", String.class) == null){
            storeObject(new Gson().toJson("topo"), "key_settings");
            openStreetMapsSwitch.performClick();
            selectedSwitch = "topo";
        }else {
            switch (selectedSwitch){
                case "topo":
                    openTopoSwitch.setChecked(true);
                    break;
                case "openStreetMaps":
                    openStreetMapsSwitch.setChecked(true);
                    break;
                case "thunderForest":
                    thunderForestSwitch.setChecked(true);
                    break;
                case "bingMaps":
                    bingMapsSwitch.setChecked(true);
                    break;
                case "mapTiler":
                    mapTilerSwitch.setChecked(true);
                    break;
            }
        }
        setOpenTopoSwitchOnClickListener();
        setOpenStreetMapsSwitchSwitchOnClickListener();
        setThunderForestSwitchOnClickListener();
        setBingMapsSwitchOnClickListener();
        setMapTilerSwitchOnClickListener();
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrefs.edit().clear().apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MyCustomDialog.createOkButtonDialog(getContext(), "Configurações", "Configurações limpas com sucesso").show();
                    }
                });
            }
        });

        return v;
    }

    public static OnlineTileSourceBase getCurrentTileProvider(Context context) {
        switch (selectedSwitch) {
            case "topo":
                return TileSourceFactory.OpenTopo;
            case "openStreetMaps":
                return new OpenStreetMapsTileSource();
            case "thunderForest":
                MyThunderForestTileSource thunderForestTileSource = new MyThunderForestTileSource(MyThunderForestTileSource.ATLAS);
                String apiKey = (String)loadObject("key_apiKey_thunderForest", String.class);
                if (apiKey == null){
                    showEditTextDialog("key_apiKey_thunderForest", "Thunder Forest", context);
                    apiKey = (String)loadObject("key_apiKey_thunderForest", String.class);
                }
                thunderForestTileSource.setApiKey(apiKey);
                return thunderForestTileSource;
            case "bingMaps":
                BingMapTileSource bingMap = new BingMapTileSource (Locale.UK.getLanguage());
                String bapiKey = (String)loadObject("key_apiKey_bingMaps", String.class);
                if (bapiKey == null){
                    showEditTextDialog("key_apiKey_bingMaps", "Bing Maps", context);
                    bapiKey = (String)loadObject("key_apiKey_bingMaps", String.class);
                }
                BingMapTileSource.setBingKey(bapiKey);
                bingMap.setStyle (BingMapTileSource. IMAGERYSET_ROAD);
                return bingMap;
            case "mapTiler":
                MyMapTilerTileSource mapTilerSource = new MyMapTilerTileSource();
                String mapiKey = (String)loadObject("key_apiKey_mapTiler", String.class);
                if (mapiKey == null){
                    showEditTextDialog("key_apiKey_mapTiler", "Map Tiler", context);
                    mapiKey = (String)loadObject("key_apiKey_mapTiler", String.class);
                }
                mapTilerSource.setApiKey(mapiKey);
            default:
                return new OpenStreetMapsTileSource();
        }
    }

    private void setOpenTopoSwitchOnClickListener(){
        openTopoSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thunderForestSwitch.isChecked()){
                    thunderForestSwitch.setChecked(false);
                }else if(bingMapsSwitch.isChecked()){
                    bingMapsSwitch.setChecked(false);
                }else if(mapTilerSwitch.isChecked()){
                    mapTilerSwitch.setChecked(false);
                }else if(openStreetMapsSwitch.isChecked()){
                    openStreetMapsSwitch.setChecked(false);
                }else if(!openTopoSwitch.isChecked()){
                    openTopoSwitch.setChecked(true);
                }
                MainActivity activity = (MainActivity) getActivity();
                MapView map1 = ((StopsMapFragment) activity.stopsMapFragment).getMap();
                MapView map2 = ((RealTimeFragment) activity.realTimeFragment).getMap();
                MapView map3 = ((RouteDetailsFragment) activity.routeDetailsFragment).getMap();
                map1.setTileSource(TileSourceFactory.OpenTopo);
                map2.setTileSource(TileSourceFactory.OpenTopo);
                map3.setTileSource(TileSourceFactory.OpenTopo);
                map1.invalidate();
                map2.invalidate();
                map3.invalidate();
                storeObject(new Gson().toJson("topo"), "key_settings");
            }
        });
    }

    private void setOpenStreetMapsSwitchSwitchOnClickListener(){
        openStreetMapsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thunderForestSwitch.isChecked()){
                    thunderForestSwitch.setChecked(false);
                }else if(bingMapsSwitch.isChecked()){
                    bingMapsSwitch.setChecked(false);
                }else if(mapTilerSwitch.isChecked()){
                    mapTilerSwitch.setChecked(false);
                }else if(openTopoSwitch.isChecked()){
                    openTopoSwitch.setChecked(false);
                } else if(!openStreetMapsSwitch.isChecked()){
                    openStreetMapsSwitch.setChecked(true);
                }
                MainActivity activity = (MainActivity) getActivity();
                MapView map1 = ((StopsMapFragment) activity.stopsMapFragment).getMap();
                MapView map2 = ((RealTimeFragment) activity.realTimeFragment).getMap();
                MapView map3 = ((RouteDetailsFragment) activity.routeDetailsFragment).getMap();
                OpenStreetMapsTileSource osm = new OpenStreetMapsTileSource();
                map1.setTileSource(osm);
                map2.setTileSource(osm);
                map3.setTileSource(osm);
                map1.invalidate();
                map2.invalidate();
                map3.invalidate();
                storeObject(new Gson().toJson("openStreetMaps"), "key_settings");
            }
        });
    }
    private void setBingMapsSwitchOnClickListener(){
        bingMapsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openTopoSwitch.isChecked()){
                    openTopoSwitch.setChecked(false);
                }else if(thunderForestSwitch.isChecked()){
                    thunderForestSwitch.setChecked(false);
                }else if(mapTilerSwitch.isChecked()){
                    mapTilerSwitch.setChecked(false);
                }else if(openStreetMapsSwitch.isChecked()){
                    openStreetMapsSwitch.setChecked(false);
                }else if(!bingMapsSwitch.isChecked()){
                    bingMapsSwitch.setChecked(true);
                }
                MainActivity activity = (MainActivity) getActivity();
                MapView map1 = ((StopsMapFragment) activity.stopsMapFragment).getMap();
                MapView map2 = ((RealTimeFragment) activity.realTimeFragment).getMap();
                MapView map3 = ((RouteDetailsFragment) activity.routeDetailsFragment).getMap();

                BingMapTileSource bingMap = new BingMapTileSource (Locale.UK.getLanguage());
                String apiKey = (String)loadObject("key_apiKey_bingMaps", String.class);
                if (apiKey == null){
                    showEditTextDialog("key_apiKey_bingMaps", "Bing Maps", getContext());
                    apiKey = (String)loadObject("key_apiKey_bingMaps", String.class);
                }
                BingMapTileSource.setBingKey(apiKey);
                bingMap.setStyle (BingMapTileSource. IMAGERYSET_ROAD);

                map1.setTileSource(bingMap);
                map2.setTileSource(bingMap);
                map3.setTileSource(bingMap);

                map1.invalidate();
                map2.invalidate();
                map3.invalidate();
                storeObject(new Gson().toJson("bingMaps"), "key_settings");
            }
        });
    }
    private void setThunderForestSwitchOnClickListener(){
        thunderForestSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openTopoSwitch.isChecked()){
                    openTopoSwitch.setChecked(false);
                }else if(bingMapsSwitch.isChecked()){
                    bingMapsSwitch.setChecked(false);
                }else if(mapTilerSwitch.isChecked()){
                    mapTilerSwitch.setChecked(false);
                }else if(openStreetMapsSwitch.isChecked()){
                    openStreetMapsSwitch.setChecked(false);
                }else if(!thunderForestSwitch.isChecked()){
                    thunderForestSwitch.setChecked(true);
                }
                MainActivity activity = (MainActivity) getActivity();
                MapView map1 = ((StopsMapFragment) activity.stopsMapFragment).getMap();
                MapView map2 = ((RealTimeFragment) activity.realTimeFragment).getMap();
                MapView map3 = ((RouteDetailsFragment) activity.routeDetailsFragment).getMap();

                MyThunderForestTileSource thunderForestTileSource = new MyThunderForestTileSource(MyThunderForestTileSource.ATLAS);
                String apiKey = (String)loadObject("key_apiKey_thunderForest", String.class);
                if (apiKey == null){
                    showEditTextDialog("key_apiKey_thunderForest", "Thunder Forest", getContext());
                    apiKey = (String)loadObject("key_apiKey_thunderForest", String.class);
                }
                thunderForestTileSource.setApiKey(apiKey);

                map1.setTileSource(thunderForestTileSource);
                map2.setTileSource(thunderForestTileSource);
                map3.setTileSource(thunderForestTileSource);
                map1.invalidate();
                map2.invalidate();
                map3.invalidate();
                storeObject(new Gson().toJson("thunderForest"), "key_settings");
            }
        });
    }
    private static void showEditTextDialog(String key, String tileSourceName, Context context){
        View view1 = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text_layout, null);
        TextInputEditText editText = view1.findViewById(R.id.editText);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Please enter your Api Key for " + tileSourceName)
                .setView(view1)
                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                    storeObject(new Gson().toJson(editText.getText().toString()), key);
                    dialogInterface.dismiss();
                }).setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss()).create();
        alertDialog.show();
    }
    private void setMapTilerSwitchOnClickListener(){
        mapTilerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openTopoSwitch.isChecked()){
                    openTopoSwitch.setChecked(false);
                }else if(bingMapsSwitch.isChecked()){
                    bingMapsSwitch.setChecked(false);
                }else if(thunderForestSwitch.isChecked()){
                    thunderForestSwitch.setChecked(false);
                }else if(openStreetMapsSwitch.isChecked()){
                    openStreetMapsSwitch.setChecked(false);
                }else if(!mapTilerSwitch.isChecked()){
                    mapTilerSwitch.setChecked(true);
                }
                MainActivity activity = (MainActivity) getActivity();
                MapView map1 = ((StopsMapFragment) activity.stopsMapFragment).getMap();
                MapView map2 = ((RealTimeFragment) activity.realTimeFragment).getMap();
                MapView map3 = ((RouteDetailsFragment) activity.routeDetailsFragment).getMap();

                MyMapTilerTileSource mapTilerSource = new MyMapTilerTileSource();
                String apiKey = (String)loadObject("key_apiKey_mapTiler", String.class);
                if (apiKey == null){
                    showEditTextDialog("key_apiKey_mapTiler", "Map Tiler", getContext());
                    apiKey = (String)loadObject("key_apiKey_mapTiler", String.class);
                }
                mapTilerSource.setApiKey(apiKey);

                map1.setTileSource(mapTilerSource);
                map2.setTileSource(mapTilerSource);
                map3.setTileSource(mapTilerSource);
                map1.invalidate();
                map2.invalidate();
                map3.invalidate();
                storeObject(new Gson().toJson("mapTiler"), "key_settings");
            }
        });
    }
    private static void storeObject(String json, String key){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.println(Log.DEBUG, "STORE THREAD", "SAVING");
                    mPrefs.edit().putString(key, json).apply();
                    Log.println(Log.DEBUG, "STORE THREAD", "FINISH");
                }catch (Exception e){
                    Log.println(Log.ERROR, "STORE THREAD", "INTERRUPTED\n\n" + e.getMessage());
                }
            }
        });
        thread.start();
    }
    private static Object loadObject(String key, Class klass){
        return new Gson().fromJson(mPrefs.getString(key, null), klass);
    }
}