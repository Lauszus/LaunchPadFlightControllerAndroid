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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class PIDFragment extends Fragment {
    private static final String TAG = PIDFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private Button mSendButton;
    private RadioButton rollPitchRadio;

    private TextView mKpCurrentValue, mKiCurrentValue, mKdCurrentValue, mIntLimitCurrentValue;
    private SeekBar mKpSeekBar, mKiSeekBar, mKdSeekBar, mIntLimitSeekBar;

    private int KpRollPitch, KiRollPitch, KdRollPitch, IntLimitRollPitch;
    private int KpYaw, KiYaw, KdYaw, IntLimitYaw;
    boolean receivedPIDValues;

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

        LinearLayout mKp = (LinearLayout) v.findViewById(R.id.Kp);
        ((TextView) mKp.findViewById(R.id.text)).setText(getResources().getText(R.string.Kp));
        LinearLayout mKi = (LinearLayout) v.findViewById(R.id.Ki);
        ((TextView) mKi.findViewById(R.id.text)).setText(getResources().getText(R.string.Ki));
        LinearLayout mKd = (LinearLayout) v.findViewById(R.id.Kd);
        ((TextView) mKd.findViewById(R.id.text)).setText(getResources().getText(R.string.Kd));
        LinearLayout mIntLimit = (LinearLayout) v.findViewById(R.id.IntLimit);
        ((TextView) mIntLimit.findViewById(R.id.text)).setText(getResources().getText(R.string.IntLimit));

        class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
            TextView SeekBarValue;

            SeekBarListener(View SeekBarValue) {
                this.SeekBarValue = (TextView) SeekBarValue;
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                SeekBarValue.setText(pidToString(progress)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }

        mKpSeekBar = (SeekBar) mKp.findViewById(R.id.seekBar);
        mKpSeekBar.setMax(1000); // 0-10
        mKpSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mKp.findViewById(R.id.value)));
        mKpSeekBar.setProgress(mKpSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        KpRollPitch = KpYaw = mKpSeekBar.getProgress();

        mKiSeekBar = (SeekBar) mKi.findViewById(R.id.seekBar);
        mKiSeekBar.setMax(1000); // 0-10
        mKiSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mKi.findViewById(R.id.value)));
        mKiSeekBar.setProgress(mKiSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        KiRollPitch = KiYaw = mKpSeekBar.getProgress();

        mKdSeekBar = (SeekBar) mKd.findViewById(R.id.seekBar);
        mKdSeekBar.setMax(1000); // 0-10
        mKdSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mKd.findViewById(R.id.value)));
        mKdSeekBar.setProgress(mKdSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        KdRollPitch = KdYaw = mKpSeekBar.getProgress();

        mIntLimitSeekBar = (SeekBar) mIntLimit.findViewById(R.id.seekBar);
        mIntLimitSeekBar.setMax(1000); // 0-10
        mIntLimitSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mIntLimit.findViewById(R.id.value)));
        mIntLimitSeekBar.setProgress(mIntLimitSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        IntLimitRollPitch = IntLimitYaw = mIntLimitSeekBar.getProgress();

        // Use custom OnArrowListener class to handle button click, button long click and if the button is held down
        new OnArrowListener(mKp.findViewById(R.id.upArrow), mKpSeekBar, true);
        new OnArrowListener(mKp.findViewById(R.id.downArrow), mKpSeekBar, false);

        new OnArrowListener(mKi.findViewById(R.id.upArrow), mKiSeekBar, true);
        new OnArrowListener(mKi.findViewById(R.id.downArrow), mKiSeekBar, false);

        new OnArrowListener(mKd.findViewById(R.id.upArrow), mKdSeekBar, true);
        new OnArrowListener(mKd.findViewById(R.id.downArrow), mKdSeekBar, false);

        new OnArrowListener(mIntLimit.findViewById(R.id.upArrow), mIntLimitSeekBar, true);
        new OnArrowListener(mIntLimit.findViewById(R.id.downArrow), mIntLimitSeekBar, false);

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

    private String pidToString(int value) {
        return String.format(Locale.US, "%.2f", (float) value / 100.0f); // SeekBar can only handle integers, so format it to a float with two decimal places
    }

    private void updateView() {
        if (mKpCurrentValue != null && mKpSeekBar != null) {
            int Kp = rollPitchRadio.isChecked() ? KpRollPitch : KpYaw;
            if (receivedPIDValues)
                mKpCurrentValue.setText(pidToString(Kp));
            mKpSeekBar.setProgress(Kp);
        }
        if (mKiCurrentValue != null && mKiSeekBar != null) {
            int Ki = rollPitchRadio.isChecked() ? KiRollPitch : KiYaw;
            if (receivedPIDValues)
                mKiCurrentValue.setText(pidToString(Ki));
            mKiSeekBar.setProgress(Ki);
        }
        if (mKdCurrentValue != null && mKdSeekBar != null) {
            int Kd = rollPitchRadio.isChecked() ? KdRollPitch : KdYaw;
            if (receivedPIDValues)
                mKdCurrentValue.setText(pidToString(Kd));
            mKdSeekBar.setProgress(Kd);
        }
        if (mIntLimitCurrentValue != null && mIntLimitSeekBar != null) {
            int IntLimit = rollPitchRadio.isChecked() ? IntLimitRollPitch : IntLimitYaw;
            if (receivedPIDValues)
                mIntLimitCurrentValue.setText(pidToString(IntLimit));
            mIntLimitSeekBar.setProgress(IntLimit);
        }
    }

    public void updateSendButton() {
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && mSendButton != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                mSendButton.setText(R.string.updateValues);
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
