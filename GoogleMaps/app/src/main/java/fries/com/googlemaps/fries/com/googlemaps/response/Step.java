package fries.com.googlemaps.fries.com.googlemaps.response;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by tmq on 11/06/15.
 */
public class Step {
    private static final String TAG = "Step";
    //    private String distance, duration;
    private String maneuver;
    private String instructionsText;
    private String instructionsInfo;
    private ArrayList<LatLng> polyline;

    public Step(JSONObject json){
        try {
            setDataFromJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Loi khi lay Step");
        }
    }

    private void setDataFromJson(JSONObject json) throws JSONException {
        maneuver = json.getString("maneuver");

        // instructions
        JSONObject instructions = json.getJSONObject("instructions");
        instructionsText = instructions.getString("text");
        instructionsInfo = instructions.getString("info");

        // polyline
        polyline = new ArrayList<>();
        JSONArray poly = json.getJSONArray("polyline");
        for (int i=0; i<poly.length(); i++){
            JSONObject point = poly.getJSONObject(i);
            polyline.add(new LatLng(point.getDouble("lat"), point.getDouble("lng")));
        }
    }

    // ---------------------------------- Get ---------------------------------------------------------------------

    public String getManeuver(){
        return maneuver;
    }
    public String getInstructionsText(){
        return instructionsText;
    }
    public String getInstructionsInfo(){
        return instructionsInfo;
    }
    public ArrayList<LatLng> getPolyline(){
        return polyline;
    }
}
