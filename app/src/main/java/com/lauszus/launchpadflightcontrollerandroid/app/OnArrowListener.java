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

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class OnArrowListener implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    private Handler handler = new Handler();
    private static final int repeatInterval = 300; // Repeat interval is 300 ms
    private SeekBar mSeekbar;
    private boolean positive;

    OnArrowListener(View v, SeekBar mSeekbar, boolean positive) {
        Button mButton = (Button) v;
        if (mButton == null)
            throw new RuntimeException();

        mButton.setOnClickListener(this);
        mButton.setOnLongClickListener(this);
        mButton.setOnTouchListener(this);

        this.mSeekbar = mSeekbar;
        this.positive = positive;
    }

    private int round10(int n) {
        return Math.round((float)n / 10.0f) * 10;
    }

    private void longClick() {
        mSeekbar.setProgress(round10(mSeekbar.getProgress() + (positive ? 10 : -10))); // Increase/decrease with 0.1 and round to nearest multiple of 10
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            longClick();
            handler.postDelayed(this, repeatInterval); // Repeat long click if button is held down
        }
    };

    @Override
    public void onClick(View v) {
        mSeekbar.setProgress(mSeekbar.getProgress() + (positive ? 1 : -1)); // Increase/decrease with 0.01
    }

    @Override
    public boolean onLongClick(View v) {
        longClick();
        handler.postDelayed(runnable, repeatInterval); // Repeat again in 300 ms
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(runnable); // Remove callback if button is released
        }
        return false;
    }
}