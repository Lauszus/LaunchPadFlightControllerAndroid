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
    static final byte SET_PID_ALT_HOLD = 4;
    static final byte GET_PID_ALT_HOLD = 5;
    static final byte SET_SETTINGS = 6;
    static final byte GET_SETTINGS = 7;
    static final byte SEND_ANGLES = 8;
    //static final byte SEND_INFO = 9;
    static final byte CAL_ACC = 9;
    static final byte CAL_MAG = 10;
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

    private void setPID(byte cmd, int Kp, int Ki, int Kd, int IntLimit) {
        byte output[] = {
                cmd, // Cmd
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

    private void getPID(byte cmd) {
        byte output[] = {
                cmd, // Cmd
                0, // Length
        };
        sendCommand(output); // Send output
    }

    /**
     * Set PID values for roll and pitch.
     *
     * @param Kp Kp value.
     * @param Ki Ki value.
     * @param Kd Kd value.
     */
    public void setPIDRollPitch(int Kp, int Ki, int Kd, int IntLimit) {
        if (D)
            Log.i(TAG, "setPIDRollPitch: " + Kp + " " + Ki + " " + Kd + " " + IntLimit);

        setPID(SET_PID_ROLL_PITCH, Kp, Ki, Kd, IntLimit);
    }

    /**
     * Use this to request PID values for roll and pitch.
     */
    public void getPIDRollPitch() {
        if (D)
            Log.i(TAG, "getPIDRollPitch");

        getPID(GET_PID_ROLL_PITCH);
    }

    /**
     * Set PID values for yaw.
     *
     * @param Kp Kp value.
     * @param Ki Ki value.
     * @param Kd Kd value.
     */
    public void setPIDYaw(int Kp, int Ki, int Kd, int IntLimit) {
        if (D)
            Log.i(TAG, "setPIDYaw: " + Kp + " " + Ki + " " + Kd + " " + IntLimit);

        setPID(SET_PID_YAW, Kp, Ki, Kd, IntLimit);
    }

    /**
     * Use this to request PID values for yaw.
     */
    public void getPIDYaw() {
        if (D)
            Log.i(TAG, "getPIDYaw");

        getPID(GET_PID_YAW);
    }

    /**
     * Set PID values for altitude hold.
     *
     * @param Kp Kp value.
     * @param Ki Ki value.
     * @param Kd Kd value.
     */
    public void setPIDAltHold(int Kp, int Ki, int Kd, int IntLimit) {
        if (D)
            Log.i(TAG, "setPIDAltHold: " + Kp + " " + Ki + " " + Kd + " " + IntLimit);

        setPID(SET_PID_ALT_HOLD, Kp, Ki, Kd, IntLimit);
    }

    /**
     * Use this to request PID values for altitude hold.
     */
    public void getPIDAltHold() {
        if (D)
            Log.i(TAG, "getPIDAltHold");

        getPID(GET_PID_ALT_HOLD);
    }

    public void setSettings(int AngleKp, int HeadingKp, byte AngleMaxInc, byte AngleMaxIncSonar, int StickScalingRollPitch, int StickScalingYaw) {
        if (D)
            Log.i(TAG, "setSettings: " + AngleKp + " " + HeadingKp + " " + AngleMaxInc + " " + AngleMaxIncSonar + " " + StickScalingRollPitch + " " + StickScalingYaw);

        byte output[] = {
                SET_SETTINGS, // Cmd
                10, // Length
                (byte) (AngleKp & 0xFF),
                (byte) (AngleKp >> 8),
                (byte) (HeadingKp & 0xFF),
                (byte) (HeadingKp >> 8),
                AngleMaxInc,
                AngleMaxIncSonar,
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
/*
        byte output[] = {
                SEND_INFO, // Cmd
                1, // Length
                enable,
        };
        sendCommand(output); // Send output
*/
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

    public void calibrateMagnetometer() {
        if (D)
            Log.i(TAG, "calibrateMagnetometer");

        byte output[] = {
                CAL_MAG, // Cmd
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

                    case GET_PID_ALT_HOLD:
                        int KpAltHold = input[0] | (input[1] << 8);
                        int KiAltHold = input[2] | (input[3] << 8);
                        int KdAltHold = input[4] | (input[5] << 8);
                        int IntLimitAltHold = input[6] | (input[7] << 8);

                        bundle.putInt(LaunchPadFlightControllerActivity.KP_ALT_HOLD_VALUE, KpAltHold);
                        bundle.putInt(LaunchPadFlightControllerActivity.KI_ALT_HOLD_VALUE, KiAltHold);
                        bundle.putInt(LaunchPadFlightControllerActivity.KD_ALT_HOLD_VALUE, KdAltHold);
                        bundle.putInt(LaunchPadFlightControllerActivity.INT_LIMIT_ALT_HOLD_VALUE, IntLimitAltHold);

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Received PID altitude hold: " + KpAltHold + " " + KiAltHold + " " + KdAltHold + " " + IntLimitAltHold);
                        break;

                    case GET_SETTINGS:
                        int AngleKp = input[0] | (input[1] << 8);
                        int HeadingKp = input[2] | (input[3] << 8);
                        byte AngleMaxInc = (byte) input[4];
                        byte AngleMaxIncSonar = (byte) input[5];
                        int StickScalingRollPitch = input[6] | (input[7] << 8);
                        int StickScalingYaw = input[8] | (input[9] << 8);

                        bundle.putInt(LaunchPadFlightControllerActivity.ANGLE_KP_VALUE, AngleKp);
                        bundle.putInt(LaunchPadFlightControllerActivity.HEADING_KP_VALUE, HeadingKp);
                        bundle.putByte(LaunchPadFlightControllerActivity.ANGLE_MAX_INC_VALUE, AngleMaxInc);
                        bundle.putByte(LaunchPadFlightControllerActivity.ANGLE_MAX_INC_SONAR_VALUE, AngleMaxIncSonar);
                        bundle.putInt(LaunchPadFlightControllerActivity.STICK_SCALING_ROLL_PITCH_VALUE, StickScalingRollPitch);
                        bundle.putInt(LaunchPadFlightControllerActivity.STICK_SCALING_YAW_VALUE, StickScalingYaw);

                        message.setData(bundle);
                        mHandler.sendMessage(message);

                        if (D)
                            Log.i(TAG, "Received settings: " + AngleKp + " " + HeadingKp + " " + AngleMaxInc + " " + AngleMaxIncSonar + " " + StickScalingRollPitch + " " + StickScalingYaw);
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