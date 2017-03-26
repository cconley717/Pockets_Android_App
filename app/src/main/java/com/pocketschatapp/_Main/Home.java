package com.pocketschatapp._Main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pocketschatapp.R;
import com.pocketschatapp._HomeUtilities.CenteredToastMessage;
import com.pocketschatapp._HomeUtilities.CustomInfoWindow;
import com.pocketschatapp._HomeUtilities.CustomMapFragment;
import com.pocketschatapp._HomeUtilities.GPS;
import com.pocketschatapp._HomeUtilities.HTTP_Communications;
import com.pocketschatapp._HomeUtilities.MapWrapperLayout;
import com.pocketschatapp._HomeUtilities.PocketCreator;
import com.pocketschatapp._HomeUtilities.PocketManagementSlidingMenu;
import com.pocketschatapp._HomeUtilities.PocketMarker;
import com.pocketschatapp._Utilities.DialogDispatcher;
import com.pocketschatapp._Utilities.GlobalVariables;
import com.pocketschatapp._Utilities.SharedPreferencesManager;
import com.pocketschatapp._Utilities.SocialMediaLoginSlidingMenu;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;



public class Home extends AppCompatActivity {

    private Activity mActivity;

    private GoogleMap mGoogleMap;
    private PocketCreator pocketCreator;

    private boolean mMapMoved;
    private boolean placingRoom;

    private TextView pocketCostDisplay;
    private int pocketCost = 0;

    private Marker selectedPocketMarker;

    private FloatingActionButton floatingSearchButton;

    private static boolean costDisplayToggle;

    private ProgressBar mapSpinner;

    private PocketManagementSlidingMenu pocketManagementSlidingMenu;
    private SocialMediaLoginSlidingMenu socialMediaLoginSlidingMenu;

    private Snackbar searchAreaSnackbarWarning;

    private Handler mDelayedHandler;

    private Marker userLocationMarker;

    private static boolean isLoggedIn;

    private Map<String, PocketMarker> mapPockets = new HashMap<>();

    private double previousMapCenterLongitude, previousMapCenterLatitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mActivity = this;

        mMapMoved = false;
        placingRoom = false;

        registerResources();
        registerListeners();

        initializeGoogleMap();

        checkIfHelped();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        Log.d("testing", "creating menu");

        isLoggedIn = SharedPreferencesManager.getFromSharedPreferences(mActivity, "isLoggedIn", false);

