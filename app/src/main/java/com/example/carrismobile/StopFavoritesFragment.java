package com.example.carrismobile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import api.Api;
import data_structure.Stop;

public class StopFavoritesFragment extends Fragment {

    private static SharedPreferences mPrefs;
    private List<Stop> stopList = new ArrayList<>();
    private ArrayAdapter<Stop> stopListAdaptor;
    ListView list;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.stop_favorites_fragment, container, false);

        list = v.findViewById(R.id.stopFavoritesList);

        init();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop selectedStop = stopList.get(index);
                        if (selectedStop == null){
                            return;
                        }
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                        stopDetailsFragment.loadNewStop(selectedStop.getStopID()+"");
                        mainActivity.openstopDetailsFragment(false);
                    }
                });
                thread.start();
            }
        });
        //testing
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                addStopToFavorites(Api.getStopFromId("110066"));
                //removeStopFromFavorites("110067");
            }
        });
        thread.start();
        //end testing
        return v;
    }

    private void init(){
        mPrefs = getActivity().getSharedPreferences("StopFavoritesFragment", MODE_PRIVATE);
        //mPrefs.edit().clear().apply();
        String size;
        size = (String)loadObject("key_stopList_size", String.class);
        if (size == null){
            Log.d("STOP FAVORITES INIT", "SIZE WAS NULL");
            storeObject(new Gson().toJson("0"), "key_stopList_size");
            init();
            return;
        }
        for (int i = 0; i < Integer.parseInt(size); i++){
            Stop stopToAdd = (Stop)loadObject("key_stopList_stop_" + i, Stop.class);
            stopList.add(stopToAdd);
            Log.d("Stop Recovered", stopToAdd.getTts_name());
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopListAdaptor = new ArrayAdapter<Stop>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, stopList);
                list.setAdapter(stopListAdaptor);
            }
        });
    }

    public void addStopToFavorites(Stop stop){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (stop != null){
                    if (!stopList.contains(stop)){
                        stopList.add(stop);
                        String size = (String)loadObject("key_stopList_size", String.class);
                        int intSize = Integer.parseInt(size);
                        intSize++;
                        String newSize = Integer.toString(intSize);
                        storeObject(new Gson().toJson(stop), "key_stopList_stop_" + size);
                        storeObject(new Gson().toJson(newSize), "key_stopList_size"); //updates size
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopListAdaptor.notifyDataSetChanged();
                            }
                        });
                        Log.d("SUCCESS", "Stop added to List successfully");
                    }
                    Log.d("WARNING", "The stop given as argument was already in the favorites List");
                }else{
                    Log.e("ERROR", "Stop provided was null");
                }
            }
        });
        thread.start();
    }
    public void removeStopFromFavorites(String stopId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Stop stopToRemove = null;
                boolean stopFound = false;
                for (Stop stop : stopList){
                    if (stopId.equals(Integer.toString(stop.getStopID()))){
                        stopToRemove = stop;
                        stopFound = true;
                        break;
                    }
                }
                if(!stopFound){
                    Log.e("ERROR", "Stop doesn't exist on favorites list");
                    return;
                }
                stopList.remove(stopToRemove);
                String size = (String)loadObject("key_stopList_size", String.class);
                int intSize = Integer.parseInt(size);
                intSize--;
                String newSize = Integer.toString(intSize);
                mPrefs.edit().remove("key_stopList_stop_" + newSize).apply();
                storeObject(new Gson().toJson(newSize), "key_stopList_size"); //updates size
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopListAdaptor.notifyDataSetChanged();
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
}