package com.mmi.demo.fragments;

import android.os.Bundle;
import android.util.Log;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.mmi.demo.R;
import com.mmi.apis.distance.Distance;
import com.mmi.apis.routing.DirectionListener;
import com.mmi.apis.routing.DirectionManager;
import com.mmi.apis.distance.DistanceListener;
import com.mmi.apis.distance.DistanceManager;

import com.mmi.apis.routing.Trip;
import com.mmi.layers.MapEventsOverlay;
import com.mmi.layers.Marker;
import com.mmi.layers.PathOverlay;
import com.mmi.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by Mohammad Akram on 03-04-2015
 */
public class DirectionPolylineFragment extends MapBaseFragment implements ISimpleDialogListener {
    public final String TAG = DirectionPolylineFragment.class.getSimpleName();

    final int REQUEST_CODE = 5;
    GeoPoint selectedPoint = null;
    GeoPoint startPoint = null;
    GeoPoint endPoint = null;
    ArrayList<GeoPoint> viaPoints = new ArrayList<>();

    @Override
    public String getSampleTitle() {
        return "Direction Polyline";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public void addPolyline(final ArrayList<GeoPoint> geoPoints, final boolean setBound) {
        PathOverlay pathOverlay = new PathOverlay(getActivity());
        pathOverlay.setColor(getResources().getColor(R.color.base_color));
        pathOverlay.setWidth(10);
        pathOverlay.setPoints(geoPoints);
        mMapView.getOverlays().add(pathOverlay);
        mMapView.postInvalidate();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (setBound)
                    mMapView.setBounds(geoPoints);
            }
        });

    }


    public void addMarker(GeoPoint point, boolean isVia) {
        Marker marker = new Marker(mMapView);
        if (isVia)
            marker.setIcon(getResources().getDrawable(R.drawable.marker_selected));

        marker.setPosition(point);
        marker.setInfoWindow(null);

        mMapView.getOverlays().add(marker);
        mMapView.postInvalidate();
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        selectedPoint = p;
        SimpleDialogFragment.createBuilder(getActivity(), getActivity().getSupportFragmentManager()).setTargetFragment(this, REQUEST_CODE)
                .setTitle(R.string.driving_directions).setMessage(R.string.set_as).setPositiveButtonText(R.string.set_departure)
                .setNegativeButtonText(R.string.set_destination).setNeutralButtonText(R.string.set_viapoint).show();
        return super.longPressHelper(p);
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE:
                startPoint = selectedPoint;
                addMarker(selectedPoint, false);
                if (endPoint != null && startPoint != null)
                    getDirections(startPoint, endPoint, viaPoints);
                break;
        }
    }


    @Override
    public void onNeutralButtonClicked(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE:
                viaPoints.add(selectedPoint);
                addMarker(selectedPoint, true);
                if (endPoint != null && startPoint != null)
                    getDirections(startPoint, endPoint, viaPoints);
                break;
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE:
                endPoint = selectedPoint;
                addMarker(selectedPoint, false);
                if (endPoint != null && startPoint != null)
                    getDirections(startPoint, endPoint, viaPoints);
                break;
        }
    }


    void getDirections(final GeoPoint startPoint, final GeoPoint endPoint, final ArrayList<GeoPoint> viaPoints) {


        try {
            DirectionManager dm=  new DirectionManager();
            dm.setRouteType(DirectionManager.RouteType.QUICKEST);
                    dm.getDirections(startPoint, endPoint, viaPoints, new DirectionListener() {
                        @Override
                        public void onResult(int code, final ArrayList<Trip> trips) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    clearOverlays();

                                    if (trips != null) {
                                        addPolyline(trips.get(0).getPath(), true);

                                        for (GeoPoint geoPoint : viaPoints) {
                                            addMarker(geoPoint, true);
                                        }
                                        addMarker(startPoint, false);
                                        addMarker(endPoint, false);
                                    }
                                }
                            });
                        }
                    });



        } catch (Exception ex) {
            ex.printStackTrace();
        }


        try{
            new DistanceManager().getDistance(startPoint, viaPoints, new DistanceListener() {
                @Override
                public void onResult(int code, ArrayList<Distance> distances) {

                    if(code==0)
                        for(Distance distance : distances){
                            Log.e(TAG,"duration= "+ distance.getDuration()+" distance= "+ distance.getDistance());
                        }
                    else{
                        Log.e(TAG,"code= "+code);

                    }
                }
            });

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    void clearOverlays() {
        mMapView.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity(), this);
        mMapView.getOverlays().add(mLocationOverlay);
        mMapView.getOverlays().add(mapEventsOverlay);

    }
}
