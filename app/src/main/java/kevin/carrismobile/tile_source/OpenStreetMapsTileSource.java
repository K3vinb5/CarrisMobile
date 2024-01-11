package kevin.carrismobile.tile_source;

import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

public class OpenStreetMapsTileSource extends OnlineTileSourceBase {

    private static final String[] baseUrl = new String[]{
            "https://a.tile.openstreetmap.org/",
            "https://b.tile.openstreetmap.org/",
            "https://c.tile.openstreetmap.org/"
    };
    public OpenStreetMapsTileSource(){
        super("Open Street Map", 0, 18, 256, ".png", baseUrl, "© OpenStreetMap contributors");
        Configuration.getInstance().setUserAgentValue("CMobileLA");
    }
    @Override
    public String getTileURLString(long pMapTileIndex) {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append(MapTileIndex.getZoom(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getX(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getY(pMapTileIndex));
        url.append(".png");
        String res = url.toString();
        Log.d("TEMP", res+"");
        return res;
    }


}
