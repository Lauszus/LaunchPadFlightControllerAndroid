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

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
        View view = activity.getLayoutInflater().inflate(R.layout.settings_dialog, null);

        Button mCalibrateAccButton = (Button) view.findViewById(R.id.calibrateAccButton);
        mCalibrateAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.mChatService != null) {
                    if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                        activity.mChatService.mBluetoothProtocol.calibrateAccelerometer();
                        Toast.makeText(activity, "Calibrating accelerometer", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }
        });

        Button mCalibrateMagButton = (Button) view.findViewById(R.id.calibrateMagButton);
        mCalibrateMagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.mChatService != null) {
                    if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                        activity.mChatService.mBluetoothProtocol.calibrateMagnetometer();
                        Toast.makeText(activity, "Calibrating magnetometer", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }
        });

        Button mRestoreButton = (Button) view.findViewById(R.id.restoreButton);
        mRestoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.mChatService != null) {
                    if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                        activity.mChatService.mBluetoothProtocol.restoreDefaults();
                        Toast.makeText(activity, "Default values have been restored", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }
        });

        if (activity.mChatService != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                mCalibrateAccButton.setText(R.string.calibrateAccButtonText);
                mCalibrateMagButton.setText(R.string.calibrateMagButtonText);
                mRestoreButton.setText(R.string.restoreButtonText);
            } else {
                mCalibrateAccButton.setText(R.string.button);
                mCalibrateMagButton.setText(R.string.button);
                mRestoreButton.setText(R.string.button);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        return builder.setTitle(R.string.dialog_title) // Set title
                .setView(view) // // Set custom view
                .create(); // Create the AlertDialog
    }
}