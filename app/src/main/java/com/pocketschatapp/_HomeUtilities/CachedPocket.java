package com.pocketschatapp._HomeUtilities;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Chris on 6/28/2016.
 */
public class CachedPocket implements Serializable {

    private LinkedList<PocketChatListItem> chatLog;

    public CachedPocket()
    {
        chatLog = new LinkedList<>();
    }

    public LinkedList<PocketChatListItem> getChatLog()
    {
        return chatLog;
    }


}
