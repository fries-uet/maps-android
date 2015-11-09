package fries.com.googlemaps.fries.com.googlemaps.response;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import fries.com.googlemaps.Logger;
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


    public ResponseDirection(Context context, JSONObject json) {
        super(context, json);
    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {
        resetData();

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

    // ------------------------------------- Direction ---------------------------------------------------------------

    public boolean isDirecting;
    private int currentStep;


    private void resetData(){
        listSteps = new ArrayList<>();
        polylineOptions = new PolylineOptions();

        isDirecting = false;
        currentStep = -1;
    }

    public boolean checkLocationAndSpeakDirection(Location currentLocation){
        // Chi duogn cho doan khoi dau
        if (currentStep==-1){
            startDirecting(currentLocation);
            currentStep = 0;
        }

        // Chi duong cho tung Step
        startDirectingStep(currentLocation);

        return false;
    }

    // Khi bat dau chi duong: Noi thong tin khi moi bat dau
    private void startDirecting(Location currentLocation){
        double distance = distance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                firstPointInStep(currentStep+1).latitude, firstPointInStep(currentStep+1).longitude);
        double speed = getSpeed(currentLocation);
        double time = distance/speed;
        // Truong hop doan duong ngan
        if (time<SHORT_STEP) typeStep = SHORT_STEP;
        speak(listSteps.get(0).getInstructionsText());
    }

    // --------------------------------------- Direct In Step: Long, Medium, Short -------------------------------------
    // Constant dung de phan biet cac doan duong, dong thoi gia tri cua no cung la khoang thoi gian de thong bao cho re cho nguoi dung
    private static final int MAX_VALUE  = 999999;
    private static final int LONG_STEP      = 120;  // 120s
    private static final int MEDIUM_STEP    = 60;   // 60s
    private static final int SHORT_STEP     = 20;   // 20s
    private static final int DISTANCE_DECREASE  = 111;  // Trang thai: khoang cach giam dan
    private static final int DISTANCE_ASCEND    = 222;  // Trang thai: khoang cach tang dan

    private int typeStep = LONG_STEP;
    private double preDistance = MAX_VALUE;               // Luu lai gia tri distance cu => Tim ra vi tri thay doi Step
    private int preDistanceState = DISTANCE_DECREASE;

    private void startDirectingStep(Location currentLocation){
        if (currentStep+1>=listSteps.size()) return;    // Out of bound

        double distance = distance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                    firstPointInStep(currentStep+1).latitude, firstPointInStep(currentStep+1).longitude);
        Logger.i(mContext, TAG + "_startDirectingStep", "Distance = " + distance);
        double speed = getSpeed(currentLocation);
        double time = distance/speed;


        if (time>LONG_STEP){                // Long step
            directLongStep(time, distance);
        }else if (time<MEDIUM_STEP){        // Medium & Short Step
            if (time>SHORT_STEP){           // Medium step
                directMediumStep(distance);
            }else{                          // Short step
                directShortStep(distance);
                if (time<SHORT_STEP/2)              // Chon khoang thoi gian nho hon de kiem tra
                    checkGoToNextStep(distance);    // Kiem tra xem da sang Step moi chua
            }
        }
        Logger.i(mContext, TAG, "type of Step = " + typeStep);
    }

    /*  LongStep:
    *       Time >= 2 minutes
    *       If speed = 10m/s, Distance >= 1200 meter
    * */
    private int currentIndex = MAX_VALUE;
    private void directLongStep(double time, double distance){
        int index = (int) time/LONG_STEP;
        if (index<currentIndex && semaphore()){
            Logger.i(mContext, TAG + "_directLongStep", "CurrentIndex = " + currentIndex);
            typeStep = LONG_STEP;
            currentIndex = index;
            speak("Còn " + (int)distance + " mét nữa thì " + listSteps.get(currentStep+1).getInstructionsText());
        }
    }

    /*  MediumStep:
    *       Time >= 20 seconds
    *       If speed = 10m/s, Distance >= 200 meter
    * */
    private void directMediumStep(double distance){
        if (typeStep==LONG_STEP && semaphore()){       // Truoc do la doan duong dai, thi chuyen sang doan duong trung
            typeStep = MEDIUM_STEP;
            speak("Còn " + (int)distance + " mét nữa thì " + listSteps.get(currentStep+1).getInstructionsText());
        }
    }

    /*  ShortStep:
    *       Time < 20 seconds
    *       If speed = 10m/s, Distance < 200 meter
    * */
    private void directShortStep(double distance){
        if (typeStep==MEDIUM_STEP && semaphore()){       // Truoc do la doan duong trung, thi chuyen sang doan duong ngan
            typeStep = SHORT_STEP;
            speak("Còn " + (int)distance + " mét nữa thì " + listSteps.get(currentStep+1).getInstructionsText());
        }
    }

    private void checkGoToNextStep(double distance){
        int stateDistance;
        if (preDistance>distance) stateDistance = DISTANCE_DECREASE;
        else stateDistance = DISTANCE_ASCEND;

        if (stateDistance != preDistanceState){
            currentStep ++;
            currentIndex = MAX_VALUE;
            typeStep = LONG_STEP;
            preDistance = MAX_VALUE;               // Luu lai gia tri distance cu => Tim ra vi tri thay doi Step
            preDistanceState = DISTANCE_DECREASE;
            Logger.i(mContext, TAG, "Go to next Step" + currentIndex);
        }
        preDistance = distance;
    }

    // -------------------------------------- Simaphore: Speak ---------------------------------------------------------
    private long lastTimeSpeak = 0;
    private static final long MIN_TIME = 5000;
    // Dua vao thoi gian cuoi cung doc thong bao, semaphore lam nhiem vu cho phep thuc hien tac vu tiep theo (vi du: doc thong bao tiep theo)
    private boolean semaphore(){
        long currentTime = System.currentTimeMillis();
        boolean result = (currentTime - lastTimeSpeak) >  MIN_TIME;
        if (result) Logger.i(mContext, TAG, "Semapore = TRUE");
        else Logger.i(mContext, TAG, "Semapore = FALSE");
        return result;
    }

    private void speak(String text){
        // Danh dau thoi gian bat dau doc thong bao
        lastTimeSpeak = System.currentTimeMillis();
        Logger.i(mContext, TAG, "Speak: " + text);
        new ReadTextStream(text).run();
    }

    // --------------------------------------- Get -----------------------------------------------------------------------
    private Location preLocation = null;
    private long preTime = 0;
    private double preSpeed = 0;

    private double getSpeed(Location currentLocation){
        long currentTime = System.currentTimeMillis();
        if (preLocation==null) preLocation = currentLocation;
        double distance = currentLocation.distanceTo(preLocation);
        double delta = (currentTime-preTime)/(double)1000;
        double speed = distance/delta;

        if (preSpeed>2 && speed>2*preSpeed){
            speed = Math.sqrt(speed);
        }

        Logger.i(mContext, TAG, "Speed = " + speed + "________ Delta-T = " + delta);

        preLocation = currentLocation;
        preTime = currentTime;
        preSpeed = speed;

        return speed;
    }

    private LatLng firstPointInStep(int index){
        return listSteps.get(index).getPolyline().get(0);
    }

    // Tinh khoang cach giua hai Toa do tren ban do
    public double distance(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371000;
        double lat_x = Math.toRadians(lat1);
        double lat_y = Math.toRadians(lat2);

        double delta_lat = Math.toRadians(lat2 - lat1);
        double delta_lng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(delta_lat / 2) * Math.sin(delta_lat / 2) +
                Math.cos(lat_x) * Math.cos(lat_y) * Math.sin(delta_lng / 2) * Math.sin(delta_lng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r * c;
    }

}
