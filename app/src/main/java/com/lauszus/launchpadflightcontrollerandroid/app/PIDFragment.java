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

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class PIDFragment extends Fragment {
    private static final String TAG = PIDFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private Button mSendButton;
    private RadioButton rollPitchRadio;

    private TextView mKpCurrentValue, mKiCurrentValue, mKdCurrentValue, mIntLimitCurrentValue;
    private SeekBarArrows mKpSeekBar, mKiSeekBar, mKdSeekBar, mIntLimitSeekBar;

    private int KpRollPitch, KiRollPitch, KdRollPitch, IntLimitRollPitch;
    private int KpYaw, KiYaw, KdYaw, IntLimitYaw;
    private boolean receivedPIDValues;

    private final Handler mHandler = new Handler();
    private int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pid, container, false);

        if (v == null)
            throw new RuntimeException();

        OnClickListener radioOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        };

        rollPitchRadio = (RadioButton) v.findViewById(R.id.rollPitchRadio);
        rollPitchRadio.setOnClickListener(radioOnClickListener);
        RadioButton yawRadio = (RadioButton) v.findViewById(R.id.yawRadio);
        yawRadio.setOnClickListener(radioOnClickListener);

        mKpCurrentValue = (TextView) v.findViewById(R.id.KpCurrentValue);
        mKiCurrentValue = (TextView) v.findViewById(R.id.KiCurrentValue);
        mKdCurrentValue = (TextView) v.findViewById(R.id.KdCurrentValue);
        mIntLimitCurrentValue = (TextView) v.findViewById(R.id.IntLimitCurrentValue);

        mKpSeekBar = new SeekBarArrows(v.findViewById(R.id.Kp), R.string.Kp, 1000, true); // 0-10 in 0.01 steps
        mKiSeekBar = new SeekBarArrows(v.findViewById(R.id.Ki), R.string.Ki, 1000, true); // 0-10 in 0.01 steps
        mKdSeekBar = new SeekBarArrows(v.findViewById(R.id.Kd), R.string.Kd, 1000, true); // 0-10 in 0.01 steps
        mIntLimitSeekBar = new SeekBarArrows(v.findViewById(R.id.IntLimit), R.string.IntLimit, 1000, true); // 0-10 in 0.01 steps

        KpRollPitch = KpYaw = mKpSeekBar.getProgress();
        KiRollPitch = KiYaw = mKpSeekBar.getProgress();
        KdRollPitch = KdYaw = mKpSeekBar.getProgress();
        IntLimitRollPitch = IntLimitYaw = mIntLimitSeekBar.getProgress();

        mSendButton = (Button) v.findViewById(R.id.button);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                if (activity == null || activity.mChatService == null) {
                    if (D)
                        Log.e(TAG, "mChatService == null");
                    return;
                }
                if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (rollPitchRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.setPIDRollPitch(mKpSeekBar.getProgress(), mKiSeekBar.getProgress(), mKdSeekBar.getProgress(), mIntLimitSeekBar.getProgress());
                            else
                                activity.mChatService.mBluetoothProtocol.setPIDYaw(mKpSeekBar.getProgress(), mKiSeekBar.getProgress(), mKdSeekBar.getProgress(), mIntLimitSeekBar.getProgress());
                        }
                    }); // Wait before sending the message
                    counter += 100;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (rollPitchRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.getPIDRollPitch();
                            else
                                activity.mChatService.mBluetoothProtocol.getPIDYaw();
                        }
                    }, counter); // Wait before sending the message
                    counter = 0; // Reset counter
                }
            }
        });

        receivedPIDValues = false;
        updateSendButton();

        return v;
    }

    public void updatePIDRollPitch(int Kp, int Ki, int Kd, int IntLimit) {
        KpRollPitch = Kp;
        KiRollPitch = Ki;
        KdRollPitch = Kd;
        IntLimitRollPitch = IntLimit;

        receivedPIDValues = true;
        updateView();
    }

    public void updatePIDYaw(int Kp, int Ki, int Kd, int IntLimit) {
        KpYaw = Kp;
        KiYaw = Ki;
        KdYaw = Kd;
        IntLimitYaw = IntLimit;

        receivedPIDValues = true;
        updateView();
    }

    private void updateView() {
        if (mKpCurrentValue != null && mKpSeekBar != null) {
            int Kp = rollPitchRadio.isChecked() ? KpRollPitch : KpYaw;
            if (receivedPIDValues)
                mKpCurrentValue.setText(mKpSeekBar.progressToString(Kp));
            mKpSeekBar.setProgress(Kp);
        }
        if (mKiCurrentValue != null && mKiSeekBar != null) {
            int Ki = rollPitchRadio.isChecked() ? KiRollPitch : KiYaw;
            if (receivedPIDValues)
                mKiCurrentValue.setText(mKiSeekBar.progressToString(Ki));
            mKiSeekBar.setProgress(Ki);
        }
        if (mKdCurrentValue != null && mKdSeekBar != null) {
            int Kd = rollPitchRadio.isChecked() ? KdRollPitch : KdYaw;
            if (receivedPIDValues)
                mKdCurrentValue.setText(mKdSeekBar.progressToString(Kd));
            mKdSeekBar.setProgress(Kd);
        }
        if (mIntLimitCurrentValue != null && mIntLimitSeekBar != null) {
            int IntLimit = rollPitchRadio.isChecked() ? IntLimitRollPitch : IntLimitYaw;
            if (receivedPIDValues)
                mIntLimitCurrentValue.setText(mIntLimitSeekBar.progressToString(IntLimit));
            mIntLimitSeekBar.setProgress(IntLimit);
        }
    }

    public void updateSendButton() {
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && mSendButton != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                mSendButton.setText(R.string.updatePIDValues);
            else
                mSendButton.setText(R.string.button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the user resumes the view, then set the values again
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null)
            updateSendButton();
    }
}
