package com.example.carrismobile;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import api.Api;
import data_structure.RealTimeSchedule;
import data_structure.Stop;
import kevin.carrismobile.adaptors.MyCustomDialog;

public class StopDetailsFragment extends Fragment {

    public List<RealTimeSchedule> currentStopRealTimeSchedules = new ArrayList<>();
    public ArrayAdapter<RealTimeSchedule> currentStopRealTimeSchedulesAdaptor;
    ListView list;
    Button removeFavoriteButton;
    TextView stopTitle;
    TextView stopDetails;
    AlertDialog confirmRemovalDialog;
    Stop currentStop = null;
    boolean currentlyDisplaying = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stop_details_fragment, container, false);

        list = v.findViewById(R.id.stopDetailsList);
        stopTitle = v.findViewById(R.id.textViewStopName);
        stopDetails = v.findViewById(R.id.textViewStopNameDetails);
        removeFavoriteButton = v.findViewById(R.id.removeFavorite);

        confirmRemovalDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem removida com sucesso", "A paragem selecionada foi removida da lista de favoritos com sucesso");

        currentStopRealTimeSchedulesAdaptor = new ArrayAdapter<RealTimeSchedule>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, currentStopRealTimeSchedules);
        list.setAdapter(currentStopRealTimeSchedulesAdaptor);

        removeFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopFavoritesFragment fragment = (StopFavoritesFragment) mainActivity.stopFavoritesFragment;
                        fragment.removeStopFromFavorites(currentStop.getStopID()+"");
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                confirmRemovalDialog.show();
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        return v;
    }

    public void loadNewStop(String stopId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String newStopId = stopId;
                while(newStopId.length() < 6){
                    newStopId = "0" + newStopId;
                }
                Stop stop;
                List<RealTimeSchedule> toAdd;
                try{
                    stop = Api.getStopFromId(newStopId); //requires API
                    toAdd = stop.getRealTimeSchedules(); //requires API
                    if (toAdd != null){
                        currentStopRealTimeSchedules.clear();
                        currentStopRealTimeSchedules.addAll(toAdd);
                        //Log.d("DEBUG LIST", currentStopRealTimeSchedules.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentStopRealTimeSchedulesAdaptor.notifyDataSetChanged();
                                stopTitle.setText(stop.getTts_name());
                                stopDetails.setText("Localidade: " + stop.getLocality() + "\nMunicipalidade: " + stop.getMunicipality_name() + "\nDistrito: " + stop.getDistrict_name());
                                currentStop = stop;
                            }
                        });

                    }else{
                        Log.d("DEBUG LIST", "TO ADD IS NULL");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopTitle.setText(stop.getTts_name());
                                stopDetails.setText("Localidade: " + stop.getLocality() + "\nMunicipalidade: " + stop.getMunicipality_name() + "\nDistrito: " + stop.getDistrict_name());
                                currentStop = stop;
                            }
                        });
                    }
                }catch (Exception e){
                    Log.e("ERROR", e.getMessage());
                    return;
                }
            }
        });
        thread.start();
    }
}