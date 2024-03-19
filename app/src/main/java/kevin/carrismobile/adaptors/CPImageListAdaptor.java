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

import java.util.ArrayList;
import java.util.List;
import kevin.carrismobile.data.train.CPStopBasic;

public class CPImageListAdaptor extends BaseAdapter {
    Context context;
    Activity activity;
    List<String> textList = new ArrayList<>();
    List<Drawable> imageList = new ArrayList<>();
    List<CPStopBasic> cpStopBasicList;
    LayoutInflater inflater;

    public CPImageListAdaptor(Activity activity, List<CPStopBasic> cpStopBasicList){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.inflater = LayoutInflater.from(context);
        this.cpStopBasicList = cpStopBasicList;
        for (CPStopBasic sb : cpStopBasicList){
            textList.add(sb.getNode_name());
            imageList.add(getImageId(activity));
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

    public static Drawable getImageId(Activity activity){
        return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.logo_cp, null);
    }
}