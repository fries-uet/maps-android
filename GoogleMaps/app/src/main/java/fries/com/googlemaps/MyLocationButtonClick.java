package fries.com.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by tmq on 10/25/15.
 */
public class MyLocationButtonClick implements GoogleMap.OnMyLocationButtonClickListener {
    private static final String TAG = "MyLocationButtonClick";
    private Context mContext;
    private GoogleMap mMap;

    public MyLocationButtonClick(Context context, GoogleMap gm){
        mContext = context;
        mMap = gm;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (!isGPSEnable())         // GPS is unable
            turnOnGPS();
        if (isGPSEnable()) {
            mMap.setMyLocationEnabled(true);
            getMyAddress();
        }
        return false;
    }

    private void turnOnGPS(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setMessage("Location is Unable!");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "Location is Unable", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        dialog.show();
    }

    private boolean isGPSEnable(){
        LocationManager locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getMyAddress() {
        Location mLocation = mMap.getMyLocation();

        double lat = mLocation.getLatitude();
        double lng = mLocation.getLongitude();

        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 10);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.i(TAG, addresses.size() + "");
//                Toast.makeText(this, addresses.size() + "", Toast.LENGTH_LONG).show();
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    result.append(address.getAddressLine(i)).append("\n");
                }
                Log.i(TAG, "LATITUDE: " + lat);
                Log.i(TAG, "LONGITUDE: " + lng);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Toast.makeText(mContext, "" + result, Toast.LENGTH_SHORT).show();
    }
}
