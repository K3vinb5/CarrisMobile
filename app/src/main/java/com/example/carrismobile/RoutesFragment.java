package com.example.carrismobile;

import android.media.Image;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import api.Api;
import data_structure.Carreira;
import data_structure.CarreiraBasic;
import kevin.carrismobile.adaptors.ImageListAdaptor;

public class RoutesFragment extends Fragment {


    ListView list;
    EditText editText;
    Button button;
    ImageListAdaptor imagesListAdapter;
    List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
    List<CarreiraBasic> currentCarreiraBasicList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.route_fragment, container, false);

        list = v.findViewById(R.id.main_list);
        editText = v.findViewById(R.id.editTextRoutes);
        button = v.findViewById(R.id.routesButton);

        Gson gson = new Gson();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCarreiraBasicList.clear();
                for (CarreiraBasic cb : carreiraBasicList){
                    if (cb.toString().contains(editText.getText())){
                        currentCarreiraBasicList.add(cb);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.println(Log.DEBUG, "DATA SET", "Changed to " + currentCarreiraBasicList.size());
                        imagesListAdapter = new ImageListAdaptor(getContext(), currentCarreiraBasicList);
                        list.setAdapter(imagesListAdapter);
                    }
                });
            }
        });

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
                                    textModifiedString = "       ";
                                }
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
        return v;
    }

}