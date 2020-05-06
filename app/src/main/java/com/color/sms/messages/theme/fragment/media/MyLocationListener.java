package com.color.sms.messages.theme.fragment.media;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public abstract class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(final Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
