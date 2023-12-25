package kevin.carrismobile.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
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

import com.example.carrismobile.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kevin.carrismobile.data.Stop;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.adaptors.StopImageListAdaptor;

public class StopFavoritesFragment extends Fragment {

    private SharedPreferences mPrefs;
    private List<Stop> stopList = new ArrayList<>();
    private List<Stop> currentStopList = new ArrayList<>();
    private StopImageListAdaptor stopImageListAdaptor;
    boolean removeListSelectionDecision = false;
    ListView list;
    Button seeRoutesFavorites;
    EditText editText;
    AlertDialog confirmRemoval;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.stop_favorites_fragment, container, false);

        list = v.findViewById(R.id.stopFavoritesList);
        editText = v.findViewById(R.id.editTextFavorites);
        seeRoutesFavorites = v.findViewById(R.id.SeeRoutesFavorites);

        confirmRemoval = MyCustomDialog.createYesAndNoButtonDialogStopFavorite(getContext(), "Queres eliminar as tuas paragens favoritas?", "Tens a certeza que queres eliminar as paragens selecionadas neste preciso momento da lista de paragens favoritas?", getActivity());

        init();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stop selectedStop = currentStopList.get(index);
                        if (selectedStop == null){
                            return;
                        }
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopDetailsFragment stopDetailsFragment = (StopDetailsFragment) mainActivity.stopDetailsFragment;
                        mainActivity.openFragment(stopDetailsFragment, 0, false);
                        if (selectedStop.isOnline()){
                            stopDetailsFragment.loadNewStop(selectedStop.getStopID()+"");
                        }else{
                            stopDetailsFragment.loadNewOfflineStop(selectedStop);
                        }
                    }
                });
                thread.start();
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
                                currentStopList.clear();
                                String textModifiedString;
                                String original = editText.getText().toString();
                                if (editText.getText().length() > 0) {
                                    textModifiedString = original.substring(0, 1).toUpperCase() + original.substring(1);
                                }else{
                                    textModifiedString = "       ";}
                                //Carreiras
                                for (Stop st : stopList){
                                    if (st.toString().contains(original) || st.toString().contains(textModifiedString)){
                                        currentStopList.add(st);
                                    }
                                }
                                currentStopList.sort(Comparator.comparing(Stop::getTts_name));
                                Log.println(Log.DEBUG, "DATA SET", "Changed to " + currentStopList.size());
                                stopImageListAdaptor = new StopImageListAdaptor(getActivity(), currentStopList);
                                list.setAdapter(stopImageListAdaptor);
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

        seeRoutesFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.openFragment(mainActivity.routeFavoritesFragment, 1, false);
            }
        });

        return v;
    }

    private void init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                currentStopList.addAll(stopList);
                currentStopList.sort(Comparator.comparing(Stop::getTts_name));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopImageListAdaptor = new StopImageListAdaptor(getActivity(), currentStopList);
                        list.setAdapter(stopImageListAdaptor);
                    }
                });
            }
        }).start();
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
                                editText.setText(""); //clears editText
                                currentStopList.clear();
                                currentStopList.addAll(stopList);
                                currentStopList.sort(Comparator.comparing(Stop::getTts_name));
                                stopImageListAdaptor = new StopImageListAdaptor(getActivity(), currentStopList);
                                list.setAdapter(stopImageListAdaptor);
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
                    if (stopId.equals(stop.getStopID())){
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
                        editText.setText(""); //clears editText
                        currentStopList.clear();
                        currentStopList.addAll(stopList);
                        currentStopList.sort(Comparator.comparing(Stop::getTts_name));
                        stopImageListAdaptor = new StopImageListAdaptor(getActivity(), currentStopList);
                        list.setAdapter(stopImageListAdaptor);
                    }
                });
                Log.d("SUCCESS", "Stop was removed from favorites list");

            }
        });
        thread.start();
    }

    private void storeObject(String json, String key){

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
    private Object loadObject(String key, Class klass){
        return new Gson().fromJson(mPrefs.getString(key, null), klass);
    }
    public boolean containsStop(Stop stop){
        return  stopList.contains(stop);
    }

    public void setRemoveListSelectionDecision(boolean removeListSelectionDecision) {
        this.removeListSelectionDecision = removeListSelectionDecision;
    }

    public boolean getRemoveListSelectionDecision() {
        return removeListSelectionDecision;
    }
}