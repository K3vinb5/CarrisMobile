package kevin.carrismobile.tile_source;

import android.util.Log;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

public class MyMapTilerTileSource extends OnlineTileSourceBase {
    private String apiKey = "";
    public static String[] baseUrl = new String[]{"https://api.maptiler.com/maps/openstreetmap/256/"};

    public MyMapTilerTileSource() {
        super("OpenStreetMap", 0, 18, 256, ".jpg", baseUrl, "Maps © MapTiler, Data © OpenStreetMap contributors.");
        //this line will ensure uniqueness in the tile cache
        //mName="thunderforest"+aMap+mMapId;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl[0]);
        url.append(MapTileIndex.getZoom(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getX(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getY(pMapTileIndex));
        url.append(".jpg?");
        url.append("key=").append(apiKey);
        String res = url.toString();
        return res;
    }
}
