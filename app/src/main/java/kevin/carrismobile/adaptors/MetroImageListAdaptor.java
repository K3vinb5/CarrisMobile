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
import kevin.carrismobile.data.metro.MetroStop;

public class MetroImageListAdaptor extends BaseAdapter {
    Context context;
    Activity activity;
    List<String> textList = new ArrayList<>();
    List<Drawable> imageList = new ArrayList<>();
    List<MetroStop> stopList;
    LayoutInflater inflater;

    public MetroImageListAdaptor(Activity activity, List<MetroStop> stopList){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.inflater = LayoutInflater.from(context);
        this.stopList = stopList;
        for (MetroStop s : stopList){
            textList.add(s.getStop_name() + " (" + s.getLinhas().get(0) + ")");
            imageList.add(getImageId(s.getLinhas().get(0), activity));
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

    public static Drawable getImageId(String line, Activity activity){
        switch (line){
            case "Amarela":
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.metropolitano_de_lisboa_3, null);
            case "Verde":
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.metropolitano_de_lisboa, null);
            case "Azul":
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.metropolitano_de_lisboa_1, null);
            case "Vermelha":
            return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.metropolitano_de_lisboa, null);
        }
        return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.stop_stop_logo, null);
    }
}

