package fries.com.googlemaps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tmq on 10/26/15.
 */
public class Step {
    private LatLng latLng;
    private String text;

//    public Step(){}

    public Step(LatLng latLng, String text){
        this.latLng = latLng;
        this.text = text;
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
}
