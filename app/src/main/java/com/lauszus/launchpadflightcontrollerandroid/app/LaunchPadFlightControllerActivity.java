/*******************************************************************************
 * Copyright (C) 2015 Kristian Sloth Lauszus. All rights reserved.
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
 * Kristian Sloth Lauszus
 * Web      :  http://www.lauszus.com
 * e-mail   :  lauszus@gmail.com
 ******************************************************************************/

package com.lauszus.launchpadflightcontrollerandroid.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class LaunchPadFlightControllerActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private static final String TAG = LaunchPadFlightControllerActivity.class.getSimpleName();
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

    public static final String ANGLE_KP_VALUE = "angle_kp_value";
    public static final String HEADING_KP_VALUE = "heading_kp_value";
    public static final String ANGLE_MAX_INC_VALUE = "angle_max_inc_value";
    public static final String ANGLE_MAX_INC_SONAR_VALUE = "angle_max_inc_sonar_value";
    public static final String STICK_SCALING_ROLL_PITCH_VALUE = "stick_scaling_roll_pitch_value";
    public static final String STICK_SCALING_YAW_VALUE = "stick_scaling_yaw_value";

    public static final String KP_ROLL_PITCH_VALUE = "kp_roll_pitch_value";
    public static final String KI_ROLL_PITCH_VALUE = "ki_roll_pitch_value";
    public static final String KD_ROLL_PITCH_VALUE = "kd_roll_pitch_value";
    public static final String INT_LIMIT_ROLL_PITCH_VALUE = "int_limit_roll_pitch_value";

    public static final String KP_YAW_VALUE = "kp_yaw_value";
    public static final String KI_YAW_VALUE = "ki_yaw_value";
    public static final String KD_YAW_VALUE = "kd_yaw_value";
    public static final String INT_LIMIT_YAW_VALUE = "int_limit_yaw_value";

    public static final String KP_SONAR_ALT_HOLD_VALUE = "kp_sonar_alt_hold_value";
    public static final String KI_SONAR_ALT_HOLD_VALUE = "ki_sonar_alt_hold_value";
    public static final String KD_SONAR_ALT_HOLD_VALUE = "kd_sonar_alt_hold_value";
    public static final String INT_LIMIT_SONAR_ALT_HOLD_VALUE = "int_limit_sonar_alt_hold_value";

    public static final String KP_BARO_ALT_HOLD_VALUE = "kp_baro_alt_hold_value";
    public static final String KI_BARO_ALT_HOLD_VALUE = "ki_baro_alt_hold_value";
    public static final String KD_BARO_ALT_HOLD_VALUE = "kd_baro_alt_hold_value";
    public static final String INT_LIMIT_BARO_ALT_HOLD_VALUE = "int_limit_baro_alt_hold_value";

    public static final String ROLL_ANGLE = "roll_angle";
    public static final String PITCH_ANGLE = "pitch_angle";
    public static final String YAW_ANGLE = "yaw_angle";

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

    public int currentTabSelected;

    ViewPager mViewPager;
    ViewPagerAdapter mViewPagerAdapter;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_pad_flight_controller);

        // Get local Bluetooth adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        else
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            showToast("Bluetooth is not available", Toast.LENGTH_LONG);
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(this);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep the screen on
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
                mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending angles
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
            case R.id.action_settings:
                // Open up the settings dialog
                new SettingsDialogFragment().show(getSupportFragmentManager(), null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (D)
            Log.d(TAG, "onTabSelected: " + tab.getPosition());

        currentTabSelected = tab.getPosition();
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(currentTabSelected);

        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
            if (checkTab(ViewPagerAdapter.SETTINGS_FRAGMENT))
                mChatService.mBluetoothProtocol.getSettings();
            else if (checkTab(ViewPagerAdapter.PID_FRAGMENT)) {
                mChatService.mBluetoothProtocol.getPIDRollPitch();
                mChatService.mBluetoothProtocol.getPIDYaw();
                mChatService.mBluetoothProtocol.getPIDSonarAltHold();
                mChatService.mBluetoothProtocol.getPIDBaroAltHold();
            } else if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                if (GraphFragment.mToggleButton != null) {
                    if (GraphFragment.mToggleButton.isChecked())
                        mChatService.mBluetoothProtocol.sendAngles((byte) 1); // Request data
                    else
                        mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
                }
            }
        }

        if (!checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) { // Needed when the user rotates the screen
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Hide the keyboard
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (D)
            Log.d(TAG, "onTabUnselected: " + tab.getPosition() + " " + currentTabSelected);

        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
            if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT))
                mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
        }

        if (checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Hide the keyboard
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
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
        SettingsFragment settingsFragment;
        PIDFragment pidFragment;
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
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getSettings();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getPIDRollPitch();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getPIDYaw();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getPIDSonarAltHold();
                                        mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.getPIDBaroAltHold();
                                    }
                                }
                            }, 1000); // Wait 1 second before sending the message

                            if (mLaunchPadFlightControllerActivity.checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        LaunchPadFlightControllerActivity mLaunchPadFlightControllerActivity = mActivity.get();
                                        if (mLaunchPadFlightControllerActivity != null) {
                                            if (GraphFragment.mToggleButton.isChecked())
                                                mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.sendAngles((byte) 1); // Request data
                                            else
                                                mLaunchPadFlightControllerActivity.mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
                                        }
                                    }
                                }, 2000); // Wait 2 seconds before sending the message
                            }
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                    }
                    settingsFragment = (SettingsFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.SETTINGS_FRAGMENT);
                    if (settingsFragment != null)
                        settingsFragment.updateSendButton();
                    pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateSendButton();
                    break;

                case MESSAGE_READ:
                    Bundle data = msg.getData();
                    if (data != null) {
                        if (data.containsKey(ANGLE_KP_VALUE) && data.containsKey(HEADING_KP_VALUE) && data.containsKey(ANGLE_MAX_INC_VALUE) && data.containsKey(ANGLE_MAX_INC_SONAR_VALUE) && data.containsKey(STICK_SCALING_ROLL_PITCH_VALUE) && data.containsKey(STICK_SCALING_YAW_VALUE)) {
                            settingsFragment = (SettingsFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.SETTINGS_FRAGMENT);
                            if (settingsFragment != null)
                                settingsFragment.updateSettings(data.getInt(ANGLE_KP_VALUE), data.getInt(HEADING_KP_VALUE), data.getByte(ANGLE_MAX_INC_VALUE), data.getByte(ANGLE_MAX_INC_SONAR_VALUE), data.getInt(STICK_SCALING_ROLL_PITCH_VALUE), data.getInt(STICK_SCALING_YAW_VALUE));
                        }

                        pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                        if (pidFragment != null) {
                            if (data.containsKey(KP_ROLL_PITCH_VALUE) && data.containsKey(KI_ROLL_PITCH_VALUE) && data.containsKey(KD_ROLL_PITCH_VALUE) && data.containsKey(INT_LIMIT_ROLL_PITCH_VALUE))
                                pidFragment.updatePIDRollPitch(data.getInt(KP_ROLL_PITCH_VALUE), data.getInt(KI_ROLL_PITCH_VALUE), data.getInt(KD_ROLL_PITCH_VALUE), data.getInt(INT_LIMIT_ROLL_PITCH_VALUE));
                            else if (data.containsKey(KP_YAW_VALUE) && data.containsKey(KI_YAW_VALUE) && data.containsKey(KD_YAW_VALUE) && data.containsKey(INT_LIMIT_YAW_VALUE))
                                pidFragment.updatePIDYaw(data.getInt(KP_YAW_VALUE), data.getInt(KI_YAW_VALUE), data.getInt(KD_YAW_VALUE), data.getInt(INT_LIMIT_YAW_VALUE));
                            else if (data.containsKey(KP_SONAR_ALT_HOLD_VALUE) && data.containsKey(KI_SONAR_ALT_HOLD_VALUE) && data.containsKey(KD_SONAR_ALT_HOLD_VALUE) && data.containsKey(INT_LIMIT_SONAR_ALT_HOLD_VALUE))
                                pidFragment.updatePIDSonarAltHold(data.getInt(KP_SONAR_ALT_HOLD_VALUE), data.getInt(KI_SONAR_ALT_HOLD_VALUE), data.getInt(KD_SONAR_ALT_HOLD_VALUE), data.getInt(INT_LIMIT_SONAR_ALT_HOLD_VALUE));
                            else if (data.containsKey(KP_BARO_ALT_HOLD_VALUE) && data.containsKey(KI_BARO_ALT_HOLD_VALUE) && data.containsKey(KD_BARO_ALT_HOLD_VALUE) && data.containsKey(INT_LIMIT_BARO_ALT_HOLD_VALUE))
                                pidFragment.updatePIDBaroAltHold(data.getInt(KP_BARO_ALT_HOLD_VALUE), data.getInt(KI_BARO_ALT_HOLD_VALUE), data.getInt(KD_BARO_ALT_HOLD_VALUE), data.getInt(INT_LIMIT_BARO_ALT_HOLD_VALUE));
                        }

                        if (data.containsKey(ROLL_ANGLE) && data.containsKey(PITCH_ANGLE) && data.containsKey(YAW_ANGLE)) {
                            graphFragment = (GraphFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.GRAPH_FRAGMENT);
                            if (graphFragment != null)
                                graphFragment.updateAngles(data.getString(ROLL_ANGLE), data.getString(PITCH_ANGLE), data.getString(YAW_ANGLE));
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    if (msg.getData() != null)
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME); // Save the connected device's name
                    break;
                case MESSAGE_DISCONNECTED:
                    mLaunchPadFlightControllerActivity.supportInvalidateOptionsMenu();
                    settingsFragment = (SettingsFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.SETTINGS_FRAGMENT);
                    if (settingsFragment != null)
                        settingsFragment.updateSendButton();
                    pidFragment = (PIDFragment) mLaunchPadFlightControllerActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateSendButton();
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