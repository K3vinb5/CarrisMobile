package kevin.carrismobile.adaptors;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.carrismobile.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import kevin.carrismobile.data.bus.Stop;

public class StopImageListAdaptor extends BaseAdapter {

    Context context;
    Activity activity;
    List<String> textList = new ArrayList<>();
    List<Drawable> imageList = new ArrayList<>();
    List<Stop> stopList;
    LayoutInflater inflater;

    public StopImageListAdaptor(Activity activity, List<Stop> stopList){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.inflater = LayoutInflater.from(context);
        this.stopList = stopList;
        for (Stop s : stopList){
            textList.add(s.getTts_name());
            imageList.add(getImageId(s.getFacilities(), s.getTts_name(), s.getAgency_id(), activity));
        }
    }

    @Override
    public int getCount() {
        return textList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    /*public ImageView getImageViewItem(int i){
        return imageList.get(i);
    }*/

    public String getStringItem(int i){
        return textList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.image_list, null);
        TextView textView = (TextView) view.findViewById(R.id.listText);
        textView.setText(textList.get(i));
        ImageView imageView = (ImageView) view.findViewById(R.id.listImage);
        Drawable imageDrawable = imageList.get(i);
        imageView.setImageDrawable(imageDrawable);
        return view;
    }

    public static Drawable getImageId(List<String> facilities, String tts_name, String agency_id, Activity activity){
        if(agency_id.equals("-1")) {
            if (facilities.contains("subway")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_metro_logo, null);
            } else if (facilities.contains("train")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_train_logo, null);
            } else if (facilities.contains("light_rail")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_lightrail_logo, null);
            } else if (facilities.contains("boat")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_boat_logo, null);
            } else if (facilities.contains("hospital")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_hospital_logo, null);
            } else if (doesTextContain(tts_name, "escola")) {
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_school_logo, null);
            }else{
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_stop_logo, null);
            }
        }else if(agency_id.equals("0")){
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_carris_min, null);
        } else if (agency_id.equals("1")) {
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_train_logo, null);
        }else if(agency_id.equals("2")){
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_train_logo, null);
        }else if(agency_id.equals("3")){
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_stop_logo, null);
        }
        return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_stop_logo, null);
    }

    public static boolean doesTextContain(String text1, String text2) {
        // Normalize and remove accents from both texts
        String normalizedText1 = normalizeAndRemoveAccents(text1);
        String normalizedText2 = normalizeAndRemoveAccents(text2);

        // Perform a case-insensitive substring check
        return normalizedText1.toLowerCase().contains(normalizedText2.toLowerCase());
    }

    private static String normalizeAndRemoveAccents(String text) {
        // Normalize the text to NFD form to separate accents and characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Use a regular expression to remove accents and non-alphanumeric characters
        String regex = "\\p{InCombiningDiacriticalMarks}+";
        String stripped = normalized.replaceAll(regex, "");

        // Remove non-alphanumeric characters and convert to lowercase
        stripped = stripped.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        return stripped;
    }
}
