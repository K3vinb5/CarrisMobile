package com.example.carrismobile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import api.Api;
import data_structure.CarreiraBasic;
import data_structure.Schedule;
import data_structure.Stop;
import kevin.carrismobile.adaptors.ImageListAdaptor;

public class RoutesFragment extends Fragment {


    ListView list;
    EditText editText;
    ImageListAdaptor imagesListAdapter;
    ArrayAdapter<Stop> stopListAdaptor;
    List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
    List<Stop> stopsList = new ArrayList<>();
    Switch aSwitch;
    List<CarreiraBasic> currentCarreiraBasicList = new ArrayList<>();
    List<Stop> currentStopList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.route_fragment, container, false);

        list = v.findViewById(R.id.main_list);
        editText = v.findViewById(R.id.editTextRoutes);
        aSwitch = v.findViewById(R.id.switch1);

        Gson gson = new Gson();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stopsList.addAll(Api.getStopList());
                        currentStopList.addAll(stopsList);
                        //TODO Might need UiThread
                        stopListAdaptor = new ArrayAdapter<Stop>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, currentStopList);
                    }
                });
                //thread1.start();

                carreiraBasicList.addAll(Api.getCarreiraBasicList());
                currentCarreiraBasicList.addAll(carreiraBasicList);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imagesListAdapter = new ImageListAdaptor(getContext(), currentCarreiraBasicList);
                        list.setAdapter(imagesListAdapter);
                    }
                });

            }
        });
        thread.start();



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
                                currentCarreiraBasicList.clear();
                                String textModifiedString;
                                String original = editText.getText().toString();
                                if (editText.getText().length() > 0) {
                                    textModifiedString = original.substring(0, 1).toUpperCase() + original.substring(1);
                                }else{
                                    textModifiedString = "       ";}
                                //Carreiras
                                for (CarreiraBasic cb : carreiraBasicList){
                                    if (cb.toString().contains(original) || cb.toString().contains(textModifiedString)){
                                        currentCarreiraBasicList.add(cb);
                                    }
                                }

                                Log.println(Log.DEBUG, "DATA SET", "Changed to " + currentCarreiraBasicList.size());
                                imagesListAdapter = new ImageListAdaptor(getContext(), currentCarreiraBasicList);
                                list.setAdapter(imagesListAdapter);
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
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String busID = currentCarreiraBasicList.get(i).getId()+"";
                        MainActivity activity = (MainActivity) getActivity();
                        activity.openRouteDetailFragment();
                        RouteDetailsFragment routeDetailFragment = (RouteDetailsFragment) activity.routeDetailsFragment;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                routeDetailFragment.getEditText().setText(busID.toString());
                                routeDetailFragment.getButton().performClick();
                            }
                        });
                    }
                });

                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO STOPS
                    }
                });

                if (!aSwitch.isChecked()){
                    thread1.start();
                }else {
                    thread2.start();
                }
            }
        });

        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aSwitch.isChecked()){
                    list.setAdapter(stopListAdaptor);
                }else{
                    list.setAdapter(imagesListAdapter);
                }
            }
        });
        return v;
    }

}