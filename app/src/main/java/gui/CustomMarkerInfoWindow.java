package gui;

import android.widget.TextView;

import com.example.carrismobile.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomMarkerInfoWindow extends MarkerInfoWindow {
    /**
     * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
     *                    bubble_subdescription, bubble_image
     * @param mapView
     */
    private String title;
    private String description;
    public CustomMarkerInfoWindow(int layoutResId, MapView mapView, String title, String description) {
        super(layoutResId, mapView);
        this.title = title;
        this.description = description;
    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);
        TextView textViewTitle = mView.findViewById(R.id.bubble_title);
        TextView textViewDescription = mView.findViewById(R.id.bubble_description);
        textViewTitle.setText(this.title);
        textViewDescription.setText(this.description);
    }

}
