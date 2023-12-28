package kevin.carrismobile.adaptors;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
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

import kevin.carrismobile.data.Carreira;
import kevin.carrismobile.data.CarreiraBasic;
import kevin.carrismobile.gui.TextDrawable;

public class RouteImageListAdaptor extends BaseAdapter {

    Context context;
    Activity activity;
    List<String> textList = new ArrayList<>();
    List<String> textIdLit = new ArrayList<>();
    List<Drawable> imageList = new ArrayList<>();
    List<CarreiraBasic> carreiraBasicList;
    LayoutInflater inflater;

    public RouteImageListAdaptor(Activity activity, List<CarreiraBasic> carreiraBasicList){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.inflater = LayoutInflater.from(context);
        this.carreiraBasicList = carreiraBasicList;
        for (CarreiraBasic cb : carreiraBasicList){
            textList.add(cb.getLong_name());
            textIdLit.add(cb.getId());
            imageList.add(getImageId(cb.getColor()));
        }
    }

    public RouteImageListAdaptor(Activity activity, List<Carreira> carreiraList, int a){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.inflater = LayoutInflater.from(context);
        List<CarreiraBasic> carreiraBasicList = new ArrayList<>();
        for (Carreira c : carreiraList){
            carreiraBasicList.add(CarreiraBasic.newCarreiraBasicFromCarreira(c));
        }
        this.carreiraBasicList = carreiraBasicList;
        for (CarreiraBasic cb : carreiraBasicList){
            textList.add(cb.getLong_name());
            textIdLit.add(cb.getId());
            imageList.add(getImageId(cb.getColor()));
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
        Drawable textDrawable = new TextDrawable(activity.getResources(), textIdLit.get(i), 15);

        LayerDrawable finalDrawable = new LayerDrawable(new Drawable[] {imageDrawable, textDrawable});
        float scaleImageX = 1.2f;
        float scaleImageY = 1.55f;
        finalDrawable.setLayerSize(0,(int) (imageDrawable.getIntrinsicWidth() * scaleImageX),(int)(imageDrawable.getIntrinsicHeight() * scaleImageY));
        finalDrawable.setLayerGravity(1, Gravity.CENTER_HORIZONTAL);
        finalDrawable.setLayerSize(1,textDrawable.getIntrinsicWidth(), textDrawable.getIntrinsicHeight());
        imageView.setImageDrawable(finalDrawable);
        //imageView.setImageResource(imageList.get(i));
        return view;
    }

    private Drawable getImageId(String string){
        switch (string) {
            case "#ED1944":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_ed1944, null);
            case "#C61D23":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_c61d23, null);
            case "#BB3E96":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_bb3e96, null);
            case "#3D85C6":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_3d85c6, null);
            case "#2A9057":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_2a9057, null);
            case "#FDB71A":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_fdb71a, null);
            case "color_cascais":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_cascais, null);
            case "color_carris":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_carris, null);
            case "color_cp":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_cp, null);
            case "color_fertagus":
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_fertagus, null);
            default:
                return ResourcesCompat.getDrawable(activity.getResources(), R.drawable.color_00b8b0, null);
        }
    }
}
