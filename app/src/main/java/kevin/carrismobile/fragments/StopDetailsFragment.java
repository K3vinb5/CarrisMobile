package kevin.carrismobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.carrismobile.R;

import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.api.Api;
import kevin.carrismobile.data.RealTimeSchedule;
import kevin.carrismobile.data.Stop;
import kevin.carrismobile.custom.MyCustomDialog;

public class StopDetailsFragment extends Fragment {

    public List<RealTimeSchedule> currentStopRealTimeSchedules = new ArrayList<>();
    public ArrayAdapter<RealTimeSchedule> currentStopRealTimeSchedulesAdaptor;
    ListView list;
    TextView stopTitle;
    TextView stopDetails;
    Toolbar stopDetailsToolbar;
    AlertDialog confirmRemovalDialog;
    AlertDialog confirmAdditionDialog;
    Stop currentStop = null;
    boolean currentlyDisplaying = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stop_details_fragment, container, false);

        list = v.findViewById(R.id.stopDetailsList);
        stopTitle = v.findViewById(R.id.textViewStopName);
        stopDetails = v.findViewById(R.id.textViewStopNameDetails);
        stopDetailsToolbar = v.findViewById(R.id.stopDetailstoolbar);

        confirmRemovalDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem removida com sucesso", "A paragem selecionada foi removida da lista de favoritos com sucesso");
        confirmAdditionDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem adicionada com sucesso", "A paragem selecionada foi adiconada á lista de favoritos com sucesso");


        currentStopRealTimeSchedulesAdaptor = new ArrayAdapter<RealTimeSchedule>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, currentStopRealTimeSchedules);
        list.setAdapter(currentStopRealTimeSchedulesAdaptor);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String selectedId = currentStop.getRealTimeSchedules().get(i).getLine_id()+"";
                        MainActivity activity = (MainActivity) getActivity();
                        RouteDetailsFragment routeDetailFragment = (RouteDetailsFragment) activity.routeDetailsFragment;
                        activity.openFragment(routeDetailFragment, 0, true);
                        if(currentStop.isOnline()){
                            routeDetailFragment.loadCarreiraFromApi(selectedId);
                        }else{
                            routeDetailFragment.loadCarreiraOffline(selectedId);
                        }
                    }
                });
                thread1.start();
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
                    Log.d("STOP SCHEDULES", toAdd.toString());
                    if (toAdd != null){
                        currentStopRealTimeSchedules.clear();
                        currentStopRealTimeSchedules.addAll(toAdd);
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopFavoritesFragment fragment = (StopFavoritesFragment)mainActivity.stopFavoritesFragment;
                        //Log.d("DEBUG LIST", currentStopRealTimeSchedules.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentStopRealTimeSchedulesAdaptor.notifyDataSetChanged();
                                stopTitle.setText(stop.getTts_name());
                                stopDetails.setText("Localidade: " + stop.getLocality() + "\nMunicipalidade: " + stop.getMunicipality_name() + "\nDistrito: " + stop.getDistrict_name());
                                currentStop = stop;
                                if (fragment.containsStop(stop)){
                                    stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_24);
                                }else{
                                    stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_border_24);
                                }
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
    public void loadNewOfflineStop(Stop stop){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<RealTimeSchedule> toAdd = new ArrayList<>();
                try{
                    toAdd = stop.getOfflineRealTimeSchedules(); //requires API
                    Log.d("STOP SCHEDULES", toAdd.toString());
                    if (toAdd != null){
                        currentStopRealTimeSchedules.clear();
                        currentStopRealTimeSchedules.addAll(toAdd);
                        MainActivity mainActivity = (MainActivity) getActivity();
                        StopFavoritesFragment fragment = (StopFavoritesFragment)mainActivity.stopFavoritesFragment;
                        //Log.d("DEBUG LIST", currentStopRealTimeSchedules.toString());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentStopRealTimeSchedulesAdaptor.notifyDataSetChanged();
                                stopTitle.setText(stop.getTts_name());
                                stopDetails.setText("Localidade: " + stop.getLocality() + "\nMunicipalidade: " + stop.getMunicipality_name() + "\nDistrito: " + stop.getDistrict_name());
                                currentStop = stop;
                                if (fragment.containsStop(stop)){
                                    stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_24);
                                }else{
                                    stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_border_24);
                                }
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
    public void addCurrentStopToFavorites(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity mainActivity = (MainActivity) getActivity();
                StopFavoritesFragment fragment = (StopFavoritesFragment) mainActivity.stopFavoritesFragment;
                if (fragment.containsStop(currentStop)){
                    fragment.removeStopFromFavorites(currentStop.getStopID()+"");
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            confirmRemovalDialog.show();
                            stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_border_24);
                        }
                    });
                }else {
                    fragment.addStopToFavorites(currentStop);
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            confirmAdditionDialog.show();
                            stopDetailsToolbar.getMenu().getItem(0).setIcon(R.drawable.baseline_star_24);
                        }
                    });
                }
            }
        });
        thread.start();
    }


}