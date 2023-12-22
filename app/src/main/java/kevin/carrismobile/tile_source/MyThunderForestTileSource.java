package kevin.carrismobile.tile_source;

import android.content.Context;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

/**
 * Thunderforest Maps
 */

public class MyThunderForestTileSource extends OnlineTileSourceBase {

    private String apiKey = "";

    /**
     * the available map types
     */
    public static final int CYCLE = 0;
    public static final int TRANSPORT = 1;
    public static final int LANDSCAPE = 2;
    public static final int OUTDOORS = 3;
    public static final int ATLAS = 4;
    public static final int TRANSPORT_DARK = 5;
    public static final int SPINAL_MAP = 6;
    public static final int PIONEER = 7;
    public static final int MOBILE_ATLAS = 8;
    public static final int NEIGHBOURHOOD = 9;


    /**
     * map names used in URLs
     */
    private static final String[] urlMap = new String[]{
            "cycle",
            "transport",
            "landscape",
            "outdoors",
            "atlas",
            "transport-dark",
            "spinal-map",
            "pioneer",
            "mobile-atlas",
            "neighbourhood"};

    /**
     * map names used in UI (eg. menu)
     */
    private static final String[] uiMap = new String[]{
            "CycleMap",
            "Transport",
            "Landscape",
            "Outdoors",
            "Atlas",
            "TransportDark",
            "Spinal",
            "Pioneer",
            "MobileAtlas",
            "Neighbourhood"};

    private static final String[] baseUrl = new String[]{
            "https://a.tile.thunderforest.com/{map}/",
            "https://b.tile.thunderforest.com/{map}/",
            "https://c.tile.thunderforest.com/{map}/"};

    private final int mMap;
    private String mMapId;

    /**
     * return the name asociated with a map.
     */
    public static final String mapName(int m) {
        if (m < 0 || m >= uiMap.length)
            return "";
        return uiMap[m];
    }

    /**
     * creates a new Thunderforest tile source, loading the access token and mapid from the manifest
     */
    public MyThunderForestTileSource(final int aMap) {
        super(uiMap[aMap], 0, 17, 256, ".png", baseUrl, "Maps © Thunderforest, Data © OpenStreetMap contributors.");
        mMap = aMap;
        mMapId = getApiKey();
        //this line will ensure uniqueness in the tile cache
        //mName="thunderforest"+aMap+mMapId;
    }

    /**
     * Reads the map id from the manifest.<br>
     */
    public final String getApiKey() {
        return apiKey;
    }

    @Override
    public String getTileURLString(final long pMapTileIndex) {
        StringBuilder url = new StringBuilder(getBaseUrl().replace("{map}", urlMap[mMap]));
        url.append(MapTileIndex.getZoom(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getX(pMapTileIndex));
        url.append("/");
        url.append(MapTileIndex.getY(pMapTileIndex));
        url.append(".png?");
        url.append("apikey=").append(mMapId);
        String res = url.toString();
        //Log.d(IMapView.LOGTAG, res);

        return res;
    }

    /**
     * check if we have a key in the manifest for this provider.
     */
    public boolean hasApiKey() {
        return apiKey.equals("");
    }

    public void setApiKey(String apiKey){
        this.apiKey = apiKey;
        mMapId = apiKey;
    }

}
