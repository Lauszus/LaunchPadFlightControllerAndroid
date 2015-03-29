/*******************************************************************************
 * Copyright (C) 2015 Kristian Lauszus, TKJ Electronics. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Lauszus, TKJ Electronics
 * Web      :  http://www.tkjelectronics.com
 * e-mail   :  kristianl@tkjelectronics.com
 ******************************************************************************/

package com.lauszus.launchpadflightcontrollerandroid.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.viewpagerindicator.UnderlinePageIndicator;

import java.lang.ref.WeakReference;

public class LaunchPadFlightControllerActivity extends ActionBarActivity implements ActionBar.TabListener {
    private static final String TAG = "LaunchPadFlightControllerActivity";
    public static final boolean D = BuildConfig.DEBUG; // This is automatically set when building

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_DISCONNECTED = 4;
    public static final int MESSAGE_RETRY = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String KP_VALUE = "kp_value";
    public static final String KI_VALUE = "ki_value";
    public static final String KD_VALUE = "kd_value";
    public static final String TARGET_ANGLE = "target_angle";
    public static final String TURNING_SCALE = "turning_scale";

    public static final String SPEED_VALUE = "speed_value";
    public static final String CURRENT_DRAW = "current_draw";
    public static final String TURNING_VALUE = "turning_value";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String RUN_TIME = "run_time";

    public static final String QANGLE_VALUE = "qangle_value";
    public static final String QBIAS_VALUE = "qbias_value";
    public static final String RMEASURE_VALUE = "rmeasure_value";

    public static final String ACC_ANGLE = "acc_angle";
    public static final String GYRO_ANGLE = "gyro_angle";
    public static final String KALMAN_ANGLE = "kalman_angle";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private final BluetoothHandler mBluetoothHandler = new BluetoothHandler(this);
    // Member object for the chat services
    public BluetoothChatService mChatService = null;

    BluetoothDevice btDevice; // The BluetoothDevice object
    boolean btSecure; // If it's a new device we will pair with the device
    public static boolean stopRetrying;

    private Toast mToast;

    /** The {@link UnderlinePageIndicator} that will host the section contents. */
    UnderlinePageIndicator mUnderlinePageIndicator;

    public int currentTabSelected;

    ViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_pad_flight_controller);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Get local Bluetooth adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBluetoothAdapter = (BluetoothAdapter) getSystemService(Context.BLUETOOTH_SERVICE);
        else
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            showToast("Bluetooth is not available", Toast.LENGTH_LONG);
            finish();
            return;
        }

        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        // Bind the underline indicator to the adapter
        mUnderlinePageIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        mUnderlinePageIndicator.setViewPager(mViewPager);
        mUnderlinePageIndicator.setFades(false);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mUnderlinePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (D)
                    Log.d(TAG, "ViewPager position: " + position);
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mViewPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep the screen on while the user is riding the robot
    }

    public void showToast(String message, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null)
            mToast.cancel(); // Close the toast if it's already open
        mToast = Toast.makeText(this, message, duration);
        mToast.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.d(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            if (D)
                Log.d(TAG, "Request enable BT");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else
            setupBTService(); // Otherwise, setup the chat session
    }

    @Override
    public void onBackPressed() {
        if (mChatService != null) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mChatService.stop(); // Stop the Bluetooth chat services if the user exits the app
                }
            }, 1000); // Wait 1 second before closing the connection, this is needed as onPause() will send stop messages before closing
        }
        finish(); // Exits the app
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (D)
            Log.d(TAG, "- ON PAUSE -");
        if (mChatService != null) { // Send stop command and stop sending graph data command
            if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                mChatService.mBluetoothProtocol.stopInfo();
                mChatService.mBluetoothProtocol.stopImu();
            }
        }
    }

    private void setupBTService() {
        if (mChatService != null)
            return;

        if (D)
            Log.d(TAG, "setupBTService()");
        mChatService = new BluetoothChatService(mBluetoothHandler, mBluetoothAdapter); // Initialize the BluetoothChatService to perform Bluetooth connections
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (D)
            Log.d(TAG, "onPrepareOptionsMenu");
        MenuItem menuItem = menu.findItem(R.id.action_connect); // Find item
        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
            menuItem.setIcon(R.drawable.device_access_bluetooth_connected);
        else
            menuItem.setIcon(R.drawable.device_access_bluetooth);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launch_pad_flight_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_connect:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            /*case R.id.action_settings:
                // TODO: Make settings dialog
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (D)
            Log.d(TAG, "onTabSelected: " + tab.getPosition());

        currentTabSelected = tab.getPosition();
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mUnderlinePageIndicator.setCurrentItem(currentTabSelected);

        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
            if (checkTab(ViewPagerAdapter.INFO_FRAGMENT))
                mChatService.mBluetoothProtocol.startInfo();
            else if (checkTab(ViewPagerAdapter.PID_FRAGMENT)) {
                mChatService.mBluetoothProtocol.getPID();
                mChatService.mBluetoothProtocol.getTarget();
                mChatService.mBluetoothProtocol.getTurning();
            } else if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                mChatService.mBluetoothProtocol.getKalman();
                if (GraphFragment.mToggleButton != null) {
                    if (GraphFragment.mToggleButton.isChecked())
                        mChatService.mBluetoothProtocol.startImu(); // Request data
                    else
                        mChatService.mBluetoothProtocol.stopImu(); // Stop sending data
                }
            }
        }

        if (!checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) { // Needed when the user rotates the screen
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Hide the keyboard
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (D)
            Log.d(TAG, "onTabUnselected: " + tab.getPosition() + " " + currentTabSelected);

        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
            if (checkTab(ViewPagerAdapter.INFO_FRAGMENT))
                mChatService.mBluetoothProtocol.stopInfo(); // Stop sending info
            else if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT))
                mChatService.mBluetoothProtocol.stopImu(); // Stop sending data
        }

        if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Hide the keyboard
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public boolean checkTab(int tab) {
        return (currentTabSelected == tab);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect to
                if (resultCode == Activity.RESULT_OK)
                    connectDevice(data, false);
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                    setupBTService(); // Bluetooth is now enabled, so set up a chat session
                else {
                    // User did not enable Bluetooth or an error occurred
                    if (D)
                        Log.d(TAG, "BT not enabled");
                    showToast(getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_SHORT);
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean retry) {
        if (retry) {
            if (btDevice != null && !stopRetrying) {
                mChatService.start(); // This will stop all the running threads
                mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            }
        } else { // It's a new connection
            stopRetrying = false;
            mChatService.newConnection = true;
            mChatService.start(); // This will stop all the running threads
            if (data.getExtras() == null)
                return;
            String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device Bluetooth address
            btSecure = data.getExtras().getBoolean(DeviceListActivity.EXTRA_NEW_DEVICE); // If it's a new device we will pair with the device
            btDevice = mBluetoothAdapter.getRemoteDevice(address); // Get the BluetoothDevice object
            mChatService.nRetries = 0; // Reset retry counter
            mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            showToast(getString(R.string.connecting), Toast.LENGTH_SHORT);
        }
    }

    public Fragment getFragment(int item) {
        return (Fragment) mViewPagerAdapter.instantiateItem(mViewPager, item);
    }

    // The Handler class that gets information back from the BluetoothChatService
    private static class BluetoothHandler extends Handler {
        private final WeakReference<LaunchPadFlightControllerActivity> mActivity; // See: http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
        PIDFragment pidFragment;
        InfoFragment infoFragment;
        GraphFragment graphFragment;
        private String mConnectedDeviceName; // Name of the connected device

        BluetoothHandler(LaunchPadFlightControllerActivity activity) {
            mActivity  = new WeakReference<LaunchPadFlightControllerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LaunchPadFlightControllerActivity mLaunchPadFlightControllerActivity = mActivity.get();
            if (mLaunchPadFlightControllerActivity == null)
                return;
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    mLaunchPadFlightControllerActivity.supportInvalidateOptionsMenu();
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            mLaunchPadFlightControllerActivity.showToast(mLaunchPadFlightControllerActivity.getString(R.string.connected_to) + " " + mConnectedDeviceName, Toast.LENGTH_SHORT);
                            if (mLaunchPadFlightControllerActivity.mChatService == null)
                                return;
                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    LaunchPadFlightControllerActivity mLaunchPadFlightControllerActivity = mActivity.get();
                                    if (mLaunchPadFlightControllerActivity != null) {
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getPID();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getTarget();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getTurning();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getKalman();
                                    }
                                }
                            }, 1000); // Wait 1 second before sending the message

                            if (mLaunchPadFlightControllerActivity.checkTab(ViewPagerAdapter.INFO_FRAGMENT)) {
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        LaunchPadFlightControllerActivity mLaunchPadFlightControllerActivity = mActivity.get();
                                        if (mLaunchPadFlightControllerActivity != null)
                                            mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.startInfo(); // Request info
                                    }
                                }, 2000); // Wait 2 seconds before sending the message
                            } else if (mLaunchPadFlightControllerActivity.checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        LaunchPadFlightControllerActivity mLaunchPadFlightControllerActivity = mActivity.get();
                                        if (mLaunchPadFlightControllerActivity != null) {
                                            if (GraphFragment.mToggleButton.isChecked())
                                                mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.startImu(); // Request data
                                            else
                                                mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.stopImu(); // Stop sending data
                                        }
                                    }
                                }, 2000); // Wait 2 seconds before sending the message
                            }
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                    }
                    pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateButton();
                    break;

                case MESSAGE_READ:
                    Bundle data = msg.getData();
                    if (data != null) {
                        pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                        if (pidFragment != null) {
                            if (data.containsKey(KP_VALUE) && data.containsKey(KI_VALUE) && data.containsKey(KD_VALUE))
                                pidFragment.updatePID(data.getString(KP_VALUE), data.getString(KI_VALUE), data.getString(KD_VALUE));
                            else if (data.containsKey(TARGET_ANGLE))
                                pidFragment.updateAngle(data.getString(TARGET_ANGLE));
                            else if (data.containsKey(TURNING_SCALE))
                                pidFragment.updateTurning(data.getInt(TURNING_SCALE));
                        }

                        if (data.containsKey(SPEED_VALUE) && data.containsKey(CURRENT_DRAW) && data.containsKey(TURNING_VALUE) && data.containsKey(BATTERY_LEVEL) && data.containsKey(RUN_TIME)) {
                            infoFragment = (InfoFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.INFO_FRAGMENT);
                            if (infoFragment != null)
                                infoFragment.updateView(data.getInt(SPEED_VALUE), data.getInt(CURRENT_DRAW), data.getInt(TURNING_VALUE), data.getInt(BATTERY_LEVEL), data.getLong(RUN_TIME));
                        }

                        if (data.containsKey(QANGLE_VALUE) && data.containsKey(QBIAS_VALUE) && data.containsKey(RMEASURE_VALUE)) {
                            graphFragment = (GraphFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.GRAPH_FRAGMENT);
                            if (graphFragment != null)
                                graphFragment.updateKalman(data.getString(QANGLE_VALUE), data.getString(QBIAS_VALUE), data.getString(RMEASURE_VALUE));
                        }

                        if (data.containsKey(ACC_ANGLE) && data.containsKey(GYRO_ANGLE) && data.containsKey(KALMAN_ANGLE)) {
                            graphFragment = (GraphFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.GRAPH_FRAGMENT);
                            if (graphFragment != null)
                                graphFragment.updateIMUValues(data.getString(ACC_ANGLE), data.getString(GYRO_ANGLE), data.getString(KALMAN_ANGLE));
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    if (msg.getData() != null)
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME); // Save the connected device's name
                    break;
                case MESSAGE_DISCONNECTED:
                    mLaunchPadFlightControllerActivity.supportInvalidateOptionsMenu();
                    pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateButton();
                    if (msg.getData() != null)
                        mLaunchPadFlightControllerActivity.showToast(msg.getData().getString(TOAST), Toast.LENGTH_SHORT);
                    break;
                case MESSAGE_RETRY:
                    if (D)
                        Log.d(TAG, "MESSAGE_RETRY");
                    mLaunchPadFlightControllerActivity.connectDevice(null, true);
                    break;
            }
        }
    }
}