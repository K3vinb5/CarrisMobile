package kevin.carrismobile.tile_source;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

public class MyMapTilerTileSource {

    public static final OnlineTileSourceBase Pastel = new XYTileSource("OpenStreetMap", 1, 22, 256, ".png",
            new String[]{
                    "https://api.maptiler.com/maps/basic/?key=pXCee81XdHjf2bL2W9ev"});

}
