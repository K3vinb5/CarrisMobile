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

import kevin.carrismobile.api.Api;
import kevin.carrismobile.api.Offline;
import kevin.carrismobile.data.CarreiraBasic;
import kevin.carrismobile.data.Direction;
import kevin.carrismobile.data.Stop;
import kevin.carrismobile.adaptors.RouteImageListAdaptor;
import kevin.carrismobile.custom.MyCustomDialog;

public class RoutesFragment extends Fragment {


    ListView list;
    EditText editText;
    Spinner spinner;
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
                        String selectedId = currentCarreiraBasicList.get(i).getId()+"";
                        MainActivity activity = (MainActivity) getActivity();
                        RouteDetailsFragment routeDetailFragment = (RouteDetailsFragment) activity.routeDetailsFragment;
                        activity.openFragment(routeDetailFragment, 0, true);
                        if(online){
                            routeDetailFragment.loadCarreiraFromApi(selectedId);
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
                        listLock.lock();
                        currentCarreiraBasicList.clear();
                        String original = editText.getText().toString();
                        if(original.length() > 0){
                            for (CarreiraBasic cb : carreiraBasicList){
                                if (doesTextContain(cb.toString(), original)){
                                    currentCarreiraBasicList.add(cb);
                                }
                            }
                        }else{
                            currentCarreiraBasicList.addAll(carreiraBasicList);
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
        });
    }

    private void initCarreiraBasicList() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<CarreiraBasic> toAdd;
                try{
                    toAdd = Api.getCarreiraBasicList();
                    carreiraBasicList.addAll(toAdd);
                    carreiraBasicList.addAll(Offline.getCarreiraList());
                    carreiraBasicList.sort(Comparator.comparing(CarreiraBasic::getId));
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
                        imagesListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraBasicList);
                        list.setAdapter(imagesListAdapter);
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
                imagesListAdapter = new RouteImageListAdaptor(getActivity(), currentCarreiraBasicList);
                list.setAdapter(imagesListAdapter);
                listLock.unlock();
            }
        });
    }

    private void setSpinnerList() {
        spinnerList.add("Todas");
        spinnerList.add("Carris Metropolitana");
        spinnerList.add("Carris");
        spinnerList.add("CP - Comboios Urbanos");
        spinnerList.add("Fertagus");
        spinnerList.add("MobiCascais");
        spinnerListAdaptor = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_list, R.id.listText, spinnerList);
        spinner.setAdapter(spinnerListAdaptor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
                        break;
                    case 1:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().equals("-1"));
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
                        break;
                    case 2:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().equals("0"));
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
                        break;
                    case 3:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().equals("1"));
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
                        break;
                    case 4:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().equals("2"));
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
                        break;
                    case 5:
                        currentCarreiraBasicList.clear();
                        currentCarreiraBasicList.addAll(carreiraBasicList);
                        currentCarreiraBasicList.removeIf(carreiraBasic -> !carreiraBasic.getAgency_id().equals("3"));
                        Log.d("DEBUG", currentCarreiraBasicList.toString());
                        updateList();
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