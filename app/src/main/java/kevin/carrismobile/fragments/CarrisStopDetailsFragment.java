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

import kevin.carrismobile.api.ScrapingCarrisApi;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.data.bus.Stop;
import kevin.carrismobile.data.bus.carris.CarrisWaitTimes;

public class CarrisStopDetailsFragment extends Fragment{
    TextView textViewStopName;
    TextView textViewStopNameDetails;
    List<CarrisWaitTimes> currentWaitTimesList = new ArrayList<>();
    ArrayAdapter<CarrisWaitTimes> currentWaitTimesListArrayAdapeter;
    ListView stopDetailsList;
    Toolbar stopDetailsToolbar;
    AlertDialog confirmRemovalDialog;
    AlertDialog confirmAdditionDialog;
    Stop currentStop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.carris_stop_details_fragment, container, false);
        textViewStopName = v.findViewById(R.id.carrisTextViewStopName);
        stopDetailsList = v.findViewById(R.id.carrisStopDetailsList);
        textViewStopNameDetails = v.findViewById(R.id.carrisTextViewStopNameDetails);
        currentWaitTimesListArrayAdapeter = new ArrayAdapter<CarrisWaitTimes>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, currentWaitTimesList);
        stopDetailsList.setAdapter(currentWaitTimesListArrayAdapeter);
        confirmRemovalDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem removida com sucesso", "A paragem selecionada foi removida da lista de favoritos com sucesso");
        confirmAdditionDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Paragem adicionada com sucesso", "A paragem selecionada foi adiconada á lista de favoritos com sucesso");
        stopDetailsToolbar = v.findViewById(R.id.carrisStopDetailstoolbar);
        stopDetailsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity activity = (MainActivity) getActivity();
                activity.openFragment(activity.routeDetailsFragment, 0, true);
                ((RouteDetailsFragment)activity.routeDetailsFragment).loadCarreiraFromApi(currentWaitTimesList.get(i).getRouteId(), "0", currentWaitTimesList.get(i).getRouteName());
            }
        });
        return v;
    }

    public void loadCarrisStop(Stop stop){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assert stop != null;
                    currentStop = stop;
                    currentWaitTimesList.clear();
                    currentWaitTimesList.addAll(ScrapingCarrisApi.getStopWaitTimes(currentStop.getStopID(), currentStop.getTts_name()));
                    Log.d("LOADED WAIT TIMES", "List :" + currentWaitTimesList);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentWaitTimesListArrayAdapeter.notifyDataSetChanged();
                            textViewStopNameDetails.setText("Proximas chegadas em tempo real");
                            textViewStopName.setText(currentStop.getStopID() + " - " + currentStop.getTts_name());
                        }
                    });
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyCustomDialog.createOkButtonDialog(getContext(), "Network Error", "Message :" + e.getMessage()).show();
                        }
                    });
                }
            }
        }).start();
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