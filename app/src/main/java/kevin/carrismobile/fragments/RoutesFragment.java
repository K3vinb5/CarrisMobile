package kevin.carrismobile.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.AlertDialog;
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
import android.widget.Spinner;

import com.example.carrismobile.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kevin.carrismobile.api.CarrisMetropolitanaApi;
import kevin.carrismobile.api.CarrisApi;
import kevin.carrismobile.api.Offline;
import kevin.carrismobile.data.bus.CarreiraBasic;
import kevin.carrismobile.adaptors.RouteImageListAdaptor;
import kevin.carrismobile.custom.MyCustomDialog;

public class RoutesFragment extends Fragment {


    ListView list;
    EditText editText;
    TextWatcher textWatcher;
    Spinner spinner;
    String currentSelectedAgencyId = "-10";
    RouteImageListAdaptor imagesListAdapter;
    ArrayAdapter<String> spinnerListAdaptor;
    List<String> spinnerList = new ArrayList<>();
    List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
    Lock listLock = new ReentrantLock();
    List<CarreiraBasic> currentCarreiraBasicList = new ArrayList<>();
    AlertDialog dialog;
    public boolean connected = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.route_fragment, container, false);

        list = v.findViewById(R.id.main_list);
        editText = v.findViewById(R.id.editTextRoutes);
        spinner = v.findViewById(R.id.listFilterSpinner);
        dialog = MyCustomDialog.createOkButtonDialog(getContext(), "Erro de conexão", "Não foi possível conectar à API da Carris Metropolitana, verifique a sua ligação á internet");
        Offline.init(getActivity());
        initCarreiraBasicList();
        setSpinnerList();
        setEditText();
        setList();
        return v;
    }

    private void setList() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception ignored) {

                        }
                        boolean online = currentCarreiraBasicList.get(i).isOnline();
                        String selectedId = currentCarreiraBasicList.get(i).getRouteId()+"";
                        MainActivity activity = (MainActivity) getActivity();
                        RouteDetailsFragment routeDetailFragment = (RouteDetailsFragment) activity.routeDetailsFragment;
                        activity.openFragment(routeDetailFragment, 0, true);
                        if(online){
                            if(currentCarreiraBasicList.get(i).getAgency_id().equals("-1")){
                                routeDetailFragment.loadCarreiraFromApi(selectedId, currentCarreiraBasicList.get(i).getAgency_id(), currentCarreiraBasicList.get(i).getLong_name());
                            }else if (currentCarreiraBasicList.get(i).getAgency_id().equals("0")){
                                routeDetailFragment.loadCarreiraFromApi(selectedId, currentCarreiraBasicList.get(i).getAgency_id(), currentCarreiraBasicList.get(i).getLong_name());
                            }
                        }else {
                            routeDetailFragment.loadCarreiraOffline(selectedId);
                        }
                    }
                });
                thread1.start();
            }
        });
    }

    private void setEditText() {
        textWatcher = new TextWatcher() {
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
                        listLock.lock();
                        currentCarreiraBasicList.clear();
                        String original = editable.toString();
                        if(original.length() > 0){
                            for (CarreiraBasic cb : carreiraBasicList){
                                if (doesTextContain(cb.toString(), original) && currentSelectedAgencyId.contains(cb.getAgency_id())){
                                    currentCarreiraBasicList.add(cb);
                                }
                            }
                        }
                        else if(currentSelectedAgencyId.equals("-10")){
                            currentCarreiraBasicList.addAll(carreiraBasicList);
                        }
                        else{
                            currentCarreiraBasicList.addAll(carreiraBasicList);
                            currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().contains(currentSelectedAgencyId));
                        }
                        listLock.unlock();
                    }
                });
                thread1.start();
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.println(Log.DEBUG, "DATA SET", "Changed to " + currentCarreiraBasicList.size());
                        updateList();
                    }
                });
                thread2.start();
            }
        };
        editText.addTextChangedListener(textWatcher);
    }

    private void initCarreiraBasicList() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<CarreiraBasic> toAdd;
                try{
                    toAdd = CarrisMetropolitanaApi.getCarreiraBasicList();
                    carreiraBasicList.addAll(toAdd);
                    toAdd = CarrisApi.getCarreiraBasicList();
                    carreiraBasicList.addAll(toAdd);
                    carreiraBasicList.addAll(Offline.getCarreiraList());
                    carreiraBasicList.sort(Comparator.comparing(CarreiraBasic::getRouteId));
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
                        listLock.lock();
                        synchronized (currentCarreiraBasicList){
                            imagesListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraBasicList);
                        }
                        list.setAdapter(imagesListAdapter);
                        listLock.unlock();
                    }
                });

            }
        });
        thread.start();
    }

    private void updateList(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listLock.lock();
                synchronized (currentCarreiraBasicList){
                    imagesListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraBasicList);
                }
                list.setAdapter(imagesListAdapter);
                listLock.unlock();
            }
        });
    }

    private void refreshList(){
        textWatcher.afterTextChanged(editText.getText());
    }

    private void setSpinnerList() {
        spinnerList.add("Todas");
        spinnerList.add("Carris Metropolitana");
        spinnerList.add("Carris");
        spinnerListAdaptor = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, spinnerList);
        spinner.setAdapter(spinnerListAdaptor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        currentSelectedAgencyId = "-10";
                        //updateList();
                        refreshList();
                        break;
                    case 1:
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        currentSelectedAgencyId = "-1";
                        //updateList();
                        refreshList();
                        break;
                    case 2:
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        currentSelectedAgencyId = "0";
                        //updateList();
                        refreshList();
                        break;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
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