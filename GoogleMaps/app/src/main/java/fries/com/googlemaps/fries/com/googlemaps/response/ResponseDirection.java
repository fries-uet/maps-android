package fries.com.googlemaps.fries.com.googlemaps.response;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import fries.com.googlemaps.ReadTextStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by tmq on 11/05/15.
 */
public class ResponseDirection extends ResponseService{
    private static final String TAG = "ResponseDirection";

    private String information;
    private AddressMap  origin,
                        destination;
    private ArrayList<Step> listSteps;

    private PolylineOptions polylineOptions;


    public ResponseDirection(JSONObject json) {
        super(json);
    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {
        origin      = getJsonAddress(json, "origin");
        destination = getJsonAddress(json, "destination");
        information = getJsonInformation(json);
        Log.i(TAG, "information: " + information);
        getListSteps(json);
    }


    // ---------------------------------- Get drection in JSON -------------------------------------------------------

    private AddressMap getJsonAddress(JSONObject json, String type) throws JSONException {
        JSONObject address = json.getJSONObject(type);
        String shortName = address.getString("short_name");
        String fullName = address.getString("long_name");
        JSONObject geo = address.getJSONObject("geo");
        LatLng latLng = new LatLng(geo.getDouble("lat"), geo.getDouble("lng"));

        return new AddressMap(shortName, fullName, latLng);
    }

    private String getJsonInformation(JSONObject json) throws JSONException {
        JSONObject info = json.getJSONObject("info");
        String summary = info.getString("summary");
        String distance_vn = info.getString("distance_vn");
        String duration = info.getString("duration");

        return  "Bạn sẽ đi từ " + origin.getShortName() +
                " đến " + destination.getShortName() +
                ", qua " + summary +
                ", dài " + distance_vn +
                ", mất " + duration;
    }

    private void getListSteps(JSONObject json) throws JSONException {
        listSteps = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        JSONArray steps = json.getJSONArray("steps");
        for (int i=0; i<steps.length(); i++){
            Step step = new Step(steps.getJSONObject(i));
            listSteps.add(step);
            ArrayList<LatLng> polyline = step.getPolyline();
            for (LatLng latLng: polyline){
                polylineOptions.add(latLng);
            }
        }
    }

    // ------------------------------------ Speak --------------------------------------------------------------------

    public void speakInformation(){
        if (information!=null){
            new ReadTextStream(information).run();  // Dung run de doc het thong tin ma khong bi gian doan
        }
    }
    public PolylineOptions getPolylineOptions(){
        return polylineOptions;
    }
    public AddressMap getOrigin(){
        return origin;
    }
    public AddressMap getDestination(){
        return destination;
    }
}
