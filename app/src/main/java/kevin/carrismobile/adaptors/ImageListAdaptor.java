package kevin.carrismobile.adaptors;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.carrismobile.R;

import java.util.ArrayList;
import java.util.List;

import data_structure.CarreiraBasic;

public class ImageListAdaptor extends BaseAdapter {

    Context context;
    List<String> textList = new ArrayList<>();
    List<Integer> imageList = new ArrayList<>();
    List<CarreiraBasic> carreiraBasicList;
    LayoutInflater inflater;

    public ImageListAdaptor(Context context, List<CarreiraBasic> carreiraBasicList){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.carreiraBasicList = carreiraBasicList;
        for (CarreiraBasic cb : carreiraBasicList){
            textList.add(cb.toString());
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
        imageView.setImageResource(imageList.get(i));
        return view;
    }

    private int getImageId(String string){
        if (string.equals("#ED1944")){
            return R.drawable.red_bus;
        }else {
            return R.drawable.blue_bus;
        }
    }
}
