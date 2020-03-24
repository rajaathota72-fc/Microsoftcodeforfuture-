package com.mmi.demo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mmi.MapView;
import com.mmi.MapmyIndiaMapView;
import com.mmi.demo.R;

import com.mmi.apis.place.geocoder.GeocodeListener;
import com.mmi.apis.place.geocoder.GeocodeManager;
import com.mmi.apis.place.Place;
import com.mmi.apis.place.reversegeocode.ReverseGeocodeListener;
import com.mmi.apis.place.reversegeocode.ReverseGeocodeManager;
import com.mmi.layers.BasicInfoWindow;
import com.mmi.layers.MapEventsOverlay;
import com.mmi.layers.MapEventsReceiver;
import com.mmi.layers.Marker;
import com.mmi.util.GeoPoint;
import com.mmi.util.LogUtils;
import com.mmi.util.constants.MapViewConstants;

import java.util.ArrayList;

/**
 * Created by Mohammad Akram on 03-04-2015
 */
public class GeoCodeReverseGeoCodeFragment extends Fragment implements MapEventsReceiver, MapViewConstants, View.OnClickListener {

    private static final String TAG = GeoCodeReverseGeoCodeFragment.class.getSimpleName();


    MapView mMapView = null;
    BasicInfoWindow infoWindow;
    EditText searchEditText = null;
    private SharedPreferences mPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_geocode_reverse_geocode, container, false);
        mMapView = ((MapmyIndiaMapView) view.findViewById(R.id.mapview)).getMapView();

        mMapView.setMultiTouchControls(true);

        setupUI(view);
        infoWindow = new BasicInfoWindow(R.layout.tooltip, mMapView);

        infoWindow.setTipColor(getResources().getColor(R.color.base_color));
        clearOverlays();
        return view;
    }

    private void setupUI(View view) {
        view.findViewById(R.id.search_button).setOnClickListener(this);
        searchEditText = (EditText) view.findViewById(R.id.search_place);
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {


        try {

            new ReverseGeocodeManager().getPlace(p, new ReverseGeocodeListener() {
                @Override
                public void onResult(int code, final Place place) {
                    if (place != null) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addOverLay(place, true);

                                mMapView.invalidate();
                            }
                        });

                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPause() {
        final SharedPreferences.Editor edit = mPrefs.edit();

        edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
        edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
        edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());

        edit.commit();

        LogUtils.LOGE(TAG, "onPause");
        LogUtils.LOGE(TAG, mMapView.getScrollX() + "");
        LogUtils.LOGE(TAG, mMapView.getScrollY() + "");
        LogUtils.LOGE(TAG, mMapView.getZoomLevel() + "");


        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 5));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

        LogUtils.LOGE(TAG, "onResume");

        LogUtils.LOGE(TAG, mPrefs.getInt(PREFS_SCROLL_X, 0) + "");
        LogUtils.LOGE(TAG, mPrefs.getInt(PREFS_SCROLL_Y, 0) + "");
        LogUtils.LOGE(TAG, mPrefs.getInt(PREFS_ZOOM_LEVEL, 5) + "");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.search_button:
                String searchText = searchEditText.getText().toString();
                try {
                    new GeocodeManager().getPlace(searchText, new GeocodeListener() {
                        @Override
                        public void onResult(int code, final ArrayList<Place> places) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    addOverLays(places);
                                }
                            });

                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }


    void addOverLays(ArrayList<Place> places) {
        ArrayList<GeoPoint> points = new ArrayList<>();

        for (Place place : places) {
            addOverLay(place, false);

            points.add(place.getGeoPoint());
        }
        mMapView.invalidate();
        mMapView.setBounds(points);
    }

    void addOverLay(Place place, boolean showInfo) {

        if(place==null)
            return;

        Marker marker = new Marker(mMapView);
        marker.setTitle(place.getDisplayName());
        marker.setDescription(place.getFullAddress());
        marker.setSubDescription(place.getFullAddress());
        marker.setIcon(getResources().getDrawable(R.drawable.marker_selected));
        marker.setPosition(place.getGeoPoint());

        marker.setInfoWindow(infoWindow);
        marker.setRelatedObject(place);

        if (showInfo)
            marker.showInfoWindow();
        mMapView.getOverlays().add(marker);


    }





    void clearOverlays() {
        mMapView.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity(), this);
        mMapView.getOverlays().add(0, mapEventsOverlay); //inserted at the "bottom" of all overlays
    }
}
