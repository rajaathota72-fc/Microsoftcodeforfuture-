package com.mmi.demo.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.mmi.MapView;
import com.mmi.MapmyIndiaMapView;
import com.mmi.apis.place.Place;
import com.mmi.apis.place.nearby.NearbyListener;
import com.mmi.apis.place.nearby.NearbyManager;
import com.mmi.demo.R;
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
public class NearbyFragment extends Fragment implements MapViewConstants, View.OnClickListener {

    private static final String TAG = NearbyFragment.class.getSimpleName();

EditText categoryEditTextView;

    MapView mMapView = null;
    BasicInfoWindow infoWindow;
    DelayAutoCompleteTextView    searchEditText = null;
    private SharedPreferences mPrefs;
    private EditText keywordsEditTextView;
    private EditText latitudeEditTextView;
    private EditText longitudeEditTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
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
        categoryEditTextView= (EditText) view.findViewById(R.id.cat_editText);
        keywordsEditTextView= (EditText) view.findViewById(R.id.keywords_editText);
        latitudeEditTextView= (EditText) view.findViewById(R.id.latitude_edit_text);
        longitudeEditTextView= (EditText) view.findViewById(R.id.longitude_edit_text);
      final  RadioGroup radioGroup= (RadioGroup)view.findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId== R.id.categories_radioButton){
                    categoryEditTextView.setEnabled(true);
                    keywordsEditTextView.setEnabled(false);
                }else{
                    categoryEditTextView.setEnabled(false);
                    keywordsEditTextView.setEnabled(true);

                }
            }
        });

      view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              try {
                  String category=categoryEditTextView.getText().toString();
                  String keywords=keywordsEditTextView.getText().toString();

                  String lat=latitudeEditTextView.getText().toString();

                  String lng=longitudeEditTextView.getText().toString();

                  if(radioGroup.getCheckedRadioButtonId()== R.id.categories_radioButton)
                      keywords=null;
                  else
                  category=null;

                  if(lat.length()>0&&lng.length()>0) {
                      GeoPoint point=new GeoPoint(Double.parseDouble(lat),Double.parseDouble(lng));
                      if((category!=null&&category.length()>0)||(keywords!=null&&keywords.length()>0)) {

                          NearbyManager nearbyManager = new NearbyManager();
                          nearbyManager.getNearbyPlaces(category, keywords, point, 1, new NearbyListener() {
                              @Override
                              public void onResult(int code, final ArrayList<Place> places) {
                                  if(places!=null&&places.size()>0){

                                      getActivity().runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              addOverLays(places);
                                          }
                                      });
                                  }
                              }
                          });
                      }
                  }
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


    void addOverLays(ArrayList<Place> places) {
        ArrayList<GeoPoint> points = new ArrayList<>();

        for (Place place : places) {
            addOverLay(place, false);

            points.add(place.getGeoPoint());
       }
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
