package kevin.carrismobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.carrismobile.R;
import com.google.gson.Gson;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import kevin.carrismobile.adaptors.CPImageListAdaptor;
import kevin.carrismobile.adaptors.MetroImageListAdaptor;
import kevin.carrismobile.api.MetroApi;
import kevin.carrismobile.api.OfflineCP;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.data.metro.MetroStop;
import kevin.carrismobile.data.metro.MetroWaitTime;
import kevin.carrismobile.data.train.CPStopBasic;

public class TrainsFragment extends Fragment {
    public List<MetroStop> metroList;
    public List<MetroStop> currentMetroList = new ArrayList<>();
    public MetroImageListAdaptor metroImageListAdaptor;
    public List<CPStopBasic> cpList;
    public List<CPStopBasic> currentCpList = new ArrayList<>();
    public CPImageListAdaptor cpImageListAdaptor;
    String mode = "metro"; //initial value
    public ListView listView;
    public Button metroLisboaButton;
    public Button comboiosButton;
    public Button mapaButton;
    EditText editText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.trains_fragment, container, false);
        listView = v.findViewById(R.id.listView);
        metroLisboaButton = v.findViewById(R.id.button1);
        comboiosButton = v.findViewById(R.id.button2);
        mapaButton = v.findViewById(R.id.button3);
        editText = v.findViewById(R.id.trainsEditText);
        setComboiosButton();
        setMetroLisboaButton();
        setListView();
        setEditText();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                metroImageListAdaptor = new MetroImageListAdaptor(getActivity(), currentMetroList);
                cpImageListAdaptor = new CPImageListAdaptor(getActivity(), currentCpList);
                listView.setAdapter(metroImageListAdaptor);
            }
        });
        return v;
    }
    private void setListView() {
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               if (mode.equals("metro")){
                   //mode metro
                   MetroStationFragment fragment = (MetroStationFragment) ((MainActivity)getActivity()).metroStationFragment;
                   fragment.loadMetroStop(currentMetroList.get(i).getStop_id(), currentMetroList.get(i).getLinhas().get(0), false);
                   ((MainActivity)getActivity()).openFragment(fragment, 0, false);
               }else {
                   //mode cp
                   CPStationFragment fragment = (CPStationFragment) ((MainActivity)getActivity()).cpStationFrament;
                   fragment.loadNewCPStation(currentCpList.get(i).getNode(),false, currentCpList.get(i).getNode_name());
                   ((MainActivity)getActivity()).openFragment(fragment, 0, false);

               }
           }
       });
    }

    private void setMetroLisboaButton(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    metroList = MetroApi.getAllStops();
                    assert metroList != null;
                    List<MetroStop> seen = new ArrayList<>();
                    for (MetroStop stop : metroList){
                        if (!seen.contains(stop)){
                            seen.add(stop);
                        }else{
                            stop.getLinhas().remove(0);
                        }
                    }
                    metroList.sort(Comparator.comparing(MetroStop::getStop_name));
                    currentMetroList.clear();
                    currentMetroList.addAll(metroList);
                }catch (Exception e){
                    Log.e("ERROR", e.getMessage());
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        metroLisboaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        comboiosButton.setTextColor(Color.GRAY);
                        mapaButton.setTextColor(Color.GRAY);
                        metroLisboaButton.setTextColor(Color.BLACK);
                    }
                });
                mode = "metro";
                metroImageListAdaptor = new MetroImageListAdaptor(getActivity(), currentMetroList);
                listView.setAdapter(metroImageListAdaptor);
                editText.setText("");
            }
        });
    }

    private void setComboiosButton(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Log.e("ERROR", "Calling OfflineCP");
                    cpList = OfflineCP.getStops();
                    currentCpList.clear();
                    currentCpList.addAll(cpList);
                }catch (Exception e){
                    Log.e("ERROR", e.getMessage());
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        comboiosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        comboiosButton.setTextColor(Color.BLACK);
                        mapaButton.setTextColor(Color.GRAY);
                        metroLisboaButton.setTextColor(Color.GRAY);
                    }
                });
                mode = "cp";
                cpImageListAdaptor = new CPImageListAdaptor(getActivity(), currentCpList);
                listView.setAdapter(cpImageListAdaptor);
                editText.setText("");
            }
        });
    }

    private void setMapaButton(){

    }

    private void setEditText() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mode.equals("cp")){
                            synchronized (cpList){
                                String text = editText.getText().toString();
                                currentCpList.clear();
                                for (CPStopBasic cpStopBasic : cpList) {
                                    if (doesTextContain(cpStopBasic.getNode_name(), text)){
                                        currentCpList.add(cpStopBasic);
                                    }
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cpImageListAdaptor = new CPImageListAdaptor(getActivity(), currentCpList);
                                        listView.setAdapter(cpImageListAdaptor);
                                    }
                                });
                            }
                        }else{
                            synchronized (metroList){
                                String text = editText.getText().toString();
                                currentMetroList.clear();
                                for (MetroStop metroStop : metroList) {
                                    if (doesTextContain(metroStop.toString(), text)){
                                        currentMetroList.add(metroStop);
                                    }
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        metroImageListAdaptor = new MetroImageListAdaptor(getActivity(), currentMetroList);
                                        listView.setAdapter(metroImageListAdaptor);
                                    }
                                });
                            }
                        }
                    }
                });
                thread.start();
            }
        });
    }

    public static boolean doesTextContain(String text1, String text2) {
        // Normalize and remove accents from both texts
        String normalizedText1 = normalizeAndRemoveAccents(text1);
        String normalizedText2 = normalizeAndRemoveAccents(text2);

        // Perform a case-insensitive substring check
        return normalizedText1.contains(normalizedText2);
    }

    private static String normalizeAndRemoveAccents(String text) {
        StringBuilder stripped = new StringBuilder();

        // Normalize the text to NFD form to separate accents and characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Iterate over each character in the normalized string
        for (char c : normalized.toCharArray()) {
            // Check if the character is alphanumeric
            if (Character.isLetterOrDigit(c)) {
                stripped.append(c);
            }
        }

        // Convert to lowercase
        return stripped.toString().toLowerCase();
    }
}