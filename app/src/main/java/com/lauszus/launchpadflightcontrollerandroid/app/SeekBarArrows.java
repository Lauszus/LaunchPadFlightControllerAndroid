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

import android.support.annotation.StringRes;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class SeekBarArrows implements SeekBar.OnSeekBarChangeListener {
    private SeekBar mSeekBar;
    private TextView mSeekBarValue;
    private float divider;

    SeekBarArrows(View v, @StringRes int resid, float max, float divider) {
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        ((TextView) v.findViewById(R.id.text)).setText(resid);
        mSeekBarValue = (TextView) v.findViewById(R.id.value);
        this.divider = divider;

        mSeekBar.setMax((int)(max * divider));
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(mSeekBar.getMax() / 2); // Call this after the OnSeekBarChangeListener is created

        // Use custom OnArrowListener class to handle button click, button long click and if the button is held down
        new OnArrowListener(v.findViewById(R.id.upArrow), mSeekBar, true);
        new OnArrowListener(v.findViewById(R.id.downArrow), mSeekBar, false);
    }

    public int getProgress() {
        return mSeekBar.getProgress();
    }

    public void setProgress(int value) {
        mSeekBar.setProgress(value);
    }

    public String progressToString(int value) {
        final String format = divider == 10000.0f ? "%.4f" : divider == 1000.0f ? "%.3f" : divider == 100.0f ? "%.2f" : divider == 10.0f ? "%.1f" : "%.0f"; // Set decimal places according to divider
        return String.format(Locale.US, format, (float) value / divider); // SeekBar can only handle integers, so format it to a float with two decimal places
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        mSeekBarValue.setText(progressToString(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
