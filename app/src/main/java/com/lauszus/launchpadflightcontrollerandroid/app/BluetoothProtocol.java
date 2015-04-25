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
import android.os.Message;
import android.util.Log;

public class BluetoothProtocol {
    // Debugging
    private static final String TAG = BluetoothProtocol.class.getSimpleName();
    private static final boolean D = LaunchPadFlightControllerActivity.D;

    static final byte SET_PID_ROLL_PITCH = 0;
    static final byte GET_PID_ROLL_PITCH = 1;
    static final byte SET_PID_YAW = 2;
    static final byte GET_PID_YAW = 3;
    static final byte SET_SETTINGS = 4;
    static final byte GET_SETTINGS = 5;
    static final byte SET_KALMAN = 6;
    static final byte GET_KALMAN = 7;
    static final byte SEND_ANGLES = 8;
    static final byte SEND_INFO = 9;
    static final byte CAL_ACC = 10;
    static final byte RESTORE_DEFAULTS = 11;

    static final String commandHeader = "$S>"; // Standard command header
    static final String responseHeader = "$S<"; // Standard response header
    static final String responseEnd = "\r\n";


    BluetoothChatService mChatService;
    private final Handler mHandler;

    public BluetoothProtocol(BluetoothChatService mChatService, Handler handler) {
        this.mChatService = mChatService;
        this.mHandler = handler;
    }

    private void sendCommand(byte output[]) {
        mChatService.write(commandHeader);
        mChatService.write(output);
        mChatService.write(getChecksum(output));
    }

    /**
     * Set PID values for roll and pitch. All floats/doubles are multiplied by 100.0 before sending.
     *
     * @param Kp Kp value.
     * @param Ki Ki value.
     * @param Kd Kd value.
     */
    public void setPIDRollPitch(int Kp, int Ki, int Kd, int IntLimit) {
        if (D)
            Log.i(TAG, "setPIDRollPitch: " + Kp + " " + Ki + " " + Kd + " " + IntLimit);

        byte output[] = {
                SET_PID_ROLL_PITCH, // Cmd
                8, // Length
                (byte) (Kp & 0xFF),
                (byte) (Kp >> 8),
                (byte) (Ki & 0xFF),
                (byte) (Ki >> 8),
                (byte) (Kd & 0xFF),
                (byte) (Kd >> 8),
                (byte) (IntLimit & 0xFF),
                (byte) (IntLimit >> 8),
        };
        sendCommand(output); // Set PID values
    }

