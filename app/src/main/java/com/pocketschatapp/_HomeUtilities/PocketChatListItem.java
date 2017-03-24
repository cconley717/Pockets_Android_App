package com.pocketschatapp._HomeUtilities;

import android.provider.Settings;

import com.pocketschatapp._Utilities.GlobalVariables;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Chris on 6/28/2016.
 */
//output objects within the chat output window
public class PocketChatListItem implements Serializable {
    private String displayName;
    private String avatar;
    private String message;
    private String messageType;
    private long timestamp;
    private String formattedDateStamp;

    private boolean isText = false;
    private boolean isImage = false;
    private boolean isVideo = false;

    private String mediaIdentifier;

    private String userid;


    public PocketChatListItem(String userid, String displayName, String avatar, String message, String messageType, String mediaIdentifier, long timestamp) {
        this.displayName = displayName;
        this.avatar = avatar;
        this.message = message;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.formattedDateStamp = makeFormattedDateStamp();

        if(messageType.matches("text"))
            isText = true;
        else if(messageType.matches("image")) {
            isImage = true;
            this.mediaIdentifier = mediaIdentifier;
        }
        else if(messageType.matches("video")) {
            isVideo = true;
            this.mediaIdentifier = mediaIdentifier;
        }

        this.userid = userid;
    }

    private String makeFormattedDateStamp()
    {
        Date date = new Date(timestamp);
        DateFormat format = new SimpleDateFormat("yyyy-M-d h:mma");
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    public String getFormattedTimestamp()
    {
        return formattedDateStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isText() {
        return isText;
    }

    public void setIsText(boolean isText) {
        this.isText = isText;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setIsVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setText(boolean text) {
        isText = text;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public String getMediaIdentifier() {
        return mediaIdentifier;
    }

    public void setMediaIdentifier(String mediaIdentifier) {
        this.mediaIdentifier = mediaIdentifier;
    }

    public String getFormattedDateStamp() {
        return formattedDateStamp;
    }

    public void setFormattedDateStamp(String formattedDateStamp) {
        this.formattedDateStamp = formattedDateStamp;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}