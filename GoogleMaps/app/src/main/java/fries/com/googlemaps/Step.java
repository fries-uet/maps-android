package fries.com.googlemaps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tmq on 10/26/15.
 */
public class Step {
    private LatLng latLng;
    private String text;
    private String maneuver;

//    public Step(){}

    public Step(LatLng latLng, String text, String maneuver){
        this.latLng = latLng;
        this.text = text;
        this.maneuver = maneuver;
    }

    public void speak(){
        new ReadText(text).run();
    }

    public String getText(){
        return text;
    }

    public LatLng getLatLng(){
        return latLng;
    }

    public String getManeuver(){
        return maneuver;
    }
}
