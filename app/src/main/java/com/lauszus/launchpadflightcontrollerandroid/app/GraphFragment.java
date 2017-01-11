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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphFragment extends Fragment {
    private static final String TAG = GraphFragment.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    private GraphView graphView;
    private LineGraphSeries rollSeries, pitchSeries, yawSeries;
    private final static int bufferSize = 500;
    private static double counter = bufferSize;

    private CheckBox mCheckBoxRoll, mCheckBoxPitch, mCheckBoxYaw;
    public ToggleButton mToggleButton;

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

        DataPoint[] data0 = new DataPoint[bufferSize + 1];
        DataPoint[] data1 = new DataPoint[bufferSize + 1];
        DataPoint[] data2 = new DataPoint[bufferSize + 1];

        for (int i = 0; i < bufferSize + 1; i++) { // Restore last data
            data0[i] = new DataPoint(counter - bufferSize + i, buffer[0][i]);
            data1[i] = new DataPoint(counter - bufferSize + i, buffer[1][i]);
            data2[i] = new DataPoint(counter - bufferSize + i, buffer[2][i]);
        }

        rollSeries = new LineGraphSeries<>(data0);
        pitchSeries = new LineGraphSeries<>(data1);
        yawSeries = new LineGraphSeries<>(data2);

        rollSeries.setTitle("Roll");
        pitchSeries.setTitle("Pitch");
        yawSeries.setTitle("Yaw");

        rollSeries.setColor(Color.RED);
        pitchSeries.setColor(Color.GREEN);
        yawSeries.setColor(Color.BLUE);

        graphView = (GraphView) v.findViewById(R.id.linegraph);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(bufferSize);

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(-180);
        graphView.getViewport().setMaxY(180);

        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(false);

        graphView.getViewport().setScalable(false);
        graphView.getViewport().setScalableY(false);

        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        graphView.getGridLabelRenderer().setGridColor(Color.argb(100, 0x88, 0x88, 0x88)); // Transparent grey color
        graphView.getGridLabelRenderer().setNumHorizontalLabels(11);
        graphView.getGridLabelRenderer().setNumVerticalLabels(9);
        graphView.getGridLabelRenderer().setTextSize(20);

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
        mCheckBoxRoll.callOnClick();

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
        mCheckBoxPitch.callOnClick();

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
        mCheckBoxYaw.callOnClick();

        graphView.getViewport().scrollToEnd(); // This has to be called after the series are added

        mToggleButton = (ToggleButton) v.findViewById(R.id.toggleButton);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked())
                    mToggleButton.setText(R.string.stop);
                else
                    mToggleButton.setText(R.string.start);

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

    public void updateAngles(float rollValue, float pitchValue, float yawValue) {
        if (mToggleButton == null || !mToggleButton.isChecked())
            return;

        for (int i = 0; i < 3; i++)
            System.arraycopy(buffer[i], 1, buffer[i], 0, bufferSize);

        buffer[0][bufferSize] = rollValue;
        buffer[1][bufferSize] = pitchValue;
        buffer[2][bufferSize] = yawValue - 180.0; // Convert from [0,360] to [-180,180]

        boolean scroll = mCheckBoxRoll.isChecked() || mCheckBoxPitch.isChecked() || mCheckBoxYaw.isChecked();

        counter++;
        rollSeries.appendData(new DataPoint(counter, buffer[0][bufferSize]), scroll, bufferSize + 1, true);
        pitchSeries.appendData(new DataPoint(counter, buffer[1][bufferSize]), scroll, bufferSize + 1, true);
        yawSeries.appendData(new DataPoint(counter, buffer[2][bufferSize]), scroll, bufferSize + 1, false); // The final call will rerender the graph
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mToggleButton.isChecked())
            mToggleButton.setText(R.string.stop);
        else
            mToggleButton.setText(R.string.start);

        LaunchPadFlightControllerActivity activity = ((LaunchPadFlightControllerActivity) getActivity());
        if (activity != null && activity.mChatService != null && activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && activity.checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
            if (mToggleButton.isChecked())
                activity.mChatService.mBluetoothProtocol.sendAngles((byte) 1); // Request data
            else
                activity.mChatService.mBluetoothProtocol.sendAngles((byte) 0); // Stop sending data
        }
    }
}
