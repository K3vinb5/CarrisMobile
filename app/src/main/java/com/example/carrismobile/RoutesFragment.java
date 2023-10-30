package com.example.carrismobile;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import kevin.carrismobile.adaptors.MyCustomDialog;

public class RoutesFragment extends Fragment {


    ListView list;
    EditText editText;
    ImageListAdaptor imagesListAdapter;
    ArrayAdapter<Stop> stopListAdaptor;
    List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
    List<Stop> stopsList = new ArrayList<>();
    List<CarreiraBasic> currentCarreiraBasicList = new ArrayList<>();
    List<Stop> currentStopList = new ArrayList<>();
    AlertDialog dialog;
    public boolean connected = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.route_fragment, container, false);

        list = v.findViewById(R.id.main_list);
        editText = v.findViewById(R.id.editTextRoutes);


        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet");

        Gson gson = new Gson();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Stop> toAdd;
                        try{
                            toAdd = Api.getStopList();
                            connected = true;
                        }catch (Exception e){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.show();
                                }
                            });
                            connected = false;
                            return;
                        }
                        stopsList.addAll(toAdd);
                        currentStopList.addAll(stopsList);
                        //TODO Might need UiThread
                        stopListAdaptor = new ArrayAdapter<Stop>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, currentStopList);
                    }
                });
                //thread1.start();
                List<CarreiraBasic> toAdd;
                try{
                    toAdd = Api.getCarreiraBasicList();
                    carreiraBasicList.addAll(toAdd);
                    connected = true;
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                    connected = false;
                    return;
                }
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
                        try {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {

                        }
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
                thread1.start();
            }
        });
        return v;
    }

}