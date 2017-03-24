package com.pocketschatapp._PocketUtilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pocketschatapp.R;
import com.pocketschatapp._Utilities.GlobalVariables;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Chris on 11/29/2015.
 */
public class Socket_Communications {

    private static Socket socket;

    private static OnSocketRespondListener onSocketRespondListener;
    public interface OnSocketRespondListener {
        public void onChatMessageReceived(JSONObject jsonObject);
        public void onConnect();
        public void onDisconnect();
    }

    public static void configureSocket(Context context, final String pocketName, OnSocketRespondListener listener)
    {
        Log.d("testing", "configuring socket");

        onSocketRespondListener = listener;

        try {
            if(socket == null) {
                socket = IO.socket(context.getResources().getString(R.string.pockets_server_address));

                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d("testing", "socket connected");

                        onSocketRespondListener.onConnect();
                    }

                }).on("chat message", new Emitter.Listener() {

                    @Override
                    public void call(final Object... args) {
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        onSocketRespondListener.onChatMessageReceived((JSONObject) args[0]);
                                    }
                                });
                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d("testing", "socket disconnected");

                        onSocketRespondListener.onDisconnect();
                    }
                });
            }

            if(!socket.connected())
                socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void sendChatMessage(String pocketOwner, String message, String messageType, String mediaIdentifier) {
        if (socket != null) {
            try {
                Log.d("testing", "sending text message");

                JSONObject chatMessagePacket = new JSONObject();

                chatMessagePacket.put("pocketOwner", pocketOwner);
                chatMessagePacket.put("userid", GlobalVariables.getUserid());
                chatMessagePacket.put("displayName", GlobalVariables.getDisplayName());
                chatMessagePacket.put("avatar", GlobalVariables.getAvatar());
                chatMessagePacket.put("message", message);
                chatMessagePacket.put("messageType", messageType);
                chatMessagePacket.put("mediaIdentifier", mediaIdentifier);

                socket.emit("chat message", chatMessagePacket);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void joinPocket(String pocketName, String pocketPassword)
    {
        if (socket != null) {
            try {
                Log.d("testing", "joining pocket");

                JSONObject joinRoomPacket = new JSONObject();

                joinRoomPacket.put("pocketName", pocketName);
                joinRoomPacket.put("pocketPassword", pocketPassword);

                socket.emit("join pocket", joinRoomPacket);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean isConnected()
    {
        if(socket == null)
            return false;
        else
            return socket.connected();
    }

    public static void disconnect()
    {
        if(socket != null) {
            socket.off();
            socket.disconnect();
            //socket.close();

            socket = null;
        }
    }
}
