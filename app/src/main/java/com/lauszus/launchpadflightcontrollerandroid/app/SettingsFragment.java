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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private Button mSendButton;

    private TextView mAngleKpCurrentValue, mHeadingKpCurrentValue, mAngleMaxIncCurrentValue, mAngleMaxIncSonarCurrentValue, mStickScalingRollPitchCurrentValue, mStickScalingYawCurrentValue;
    private SeekBarArrows mAngleKpSeekBar, mHeadingKpSeekBar, mAngleMaxIncSeekBar, mAngleMaxIncSonarSeekBar, mStickScalingRollPitchSeekBar, mStickScalingYawSeekBar;

    private int AngleKpValue, HeadingKpValue, StickScalingRollPitchValue, StickScalingYawValue;
    private byte AngleMaxIncValue, AngleMaxIncSonarValue;
    private boolean receivedSettings;

    private final Handler mHandler = new Handler();
    private int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, container, false);

        if (v == null)
            throw new RuntimeException();

        mAngleKpCurrentValue = (TextView) v.findViewById(R.id.AngleKpCurrentValue);
        mHeadingKpCurrentValue = (TextView) v.findViewById(R.id.HeadingKpCurrentValue);
        mAngleMaxIncCurrentValue = (TextView) v.findViewById(R.id.AngleMaxIncCurrentValue);
        mAngleMaxIncSonarCurrentValue = (TextView) v.findViewById(R.id.AngleMaxIncSonarCurrentValue);
        mStickScalingRollPitchCurrentValue = (TextView) v.findViewById(R.id.StickScalingRollPitchCurrentValue);
        mStickScalingYawCurrentValue = (TextView) v.findViewById(R.id.StickScalingYawCurrentValue);

        mAngleKpSeekBar = (SeekBarArrows) v.findViewById(R.id.AngleKp); // 0-10 in 0.01 steps
        mHeadingKpSeekBar = (SeekBarArrows) v.findViewById(R.id.HeadingKp); // 0-10 in 0.01 steps
        mAngleMaxIncSeekBar = (SeekBarArrows) v.findViewById(R.id.AngleMaxInc); // 0-90 in 1 steps
        mAngleMaxIncSonarSeekBar = (SeekBarArrows) v.findViewById(R.id.AngleMaxIncSonar); // 0-90 in 1 steps
        mStickScalingRollPitchSeekBar = (SeekBarArrows) v.findViewById(R.id.StickScalingRollPitch); // 0-10 in 0.01 steps
        mStickScalingYawSeekBar = (SeekBarArrows) v.findViewById(R.id.StickScalingYaw); // 0-10 in 0.01 steps

        // Set default values
        AngleKpValue = mAngleKpSeekBar.getProgress();
        HeadingKpValue = mHeadingKpSeekBar.getProgress();
        AngleMaxIncValue = (byte) mAngleMaxIncSeekBar.getProgress();
        AngleMaxIncSonarValue = (byte) mAngleMaxIncSonarSeekBar.getProgress();
        StickScalingRollPitchValue = mStickScalingRollPitchSeekBar.getProgress();
        StickScalingYawValue = mStickScalingYawSeekBar.getProgress();

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
                            activity.mChatService.mBluetoothProtocol.setSettings(mAngleKpSeekBar.getProgress(), mHeadingKpSeekBar.getProgress(), (byte) mAngleMaxIncSeekBar.getProgress(), (byte) mAngleMaxIncSonarSeekBar.getProgress(), mStickScalingRollPitchSeekBar.getProgress(), mStickScalingYawSeekBar.getProgress());
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

    public void updateSettings(int AngleKpValue, int HeadingKpValue, byte AngleMaxIncValue, byte AngleMaxIncSonarValueValue, int StickScalingRollPitchValue, int StickScalingYawValue) {
        this.AngleKpValue = AngleKpValue;
        this.HeadingKpValue = HeadingKpValue;
        this.AngleMaxIncValue = AngleMaxIncValue;
        this.AngleMaxIncSonarValue = AngleMaxIncSonarValueValue;
        this.StickScalingRollPitchValue = StickScalingRollPitchValue;
        this.StickScalingYawValue = StickScalingYawValue;

        receivedSettings = true;
        updateView();
    }

    private void updateView() {
        if (mAngleKpCurrentValue != null && mAngleKpSeekBar != null) {
            if (receivedSettings)
                mAngleKpCurrentValue.setText(mAngleKpSeekBar.progressToString(AngleKpValue));
            mAngleKpSeekBar.setProgress(AngleKpValue);
        }
        if (mHeadingKpCurrentValue != null && mHeadingKpSeekBar != null) {
            if (receivedSettings)
                mHeadingKpCurrentValue.setText(mHeadingKpSeekBar.progressToString(HeadingKpValue));
            mHeadingKpSeekBar.setProgress(HeadingKpValue);
        }
        if (mAngleMaxIncCurrentValue != null && mAngleMaxIncSeekBar != null) {
            if (receivedSettings)
                mAngleMaxIncCurrentValue.setText(mAngleMaxIncSeekBar.progressToString(AngleMaxIncValue));
            mAngleMaxIncSeekBar.setProgress(AngleMaxIncValue);
        }
        if (mAngleMaxIncSonarCurrentValue != null && mAngleMaxIncSonarSeekBar != null) {
            if (receivedSettings)
                mAngleMaxIncSonarCurrentValue.setText(mAngleMaxIncSonarSeekBar.progressToString(AngleMaxIncSonarValue));
            mAngleMaxIncSonarSeekBar.setProgress(AngleMaxIncSonarValue);
        }
        if (mStickScalingRollPitchCurrentValue != null && mStickScalingRollPitchSeekBar != null) {
            if (receivedSettings)
                mStickScalingRollPitchCurrentValue.setText(mStickScalingRollPitchSeekBar.progressToString(StickScalingRollPitchValue));
            mStickScalingRollPitchSeekBar.setProgress(StickScalingRollPitchValue);
        }
        if (mStickScalingYawCurrentValue != null && mStickScalingYawSeekBar != null) {
            if (receivedSettings)
                mStickScalingYawCurrentValue.setText(mStickScalingYawSeekBar.progressToString(StickScalingYawValue));
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
