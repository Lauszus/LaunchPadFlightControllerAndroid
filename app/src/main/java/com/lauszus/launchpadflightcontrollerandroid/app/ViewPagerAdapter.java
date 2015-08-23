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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public static final int INFO_FRAGMENT = 4;
    public static final int PID_FRAGMENT = 0;
    public static final int SETTINGS_FRAGMENT = 1;
    public static final int GRAPH_FRAGMENT = 2;
    public static final int MAP_FRAGMENT = 3;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            /*case INFO_FRAGMENT:
                return new InfoFragment();*/
            case PID_FRAGMENT:
                return new PIDFragment();
            case SETTINGS_FRAGMENT:
                return new SettingsFragment();
            case GRAPH_FRAGMENT:
                return new GraphFragment();
            case MAP_FRAGMENT:
                return new MapFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4; // Return number of tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            /*case INFO_FRAGMENT:
                return "Info";*/
            case PID_FRAGMENT:
                return "PID";
            case SETTINGS_FRAGMENT:
                return "Settings";
            case GRAPH_FRAGMENT:
                return "Graph";
            case MAP_FRAGMENT:
                return "Map";
        }
        return null;
    }
}