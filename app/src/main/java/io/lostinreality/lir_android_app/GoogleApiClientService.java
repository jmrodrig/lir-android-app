package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by jose on 02/08/16.
 */
public class GoogleApiClientService extends Application {
    //instantiate object public static
    private static GoogleApiClient mGoogleApiClient;

    public static void setApplicationGoogleApiClient(GoogleApiClient apiclient) {
        mGoogleApiClient = apiclient;
    };

    public static GoogleApiClient getApplicationGoogleApiClient() { return mGoogleApiClient; }


}
