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
import android.widget.SeekBar;
import android.widget.TextView;

public class PIDFragment extends Fragment {
    private static final String TAG = PIDFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    Button mButton;
    TextView mKpView, mKiView, mKdView, mTargetAngleView, mTurningView;
    SeekBar mKpSeekBar, mKiSeekBar, mKdSeekBar, mTargetAngleSeekBar, mTurningSeekBar;
    CharSequence oldKpValue, oldKiValue, oldKdValue, oldTargetAngleValue, oldTurningValue;

    final Handler mHandler = new Handler();
    int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pid, container, false);

        if (v == null)
            throw new RuntimeException();

        mKpView = (TextView) v.findViewById(R.id.textView1);
        mKiView = (TextView) v.findViewById(R.id.textView2);
        mKdView = (TextView) v.findViewById(R.id.textView3);
        mTargetAngleView = (TextView) v.findViewById(R.id.textView4);
        mTurningView = (TextView) v.findViewById(R.id.textView5);

        mKpSeekBar = (SeekBar) v.findViewById(R.id.KpSeekBar);
        mKpSeekBar.setMax(1000); // 0-10
        final TextView mKpSeekBarValue = (TextView) v.findViewById(R.id.KpValue);
        mKpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKpSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mKpSeekBar.setProgress(mKpSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mKiSeekBar = (SeekBar) v.findViewById(R.id.KiSeekBar);
        mKiSeekBar.setMax(1000); // 0-10
        final TextView mKiSeekBarValue = (TextView) v.findViewById(R.id.KiValue);
        mKiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKiSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mKiSeekBar.setProgress(mKiSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mKdSeekBar = (SeekBar) v.findViewById(R.id.KdSeekBar);
        mKdSeekBar.setMax(1000); // 0-10
        final TextView mKdSeekBarValue = (TextView) v.findViewById(R.id.KdValue);
        mKdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKdSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mKdSeekBar.setProgress(mKdSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mTargetAngleSeekBar = (SeekBar) v.findViewById(R.id.TargetAngleSeekBar);
        mTargetAngleSeekBar.setMax(6000); // -30 to 30
        final TextView mTargetAngleSeekBarValue = (TextView) v.findViewById(R.id.TargetAngleValue);
        mTargetAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mTargetAngleSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f - 30.0f)); // It's not possible to set the minimum value either, so we will add a offset
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mTargetAngleSeekBar.setProgress(mTargetAngleSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mTurningSeekBar = (SeekBar) v.findViewById(R.id.TurningSeekBar);
        mTurningSeekBar.setMax(100); // 0-100
        final TextView mTurningSeekBarValue = (TextView) v.findViewById(R.id.TurningValue);
        mTurningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mTurningSeekBarValue.setText(Integer.toString(progress)); // Set it directly
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mTurningSeekBar.setProgress(mTurningSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        mButton = (Button) v.findViewById(R.id.button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                if (activity == null || activity.mChatService == null) {
                    if (D)
                        Log.e(TAG, "mChatService == null");
                    return;
                }
                if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    if (mKpSeekBarValue.getText() != null && mKiSeekBarValue.getText() != null && mKdSeekBarValue.getText() != null && (!mKpSeekBarValue.getText().equals(oldKpValue) || !mKiSeekBarValue.getText().equals(oldKiValue) || !mKdSeekBarValue.getText().equals(oldKdValue))) {
                        oldKpValue = mKpSeekBarValue.getText();
                        oldKiValue = mKiSeekBarValue.getText();
                        oldKdValue = mKdSeekBarValue.getText();
                        mHandler.post(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.setPIDRollPitch((int) (Float.parseFloat(mKpSeekBarValue.getText().toString()) * 100.0f), (int) (Float.parseFloat(mKiSeekBarValue.getText().toString()) * 100.0f), (int) (Float.parseFloat(mKdSeekBarValue.getText().toString()) * 100.0f), 60);
                            }
                        }); // Wait before sending the message
                        counter += 25;
                        mHandler.post(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.getPIDRollPitch();
                            }
                        }); // Wait before sending the message
                        counter += 25;
                    }
