package com.lauszus.launchpadflightcontrollerandroid;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A placeholder fragment containing a simple view.
 */
public class LaunchPadFlightControllerActivityFragment extends Fragment {

    public LaunchPadFlightControllerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launch_pad_flight_controller, container, false);
    }
}
