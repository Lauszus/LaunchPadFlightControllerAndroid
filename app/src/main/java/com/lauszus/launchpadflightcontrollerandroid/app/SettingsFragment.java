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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private Button mSendButton;

    private TextView mAngleKpCurrentValue, mAngleMaxIncCurrentValue, mStickScalingRollPitchCurrentValue, mStickScalingYawCurrentValue;
    private SeekBar mAngleKpSeekBar, mAngleMaxIncSeekBar, mStickScalingRollPitchSeekBar, mStickScalingYawSeekBar;

    private int AngleKpValue, StickScalingRollPitchValue, StickScalingYawValue;
    private byte AngleMaxIncValue;
    private boolean receivedSettings;

    private final Handler mHandler = new Handler();
    private int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, container, false);

        if (v == null)
            throw new RuntimeException();

        mAngleKpCurrentValue = (TextView) v.findViewById(R.id.AngleKpCurrentValue);
        mAngleMaxIncCurrentValue = (TextView) v.findViewById(R.id.AngleMaxIncCurrentValue);
        mStickScalingRollPitchCurrentValue = (TextView) v.findViewById(R.id.StickScalingRollPitchCurrentValue);
        mStickScalingYawCurrentValue = (TextView) v.findViewById(R.id.StickScalingYawCurrentValue);

        LinearLayout mAngleKp = (LinearLayout) v.findViewById(R.id.AngleKp);
        ((TextView) mAngleKp.findViewById(R.id.text)).setText(getResources().getText(R.string.AngleKp));
        LinearLayout mAngleMaxInc = (LinearLayout) v.findViewById(R.id.AngleMaxInc);
        ((TextView) mAngleMaxInc.findViewById(R.id.text)).setText(getResources().getText(R.string.AngleMaxInc));
        LinearLayout mStickScalingRollPitch = (LinearLayout) v.findViewById(R.id.StickScalingRollPitch);
        ((TextView) mStickScalingRollPitch.findViewById(R.id.text)).setText(getResources().getText(R.string.StickScalingRollPitch));
        LinearLayout mStickScalingYaw = (LinearLayout) v.findViewById(R.id.StickScalingYaw);
        ((TextView) mStickScalingYaw.findViewById(R.id.text)).setText(getResources().getText(R.string.StickScalingYaw));

        class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
            TextView SeekBarValue;
            boolean divide;

            SeekBarListener(View SeekBarValue, boolean divide) {
                this.SeekBarValue = (TextView) SeekBarValue;
                this.divide = divide;
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if (divide)
                    SeekBarValue.setText(settingsToString(progress)); // SeekBar can only handle integers, so format it to a float with two decimal places
                else
                    SeekBarValue.setText(Integer.toString(progress)); // The maximum angle inclination value should not be divided
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }

        mAngleKpSeekBar = (SeekBar) mAngleKp.findViewById(R.id.seekBar);
        mAngleKpSeekBar.setMax(1000); // 0-10
        mAngleKpSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mAngleKp.findViewById(R.id.value), true));
        mAngleKpSeekBar.setProgress(mAngleKpSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        AngleKpValue = mAngleKpSeekBar.getProgress();

        mAngleMaxIncSeekBar = (SeekBar) mAngleMaxInc.findViewById(R.id.seekBar);
        mAngleMaxIncSeekBar.setMax(90); // 0-90
        mAngleMaxIncSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mAngleMaxInc.findViewById(R.id.value), false));
        mAngleMaxIncSeekBar.setProgress(mAngleMaxIncSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        AngleMaxIncValue = (byte) mAngleMaxIncSeekBar.getProgress();

        mStickScalingRollPitchSeekBar = (SeekBar) mStickScalingRollPitch.findViewById(R.id.seekBar);
        mStickScalingRollPitchSeekBar.setMax(1000); // 0-10
        mStickScalingRollPitchSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mStickScalingRollPitch.findViewById(R.id.value), true));
        mStickScalingRollPitchSeekBar.setProgress(mStickScalingRollPitchSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        StickScalingRollPitchValue = mStickScalingRollPitchSeekBar.getProgress();

        mStickScalingYawSeekBar = (SeekBar) mStickScalingYaw.findViewById(R.id.seekBar);
        mStickScalingYawSeekBar.setMax(1000); // 0-10
        mStickScalingYawSeekBar.setOnSeekBarChangeListener(new SeekBarListener(mStickScalingYaw.findViewById(R.id.value), true));
        mStickScalingYawSeekBar.setProgress(mStickScalingYawSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created
        StickScalingYawValue = mStickScalingYawSeekBar.getProgress();

        // Use custom OnArrowListener class to handle button click, button long click and if the button is held down
        new OnArrowListener(mAngleKp.findViewById(R.id.upArrow), mAngleKpSeekBar, true);
        new OnArrowListener(mAngleKp.findViewById(R.id.downArrow), mAngleKpSeekBar, false);

        new OnArrowListener(mAngleMaxInc.findViewById(R.id.upArrow), mAngleMaxIncSeekBar, true);
        new OnArrowListener(mAngleMaxInc.findViewById(R.id.downArrow), mAngleMaxIncSeekBar, false);

        new OnArrowListener(mStickScalingRollPitch.findViewById(R.id.upArrow), mStickScalingRollPitchSeekBar, true);
        new OnArrowListener(mStickScalingRollPitch.findViewById(R.id.downArrow), mStickScalingRollPitchSeekBar, false);

        new OnArrowListener(mStickScalingYaw.findViewById(R.id.upArrow), mStickScalingYawSeekBar, true);
        new OnArrowListener(mStickScalingYaw.findViewById(R.id.downArrow), mStickScalingYawSeekBar, false);

        mSendButton = (Button) v.findViewById(R.id.button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
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
                            activity.mChatService.mBluetoothProtocol.setSettings(mAngleKpSeekBar.getProgress(), (byte) mAngleMaxIncSeekBar.getProgress(), mStickScalingRollPitchSeekBar.getProgress(), mStickScalingYawSeekBar.getProgress());
                        }
                    }); // Wait before sending the message
                    counter += 100;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            activity.mChatService.mBluetoothProtocol.getSettings();
                        }
                    }, counter); // Wait before sending the message
                    counter = 0; // Reset counter
                }
            }
        });

        receivedSettings = false;
        updateSendButton();

        return v;
    }

    public void updateSettings(int AngleKpValue, byte AngleMaxIncValue, int StickScalingRollPitchValue, int StickScalingYawValue) {
        this.AngleKpValue = AngleKpValue;
        this.AngleMaxIncValue = AngleMaxIncValue;
        this.StickScalingRollPitchValue = StickScalingRollPitchValue;
        this.StickScalingYawValue = StickScalingYawValue;

        receivedSettings = true;
        updateView();
    }

    private String settingsToString(int value) {
        return String.format(Locale.US, "%.2f", (float) value / 100.0f); // SeekBar can only handle integers, so format it to a float with two decimal places
    }

    private void updateView() {
        if (mAngleKpCurrentValue != null && mAngleKpSeekBar != null) {
            if (receivedSettings)
                mAngleKpCurrentValue.setText(settingsToString(AngleKpValue));
            mAngleKpSeekBar.setProgress(AngleKpValue);
        }
        if (mAngleMaxIncCurrentValue != null && mAngleMaxIncSeekBar != null) {
            if (receivedSettings)
                mAngleMaxIncCurrentValue.setText(Integer.toString(AngleMaxIncValue));
            mAngleMaxIncSeekBar.setProgress(AngleMaxIncValue);
        }
        if (mStickScalingRollPitchCurrentValue != null && mStickScalingRollPitchSeekBar != null) {
            if (receivedSettings)
                mStickScalingRollPitchCurrentValue.setText(settingsToString(StickScalingRollPitchValue));
            mStickScalingRollPitchSeekBar.setProgress(StickScalingRollPitchValue);
        }
        if (mStickScalingYawCurrentValue != null && mStickScalingYawSeekBar != null) {
            if (receivedSettings)
                mStickScalingYawCurrentValue.setText(settingsToString(StickScalingYawValue));
            mStickScalingYawSeekBar.setProgress(StickScalingYawValue);
        }
    }

    public void updateSendButton() {
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && mSendButton != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                mSendButton.setText(R.string.updateSettings);
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
