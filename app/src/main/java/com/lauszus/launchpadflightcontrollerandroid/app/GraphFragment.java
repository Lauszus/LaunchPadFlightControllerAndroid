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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

// TODO: Remove static
public class GraphFragment extends Fragment {
    private static final String TAG = GraphFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private static LineGraphView graphView;
    private static GraphViewSeries rollSeries, pitchSeries, yawSeries;
    private final static int bufferSize = 1000;
    private static double counter = bufferSize;

    private static CheckBox mCheckBoxRoll, mCheckBoxPitch, mCheckBoxYaw;
    public static ToggleButton mToggleButton;

    private static double[][] buffer = new double[3][bufferSize + 1]; // Used to store last readings

    public GraphFragment() {
        for (int i = 0; i < 3; i++)
            for (int i2 = 0; i2 < buffer[i].length; i2++)
                buffer[i][i2] = 0.0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.graph, container, false);

        if (v == null)
            throw new RuntimeException();

        GraphViewData[] data0 = new GraphViewData[bufferSize + 1];
        GraphViewData[] data1 = new GraphViewData[bufferSize + 1];
        GraphViewData[] data2 = new GraphViewData[bufferSize + 1];

        for (int i = 0; i < bufferSize + 1; i++) { // Restore last data
            data0[i] = new GraphViewData(counter - bufferSize + i, buffer[0][i]);
            data1[i] = new GraphViewData(counter - bufferSize + i, buffer[1][i]);
            data2[i] = new GraphViewData(counter - bufferSize + i, buffer[2][i]);
        }

        rollSeries = new GraphViewSeries("Roll", new GraphViewSeriesStyle(Color.RED, 2), data0);
        pitchSeries = new GraphViewSeries("Pitch", new GraphViewSeriesStyle(Color.GREEN, 2), data1);
        yawSeries = new GraphViewSeries("Yaw", new GraphViewSeriesStyle(Color.BLUE, 2), data2);

        graphView = new LineGraphView(getActivity(), "");
        if (mCheckBoxRoll != null) {
            if (mCheckBoxRoll.isChecked())
                graphView.addSeries(rollSeries);
        } else
            graphView.addSeries(rollSeries);
        if (mCheckBoxPitch != null) {
            if (mCheckBoxPitch.isChecked())
                graphView.addSeries(pitchSeries);
        } else
            graphView.addSeries(pitchSeries);
        if (mCheckBoxYaw != null) {
            if (mCheckBoxYaw.isChecked())
                graphView.addSeries(yawSeries);
        } else
            graphView.addSeries(yawSeries);

        graphView.setManualYAxisBounds(180, -180);
        graphView.setViewPort(0, bufferSize);
        graphView.setScrollable(true);
        graphView.setDisableTouch(true);

        graphView.setShowLegend(true);
        graphView.setLegendAlign(LegendAlign.BOTTOM);
        graphView.scrollToEnd();

        GraphViewStyle mGraphViewStyle = new GraphViewStyle();
        mGraphViewStyle.setNumHorizontalLabels(11);
        graphView.setVerticalLabels(new String[]{ "180", "135", "90", "45", "0", "-45", "-90", "-135", "-180" });
        mGraphViewStyle.setTextSize(15);
        mGraphViewStyle.setLegendWidth(140);
        mGraphViewStyle.setLegendMarginBottom(30);

        graphView.setGraphViewStyle(mGraphViewStyle);

        ((LinearLayout) v.findViewById(R.id.linegraph)).addView(graphView);

        mCheckBoxRoll = (CheckBox) v.findViewById(R.id.checkBoxRoll);
        mCheckBoxRoll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    graphView.addSeries(rollSeries);
                else
                    graphView.removeSeries(rollSeries);
            }
        });
        mCheckBoxPitch = (CheckBox) v.findViewById(R.id.checkBoxPitch);
        mCheckBoxPitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    graphView.addSeries(pitchSeries);
                else
                    graphView.removeSeries(pitchSeries);
            }
        });
        mCheckBoxYaw = (CheckBox) v.findViewById(R.id.checkBoxYaw);
        mCheckBoxYaw.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    graphView.addSeries(yawSeries);
                else
                    graphView.removeSeries(yawSeries);
            }
        });

        mToggleButton = (ToggleButton) v.findViewById(R.id.toggleButton);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked())
                    mToggleButton.setText("Stop");
                else
                    mToggleButton.setText("Start");

                LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
                if (activity != null && activity.mChatService != null) {
                    if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && activity.checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                        if (((ToggleButton) v).isChecked())
                            activity.mChatService.mBluetoothProtocol.sendAngles((byte) 1); // Request data
                        else
                            activity.mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
                    }
                }
            }
        });

        return v;
    }

    public void updateAngles(String rollValue, String pitchValue, String yawValue) {
        if (mToggleButton == null || !(mToggleButton.isChecked()))
            return;

        for (int i = 0; i < 3; i++)
            System.arraycopy(buffer[i], 1, buffer[i], 0, bufferSize);

        try { // In some rare occasions the values can be corrupted
            buffer[0][bufferSize] = Double.parseDouble(rollValue);
            buffer[1][bufferSize] = Double.parseDouble(pitchValue);
            buffer[2][bufferSize] = Double.parseDouble(yawValue) - 180.0;
        } catch (NumberFormatException e) {
            if (D)
                Log.e(TAG, "Error in input", e);
            return;
        }

        boolean scroll = mCheckBoxRoll.isChecked() || mCheckBoxPitch.isChecked() || mCheckBoxYaw.isChecked();

        counter++;
        rollSeries.appendData(new GraphViewData(counter, buffer[0][bufferSize]), scroll, bufferSize + 1);
        pitchSeries.appendData(new GraphViewData(counter, buffer[1][bufferSize]), scroll, bufferSize + 1);
        yawSeries.appendData(new GraphViewData(counter, buffer[2][bufferSize]), scroll, bufferSize + 1);

        if (!scroll)
            graphView.redrawAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mToggleButton.isChecked())
            mToggleButton.setText("Stop");
        else
            mToggleButton.setText("Start");

        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && activity.checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
            if (mToggleButton.isChecked())
                activity.mChatService.mBluetoothProtocol.sendAngles((byte) 1); // Request data
            else
                activity.mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
        }
    }
}
