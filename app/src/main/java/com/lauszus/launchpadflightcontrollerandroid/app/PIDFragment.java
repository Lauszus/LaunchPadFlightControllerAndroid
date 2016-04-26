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
    private RadioButton rollPitchRadio, yawRadio, sonarAltHoldRadio;

    private TextView mKpCurrentValue, mKiCurrentValue, mKdCurrentValue, mIntLimitCurrentValue;
    private SeekBarArrows mKpSeekBar, mKiSeekBar, mKdSeekBar, mIntLimitSeekBar;

    private int KpRollPitch, KiRollPitch, KdRollPitch, IntLimitRollPitch;
    private int KpYaw, KiYaw, KdYaw, IntLimitYaw;
    private int KpSonarAltHold, KiSonarAltHold, KdSonarAltHold, IntLimitSonarAltHold;
    private int KpBaroAltHold, KiBaroAltHold, KdBaroAltHold, IntLimitBaroAltHold;
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
        yawRadio = (RadioButton) v.findViewById(R.id.yawRadio);
        yawRadio.setOnClickListener(radioOnClickListener);
        sonarAltHoldRadio = (RadioButton) v.findViewById(R.id.sonarAltHoldRadio);
        sonarAltHoldRadio.setOnClickListener(radioOnClickListener);
        RadioButton baroAltHoldRadio = (RadioButton) v.findViewById(R.id.baroAltHoldRadio);
        baroAltHoldRadio.setOnClickListener(radioOnClickListener);

        mKpCurrentValue = (TextView) v.findViewById(R.id.KpCurrentValue);
        mKiCurrentValue = (TextView) v.findViewById(R.id.KiCurrentValue);
        mKdCurrentValue = (TextView) v.findViewById(R.id.KdCurrentValue);
        mIntLimitCurrentValue = (TextView) v.findViewById(R.id.IntLimitCurrentValue);

        mKpSeekBar = new SeekBarArrows(v.findViewById(R.id.Kp), R.string.Kp, 10, 0.001f); // 0-10 in 0.001 steps
        mKiSeekBar = new SeekBarArrows(v.findViewById(R.id.Ki), R.string.Ki, 100, 0.01f); // 0-100 in 0.01 steps
        mKdSeekBar = new SeekBarArrows(v.findViewById(R.id.Kd), R.string.Kd, 0.1f, 0.00001f); // 0-0.1 in 0.00001 steps
        mIntLimitSeekBar = new SeekBarArrows(v.findViewById(R.id.IntLimit), R.string.IntLimit, 100, 0.01f); // 0-100 in 0.01 steps

        // Set default values
        KpRollPitch = KpYaw = KpSonarAltHold = KpBaroAltHold = mKpSeekBar.getProgress();
        KiRollPitch = KiYaw = KiSonarAltHold = KiBaroAltHold = mKiSeekBar.getProgress();
        KdRollPitch = KdYaw = KdSonarAltHold = KdBaroAltHold = mKdSeekBar.getProgress();
        IntLimitRollPitch = IntLimitYaw = IntLimitSonarAltHold = IntLimitBaroAltHold = mIntLimitSeekBar.getProgress();

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
                            int Kp = mKpSeekBar.getProgress();
                            int Ki = mKiSeekBar.getProgress();
                            int Kd = mKdSeekBar.getProgress();
                            int IntLimit = mIntLimitSeekBar.getProgress();
                            if (rollPitchRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.setPIDRollPitch(Kp, Ki, Kd, IntLimit);
                            else if (yawRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.setPIDYaw(Kp, Ki, Kd, IntLimit);
                            else if (sonarAltHoldRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.setPIDSonarAltHold(Kp, Ki, Kd, IntLimit);
                            else
                                activity.mChatService.mBluetoothProtocol.setPIDBaroAltHold(Kp, Ki, Kd, IntLimit);
                        }
                    }); // Wait before sending the message
                    counter += 100;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (rollPitchRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.getPIDRollPitch();
                            else if (yawRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.getPIDYaw();
                            else if (sonarAltHoldRadio.isChecked())
                                activity.mChatService.mBluetoothProtocol.getPIDSonarAltHold();
                            else
                                activity.mChatService.mBluetoothProtocol.getPIDBaroAltHold();
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

    public void updatePIDSonarAltHold(int Kp, int Ki, int Kd, int IntLimit) {
        KpSonarAltHold = Kp;
        KiSonarAltHold = Ki;
        KdSonarAltHold = Kd;
        IntLimitSonarAltHold = IntLimit;

        receivedPIDValues = true;
        updateView();
    }

    public void updatePIDBaroAltHold(int Kp, int Ki, int Kd, int IntLimit) {
        KpBaroAltHold = Kp;
        KiBaroAltHold = Ki;
        KdBaroAltHold = Kd;
        IntLimitBaroAltHold = IntLimit;

        receivedPIDValues = true;
        updateView();
    }

    private void updateView() {
        if (mKpCurrentValue != null && mKpSeekBar != null) {
            int Kp = rollPitchRadio.isChecked() ? KpRollPitch : yawRadio.isChecked() ? KpYaw : sonarAltHoldRadio.isChecked() ? KpSonarAltHold : KpBaroAltHold;
            if (receivedPIDValues)
                mKpCurrentValue.setText(mKpSeekBar.progressToString(Kp));
            mKpSeekBar.setProgress(Kp);
        }
        if (mKiCurrentValue != null && mKiSeekBar != null) {
            int Ki = rollPitchRadio.isChecked() ? KiRollPitch : yawRadio.isChecked() ? KiYaw : sonarAltHoldRadio.isChecked() ? KiSonarAltHold : KiBaroAltHold;
            if (receivedPIDValues)
                mKiCurrentValue.setText(mKiSeekBar.progressToString(Ki));
            mKiSeekBar.setProgress(Ki);
        }
        if (mKdCurrentValue != null && mKdSeekBar != null) {
            int Kd = rollPitchRadio.isChecked() ? KdRollPitch : yawRadio.isChecked() ? KdYaw : sonarAltHoldRadio.isChecked() ? KdSonarAltHold : KdBaroAltHold;
            if (receivedPIDValues)
                mKdCurrentValue.setText(mKdSeekBar.progressToString(Kd));
            mKdSeekBar.setProgress(Kd);
        }
        if (mIntLimitCurrentValue != null && mIntLimitSeekBar != null) {
            int IntLimit = rollPitchRadio.isChecked() ? IntLimitRollPitch : yawRadio.isChecked() ? IntLimitYaw : sonarAltHoldRadio.isChecked() ? IntLimitSonarAltHold : IntLimitBaroAltHold;
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
