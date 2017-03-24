package com.pocketschatapp._HomeUtilities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.pocketschatapp.R;
import com.pocketschatapp._Utilities.DialogDispatcher;
import com.pocketschatapp._Utilities.GlobalVariables;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Chris on 3/25/2015.
 */
public class PocketManagementSlidingMenu extends SlidingMenu {
    private Activity mActivity;

    private View pocketManagementView;

    private TabHost pocketManagementTabHost;
    private ListView createdListView;
    private ListView favoritesListView;
    private ListView historyListView;

    private PocketsListViewAdapter createdListViewAdapter;
    private PocketsListViewAdapter favoritesListViewAdapter;
    private PocketsListViewAdapter historyListViewAdapter;

    private int longTouchedPocketIndex;

    private final int CREATED_CATEGORY = 0;
    private final int FAVORITES_CATEGORY = 1;
    private final int HISTORY_CATEGORY = 2;

    private final int SHOW_POCKET_ON_MAP = 0;
    private final int SORT_POCKETS_ASCENDING = 1;
    private final int SORT_POCKETS_DESCENDING = 2;
    private final int MOVE_POCKET_UP = 3;
    private final int MOVE_POCKET_DOWN = 4;
    private final int REMOVE_POCKET = 5;
    private final int DELETE_POCKET = 6;


    private PocketManagementMenuListener pocketManagementMenuListener;
    public interface PocketManagementMenuListener {
        public void onPocketSelectedFromMenu(String pocketName);
        public void onShowPocketOnMap(String pocketName);
    }

    public PocketManagementSlidingMenu(Activity activity, PocketManagementMenuListener listener) {
        super(activity);

        pocketManagementMenuListener = listener;

        this.mActivity = activity;

        configureRoomManagementMenu();
        registerListeners();
        initializeSlidingWindow();
    }

    public void configureRoomManagementMenu() {
        pocketManagementView = View.inflate(mActivity, R.layout.management_tabhost, null);
        pocketManagementTabHost = (TabHost) pocketManagementView.findViewById(R.id.tabHost);

        pocketManagementTabHost.setup();
        TabHost.TabSpec createdTab = pocketManagementTabHost.newTabSpec("created");
        createdTab.setIndicator("Created");
        createdTab.setContent(R.id.Created);
        pocketManagementTabHost.addTab(createdTab);

        TabHost.TabSpec favoritesTab = pocketManagementTabHost.newTabSpec("favorites");
        favoritesTab.setIndicator("Favorites");
        favoritesTab.setContent(R.id.Favorites);
        pocketManagementTabHost.addTab(favoritesTab);

        TabHost.TabSpec historyTab = pocketManagementTabHost.newTabSpec("history");
        historyTab.setIndicator("History");
        historyTab.setContent(R.id.History);
        pocketManagementTabHost.addTab(historyTab);

        createdListView = (ListView) pocketManagementView.findViewById(R.id.createdListView);
        favoritesListView = (ListView) pocketManagementView.findViewById(R.id.favoritesListView);
        historyListView = (ListView) pocketManagementView.findViewById(R.id.historyListView);

        createdListViewAdapter = new PocketsListViewAdapter();
        favoritesListViewAdapter = new PocketsListViewAdapter();
        historyListViewAdapter = new PocketsListViewAdapter();

        createdListView.setAdapter(createdListViewAdapter);
        favoritesListView.setAdapter(favoritesListViewAdapter);
        historyListView.setAdapter(historyListViewAdapter);

        mActivity.registerForContextMenu(createdListView);
        mActivity.registerForContextMenu(favoritesListView);
        mActivity.registerForContextMenu(historyListView);
    }

