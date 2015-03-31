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
import android.widget.SeekBar;
import android.widget.TextView;

public class PIDFragment extends Fragment {
    private static final String TAG = PIDFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private Button mSendButton;
    private RadioButton rollPitchRadio;

    private TextView mKpView, mKiView, mKdView, mIntLimitView;
    private SeekBar mKpSeekBar, mKiSeekBar, mKdSeekBar, mIntLimitSeekBar;

    private int KpRollPitch, KiRollPitch, KdRollPitch, IntLimitRollPitch;
    private int KpYaw, KiYaw, KdYaw, IntLimitYaw;

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

        mKpView = (TextView) v.findViewById(R.id.KpView);
        mKiView = (TextView) v.findViewById(R.id.KiView);
        mKdView = (TextView) v.findViewById(R.id.KdView);
        mIntLimitView = (TextView) v.findViewById(R.id.IntLimitView);

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

        mKpSeekBar = (SeekBar) v.findViewById(R.id.KpSeekBar);
        mKpSeekBar.setMax(1000); // 0-10
        mKpSeekBar.setOnSeekBarChangeListener(new SeekBarListener(v.findViewById(R.id.KpValue)));
        mKpSeekBar.setProgress(mKpSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mKiSeekBar = (SeekBar) v.findViewById(R.id.KiSeekBar);
        mKiSeekBar.setMax(1000); // 0-10
        mKiSeekBar.setOnSeekBarChangeListener(new SeekBarListener(v.findViewById(R.id.KiValue)));
        mKiSeekBar.setProgress(mKiSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mKdSeekBar = (SeekBar) v.findViewById(R.id.KdSeekBar);
        mKdSeekBar.setMax(1000); // 0-10
        mKdSeekBar.setOnSeekBarChangeListener(new SeekBarListener(v.findViewById(R.id.KdValue)));
        mKdSeekBar.setProgress(mKdSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mIntLimitSeekBar = (SeekBar) v.findViewById(R.id.IntLimitSeekBar);
        mIntLimitSeekBar.setMax(1000); // 0-10
        mIntLimitSeekBar.setOnSeekBarChangeListener(new SeekBarListener(v.findViewById(R.id.IntLimitValue)));
        mIntLimitSeekBar.setProgress(mIntLimitSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

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

        // Use custom OnArrowListener class to handle button click, button long click and if the button is held down
        new OnArrowListener(v.findViewById(R.id.KpUpArrow), mKpSeekBar, true);
        new OnArrowListener(v.findViewById(R.id.KpDownArrow), mKpSeekBar, false);

        new OnArrowListener(v.findViewById(R.id.KiUpArrow), mKiSeekBar, true);
        new OnArrowListener(v.findViewById(R.id.KiDownArrow), mKiSeekBar, false);

        new OnArrowListener(v.findViewById(R.id.KdUpArrow), mKdSeekBar, true);
        new OnArrowListener(v.findViewById(R.id.KdDownArrow), mKdSeekBar, false);

        new OnArrowListener(v.findViewById(R.id.IntLimitUpArrow), mIntLimitSeekBar, true);
        new OnArrowListener(v.findViewById(R.id.IntLimitDownArrow), mIntLimitSeekBar, false);

        updateSendButton();

        return v;
    }

    public void updatePIDRollPitch(int Kp, int Ki, int Kd, int intLimit) {
        KpRollPitch = Kp;
        KiRollPitch = Ki;
        KdRollPitch = Kd;
        IntLimitRollPitch = intLimit;

        updateView();
    }

    public void updatePIDYaw(int Kp, int Ki, int Kd, int intLimit) {
        KpYaw = Kp;
        KiYaw = Ki;
        KdYaw = Kd;
        IntLimitYaw = intLimit;

        updateView();
    }

    private String pidToString(int value) {
        return String.format("%.2f", (float) value / 100.0f); // SeekBar can only handle integers, so format it to a float with two decimal places
    }

    private void updateView() {
        if (mKpView != null && mKpSeekBar != null) {
            int Kp = rollPitchRadio.isChecked() ? KpRollPitch : KpYaw;
            mKpView.setText(pidToString(Kp));
            mKpSeekBar.setProgress(Kp);
        }
        if (mKiView != null && mKiSeekBar != null) {
            int Ki = rollPitchRadio.isChecked() ? KiRollPitch : KiYaw;
            mKiView.setText(pidToString(Ki));
            mKiSeekBar.setProgress(Ki);
        }
        if (mKdView != null && mKdSeekBar != null) {
            int Kd = rollPitchRadio.isChecked() ? KdRollPitch : KdYaw;
            mKdView.setText(pidToString(Kd));
            mKdSeekBar.setProgress(Kd);
        }
        if (mIntLimitView != null && mIntLimitSeekBar != null) {
            int IntLimit = rollPitchRadio.isChecked() ? IntLimitRollPitch : IntLimitYaw;
            mIntLimitView.setText(pidToString(IntLimit));
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
