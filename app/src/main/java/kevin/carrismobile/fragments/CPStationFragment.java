package kevin.carrismobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.carrismobile.R;

import java.util.List;

import kevin.carrismobile.api.CPApi;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.data.train.CPStop;

public class CPStationFragment extends Fragment {
    TextView currentStationTextView;
    ListView currentStationTrips;
    AlertDialog errorDialog;
    List<CPStop> currentTrips;
    ArrayAdapter<CPStop> currentTripsAdapter;
    BackgroundThread backgroundThread;
    boolean backgroundThreadStarted = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.cp_station_fragment, container, false);
        currentStationTextView = v.findViewById(R.id.currentStation);
        currentStationTrips = v.findViewById(R.id.cpStationList);
        errorDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Error accessing Metro Api", "Please chekc your Internet connection");
        return v;
    }

    public void loadNewCPStation(String currentNode, boolean isThread, String name){
        Fragment fragment = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(!isThread && backgroundThreadStarted){
                    backgroundThread.setCurrentNode(currentNode);
                    backgroundThread.setNodeName(name);
                    Log.d("DEBUG CP STATION", currentNode + " " + name);
                }
                currentTrips = CPApi.getStopArrivals(currentNode);
                if (currentTrips.isEmpty()){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyCustomDialog.createOkButtonDialog(getContext(), "Erro a atualizar Lista", "Erro de conexão, por vafor verifique a sua conexão á internet").show();
                        }
                    });
                    return;
                }
                Log.d("DEBUG CP STATION", currentTrips+"");
                currentTrips.removeIf(cpStop -> !cpStop.getServiceType().equals("U"));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentStationTextView.setText(name);
                            currentTripsAdapter = new ArrayAdapter<CPStop>(getContext(), R.layout.simple_list, R.id.listText, currentTrips);
                            currentStationTrips.setAdapter(currentTripsAdapter);
                        }catch (Exception e ){
                            Log.e("ERROR", e.getMessage()+"");
                            errorDialog.show();
                        }
                    }
                });
                if (!backgroundThreadStarted){
                    backgroundThread = new BackgroundThread(currentNode, name, fragment);
                    backgroundThreadStarted = true;
                    backgroundThread.start();
                }
            }
        });
        thread.start();
    }


    public class BackgroundThread extends Thread{
        private String currentNode;
        private String name;
        private final Fragment fragment;

        public BackgroundThread(String currentNode, String name, Fragment fragment){
            this.currentNode = currentNode;
            this.name = name;
            this.fragment = fragment;
        }
        public void setNodeName(String name) {
            this.name = name;
        }

        public void setCurrentNode(String currentNode) {
            this.currentNode = currentNode;
        }

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(30000); //30secs
                    if (fragment.isHidden()){
                        continue;
                    }
                    Log.d("DEBUG CPSTATION", "UPDATED " + currentNode + "-" + name);
                    loadNewCPStation(currentNode, true, name);
                }catch (Exception e) {
                    Log.e("ERROR", e.getMessage()+"");
                    break;
                }
            }
        }
    }
}