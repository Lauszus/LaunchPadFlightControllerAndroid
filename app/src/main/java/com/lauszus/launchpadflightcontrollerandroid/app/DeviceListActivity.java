/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lauszus.launchpadflightcontrollerandroid.app;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class DeviceListActivity extends AppCompatActivity {
    // Debugging
    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    // Return Intent extra
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_NEW_DEVICE = "new_device";
    public static boolean new_device;

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private CustomArrayAdapter mNewDevicesArrayAdapter;
    private ProgressBar mProgressBar;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        setSupportActionBar((Toolbar) findViewById(R.id.device_list_toolbar));
        mProgressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        mProgressBar.setScaleX(.5f); // Make the progress bar half the size
        mProgressBar.setScaleY(.5f);

        LaunchPadFlightControllerActivity.stopRetrying = true; // Stop retrying connecting to another device

        // Set result CANCELED in case the user backs out
        setResult(AppCompatActivity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!LaunchPadFlightControllerActivity.isEmulator())
                    doDiscovery();
                else
                    mProgressBar.setVisibility(View.VISIBLE); // Just show the progress bar in the emulator
                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        CustomArrayAdapter mPairedDevicesArrayAdapter = new CustomArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new CustomArrayAdapter(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBtAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        else
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = null;
        if (!LaunchPadFlightControllerActivity.isEmulator())
            pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices != null && pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices)
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        } else if (LaunchPadFlightControllerActivity.isEmulator()) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            mPairedDevicesArrayAdapter.setSelectable(false);
            for (int i = 0; i < 3; i++)
                mPairedDevicesArrayAdapter.add("Name" + i + "\nXX:XX:XX:XX:XX:XX");
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
            mPairedDevicesArrayAdapter.setSelectable(false);
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<String> {
        private boolean selectable;

        CustomArrayAdapter(Context context, int resource) {
            super(context, resource);
            this.selectable = true; // Selectable by default
        }

        /**
         * Used to set the ListView non-selectable. Defaults to true.
         *
         * @param selectable Set to false to set it non-selectable.
         */
        void setSelectable(boolean selectable) {
            this.selectable = selectable;
        }

        @Override
        public boolean isEnabled(int position) {
            return selectable;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null)
            mBtAdapter.cancelDiscovery();

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D)
            Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        mProgressBar.setVisibility(View.VISIBLE);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            CharSequence text = ((TextView) v).getText();
            if (text == null)
                return;
            String info = text.toString();
            if (D)
                Log.d(TAG, "Info: " + info);

            mBtAdapter.cancelDiscovery(); // Cancel discovery because it's costly and we're about to connect

            String address = info.substring(info.length() - 17); // Get the device MAC address, which is the last 17 chars in the View
            new_device = av.getId() == R.id.new_devices;

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_NEW_DEVICE, new_device);

            // Set result and finish this Activity
            setResult(AppCompatActivity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // When discovery finds a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Get the BluetoothDevice object from the Intent
                if (device != null) {
                    if (D)
                        Log.i(TAG, "Device name: " + device.getName());
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) // If it's already paired, skip it, because it's been listed already
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // When discovery is finished, change the Activity title
                mProgressBar.setVisibility(View.GONE);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                    mNewDevicesArrayAdapter.setSelectable(false);
                }
            }
        }
    };
}