    private void registerListeners() {
        setOnOpenedListener(new OnOpenedListener() {
            @Override
            public void onOpened() {
                String pocketCategory = pocketManagementTabHost.getCurrentTabTag();
                getPocketsListForCategory(pocketCategory);
            }
        });

        setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                Log.d("testing", "closed");

                clearCategoryTabs();
            }
        });

        pocketManagementTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String pocketCategory) {
                clearCategoryTabs();
                getPocketsListForCategory(pocketCategory);
            }
        });

        createdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("testing", "short clicked");

                view.setBackgroundColor(Color.TRANSPARENT);

                String pocketName = createdListViewAdapter.getPockets().get(i).getPocketName();
                pocketManagementMenuListener.onPocketSelectedFromMenu(pocketName);
            }
        });

        createdListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                Log.d("testing", "long clicked");

                longTouchedPocketIndex = index;

                mActivity.openContextMenu(createdListView);

                return true;
            }
        });

        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("testing", "short clicked");

                view.setBackgroundColor(Color.TRANSPARENT);

                String pocketName = favoritesListViewAdapter.getPockets().get(i).getPocketName();
                pocketManagementMenuListener.onPocketSelectedFromMenu(pocketName);
            }
        });

        favoritesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                Log.d("testing", "long clicked");

                longTouchedPocketIndex = index;

                mActivity.openContextMenu(favoritesListView);

                return true;
            }
        });

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("testing", "short clicked: " + i);

                view.setBackgroundColor(Color.TRANSPARENT);

                String pocketName = historyListViewAdapter.getPockets().get(i).getPocketName();
                pocketManagementMenuListener.onPocketSelectedFromMenu(pocketName);
            }
        });

        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                Log.d("testing", "long clicked");

                longTouchedPocketIndex = index;

                mActivity.openContextMenu(historyListView);

                return true;
            }
        });
    }

    private void initializeSlidingWindow() {
        setMode(SlidingMenu.RIGHT);
        setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        setFadeDegree(0.35f);
        attachToActivity(mActivity, SlidingMenu.SLIDING_CONTENT);

        setMenu(pocketManagementView);
    }

    private void removePocketFromCategory(String pocketsCategory, String pocketName, final PocketsListViewAdapter pocketsListViewAdapter)
    {
        showLoadingIndicator();
        HTTP_Communications.removePocketFromCategory(mActivity, pocketsCategory, pocketName, new HTTP_Communications.OnServerRespond() {
                    @Override
                    public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                        hideLoadingIndicator();

                        if(success) {
                            pocketsListViewAdapter.getPockets().remove(longTouchedPocketIndex);
                            pocketsListViewAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                        }
                    }
                });
    }

    private void getPocketsListForCategory(final String pocketCategory) {
        showLoadingIndicator();

        HTTP_Communications.getPocketsForCategory(mActivity, pocketCategory, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                hideLoadingIndicator();

                if (success) {
                    if (pocketCategory.equals("created"))
                        createdListViewAdapter.updateList(response);
                    else if (pocketCategory.equals("favorites"))
                        favoritesListViewAdapter.updateList(response);
                    else if (pocketCategory.equals("history"))
                        historyListViewAdapter.updateList(response);
                } else {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                }
            }
        });
    }

    public class PocketsListViewAdapter extends BaseAdapter {
        private LinkedList<PocketManagerListItem> pocketsList;
        private int position;

        public PocketsListViewAdapter() {
            pocketsList = new LinkedList<PocketManagerListItem>();
        }

        public void updateList(JSONObject response) {
            pocketsList.clear();

            Log.d("testing", response.toString());

            try {
                JSONObject data = response.getJSONObject("data");
                JSONArray existingPockets = data.getJSONArray("existingPockets");
                JSONArray uncertainPockets = data.getJSONArray("uncertainPockets");

                JSONObject uncertainPocket;
                JSONObject existingPocket;
                for(int i = 0; i < uncertainPockets.length(); i++)
                {
                    uncertainPocket = uncertainPockets.getJSONObject(i);

                    boolean isUncertain = true;
                    for(int j = 0; j < existingPockets.length(); j++)
                    {
                        existingPocket = existingPockets.getJSONObject(j);

                        if(existingPocket.getString("pocketname").equals(uncertainPocket.getString("pocketname")))
                        {
                            isUncertain = false;
                            continue;
                        }
                    }

                    if(isUncertain)
                        existingPockets.put(uncertainPocket);
                }

                String pocketName;
                long lastActivity;
                JSONObject currentPocket;
                for (int i = 0; i < existingPockets.length(); i++) {
                    currentPocket = existingPockets.getJSONObject(i);

                    pocketName = currentPocket.getString("pocketname");
                    lastActivity = currentPocket.getLong("lastactivity");

                    if (lastActivity > GlobalVariables.getLastActivityForPocket(pocketName))
                        pocketsList.add(new PocketManagerListItem(pocketName, true));
                    else
                        pocketsList.add(new PocketManagerListItem(pocketName, false));
                }

                notifyDataSetChanged();
            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public LinkedList<PocketManagerListItem> getPockets() {
            return pocketsList;
        }

        public void setCurrentPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public int getCount() {
            return pocketsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = View.inflate(mActivity, R.layout.groups_list_item, null);
            }

            PocketManagerListItem pocketManagerListItem = pocketsList.get(position);

            String pocketName = pocketManagerListItem.getPocketName();
            boolean hasNewContent = pocketManagerListItem.hasNewContent();

            if (hasNewContent)
                view.setBackgroundColor(Color.parseColor("#fff6cf"));
            else
                view.setBackgroundColor(Color.TRANSPARENT);

            TextView pocketNameTextView = (TextView) view.findViewById(R.id.pocketName);
            pocketNameTextView.setText(pocketName);

            return view;
        }
    }

    public void showLoadingIndicator() {
        pocketManagementView.findViewById(R.id.loadingIndicator).setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        pocketManagementView.findViewById(R.id.loadingIndicator).setVisibility(View.INVISIBLE);
    }

    public void reactToContextMenu(MenuItem item) {
        int categoryIndex = item.getGroupId();
        int menuItemIndex = item.getItemId();

        String pocketCategory = null;
        LinkedList<PocketManagerListItem> pocketsList = null;
        PocketsListViewAdapter pocketsListViewAdapter = null;

        if (categoryIndex == CREATED_CATEGORY) {
            pocketCategory = "created";
            pocketsList = createdListViewAdapter.getPockets();
            pocketsListViewAdapter = createdListViewAdapter;
        } else if (categoryIndex == FAVORITES_CATEGORY) {
            pocketCategory = "favorites";
            pocketsList = favoritesListViewAdapter.getPockets();
            pocketsListViewAdapter = favoritesListViewAdapter;
        } else if (categoryIndex == HISTORY_CATEGORY) {
            pocketCategory = "history";
            pocketsList = historyListViewAdapter.getPockets();
            pocketsListViewAdapter = historyListViewAdapter;
        }

        String pocketName = pocketsList.get(longTouchedPocketIndex).getPocketName();

        if (menuItemIndex == SHOW_POCKET_ON_MAP) {
            pocketManagementMenuListener.onShowPocketOnMap(pocketName);
        }
        /*else if (menuItemIndex == SORT_POCKETS_ASCENDING) {
            Collections.sort(pockets, new Comparator<PocketManagerListItem>() {
                public int compare(PocketManagerListItem first, PocketManagerListItem second) {
                    return first.getPocketName().compareTo(second.getPocketName());
                }
            });
        } else if (menuItemIndex == SORT_POCKETS_DESCENDING) {
            Collections.sort(pockets, new Comparator<PocketManagerListItem>() {
                public int compare(PocketManagerListItem first, PocketManagerListItem second) {
                    return first.getPocketName().compareTo(second.getPocketName()) * -1;
                }
            });
        } else if (menuItemIndex == MOVE_POCKET_UP) {
            if (longTouchedPocketIndex > 0) {
                Collections.swap(pockets, longTouchedPocketIndex, longTouchedPocketIndex - 1);
            }
        } else if (menuItemIndex == MOVE_POCKET_DOWN) {
            if (longTouchedPocketIndex < pockets.size() - 1) {
                Collections.swap(pockets, longTouchedPocketIndex, longTouchedPocketIndex + 1);
            }
        } */
        else if (menuItemIndex == REMOVE_POCKET) {
            removePocketFromCategory(pocketCategory, pocketName, pocketsListViewAdapter);
        } else if (menuItemIndex == DELETE_POCKET) {
            showDeletePocketDialogue(pocketName, pocketsListViewAdapter);

            //"This will remove the pocket from our servers. Are you sure you want to do this?"
        }
    }

    private void showDeletePocketDialogue(final String pocketName, final PocketsListViewAdapter pocketsListViewAdapter)
    {
        new MaterialDialog.Builder(mActivity)
                .title("Delete Pocket?")
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .customView(R.layout.delete_pocket_dialog, false)
                .positiveText("Delete")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .neutralText("Cancel")
                .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {

                        View view = dialog.getCustomView();

                        MaterialEditText pocketPasswordInput = (MaterialEditText) view.findViewById(R.id.pocketPasswordInput);
                        String pocketPassword = pocketPasswordInput.getText().toString();

                        HTTP_Communications.deletePocket(mActivity, pocketName, pocketPassword, new HTTP_Communications.OnServerRespond() {
                            @Override
                            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                if(success)
                                {
                                    pocketsListViewAdapter.getPockets().remove(longTouchedPocketIndex);
                                    pocketsListViewAdapter.notifyDataSetChanged();
                                    CenteredToastMessage.showCenteredToastMessage(mActivity, "Pocket deleted.");
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

    private void clearCategoryTabs()
    {
        createdListViewAdapter.getPockets().clear();
        favoritesListViewAdapter.getPockets().clear();
        historyListViewAdapter.getPockets().clear();
    }
}