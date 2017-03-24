package com.pocketschatapp._Utilities;

import com.pocketschatapp._HomeUtilities.PocketManagerListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chris on 1/28/2015.
 */
public class ProfilePackage implements Serializable
{
    private String registrationID;
    private String username;
    private String UUID;
    private String passkey;
    private boolean newProfile = true;
    private long imageTimestamp = 0;

    private List<PocketManagerListItem> created = Collections.synchronizedList(new ArrayList<PocketManagerListItem>());
    private List<PocketManagerListItem> favorites = Collections.synchronizedList(new ArrayList<PocketManagerListItem>());
    private List<PocketManagerListItem> history = Collections.synchronizedList(new ArrayList<PocketManagerListItem>());

    private boolean mapPocketListEnabled = true;
    private boolean costDisplayEnabled = false;

    private double lastKnownLatitude = 0.0;
    private double lastKnownLongitude = 0.0;

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public List<PocketManagerListItem> getCreated() {
        synchronized(created) {
            return created;
        }
    }

    public List<PocketManagerListItem> getFavorites() {
        synchronized(favorites) {
            return favorites;
        }
    }

    public List<PocketManagerListItem> getHistory() {
        synchronized(history) {
            return history;
        }
    }

    public boolean isNewProfile() {
        return newProfile;
    }

    public void setNewProfile(boolean newProfile) {
        this.newProfile = newProfile;
    }

    public boolean isMapPocketListEnabled() {
        return mapPocketListEnabled;
    }

    public void setMapPocketListEnabled(boolean mapPocketListEnabled) {
        this.mapPocketListEnabled = mapPocketListEnabled;
    }

    public boolean isCostDisplayEnabled() {
        return costDisplayEnabled;
    }

    public void setCostDisplayEnabled(boolean costDisplayEnabled) {
        this.costDisplayEnabled = costDisplayEnabled;
    }

    public double getLastKnownLatitude() {
        return lastKnownLatitude;
    }

    public void setLastKnownLatitude(double lastKnownLatitude) {
        this.lastKnownLatitude = lastKnownLatitude;
    }

    public double getLastKnownLongitude() {
        return lastKnownLongitude;
    }

    public void setLastKnownLongitude(double lastKnownLongitude) {
        this.lastKnownLongitude = lastKnownLongitude;
    }

    public long getImageTimestamp() {
        return imageTimestamp;
    }

    public void setImageTimestamp(long imageTimestamp) {
        this.imageTimestamp = imageTimestamp;
    }

    @Override
    public boolean equals(Object object)
    {
        ProfilePackage profilePackage = (ProfilePackage) object;

        if(this.getUsername().matches(profilePackage.getUsername()) && this.imageTimestamp == profilePackage.getImageTimestamp())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
