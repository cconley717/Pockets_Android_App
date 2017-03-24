package com.pocketschatapp._HomeUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chris on 2/7/2015.
 */
public class PocketManagerListItem implements Serializable
{
    private String pocketName;
    private float topLatitude;
    private float bottomLatitude;
    private float leftLongitude;
    private float rightLongitude;
    private float centerLatitude;
    private float centerLongitude;
    private float zoomLevel;
    private String avatarLink;

    private boolean hasNewContent;

    public PocketManagerListItem(String pocketName, boolean hasNewContent)
    {
        this.pocketName = pocketName;
        this.topLatitude = topLatitude;
        this.bottomLatitude = bottomLatitude;
        this.leftLongitude = leftLongitude;
        this.rightLongitude = rightLongitude;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.zoomLevel = zoomLevel;
        this.avatarLink = avatarLink;

        this.hasNewContent = hasNewContent;
    }

    public String getPocketName() {
        return pocketName;
    }

    public boolean hasNewContent() {
        return hasNewContent;
    }

    public float getTopLatitude() {
        return topLatitude;
    }

    public float getBottomLatitude() {
        return bottomLatitude;
    }

    public float getLeftLongitude() {
        return leftLongitude;
    }

    public float getRightLongitude() {
        return rightLongitude;
    }

    public float getCenterLatitude() {
        return centerLatitude;
    }

    public float getCenterLongitude() {
        return centerLongitude;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public String getAvatarLink() {
        return avatarLink;
    }
}