        if(isLoggedIn) {
            menu.findItem(R.id.showLogin).setVisible(false);
            menu.findItem(R.id.doLogOut).setVisible(true);
        }
        else {
            menu.findItem(R.id.showLogin).setVisible(true);
            menu.findItem(R.id.doLogOut).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.findMe) {
            focusOnUser(true, mGoogleMap.getCameraPosition().zoom);
        } else if (id == R.id.zoomInOnMe) {
            focusOnUser(true, 14.0f);
        } else if (id == R.id.zoomOutOnMe) {
            focusOnUser(true, 6.8f);
        } else if (id == R.id.worldView) {
            focusOnUser(false, 2.0f);
        } else if (id == R.id.manageRooms) {
            togglePocketManagementSlider();
        } else if (id == R.id.showLint) {
            showMyLint();
        } else if (id == R.id.showHelp) {
            showPocketsHelpDialog("Pockets Help");
        } else if (id == R.id.showLogin) {
            showLoginChoicesDialog();
        } else if (id == R.id.doLogOut) {
            performLogOut();
        } else if (id == R.id.plainMap) {
            hidePocketManagementMenuIfShowing();
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.geographicMap) {
            hidePocketManagementMenuIfShowing();
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.satelliteMap) {
            hidePocketManagementMenuIfShowing();
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if(!pocketManagementSlidingMenu.isMenuShowing() && !socialMediaLoginSlidingMenu.isMenuShowing())
            {
                finish();
            }
            else
            {
                if(pocketManagementSlidingMenu.isMenuShowing()) {
                    pocketManagementSlidingMenu.toggle(true);
                }

                if(socialMediaLoginSlidingMenu.isMenuShowing()) {
                    socialMediaLoginSlidingMenu.toggle(true);
                }
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);

        Log.d("testing", "creating context menu");

        if(view.getId() == R.id.createdListView)
        {
            menu.setHeaderTitle("Options");
            menu.add(0, 0, 0, "Show Pocket On Map");
            /*
            menu.add(0, 1, 1, "Sort Pockets Ascending");
            menu.add(0, 2, 2, "Sort Pockets Descending");
            menu.add(0, 3, 3, "Move Pocket Up");
            menu.add(0, 4, 4, "Move Pocket Down");
            menu.add(0, 5, 5, "Leave Pocket");
            */
            menu.add(0, 6, 6, "Delete Pocket");
        }
        else if(view.getId() == R.id.favoritesListView)
        {
            menu.setHeaderTitle("Options");
            menu.add(1, 0, 0, "Show Pocket On Map");
            /*
            menu.add(1, 1, 1, "Sort Pockets Ascending");
            menu.add(1, 2, 2, "Sort Pockets Descending");
            menu.add(1, 3, 3, "Move Pocket Up");
            menu.add(1, 4, 4, "Move Pocket Down");
            */
            menu.add(1, 5, 5, "Remove Pocket");
        }
        else if(view.getId() == R.id.historyListView)
        {
            menu.setHeaderTitle("Options");
            menu.add(2, 0, 0, "Show Pocket On Map");
            /*
            menu.add(2, 1, 1, "Sort Pockets Ascending");
            menu.add(2, 2, 2, "Sort Pockets Descending");
            menu.add(2, 3, 3, "Move Pocket Up");
            menu.add(2, 4, 4, "Move Pocket Down");
            */
            menu.add(2, 5, 5, "Remove Pocket");
        }

        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);
                return true;
            }
        };

        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        pocketManagementSlidingMenu.reactToContextMenu(item);

        return true;
    }

    private void registerResources()
    {
        pocketManagementSlidingMenu = new PocketManagementSlidingMenu(this, new PocketManagementSlidingMenu.PocketManagementMenuListener() {
            @Override
            public void onPocketSelectedFromMenu(String pocketName) {
                joinPocket(pocketName);
            }

            @Override
            public void onShowPocketOnMap(String pocketName) {
                showPocket(pocketName);
            }
        });

        socialMediaLoginSlidingMenu = new SocialMediaLoginSlidingMenu(this);

        floatingSearchButton = (FloatingActionButton) findViewById(R.id.floatingSearchButton);
        mapSpinner = (ProgressBar) findViewById(R.id.mapSpinner);
        pocketCostDisplay = (TextView) findViewById(R.id.pocketCostDisplay);

        searchAreaSnackbarWarning = Snackbar.make(findViewById(android.R.id.content), "Search Area too large; zoom in a little.", Snackbar.LENGTH_INDEFINITE);
        mDelayedHandler = new Handler();
    }

    private void registerListeners()
    {
        floatingSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("testing", "floating search button touched");


                /*
                if (isWithinCoverageArea()) {
                    searchAreaSnackbarWarning.dismiss();
                    scanVisibleAreaForPockets();
                } else {
                    if(searchAreaSnackbarWarning.isShown())
                    {
                        searchAreaSnackbarWarning.dismiss();

                        mDelayedHandler.removeCallbacksAndMessages(null);
                        mDelayedHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showSearchAreaWarning();
                            }
                        }, 1500);
                    }
                    else
                        showSearchAreaWarning();
                }
                */

                scanVisibleAreaForPockets();
            }
        });
    }

    private void initializeGoogleMap() {
        final CustomMapFragment customMapFragment = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

        customMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                mGoogleMap = googleMap;

                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);

                customMapFragment.setOnDragListener(new MapWrapperLayout.OnDragListener() {
                    @Override
                    public void onDrag(MotionEvent motionEvent) {
                        //Log.d("testing", "Map Motion Event: " + String.format("ME: %s", motionEvent));

                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                           mapReactToTouch(true);
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                            mapReactToTouch(false);
                        }
                    }
                });

                /*

                if the user moves the map, then hide the info window
                    touching the info window will trigger the map click listener
                        if the map is unmoved, then do not hide the info window
                 */

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Log.d("testing", "map touched at zoom level: " + googleMap.getCameraPosition().zoom);

                        Log.d("testing", "adding room! : " + googleMap.getCameraPosition().zoom);

                        if (!placingRoom) {
                            Log.d("testing", "placingRoom was false, now true");

                            placingRoom = true;

                            Projection projection = googleMap.getProjection();
                            Point screenPosition = projection.toScreenLocation(latLng);

                            pocketCreator.resetRoomSize();
                            pocketCreator.setX(screenPosition.x - pocketCreator.getRadius());
                            pocketCreator.setY(screenPosition.y - pocketCreator.getRadius());

                            pocketCreator.show();
                            dimPocketCostDisplay(false);
                        } else {
                            Projection projection = googleMap.getProjection();
                            Point screenPosition = projection.toScreenLocation(latLng);

                            pocketCreator.setX(screenPosition.x - pocketCreator.getRadius());
                            pocketCreator.setY(screenPosition.y - pocketCreator.getRadius());
                        }
                    }
                });

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        Log.d("testing", "marker touched");

                        selectedPocketMarker = marker;

                        if(mapPockets.containsKey(marker.getTitle()))
                            moveToMarker(mapPockets.get(marker.getTitle()));
                        else
                            moveToMarker(new PocketMarker(null, marker, googleMap.getCameraPosition().zoom));

                        return true;
                    }
                });

                googleMap.setInfoWindowAdapter(new CustomInfoWindow(mActivity));

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Log.d("testing", "marker info window touched");

                        if(mapPockets.containsKey(marker.getTitle()))
                            joinPocket(marker.getTitle());
                        else
                            marker.remove();
                    }
                });

                updatePocketCostDisplay();
                initializePocketCreatorTool();
                focusOnUser(false, mGoogleMap.getCameraPosition().zoom);
            }
        });
    }

    private void checkIfHelped()
    {
        if(!GlobalVariables.hasSeenInitialHelp())
        {
            showPocketsHelpDialog("Welcome!");
            GlobalVariables.setHasSeenInitialHelp();
        }
    }

    private void initializePocketCreatorTool() {
        RelativeLayout parentView = (RelativeLayout) findViewById(R.id.rootHomeView);

        pocketCreator = new PocketCreator(this, parentView, 200.0f, 200.0f, 0, 0, true, new PocketCreator.PocketCreatorListener() {
            @Override
            public void onLongTouch(float longTouchedCenterX, float longTouchedCenterY) {
                if(mGoogleMap.getCameraPosition().zoom < 7.0)
                    showPocketRestrictionDialog();
                else
                    showPocketCreationDialog(longTouchedCenterX, longTouchedCenterY);
            }
        });
    }

    private void hidePocketManagementMenuIfShowing()
    {
        if(pocketManagementSlidingMenu.isMenuShowing())
            pocketManagementSlidingMenu.toggle(true);
    }

    private void focusOnUser(final boolean showMarker, final float zoomLevel)
    {
        GPS.getUsersLocation(this, new GPS.OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(final Location location, boolean success) {
                if (success) {
                    hidePocketManagementMenuIfShowing();

                    Log.d("testing", "(gps) moving to user (gps)");
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel), 1000, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            Log.d("testing", "(gps) moved to user");

                            if(showMarker) {
                                userLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .title("You!")
                                        .snippet("you are here"));

                                userLocationMarker.showInfoWindow();
                            }

                            updatePocketCostDisplay();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }
            }
        });
    }

    public void moveToMarker(PocketMarker pocketMarker)
    {
        final Marker marker = pocketMarker.getMarker();
        final float zoomLevel = pocketMarker.getZoomLevel();

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), zoomLevel), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

                marker.showInfoWindow();

                previousMapCenterLongitude = mGoogleMap.getCameraPosition().target.longitude;
                previousMapCenterLatitude = mGoogleMap.getCameraPosition().target.latitude;

                updatePocketCostDisplay();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private boolean isWithinCoverageArea() {
        if (getCurrentCoverageArea() < 110000)
            return true;
        else
            return false;
    }

    private double getCurrentCoverageArea() {
        final double topLatitude = round(mGoogleMap.getProjection().getVisibleRegion().farLeft.latitude, 10);
        final double bottomLatitude = round(mGoogleMap.getProjection().getVisibleRegion().nearLeft.latitude, 10);
        final double leftLongitude = round(mGoogleMap.getProjection().getVisibleRegion().farLeft.longitude, 10);
        final double rightLongitude = round(mGoogleMap.getProjection().getVisibleRegion().farRight.longitude, 10);

        double theta = leftLongitude - rightLongitude;
        double distance = Math.sin(Math.toRadians(topLatitude)) * Math.sin(Math.toRadians(topLatitude)) +
                Math.cos(Math.toRadians(topLatitude)) * Math.cos(Math.toRadians(topLatitude)) * Math.cos(Math.toRadians(theta));
        distance = Math.acos(distance);
        distance = Math.toDegrees(distance);
        double miles1 = distance * 60.0 * 1.1515;

        theta = leftLongitude - leftLongitude;
        distance = Math.sin(Math.toRadians(topLatitude)) * Math.sin(Math.toRadians(bottomLatitude)) +
                Math.cos(Math.toRadians(topLatitude)) * Math.cos(Math.toRadians(bottomLatitude)) * Math.cos(Math.toRadians(theta));
        distance = Math.acos(distance);
        distance = Math.toDegrees(distance);
        double miles2 = distance * 60.0 * 1.1515;

        return miles1 * miles2;
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    private void showMapBusySpinner() {
        mapSpinner.setVisibility(View.VISIBLE);
    }

    private void hideMapBusySpinner() {
        mapSpinner.setVisibility(View.GONE);
    }

    private void showSearchAreaWarning() {
        View mView = searchAreaSnackbarWarning.getView();
        TextView mTextView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        searchAreaSnackbarWarning.show();
    }

    public void dimPocketCostDisplay(boolean dim)
    {
        if(dim)
            pocketCostDisplay.setAlpha(0.5f);
        else
            pocketCostDisplay.setAlpha(1.0f);
    }

    private void mapReactToTouch(boolean shouldOnlyUpdatePocketCostDisplay) {
        Log.d("testing", "mapReactToTouch");

        double currentMapCenterLongitude = mGoogleMap.getCameraPosition().target.longitude;
        double currentMapCenterLatitude = mGoogleMap.getCameraPosition().target.latitude;

        if(shouldOnlyUpdatePocketCostDisplay)
            updatePocketCostDisplay();
        else {
            mMapMoved = true;
            placingRoom = false;

            if (pocketCreator != null)
                pocketCreator.hide();


            if(previousMapCenterLongitude != currentMapCenterLongitude && previousMapCenterLatitude != currentMapCenterLatitude) {
                if (userLocationMarker != null)
                    userLocationMarker.hideInfoWindow();

                if (selectedPocketMarker != null)
                    selectedPocketMarker.hideInfoWindow();
            }



            updatePocketCostDisplay();
        }

        previousMapCenterLongitude = currentMapCenterLongitude;
        previousMapCenterLatitude = currentMapCenterLatitude;
    }

    public void updatePocketCostDisplay() {
        float currentZoom = mGoogleMap.getCameraPosition().zoom;

        if(currentZoom < 7.0)
            dimPocketCostDisplay(true);
        else
            dimPocketCostDisplay(false);

        if (currentZoom >= 2.0 && currentZoom < 3.0) {
            pocketCostDisplay.setText(" Cost: (zoom in more)");
        }
        else if (currentZoom >= 3.0 && currentZoom < 5.0) {
            pocketCostDisplay.setText(" Cost: (a little more)");
        }
        else if (currentZoom >= 5.0 && currentZoom < 7.0) {
            pocketCostDisplay.setText(" Cost: (almost there)");
        }
        else if (currentZoom >= 7.0 && currentZoom < 17.0) {
            //Log.d("testing", "cost: " + (((400.0f / 9.0f) * currentZoom * currentZoom) - ((13600.0f / 9.0f) * currentZoom) + (115600.0f / 9.0f)));
            //pocketCost = Math.round(((400.0f / 9.0f) * currentZoom * currentZoom) - ((13600.0f / 9.0f) * currentZoom) + (115600.0f / 9.0f));
            pocketCost = Math.round(((50.0f) * currentZoom * currentZoom) - ((1700.0f) * currentZoom) + (14450.0f));
            pocketCostDisplay.setText(" Cost: " + String.valueOf(pocketCost) + " Lint ");
        } else if (currentZoom >= 17.0 || pocketCost <= 0) {
            pocketCostDisplay.setText(" Cost: Free!");
        }
    }

    private void showPocketsHelpDialog(String title) {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .content("Welcome to Pockets! Let's take a moment to get you acquainted with the app.\n\n" +
                        "'Pockets' are visual chat rooms that users can create all over the world. To create a pocket, tap the screen " +
                        "and the pocket creator tool will appear. Drag it around to position the pocket, then when you are satisfied " +
                        "with the placement of your pocket, hold your finger on the pocket creator to name the pocket and set an " +
                        "(optional) password.\n\n" +
                        "You gain 'Lint' when people chat in your pocket. Lint is the currency of Pockets; You use Lint to create " +
                        "Pockets. Big pockets require a lot of Lint while small pockets require either very little or none at all. " +
                        "You can also gain Lint by chatting within other people's pockets.\n\n" +
                        "That's all there is to it! Enjoy the app and thanks for using Pockets.")
                .positiveText("Close")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .neutralText("About Pockets")
                .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showAboutDialog();
                    }
                })
                .show();
    }

    private void togglePocketManagementSlider()
    {
        if(isLoggedIn)
        {
            pocketManagementSlidingMenu.toggle(true);
        }
        else
        {
            CenteredToastMessage.showCenteredToastMessage(this, "You gotta log in first.");
        }
    }

    private void showMyLint()
    {
        HTTP_Communications.myLint(this, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                if(success)
                {
                    int lint = 0;
                    try {
                        lint = response.getInt("lint");
                        CenteredToastMessage.showCenteredToastMessage(mActivity, "You have " + lint + " Lint!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                }
            }
        });
    }

    private void showSendLintDialog()
    {
        new MaterialDialog.Builder(this)
                .title("Send Lint")
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .positiveText("Send")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .neutralText("Cancel")
                .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .customView(R.layout.send_lint_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();

                        TextView recipientInput = (TextView) view.findViewById(R.id.recipientInput);
                        TextView amountInput = (TextView) view.findViewById(R.id.amountInput);

                        String recipient = recipientInput.getText().toString();
                        String amount = amountInput.getText().toString();

                        if(recipient.equals("") || amount.equals(""))
                            return;
                        else {

                            int intAmount = Integer.parseInt(amount);

                            HTTP_Communications.sendLint(mActivity, recipient, intAmount, new HTTP_Communications.OnServerRespond() {
                                @Override
                                public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                    if (success) {
                                        try {
                                            String result = response.getString("result");
                                            CenteredToastMessage.showCenteredToastMessage(mActivity, result);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                                    }
                                }
                            });
                        }
                    }
                })
                .show();
    }

    private void showAboutDialog()
    {
        DialogDispatcher.showInformationDialog(this, "About Pockets", "Created by Chris Conley.");
    }

    private void showLoginChoicesDialog()
    {
        View loginView = View.inflate(this, R.layout.login_options_layout, null);

        ImageButton facebookLoginButton = (ImageButton) loginView.findViewById(R.id.facebookLogin);
        ImageButton twitterLoginButton = (ImageButton) loginView.findViewById(R.id.twitterLogin);

        final MaterialDialog loginDialog = new MaterialDialog.Builder(this)
                .title("Choose a Login")
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .positiveText("Close")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .customView(loginView, false)
                .show();

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginDialog.dismiss();
                socialMediaLoginSlidingMenu.initializeFacebookLoginWebView(new SocialMediaLoginSlidingMenu.OnLoginResultReceived() {
                    @Override
                    public void result(boolean success, JSONObject jsonObject, String errorReason) {
                        if(success)
                        {
                            isLoggedIn = true;
                            CenteredToastMessage.showCenteredToastMessage(mActivity, "Facebook Login Successful!");

                            try {
                                String displayName = jsonObject.getString("displayName");
                                String avatar = jsonObject.getString("avatar");
                                String userid = jsonObject.getString("userid");

                                GlobalVariables.setDisplayName(displayName);
                                GlobalVariables.setAvatar(avatar);
                                GlobalVariables.setUserid(userid);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            isLoggedIn = false;
                            DialogDispatcher.showInformationDialog(mActivity, "oops", errorReason);
                        }

                        SharedPreferencesManager.saveToSharedPreferences(mActivity, "isLoggedIn", isLoggedIn);
                        invalidateOptionsMenu();
                    }
                });
            }
        });

        twitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginDialog.dismiss();
                socialMediaLoginSlidingMenu.initializeTwitterLoginWebView(new SocialMediaLoginSlidingMenu.OnLoginResultReceived() {
                    @Override
                    public void result(boolean success, JSONObject jsonObject, String errorReason) {
                        if(success)
                        {
                            isLoggedIn = true;
                            CenteredToastMessage.showCenteredToastMessage(mActivity, "Twitter Login Successful!");

                            try {
                                String displayName = jsonObject.getString("displayName");
                                String avatar = jsonObject.getString("avatar");
                                String userid = jsonObject.getString("userid");

                                GlobalVariables.setDisplayName(displayName);
                                GlobalVariables.setAvatar(avatar);
                                GlobalVariables.setUserid(userid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            isLoggedIn = false;
                            DialogDispatcher.showInformationDialog(mActivity, "oops", errorReason);
                        }

                        SharedPreferencesManager.saveToSharedPreferences(mActivity, "isLoggedIn", isLoggedIn);
                        invalidateOptionsMenu();
                    }
                });
            }
        });
    }

    private void performLogOut()
    {
        com.pocketschatapp._Utilities.HTTP_Communications.logout(this, new com.pocketschatapp._Utilities.HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                if(success)
                {
                    isLoggedIn = false;
                    invalidateOptionsMenu();
                    SharedPreferencesManager.saveToSharedPreferences(mActivity, "isLoggedIn", isLoggedIn);

                    GlobalVariables.setDisplayName("");
                    GlobalVariables.setAvatar("");
                    GlobalVariables.setUserid("");

                    CenteredToastMessage.showCenteredToastMessage(mActivity, "You have been logged out.");
                }
                else
                {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                }
            }
        });
    }

    private void scanVisibleAreaForPockets() {
        pocketCreator.hide();

        double topLatitude = mGoogleMap.getProjection().getVisibleRegion().farRight.latitude;
        double bottomLatitude = mGoogleMap.getProjection().getVisibleRegion().nearLeft.latitude;
        double leftLongitude = mGoogleMap.getProjection().getVisibleRegion().nearLeft.longitude;
        double rightLongitude = mGoogleMap.getProjection().getVisibleRegion().farRight.longitude;


        //straddle point also


        showMapBusySpinner();
        HTTP_Communications.getPocketsWithinGeographicArea(mActivity, topLatitude, bottomLatitude,
                leftLongitude, rightLongitude, new HTTP_Communications.OnServerRespond() {
                    @Override
                    public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                        hideMapBusySpinner();

                        Log.d("testing", response.toString());

                        if (success) {
                            try {
                                JSONArray pockets = response.getJSONArray("data");

                                mapPockets.clear();
                                mGoogleMap.clear();

                                for (int i = 0; i < pockets.length(); i++) {
                                    addPocketToMap(pockets.getJSONObject(i));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                        }
                    }
                });
    }

    private void showPocketRestrictionDialog()
    {
        CenteredToastMessage.showCenteredToastMessage(mActivity, "Zoom in more to create a pocket!");
    }

    private void showPocketCreationDialog(final float longTouchedCenterX, final float longTouchedCenterY) {
        new MaterialDialog.Builder(this)
                .title("Create Pocket")
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                .positiveText("Ok")
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .neutralText("Cancel")
                .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                .customView(R.layout.create_pocket_dialog, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog pocketCreationDialog, @NonNull DialogAction which) {

                        View view = pocketCreationDialog.getCustomView();

                        MaterialEditText pocketNameInput = (MaterialEditText) view.findViewById(R.id.pocketNameInput);
                        MaterialEditText pocketPasswordInput = (MaterialEditText) view.findViewById(R.id.pocketPasswordInput);

                        final String pocketName = pocketNameInput.getText().toString();
                        String pocketPassword = pocketPasswordInput.getText().toString();

                        final Projection projection = mGoogleMap.getProjection();

                        Point centerPoint = new Point((int) pocketCreator.getLongTouchedCenterX(), (int) pocketCreator.getLongTouchedCenterY());
                        Point northEastPoint = new Point((int) (longTouchedCenterX + pocketCreator.getRadius()), (int) (longTouchedCenterY - pocketCreator.getRadius()));
                        Point southWestPoint = new Point((int) (longTouchedCenterX - pocketCreator.getRadius()), (int) (longTouchedCenterY + pocketCreator.getRadius()));

                        LatLng center = projection.fromScreenLocation(centerPoint);
                        final LatLng northEast = projection.fromScreenLocation(northEastPoint);
                        final LatLng southWest = projection.fromScreenLocation(southWestPoint);

                        double topLatitude = northEast.latitude;
                        double bottomLatitude = southWest.latitude;
                        double leftLongitude = southWest.longitude;
                        double rightLongitude = northEast.longitude;
                        double centerLatitude = center.latitude;
                        double centerLongitude = center.longitude;

                        final float zoomLevel = mGoogleMap.getCameraPosition().zoom;

                        DialogDispatcher.showPassiveProgressDialog(mActivity, "...one sec...", "  creating your pocket");

                        HTTP_Communications.createPocket(mActivity, pocketName, pocketPassword, topLatitude, bottomLatitude, leftLongitude,
                                rightLongitude, centerLatitude, centerLongitude, zoomLevel, pocketCost,
                                new HTTP_Communications.OnServerRespond() {
                                    @Override
                                    public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                        if (success) {
                                            pocketCreator.hide();

                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            builder.include(northEast);
                                            builder.include(southWest);
                                            LatLngBounds bounds = builder.build();

                                            GroundOverlayOptions newarkMap = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromBitmap(pocketCreator.getAsBitmap())).positionFromBounds(bounds);
                                            GroundOverlay groundOverlay = mGoogleMap.addGroundOverlay(newarkMap);

                                            Point centerPoint = new Point((int) pocketCreator.getLongTouchedCenterX(), (int) pocketCreator.getLongTouchedCenterY());
                                            LatLng center = projection.fromScreenLocation(centerPoint);
                                            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(center)
                                                    .title(pocketName)
                                                    .snippet("Touch to Join")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.room_marker)));

                                            mapPockets.put(pocketName, new PocketMarker(groundOverlay, marker, zoomLevel));

                                            pocketCreationDialog.dismiss();
                                            DialogDispatcher.dismissCurrentDialog();
                                            CenteredToastMessage.showCenteredToastMessage(mActivity, "Pocket created!");

                                        } else {
                                            DialogDispatcher.adjustToInformationDialog("oops..", errorReason, "close");
                                        }
                                    }
                                });
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog pocketCreationDialog, @NonNull DialogAction which) {
                        pocketCreationDialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .show();
    }

    private void joinPocket(final String pocketName)
    {
        DialogDispatcher.showPassiveProgressDialog(mActivity, "one sec", "joining pocket...");

        HTTP_Communications.joinPocket(mActivity, pocketName, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                if(success) {
                    try {
                        boolean passwordRequired = response.getBoolean("passwordRequired");
                        if (passwordRequired) {

                            DialogDispatcher.dismissCurrentDialog();

                            new MaterialDialog.Builder(mActivity)
                                    .title("Password Required")
                                    .titleColor(ContextCompat.getColor(mActivity, R.color.dialogTitleColor))
                                    .content("This pocket requires a password.")
                                    .positiveColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                                    .neutralColor(ContextCompat.getColor(mActivity, R.color.dialogButtonColor))
                                    .neutralText("Cancel")
                                    .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialogBackgroundColor))
                                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                    .input(null, null, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                            DialogDispatcher.showPassiveProgressDialog(mActivity, "one sec", "joining pocket...");

                                            final String pocketPassword = input.toString();
                                            HTTP_Communications.joinPasswordedPocket(mActivity, pocketName, pocketPassword, new HTTP_Communications.OnServerRespond() {
                                                @Override
                                                public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                                                    if(success)
                                                    {
                                                        try {
                                                            String hashedPassword = response.getString("hashedPassword");
                                                            String pocketOwner = response.getString("pocketOwner");

                                                            Intent pocket = new Intent(mActivity, Pocket.class);
                                                            pocket.putExtra("pocketOwner", pocketOwner);
                                                            pocket.putExtra("pocketName", pocketName);
                                                            pocket.putExtra("pocketPassword", hashedPassword);
                                                            pocket.putExtra("isLoggedIn", isLoggedIn);
                                                            mActivity.startActivity(pocket);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        DialogDispatcher.dismissCurrentDialog();
                                                    }
                                                    else
                                                    {
                                                        DialogDispatcher.adjustToInformationDialog("oops..", errorReason, "close");
                                                    }
                                                }
                                            });
                                        }
                                    }).show();
                        } else {
                            try
                            {
                                String pocketOwner = response.getString("pocketOwner");

                                Intent pocket = new Intent(mActivity, Pocket.class);
                                pocket.putExtra("pocketOwner", pocketOwner);
                                pocket.putExtra("pocketName", pocketName);
                                pocket.putExtra("pocketPassword", "");
                                pocket.putExtra("isLoggedIn", isLoggedIn);
                                mActivity.startActivity(pocket);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            DialogDispatcher.dismissCurrentDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    DialogDispatcher.adjustToInformationDialog("oops..", errorReason, "close");
                }
            }
        });
    }

    private void showPocket(String pocketName)
    {
        pocketManagementSlidingMenu.showLoadingIndicator();

        HTTP_Communications.showPocketOnMap(this, pocketName, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject response, boolean success, String errorReason) {
                pocketManagementSlidingMenu.hideLoadingIndicator();

                if(success)
                {
                    pocketManagementSlidingMenu.toggle(true);

                    try {
                        moveToMarker(addPocketToMap(response.getJSONObject("pocket")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    CenteredToastMessage.showCenteredToastMessage(mActivity, errorReason);
                }
            }
        });
    }

    private PocketMarker addPocketToMap(JSONObject pocket) throws JSONException {
        String pocketName = pocket.getString("pocketname");
        float topLatitude = (float) pocket.getDouble("toplatitude");
        float bottomLatitude = (float) pocket.getDouble("bottomlatitude");
        float leftLongitude = (float) pocket.getDouble("leftlongitude");
        float rightLongitude = (float) pocket.getDouble("rightlongitude");
        float centerLatitude = (float) pocket.getDouble("centerlatitude");
        float centerLongitude = (float) pocket.getDouble("centerlongitude");
        float zoomLevel = (float) pocket.getDouble("zoomlevel");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng latLng1 = new LatLng(topLatitude, rightLongitude);
        LatLng latLng2 = new LatLng(bottomLatitude, leftLongitude);
        builder.include(latLng1);
        builder.include(latLng2);
        LatLngBounds bounds = builder.build();

        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromBitmap(pocketCreator.getAsBitmap())).positionFromBounds(bounds);
        GroundOverlay groundOverlay = mGoogleMap.addGroundOverlay(groundOverlayOptions);

        LatLng center = new LatLng(centerLatitude, centerLongitude);
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(center)
                .title(pocketName)
                .snippet("Touch to Join")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.room_marker)));

        PocketMarker pocketMarker = new PocketMarker(groundOverlay, marker, zoomLevel);
        mapPockets.put(pocketName, pocketMarker);

        return pocketMarker;
    }
}