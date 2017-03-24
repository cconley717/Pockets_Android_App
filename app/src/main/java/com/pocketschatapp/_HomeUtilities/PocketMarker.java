package com.pocketschatapp._HomeUtilities;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Chris on 3/28/2015.
 */
public class PocketMarker
{
    private GroundOverlay groundOverlay;
    private Marker marker;
    private float zoomLevel;

    public PocketMarker(GroundOverlay groundOverlay, Marker marker, float zoomLevel)
    {
        this.groundOverlay = groundOverlay;
        this.marker = marker;
        this.zoomLevel = zoomLevel;
    }


    public GroundOverlay getGroundOverlay() {
        return groundOverlay;
    }

    public Marker getMarker() {
        return marker;
    }

    public float getZoomLevel()
    {
        return zoomLevel;
    }
}
