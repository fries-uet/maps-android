package fries.com.googlemaps.fries.com.googlemaps.response;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tmq on 11/06/15.
 */
public class AddressMap {
    private String shortName;
    private String fullName;
    private LatLng latLng;

    public AddressMap(String shortName, String fullName, LatLng latLng){
        this.shortName = shortName;
        this.fullName = fullName;
        this.latLng = latLng;
    }

    public String getShortName(){
        return shortName;
    }

    public String getFullName(){
        return fullName;
    }

    public LatLng getLatLng(){
        return latLng;
    }
}
