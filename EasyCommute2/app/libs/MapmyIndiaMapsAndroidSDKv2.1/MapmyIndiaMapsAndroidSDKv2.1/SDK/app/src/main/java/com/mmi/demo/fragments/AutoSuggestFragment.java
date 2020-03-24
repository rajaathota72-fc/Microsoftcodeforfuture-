package com.mmi.demo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.mmi.MapView;
import com.mmi.MapmyIndiaMapView;
import com.mmi.apis.place.Place;
import com.mmi.apis.place.autosuggest.AutoSuggest;
import com.mmi.apis.place.details.PlaceDetailsListener;
import com.mmi.apis.place.details.PlaceDetailsManager;
import com.mmi.demo.R;
import com.mmi.demo.adapter.AutoCompleteAdapter;
import com.mmi.demo.widget.DelayAutoCompleteTextView;
import com.mmi.layers.BasicInfoWindow;
import com.mmi.layers.Marker;
import com.mmi.util.GeoPoint;
import com.mmi.util.LogUtils;
import com.mmi.util.constants.MapViewConstants;

import java.util.ArrayList;

/**
 * Created by Mohammad Akram on 03-04-2015
 */
public class AutoSuggestFragment extends Fragment implements MapViewConstants, View.OnClickListener {

    private static final String TAG = AutoSuggestFragment.class.getSimpleName();



    MapView mMapView = null;
    BasicInfoWindow infoWindow;
    DelayAutoCompleteTextView    searchEditText = null;
    private SharedPreferences mPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auto_complete, container, false);
        mMapView = ((MapmyIndiaMapView) view.findViewById(R.id.mapview)).getMapView();

        mMapView.setMultiTouchControls(true);

        setupUI(view);
        infoWindow = new BasicInfoWindow(R.layout.tooltip, mMapView);

        infoWindow.setTipColor(getResources().getColor(R.color.base_color));
        clearOverlays();
        return view;
    }

    private void setupUI(View view) {

        // view.findViewById(R.id.search_button).setOnClickListener(this);
        searchEditText= (DelayAutoCompleteTextView) view.findViewById(R.id.search_place);

        searchEditText.setAdapter(new AutoCompleteAdapter(getActivity()));
        searchEditText.setLoadingIndicator(
                (ProgressBar) view.findViewById(R.id.loading_indicator));
        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AutoSuggest book = (AutoSuggest) adapterView.getItemAtPosition(position);
                searchEditText.setText(book.getAddr());

                try {
                    String poiID=book.getPlaceId();

                    PlaceDetailsManager placeDetailsManager=new PlaceDetailsManager();
                    placeDetailsManager.getPlaceDetails(poiID, new PlaceDetailsListener() {
                        @Override
                        public void onResult(int code,final Place place) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addOverLays(place);
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
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

                break;
        }
    }


    void addOverLays(Place places) {
        ArrayList<GeoPoint> points = new ArrayList<>();

       // for (Place place : places) {
            addOverLay(places, false);

            points.add(places.getGeoPoint());
       // }
        mMapView.postInvalidate();
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

    }

    @Override
    public void onDestroyView() {



        super.onDestroyView();


    }
}
