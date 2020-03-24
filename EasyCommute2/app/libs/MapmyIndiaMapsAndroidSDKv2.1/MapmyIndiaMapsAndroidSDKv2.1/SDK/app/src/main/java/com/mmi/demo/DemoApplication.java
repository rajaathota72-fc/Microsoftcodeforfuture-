package com.mmi.demo;

import android.app.Application;

import com.mmi.LicenceManager;

/**
 * Created by CE on 29/09/15.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LicenceManager.getInstance().setRestAPIKey(your_rest_api_key);
        LicenceManager.getInstance().setMapSDKKey(your_java_script_key);

    }
}
