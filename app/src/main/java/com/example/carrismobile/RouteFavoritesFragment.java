package com.example.carrismobile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import data_structure.Carreira;
import kevin.carrismobile.adaptors.RouteImageListAdaptor;

public class RouteFavoritesFragment extends Fragment {

    EditText editText;
    ListView list;
    Button seeStopsFavorites;
    List<Carreira> carreiraList = new ArrayList<>();
    List<Carreira> currentCarreiraList = new ArrayList<>();
    RouteImageListAdaptor carreiraListAdapter;
    private static SharedPreferences mPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.route_favorites_fragment, container, false);

        list = v.findViewById(R.id.routeFavoritesList);
        editText = v.findViewById(R.id.editTextRouteFavorites);
        seeStopsFavorites = v.findViewById(R.id.SeeStopsFavorites);

        init();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentCarreiraList.clear();
                                String textModifiedString;
                                String original = editText.getText().toString();
                                if (editText.getText().length() > 0) {
                                    textModifiedString = original.substring(0, 1).toUpperCase() + original.substring(1);
                                }else{
                                    textModifiedString = "       ";}
                                //Carreiras
                                for (Carreira carreira : carreiraList){
                                    if (carreira.toString().contains(original) || carreira.toString().contains(textModifiedString)){
                                        currentCarreiraList.add(carreira);
                                    }
                                }

                                Log.println(Log.DEBUG, "DATA SET", "Changed to " + currentCarreiraList.size());
                                carreiraListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraList, 0);
                                list.setAdapter(carreiraListAdapter);
                            }
                        });
                    }
                });
                try{
                    thread1.start();
                }catch (Exception e){
                    return;
                }
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        RouteDetailsFragment fragment = (RouteDetailsFragment) mainActivity.routeDetailsFragment;
                        fragment.loadCarreira(currentCarreiraList.get(i));
                        mainActivity.openRouteDetailsFragment(false);
                    }
                });
                thread.start();
            }
        });

        seeStopsFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.openstopFavoritesFragment(false);
            }
        });

        return v;
    }

    private void init(){
        mPrefs = getActivity().getSharedPreferences("RouteFavoritesFragment", MODE_PRIVATE);
        String size;
        size = (String)loadObject("key_carreiraList_size", String.class);
        if (size == null){
            Log.d("STOP FAVORITES INIT", "SIZE WAS NULL");
            storeObject(new Gson().toJson("0"), "key_carreiraList_size");
            init();
            return;
        }
        for (int i = 0; i < Integer.parseInt(size); i++){
            Carreira carreiraToAdd = (Carreira)loadObject("key_carreiraList_carreira_" + i, Carreira.class);
            carreiraList.add(carreiraToAdd);
            Log.d("Carreira Recovered", carreiraToAdd.getName());
        }
        currentCarreiraList.addAll(carreiraList);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                carreiraListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraList, 0);
                list.setAdapter(carreiraListAdapter);
            }
        });
    }

    public void addCarreiraToFavorites(Carreira carreira){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (carreira != null){
                    if (!carreiraList.contains(carreira)){
                        carreiraList.add(carreira);
                        String size = (String)loadObject("key_carreiraList_size", String.class);
                        int intSize = Integer.parseInt(size);
                        intSize++;
                        String newSize = Integer.toString(intSize);
                        storeObject(new Gson().toJson(carreira), "key_carreiraList_carreira_" + size);
                        storeObject(new Gson().toJson(newSize), "key_carreiraList_size"); //updates size
                        Log.d("ROUTE SAVING", "Route was written into memory");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editText.setText(""); //clears editText
                                currentCarreiraList.clear();
                                currentCarreiraList.addAll(carreiraList);
                                carreiraListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraList, 0);
                                list.setAdapter(carreiraListAdapter);
                            }
                        });
                        Log.d("SUCCESS", "Route added to List successfully");
                    }else{
                        Log.d("WARNING", "The route given as argument was already in the favorites List");
                    }
                }else{
                    Log.e("ERROR", "Route provided was null");
                }
            }
        });
        thread.start();
    }

    public void removeStopFromFavorites(String carreiraId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Carreira stopToRemove = null;
                boolean stopFound = false;
                for (Carreira carreira : carreiraList){
                    if (carreiraId.equals(carreira.getRouteId())){
                        stopToRemove = carreira;
                        stopFound = true;
                        break;
                    }
                }
                if(!stopFound){
                    Log.e("ERROR", "Stop doesn't exist on favorites list");
                    return;
                }
                carreiraList.remove(stopToRemove);
                String size = (String)loadObject("key_carreiraList_size", String.class);
                int intSize = Integer.parseInt(size);
                intSize--;
                String newSize = Integer.toString(intSize);
                mPrefs.edit().remove("key_carreiraList_carreira_" + newSize).apply();
                storeObject(new Gson().toJson(newSize), "key_carreiraList_size"); //updates size
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(""); //clears editText
                        currentCarreiraList.clear();
                        currentCarreiraList.addAll(carreiraList);
                        carreiraListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraList, 0);
                        list.setAdapter(carreiraListAdapter);
                    }
                });
                Log.d("SUCCESS", "Stop was removed from favorites list");

            }
        });
        thread.start();
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

    public boolean containsCarreira(Carreira carreira){
        return carreiraList.contains(carreira);
    }
}