/*
                    if (mTargetAngleSeekBarValue.getText() != null && !mTargetAngleSeekBarValue.getText().equals(oldTargetAngleValue)) {
                        oldTargetAngleValue = mTargetAngleSeekBarValue.getText();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.setTarget((int) (Float.parseFloat(mTargetAngleSeekBarValue.getText().toString()) * 100.0f) ); // The SeekBar can't handle negative numbers, do this to convert it
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.getTarget();
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }

                    if (mTurningSeekBarValue.getText() != null && !mTurningSeekBarValue.getText().equals(oldTurningValue)) {
                        oldTurningValue = mTurningSeekBarValue.getText();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.setTurning(Byte.parseByte(mTurningSeekBarValue.getText().toString()));
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                LaunchPadFlightControllerActivity activity = (LaunchPadFlightControllerActivity) getActivity();
                                if (activity != null)
                                    activity.mChatService.mBluetoothProtocol.getTurning();
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }
*/
                    counter = 0; // Reset counter
                }
            }
        });


        Button mKpUpArrow = (Button) v.findViewById(R.id.KpUpArrow);
        Button mKpDownArrow = (Button) v.findViewById(R.id.KpDownArrow);

        Button mKiUpArrow = (Button) v.findViewById(R.id.KiUpArrow);
        Button mKiDownArrow = (Button) v.findViewById(R.id.KiDownArrow);

        Button mKdUpArrow = (Button) v.findViewById(R.id.KdUpArrow);
        Button mKdDownArrow = (Button) v.findViewById(R.id.KdDownArrow);

        Button mTargetAngleUpArrow = (Button) v.findViewById(R.id.TargetAngleUpArrow);
        Button mTargetAngleDownArrow = (Button) v.findViewById(R.id.TargetAngleDownArrow);

        Button mTurningUpArrow = (Button) v.findViewById(R.id.TurningUpArrow);
        Button mTurningDownArrow = (Button) v.findViewById(R.id.TurningDownArrow);

        mKpUpArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKpSeekBar.setProgress(round10(mKpSeekBar.getProgress() + 10)); // Increase with 0.1 and round to nearest multiple of 10
            }
        });
        mKpDownArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKpSeekBar.setProgress(round10(mKpSeekBar.getProgress() - 10)); // Decrease with 0.1 and round to nearest multiple of 10
            }
        });

        mKiUpArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKiSeekBar.setProgress(round10(mKiSeekBar.getProgress() + 10)); // Increase with 0.1 and round to nearest multiple of 10
            }
        });
        mKiDownArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKiSeekBar.setProgress(round10(mKiSeekBar.getProgress() - 10)); // Decrease with 0.1 and round to nearest multiple of 10
            }
        });

        mKdUpArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKdSeekBar.setProgress(round10(mKdSeekBar.getProgress() + 10)); // Increase with 0.1 and round to nearest multiple of 10
            }
        });
        mKdDownArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKdSeekBar.setProgress(round10(mKdSeekBar.getProgress() - 10)); // Decrease with 0.1 and round to nearest multiple of 10
            }
        });

        mTargetAngleUpArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetAngleSeekBar.setProgress(round10(mTargetAngleSeekBar.getProgress() + 10)); // Increase with 0.1 and round to nearest multiple of 10
            }
        });
        mTargetAngleDownArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetAngleSeekBar.setProgress(round10(mTargetAngleSeekBar.getProgress() - 10)); // Decrease with 0.1 and round to nearest multiple of 10
            }
        });

        mTurningUpArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTurningSeekBar.setProgress(mTurningSeekBar.getProgress() + 1); // Increase with 1
            }
        });
        mTurningDownArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTurningSeekBar.setProgress(mTurningSeekBar.getProgress() - 1); // Decrease with 1
            }
        });

        updateButton();
        return v;
    }

    private int round10(int n) {
        return Math.round((float)n / 10.0f) * 10;
    }

    public void updatePID(String Kp, String Ki, String Kd) {
        if (mKpView != null && mKpSeekBar != null && !Kp.isEmpty()) {
            mKpView.setText(Kp);
            mKpSeekBar.setProgress((int) (Float.parseFloat(Kp) * 100.0f));
        }
        if (mKiView != null && mKiSeekBar != null && !Ki.isEmpty()) {
            mKiView.setText(Ki);
            mKiSeekBar.setProgress((int) (Float.parseFloat(Ki) * 100.0f));
        }
        if (mKdView != null && mKdSeekBar != null && !Kd.isEmpty()) {
            mKdView.setText(Kd);
            mKdSeekBar.setProgress((int) (Float.parseFloat(Kd) * 100.0f));
        }
    }

    public void updateAngle(String targetAngleValue) {
        if (mTargetAngleView != null && mTargetAngleSeekBar != null && !targetAngleValue.isEmpty()) {
            mTargetAngleView.setText(targetAngleValue);
            mTargetAngleSeekBar.setProgress((int) ((Float.parseFloat(targetAngleValue) + 30) * 100.0f));
        }
    }

    public void updateTurning(int turningScale) {
        if (mTurningView != null && mTurningSeekBar != null) {
            mTurningView.setText(Integer.toString(turningScale));
            mTurningSeekBar.setProgress(turningScale);
        }
    }

    public void updateButton() {
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && mButton != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                mButton.setText(R.string.updateValues);
            else
                mButton.setText(R.string.button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the user resumes the view, then set the values again
        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null)
            updateButton();
    }
}
