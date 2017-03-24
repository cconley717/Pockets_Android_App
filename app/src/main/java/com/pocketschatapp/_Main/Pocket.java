package com.pocketschatapp._Main;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.pocketschatapp.R;
import com.pocketschatapp._HomeUtilities.CachedPocket;
import com.pocketschatapp._HomeUtilities.CenteredToastMessage;
import com.pocketschatapp._HomeUtilities.PocketChatListItem;
import com.pocketschatapp._PocketUtilities.HTTP_Communications;
import com.pocketschatapp._PocketUtilities.Socket_Communications;
import com.pocketschatapp._Utilities.DialogDispatcher;
import com.pocketschatapp._Utilities.GlobalVariables;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;


public class Pocket extends AppCompatActivity {

    private Activity mActivity;

    private String userid;

    private String pocketOwner;
    private String pocketName;
    private String pocketPassword;
    private boolean isLoggedIn;

    private CachedPocket cachedPocket;

    private EditText chatInput;
    private ImageButton sendMessageButton;
    private ImageButton sendMediaButton;
    private ProgressWheel sendMessageSpinner;
    private ProgressWheel sendMediaSpinner;

    private ListView chatOutput;
    private ChatListViewAdapter chatListViewAdapter;

    private File cachedPocketFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocket);

        this.mActivity = this;

        userid = GlobalVariables.getUserid();

        pocketOwner = getIntent().getStringExtra("pocketOwner");
        pocketName = getIntent().getStringExtra("pocketName");
        pocketPassword = getIntent().getStringExtra("pocketPassword");
        isLoggedIn = getIntent().getBooleanExtra("isLoggedIn", false);

        getSupportActionBar().setTitle(pocketName);

        registerResources();
        registerListeners();

        loadPocketCache();
        configureChatOutput();
        getRecentChatActivity();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        Socket_Communications.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pocket, menu);

        Log.d("testing", "creating menu");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.addFavorite) {
            addToFavoritePockets();
        }
        else if(id == R.id.changeSettings)
        {
            showChangePocketPasswordDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerResources()
    {
        chatInput = (EditText) findViewById(R.id.chatInput);

        sendMessageButton = (ImageButton) findViewById(R.id.sendMessageButton);
        sendMediaButton = (ImageButton) findViewById(R.id.sendMediaButton);

        sendMessageSpinner = (ProgressWheel) findViewById(R.id.sendMessageSpinner);
        sendMediaSpinner = (ProgressWheel) findViewById(R.id.sendMediaSpinner);

        chatOutput = (ListView) findViewById(R.id.chatOutput);
    }

    private void registerListeners()
    {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = chatInput.getText().toString();

                if(!message.equals("")) {
                    Socket_Communications.sendChatMessage(pocketOwner, message, "text", "");

                    chatInput.setText("");

                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
                }
            }
        });

        sendMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void loadPocketCache() {
        cachedPocketFile = new File(getFilesDir().toString() + File.separator + "pockets" + File.separator + pocketName);
        try {
            if (cachedPocketFile.exists()) {
                ObjectInputStream cachedPocketInput = new ObjectInputStream(new FileInputStream(cachedPocketFile));
                cachedPocket = (CachedPocket) cachedPocketInput.readObject();
                cachedPocketInput.close();
            } else {
                cachedPocket = new CachedPocket();

                ObjectOutputStream cachedPocketOutput = new ObjectOutputStream(new FileOutputStream(cachedPocketFile));
                cachedPocketOutput.writeObject(cachedPocket);
                cachedPocketOutput.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveCachedPocket()
    {
        try
        {
            ObjectOutputStream cachedPocketOutput = new ObjectOutputStream(new FileOutputStream(cachedPocketFile));
            cachedPocketOutput.writeObject(cachedPocket);
            cachedPocketOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRecentChatActivity()
    {
        LinkedList<PocketChatListItem> chatLog = chatListViewAdapter.getChatLog();

        long mostRecentTimestamp = 0;

        if(!chatLog.isEmpty())
            mostRecentTimestamp = chatListViewAdapter.getChatLog().getLast().getTimestamp();

        HTTP_Communications.getRecentChatActivity(this, pocketName, pocketPassword, mostRecentTimestamp, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                if(success)
                {
                    chatListViewAdapter.addChats(response);

                    configureWebSocket();
                }
                else
                {
                    DialogDispatcher.showInformationDialog(mActivity, "oops", errorReason);
                }
            }
        });
    }

    private void addToFavoritePockets()
    {
        HTTP_Communications.addToFavoritePockets(this, pocketName, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                if(success)
                {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, "Added to favorites!");
                }
                else
                {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                }
            }
        });
    }

    private void showChangePocketPasswordDialog()
    {
        new MaterialDialog.Builder(this)
                .title("Change Password")
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .positiveText("Submit")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .neutralText("Cancel")
                .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .customView(R.layout.change_pocket_password_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();

                        MaterialEditText oldPasswordInput = (MaterialEditText) view.findViewById(R.id.oldPocketPasswordInput);
                        MaterialEditText newPasswordInput = (MaterialEditText) view.findViewById(R.id.newPocketPasswordInput);

                        String oldPassword = oldPasswordInput.getText().toString();
                        String newPassword = newPasswordInput.getText().toString();

                        HTTP_Communications.changePocketPassword(mActivity, pocketName, oldPassword, newPassword, new HTTP_Communications.OnServerRespond() {
                            @Override
                            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                if(success)
                                {
                                    CenteredToastMessage.showCenteredToastMessage(mActivity, "Password changed successfully.");
                                }
                                else
                                {
                                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                                }
                            }
                        });
                    }
                })
                .show();
    }

    private void configureWebSocket()
    {
        Socket_Communications.configureSocket(this, pocketName, new Socket_Communications.OnSocketRespondListener() {
            @Override
            public void onChatMessageReceived(JSONObject jsonObject) {
                Log.d("testing", "MESSAGE: " + jsonObject.toString());

                chatListViewAdapter.addTextChat(jsonObject);
            }

            @Override
            public void onConnect() {
                Socket_Communications.joinPocket(pocketName, pocketPassword);
            }

            @Override
            public void onDisconnect() {

            }
        });
    }

    private void configureChatOutput()
    {
        chatListViewAdapter = new ChatListViewAdapter(cachedPocket.getChatLog());
        chatOutput.setAdapter(chatListViewAdapter);

        if(isLoggedIn) {
            Log.d("testing", "fuck");
            chatInput.setHint("Type Here");
            chatInput.setEnabled(true);
            sendMessageButton.setEnabled(true);
        }
        else {
            Log.d("testing", "fuck2");
            chatInput.setHint("log in to chat");
            chatInput.setEnabled(false);
            sendMessageButton.setEnabled(false);
        }
    }


    public class ChatListViewAdapter extends BaseAdapter {
        private LinkedList<PocketChatListItem> chatLog;
        private int position;

        public ChatListViewAdapter(LinkedList<PocketChatListItem> chatLog) {
            this.chatLog = chatLog;
        }

        public void addTextChat(JSONObject jsonObject) {
            try {
                String userid = jsonObject.getString("userid");
                String displayName = jsonObject.getString("displayName");
                String avatar = jsonObject.getString("avatar");
                String message = jsonObject.getString("message");
                String messageType = jsonObject.getString("messageType");
                String mediaIdentifier = jsonObject.getString("mediaIdentifier");
                long timestamp = jsonObject.getLong("timestamp");

                chatLog.add(new PocketChatListItem(userid, displayName, avatar, message, messageType, mediaIdentifier, timestamp));
                GlobalVariables.setLastActivityForPocket(pocketName, timestamp);

                notifyDataSetChanged();

                saveCachedPocket();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void addChats(JSONObject response) {
            try {
                String userid;
                String displayName;
                String avatar;
                String message;
                String messageType;
                String mediaIdentifier;
                long timestamp;

                JSONObject currentChat;
                JSONArray logs = response.getJSONArray("logs");
                for (int i = 0; i < logs.length(); i++) {
                    currentChat = logs.getJSONObject(i);

                    userid = currentChat.getString("userid");
                    displayName = currentChat.getString("displayname");
                    avatar = currentChat.getString("avatar");
                    message = currentChat.getString("message");
                    messageType = currentChat.getString("messagetype");
                    mediaIdentifier = currentChat.getString("mediaidentifier");
                    timestamp = currentChat.getLong("timestamp");

                    chatLog.add(new PocketChatListItem(userid, displayName, avatar, message, messageType, mediaIdentifier, timestamp));
                }

                if(chatLog.size() > 0)
                    GlobalVariables.setLastActivityForPocket(pocketName, chatLog.getLast().getTimestamp());
                else
                    GlobalVariables.setLastActivityForPocket(pocketName, 0);

                saveCachedPocket();
                notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public LinkedList<PocketChatListItem> getChatLog() {
            return chatLog;
        }

        public void setCurrentPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public int getCount() {
            return chatLog.size();
        }

        @Override
        public Object getItem(int position) {
            return chatLog.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(mActivity).inflate(R.layout.chat_item, parent, false);
                final ViewHolder holder = new ViewHolder();

                holder.meContainer = (LinearLayout)view.findViewById(R.id.meContainer);
                holder.meAvatar = (SimpleDraweeView)view.findViewById(R.id.meAvatar);
                holder.meMessage = (TextView)view.findViewById(R.id.meMessage);
                holder.meDisplayName = (TextView)view.findViewById(R.id.meDisplayName);
                holder.meTimestamp = (TextView)view.findViewById(R.id.meTimestamp);
                holder.meMediaButton = (ImageButton)view.findViewById(R.id.meMediaButton);

                holder.themContainer = (LinearLayout)view.findViewById(R.id.themContainer);
                holder.themAvatar = (SimpleDraweeView)view.findViewById(R.id.themAvatar);
                holder.themMessage = (TextView)view.findViewById(R.id.themMessage);
                holder.themDisplayName = (TextView)view.findViewById(R.id.themDisplayName);
                holder.themTimestamp = (TextView)view.findViewById(R.id.themTimestamp);
                holder.themMediaButton = (ImageButton)view.findViewById(R.id.themMediaButton);

                view.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) view.getTag();

            PocketChatListItem pocketChatListItem = chatLog.get(position);

            String logUserid = pocketChatListItem.getUserid();
            String displayName = pocketChatListItem.getDisplayName();
            String avatar = pocketChatListItem.getAvatar();
            String message = pocketChatListItem.getMessage();
            String formattedTimestamp = pocketChatListItem.getFormattedTimestamp();


            TextView displayNameOutput;
            SimpleDraweeView avatarOutput;
            TextView messageOutput;
            TextView timestampOutput;

            if(userid.equals(logUserid))
            {
                holder.meContainer.setVisibility(View.VISIBLE);
                holder.themContainer.setVisibility(View.GONE);

                displayNameOutput = holder.meDisplayName;
                avatarOutput = holder.meAvatar;
                messageOutput = holder.meMessage;
                timestampOutput = holder.meTimestamp;
            }
            else
            {
                holder.meContainer.setVisibility(View.GONE);
                holder.themContainer.setVisibility(View.VISIBLE);

                displayNameOutput = holder.themDisplayName;
                avatarOutput = holder.themAvatar;
                messageOutput = holder.themMessage;
                timestampOutput = holder.themTimestamp;
            }

            displayNameOutput.setText(displayName);
            avatarOutput.setImageURI(getAvatarURI(avatar));
            messageOutput.setText(message);
            timestampOutput.setText(formattedTimestamp);

            return view;
        }

        final class ViewHolder {
            public LinearLayout meContainer;
            public LinearLayout themContainer;

            public SimpleDraweeView meAvatar;
            public SimpleDraweeView themAvatar;

            public TextView meMessage;
            public TextView themMessage;

            public TextView meDisplayName;
            public TextView themDisplayName;

            public TextView meTimestamp;
            public TextView themTimestamp;

            public ImageButton meMediaButton;
            public ImageButton themMediaButton;
        }

        private Uri getAvatarURI(String avatar)
        {
            return Uri.parse(avatar);
        }
    }
}