    /**
     * Use this to request PID values for roll and pitch.
     */
    public void getPIDRollPitch() {
        if (D)
            Log.i(TAG, "getPIDRollPitch");

        byte output[] = {
                GET_PID_ROLL_PITCH, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    /**
     * Set PID values for yaw. All floats/doubles are multiplied by 100.0 before sending.
     *
     * @param Kp Kp value.
     * @param Ki Ki value.
     * @param Kd Kd value.
     */
    public void setPIDYaw(int Kp, int Ki, int Kd, int integrationLimit) {
        if (D)
            Log.i(TAG, "setPIDYaw: " + Kp + " " + Ki + " " + Kd + " " + integrationLimit);

        byte output[] = {
                SET_PID_YAW, // Cmd
                8, // Length
                (byte) (Kp & 0xFF),
                (byte) (Kp >> 8),
                (byte) (Ki & 0xFF),
                (byte) (Ki >> 8),
                (byte) (Kd & 0xFF),
                (byte) (Kd >> 8),
                (byte) (integrationLimit & 0xFF),
                (byte) (integrationLimit >> 8),
        };
        sendCommand(output); // Set PID values
    }

    /**
     * Use this to request PID values for yaw.
     */
    public void getPIDYaw() {
        if (D)
            Log.i(TAG, "getPIDYaw");

        byte output[] = {
                GET_PID_YAW, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    public void setSettings(int AngleKp, int HeadingKp, byte AngleMaxInc, int StickScalingRollPitch, int StickScalingYaw) {
        if (D)
            Log.i(TAG, "setSettings: " + AngleKp + " " + HeadingKp + " " + AngleMaxInc + " " + StickScalingRollPitch + " " + StickScalingYaw);

        byte output[] = {
                SET_SETTINGS, // Cmd
                9, // Length
                (byte) (AngleKp & 0xFF),
                (byte) (AngleKp >> 8),
                (byte) (HeadingKp & 0xFF),
                (byte) (HeadingKp >> 8),
                AngleMaxInc,
                (byte) (StickScalingRollPitch & 0xFF),
                (byte) (StickScalingRollPitch >> 8),
                (byte) (StickScalingYaw & 0xFF),
                (byte) (StickScalingYaw >> 8),
        };
        sendCommand(output); // Send output
    }

    public void getSettings() {
        if (D)
            Log.i(TAG, "getSettings");

        byte output[] = {
                GET_SETTINGS, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    /**
     * Set the Kalman coeficients. All floats/doubles are multiplied by 10000.0 before sending.
     * See: http://blog.tkjelectronics.dk/2012/09/a-practical-approach-to-kalman-filter-and-how-to-implement-it/.
     *
     * @param Qangle   Qangle
     * @param Qbias    Qbias
     * @param Rmeasure Rmeasure
     */
    public void setKalman(int Qangle, int Qbias, int Rmeasure) {
        if (D)
            Log.i(TAG, "setKalman: " + Qangle + " " + Qbias + " " + Rmeasure);

        byte output[] = {
                SET_KALMAN, // Cmd
                6, // Length
                (byte) (Qangle & 0xFF),
                (byte) (Qangle >> 8),
                (byte) (Qbias & 0xFF),
                (byte) (Qbias >> 8),
                (byte) (Rmeasure & 0xFF),
                (byte) (Rmeasure >> 8),
        };
        sendCommand(output); // Set PID values
    }

    public void getKalman() {
        if (D)
            Log.i(TAG, "getKalman");

        byte output[] = {
                GET_KALMAN, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    public void sendAngles(byte enable) {
        if (D)
            Log.i(TAG, "sendAngles: " + enable);

        byte output[] = {
                SEND_ANGLES, // Cmd
                1, // Length
                enable,
        };
        sendCommand(output); // Send output
    }

    public void sendInfo(byte enable) {
        if (D)
            Log.i(TAG, "sendInfo: " + enable);

        byte output[] = {
                SEND_INFO, // Cmd
                1, // Length
                enable,
        };
        sendCommand(output); // Send output
    }

    public void calibrateAccelerometer() {
        if (D)
            Log.i(TAG, "calibrateAccelerometer");

        byte output[] = {
                CAL_ACC, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    public void restoreDefaults() {
        if (D)
            Log.i(TAG, "restoreDefaults");

        byte output[] = {
                RESTORE_DEFAULTS, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    private byte[] buffer = new byte[1024];

    public void parseData(byte msg[], int offset, int length) {
        System.arraycopy(msg, offset, buffer, 0, length);

        String readMessage = new String(buffer, 0, length);

        if (D)
            Log.d(TAG, "Received string: " + readMessage);

        int[] data = new int[length];
        for (int i = 0; i < length; i++) {
            data[i] = buffer[i] & 0xFF; // Cast to unsigned value
            if (D)
                Log.d(TAG, "Data[" + i + "]: " + data[i]);
        }

        if (length < responseHeader.length() + 2) { // We should have at least received the header, cmd and length
            if (D)
                Log.e(TAG, "String is too short!");
            return;
        }

        // TODO: Remove whitespace in front
        if (new String(buffer).startsWith(responseHeader)) {
            int cmd = data[responseHeader.length()];
            int msgLength = data[responseHeader.length() + 1];
            if (msgLength > (length - responseHeader.length() - 3)) { // Check if there is enough data - there needs to be the header, cmd, length, data and checksum
                if (D)
                    Log.e(TAG, "Not enough data!");
                return;
            }

            int input[] = new int[msgLength];
            int i;
            for (i = 0; i < msgLength; i++)
                input[i] = data[i + responseHeader.length() + 2];
            int checksum = data[i + responseHeader.length() + 2];

            int msgChecksum = cmd ^ msgLength ^ getChecksum(input);

            if (checksum == msgChecksum) {
                Message message = mHandler.obtainMessage(LaunchPadFlightControllerActivity.MESSAGE_READ); // Send message back to the UI Activity
                Bundle bundle = new Bundle();

                switch (cmd) {
                    case GET_PID_ROLL_PITCH:
                        int KpRollPitch = input[0] | (input[1] << 8);
                        int KiRollPitch = input[2] | (input[3] << 8);
                        int KdRollPitch = input[4] | (input[5] << 8);
                        int IntLimitRollPitch = input[6] | (input[7] << 8);

                        bundle.putInt(LaunchPadFlightControllerActivity.KP_ROLL_PITCH_VALUE, KpRollPitch);
                        bundle.putInt(LaunchPadFlightControllerActivity.KI_ROLL_PITCH_VALUE, KiRollPitch);
                        bundle.putInt(LaunchPadFlightControllerActivity.KD_ROLL_PITCH_VALUE, KdRollPitch);
                        bundle.putInt(LaunchPadFlightControllerActivity.INT_LIMIT_ROLL_PITCH_VALUE, IntLimitRollPitch);

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Received PID roll & pitch: " + KpRollPitch + " " + KiRollPitch + " " + KdRollPitch + " " + IntLimitRollPitch);
                        break;

                    case GET_PID_YAW:
                        int KpYaw = input[0] | (input[1] << 8);
                        int KiYAw = input[2] | (input[3] << 8);
                        int KdYAw = input[4] | (input[5] << 8);
                        int IntLimitYaw = input[6] | (input[7] << 8);

                        bundle.putInt(LaunchPadFlightControllerActivity.KP_YAW_VALUE, KpYaw);
                        bundle.putInt(LaunchPadFlightControllerActivity.KI_YAW_VALUE, KiYAw);
                        bundle.putInt(LaunchPadFlightControllerActivity.KD_YAW_VALUE, KdYAw);
                        bundle.putInt(LaunchPadFlightControllerActivity.INT_LIMIT_YAW_VALUE, IntLimitYaw);

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Received PID yaw: " + KpYaw + " " + KiYAw + " " + KdYAw + " " + IntLimitYaw);
                        break;

                    case GET_SETTINGS:
                        int AngleKp = input[0] | (input[1] << 8);
                        int HeadingKp = input[2] | (input[3] << 8);
                        byte AngleMaxInc = (byte) input[4];
                        int StickScalingRollPitch = input[5] | (input[6] << 8);
                        int StickScalingYaw = input[7] | (input[8] << 8);

                        bundle.putInt(LaunchPadFlightControllerActivity.ANGLE_KP_VALUE, AngleKp);
                        bundle.putInt(LaunchPadFlightControllerActivity.HEADING_KP_VALUE, HeadingKp);
                        bundle.putByte(LaunchPadFlightControllerActivity.ANGLE_MAX_INC_VALUE, AngleMaxInc);
                        bundle.putInt(LaunchPadFlightControllerActivity.STICK_SCALING_ROLL_PITCH_VALUE, StickScalingRollPitch);
                        bundle.putInt(LaunchPadFlightControllerActivity.STICK_SCALING_YAW_VALUE, StickScalingYaw);

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Received settings: " + AngleKp + " " + HeadingKp + " " + AngleMaxInc + " " + StickScalingRollPitch + " " + StickScalingYaw);
                        break;

                    case GET_KALMAN:
                        int Qangle = input[0] | (input[1] << 8);
                        int Qbias = input[2] | (input[3] << 8);
                        int Rmeasure = input[4] | (input[5] << 8);

                        // TODO: Just store this as an int
                        bundle.putString(LaunchPadFlightControllerActivity.QANGLE_VALUE, String.format("%.4f", (float) Qangle / 10000.0f));
                        bundle.putString(LaunchPadFlightControllerActivity.QBIAS_VALUE, String.format("%.4f", (float) Qbias / 10000.0f));
                        bundle.putString(LaunchPadFlightControllerActivity.RMEASURE_VALUE, String.format("%.4f", (float) Rmeasure / 10000.0f));

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Kalman: " + Qangle + " " + Qbias + " " + Rmeasure);
                        break;

                    case SEND_ANGLES:
                        int roll = input[0] | ((byte) input[1] << 8); // This can be negative as well
                        int pitch = input[2] | ((byte) input[3] << 8); // This can be negative as well
                        int yaw = input[4] | (input[5] << 8); // Heading is always positive

                        // TODO: Just store this as an int
                        bundle.putString(LaunchPadFlightControllerActivity.ROLL_ANGLE, String.format("%.2f", (float) roll / 100.0f));
                        bundle.putString(LaunchPadFlightControllerActivity.PITCH_ANGLE, String.format("%.2f", (float) pitch / 100.0f));
                        bundle.putString(LaunchPadFlightControllerActivity.YAW_ANGLE, String.format("%.2f", (float) yaw / 100.0f));

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.v(TAG, "Acc: " + roll + " Gyro: " + pitch + " Kalman: " + yaw);
                        break;

                    default:
                        if (D)
                            Log.e(TAG, "Unknown command: " + cmd);
                        break;
                }
            } else {
                if (D)
                    Log.e(TAG, "Checksum error! Got: " + checksum + " Expected: " + msgChecksum);
            }
        } else {
            if (D)
                Log.e(TAG, "Wrong header! " + readMessage);
        }
    }

    private byte getChecksum(byte data[]) {
        byte checksum = 0;
        for (byte val : data)
            checksum ^= val;
        return checksum;
    }

    private int getChecksum(int data[]) {
        int checksum = 0;
        for (int val : data)
            checksum ^= val;
        return checksum;
    }
}