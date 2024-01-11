package kevin.carrismobile.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.carrismobile.R;

import kevin.carrismobile.adaptors.MetroImageListAdaptor;
import kevin.carrismobile.api.MetroApi;
import kevin.carrismobile.custom.MyCustomDialog;
import kevin.carrismobile.data.metro.MetroStop;

public class MetroStationFragment extends Fragment {
    TextView title;
    TextView firstDestiny;
    TextView firstDesinyFirstTime;
    TextView firstDesinySecondTime;
    TextView firstDesinyThirdTime;
    TextView secondDestiny;
    TextView secondDestinyFirstTime;
    TextView secondDestinySecondTime;
    TextView secondDestinyThirdTime;
    ImageView metroStationLineImage;
    AlertDialog errorDialog;
    MetroStop currentStop;
    private BackgroundThread backgroundThread;
    private boolean backgroundThreadStarted = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.metro_station_fragment, container, false);
        title = v.findViewById(R.id.metroStationTitle);
        firstDestiny = v.findViewById(R.id.firstDestiniy);
        firstDesinyFirstTime = v.findViewById(R.id.firstDestiniyFirstTime);
        firstDesinySecondTime = v.findViewById(R.id.firstDestiniySecondTime);
        firstDesinyThirdTime = v.findViewById(R.id.firstDestiniyThirdTime);
        secondDestiny = v.findViewById(R.id.secondDestiniy);
        secondDestinyFirstTime = v.findViewById(R.id.secondDestiniyFirstTime);
        secondDestinySecondTime = v.findViewById(R.id.secondDestiniySecondTime);
        secondDestinyThirdTime = v.findViewById(R.id.secondDestiniyThirdTime);
        metroStationLineImage = v.findViewById(R.id.metroStationLineImage);
        errorDialog = MyCustomDialog.createOkButtonDialog(getContext(), "Error accessing Metro Api", "Please chekc your Internet connection");
        return v;
    }

    public void loadMetroStop(String stop_id, String line, boolean isThread){
        Fragment fragment = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isThread && backgroundThreadStarted){
                    backgroundThread.setLine(line);
                    backgroundThread.setStop_id(stop_id);
                }
                currentStop = MetroApi.getMetroStopArrivals(stop_id);
                currentStop.getWaitTimes().removeIf(waitTime -> !waitTime.getDestination().getLinhas().contains(line));
                getActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try{
                            firstDestiny.setVisibility(View.VISIBLE);
                            firstDesinyFirstTime.setVisibility(View.VISIBLE);
                            firstDesinySecondTime.setVisibility(View.VISIBLE);
                            firstDesinyThirdTime.setVisibility(View.VISIBLE);

                            secondDestiny.setVisibility(View.VISIBLE);
                            secondDestinyFirstTime.setVisibility(View.VISIBLE);
                            secondDestinySecondTime.setVisibility(View.VISIBLE);
                            secondDestinyThirdTime.setVisibility(View.VISIBLE);
                            metroStationLineImage.setImageDrawable(MetroImageListAdaptor.getImageId(line, getActivity()));
                            title.setText(currentStop.getStop_name() + " (" + line +")");
                            if (currentStop.getWaitTimes().size() > 1){
                                firstDestiny.setText(currentStop.getWaitTimes().get(0).getDestination().getStop_name() + " (" + line +")");
                                firstDesinyFirstTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(0).getTimeLeft() + " min");
                                firstDesinySecondTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(1).getTimeLeft() + " min");
                                firstDesinyThirdTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(2).getTimeLeft() + " min");

                                secondDestiny.setText(currentStop.getWaitTimes().get(1).getDestination().getStop_name() + " (" + line +")");
                                secondDestinyFirstTime.setText(currentStop.getWaitTimes().get(1).getArrivalTimes().get(0).getTimeLeft() + " min");
                                secondDestinySecondTime.setText(currentStop.getWaitTimes().get(1).getArrivalTimes().get(1).getTimeLeft() + " min");
                                secondDestinyThirdTime.setText(currentStop.getWaitTimes().get(1).getArrivalTimes().get(2).getTimeLeft() + " min");
                            } else if (currentStop.getWaitTimes().size() == 1) {
                                firstDestiny.setText(currentStop.getWaitTimes().get(0).getDestination().getStop_name() + " (" + line +")");
                                firstDesinyFirstTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(0).getTimeLeft() + " min");
                                firstDesinySecondTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(1).getTimeLeft() + " min");
                                firstDesinyThirdTime.setText(currentStop.getWaitTimes().get(0).getArrivalTimes().get(2).getTimeLeft() + " min");

                                secondDestiny.setVisibility(View.INVISIBLE);
                                secondDestinyFirstTime.setVisibility(View.INVISIBLE);
                                secondDestinySecondTime.setVisibility(View.INVISIBLE);
                                secondDestinyThirdTime.setVisibility(View.INVISIBLE);
                            } else{
                                firstDestiny.setVisibility(View.INVISIBLE);
                                firstDesinyFirstTime.setVisibility(View.INVISIBLE);
                                firstDesinySecondTime.setVisibility(View.INVISIBLE);
                                firstDesinyThirdTime.setVisibility(View.INVISIBLE);

                                secondDestiny.setVisibility(View.INVISIBLE);
                                secondDestinyFirstTime.setVisibility(View.INVISIBLE);
                                secondDestinySecondTime.setVisibility(View.INVISIBLE);
                                secondDestinyThirdTime.setVisibility(View.INVISIBLE);
                            }
                        }catch (Exception e){
                            Log.e("ERROR", e.getMessage()+"");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorDialog.show();
                                }
                            });
                        }
                    }
                });
                if (!backgroundThreadStarted){
                    backgroundThread = new BackgroundThread(stop_id, line, fragment);
                    backgroundThreadStarted = true;
                    backgroundThread.start();
                }
            }
        });
        thread.start();

    }

    private class BackgroundThread extends Thread{
        private String stop_id;
        private String line;
        private final Fragment fragment;
        public BackgroundThread(String stop_id, String line, Fragment fragment){
            this.stop_id = stop_id;
            this.line = line;
            this.fragment = fragment;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(10000);
                    if (fragment.isHidden()){
                        continue;
                    }
                    loadMetroStop(stop_id, line, true);
                    Log.d("METRO STATION BACKGROUND THREAD", "UPDATE");

                } catch (InterruptedException e) {
                    Log.e("ERROR IN METRO STATION BACKGROUND THREAD", e.getMessage() + "");
                    break;
                }
            }
        }
        public void setStop_id(String stop_id) {
            this.stop_id = stop_id;
        }

        public void setLine(String line) {
            this.line = line;
        }
    }
}