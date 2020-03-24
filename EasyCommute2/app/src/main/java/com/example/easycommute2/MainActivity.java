package com.example.easycommute2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate();
            LicenceManager.getInstance().setRestAPIKey("qql28ulzq1a62t3zaccg26u73y3zy5p1");
            LicenceManager.getInstance().setMapSDKKey("um22gy1jymocf4n2fbtggnyjyduwhv9c");
        setContentView(R.layout.main);
//Add a Map View to your XML layout
        MapmyIndiaMapView  mapMyIndiaMapView = (MapmyIndiaMapView)  findViewById(R.id.map);
        MapView mMapView = mapMyIndiaMapView.getMapView();
        //if you want to create map view Instance D dynamically
        MapmyIndiaMapView  mapMyIndiaMapView = new MapmyIndiaMapView (this);
        MapView mMapView = mapMyIndiaMapView.getMapView();
        setContentView(mapMyIndiaMapView);
    }

    }

}
}
