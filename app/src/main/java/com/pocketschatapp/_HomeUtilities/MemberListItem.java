package com.pocketschatapp._HomeUtilities;

import java.io.Serializable;

/**
 * Created by Chris on 2/14/2015.
 */
public class MemberListItem implements Serializable, Comparable
{
    private String memberName;
    private String memberPhoto;
    private long usernameTimestamp = 0;
    private long imageTimestamp = 0;
    private long activityTimestamp = 0;
    private String uuid;
    private int level;
    private String active;
    private long lint;
    private int banLevel;
    private boolean ignored;

    public MemberListItem(String uuid, String memberName, String memberPhoto, int level,
                          long usernameTimestamp, long imageTimestamp, long activityTimestamp,
                          String active, int banLevel)
    {
        this.uuid = uuid;

        this.memberName = memberName;

        if(memberName != null)
            this.memberPhoto = memberPhoto;
        else
            this.memberPhoto = null;

        this.level = level;

        this.lint = 0;

        this.usernameTimestamp = usernameTimestamp;
        this.imageTimestamp = imageTimestamp;
        this.activityTimestamp = activityTimestamp;

        this.active = active;
        this.banLevel = banLevel;

        ignored = false;
    }

    @Override
    public int compareTo(Object object){
        MemberListItem temp = (MemberListItem) object;
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        int last = this.memberName.toLowerCase().compareTo(temp.memberName.toLowerCase());
        return last == 0 ? this.memberName.toLowerCase().compareTo(temp.memberName.toLowerCase()) : last;
    }

    public MemberListItem(String memberName)
    {
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhoto() {
        return memberPhoto;
    }

    public void setMemberPhoto(String memberPhoto) {
        this.memberPhoto = memberPhoto;
    }

    public long getUsernameTimestamp() {
        return usernameTimestamp;
    }

    public void setUsernameTimestamp(long usernameTimestamp) {
        this.usernameTimestamp = usernameTimestamp;
    }

    public long getImageTimestamp() {
        return imageTimestamp;
    }

    public void setImageTimestamp(long imageTimestamp) {
        this.imageTimestamp = imageTimestamp;
    }

    public long getActivityTimestamp() {
        return activityTimestamp;
    }

    public void setActivityTimestamp(long activityTimestamp) {
        this.activityTimestamp = activityTimestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public long getLint() {
        return lint;
    }

    public void setLint(long lint) {
        this.lint = lint;
    }

    public int getBanLevel() {
        return banLevel;
    }

    public void setBanLevel(int banLevel) {
        this.banLevel = banLevel;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }
}
