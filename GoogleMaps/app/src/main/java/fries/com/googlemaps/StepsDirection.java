package fries.com.googlemaps;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by tmq on 10/26/15.
 */
public class StepsDirection {
    private static final String TAG = "StepsDirection";
    private Context mContext;
    private ArrayList<Step> listSteps;
    private String origin, destination, distance, duration;


    private int preDistance;
    private int stateLocation;

    private static final int DISTANCE_MIN           = 8;    // 8 meter
    private static final int DISTACE_SPEAK_AGAIN    = 200;  // 200 meter
    private static final int STATE_GO_TO_STEP       = 111111;     //
    private static final int STATE_LEFT_STEP        = 222222;     //

    public static final int STATE_DIRECTION_IS_SPEAKING = 0;
    public static final int STATE_DIRECTION_IS_NOT_SPEAK = 1;
    public static final int STATE_DIRECTION_IS_FINISHED = 2;

    private static boolean isDirecting;
    private static int currentStep;
    private int preIndex;                   // = (Khoang cach toi step ke tiep)/(DISTACE_SPEAK_AGAIN)


    public StepsDirection(Context context){
        mContext = context;
        resetDirection();
    }

    public void resetDirection(){
        currentStep     = 0;

        stateLocation   = STATE_GO_TO_STEP;
        isDirecting     = false;
        preIndex        = 999999;
        setInfomationOfDirection("null","null","null","null", null);
        listSteps       = new ArrayList<>();

        //----------------------- test --------------------------------
        originIsSpeak = false;
        allowGoToNextStep = true;
        preDistance     = 999999;
        preDistanceState    = DISTANCE_DECREASE;
    }

    public void addStepLatLng(Step step){
        listSteps.add(step);
        Logger.i(mContext, TAG, "Step: " + listSteps.size() + ": " + directStep(listSteps.size() - 1));
        if (!isDirecting){
            currentStep = 0;
//            isDirecting = true;
        }
    }

    public int checkLocationAndSpeak(LatLng currentLatLng){
        // If direction is empty
        if (!isDirecting) return STATE_DIRECTION_IS_NOT_SPEAK;

        LatLng currentLatLngStep = listSteps.get(currentStep).getLatLng();
        String text = "";



        // Bat dau chi duong
        if (currentStep==0 && !originIsSpeak){
//            Logger.i(TAG, mContext, directStep(currentStep-1));
//            new ReadText(directStep(currentStep)).run();
            text += directStep(currentStep-1) + ". ";
//            currentStep ++;
            currentStep ++;
            originIsSpeak = true;
        }

        // Get distance from my location to next step
        int distance = (int) distance(currentLatLng.latitude, currentLatLng.longitude, currentLatLngStep.latitude, currentLatLngStep.longitude);
        Logger.i(mContext, TAG, "Distance = " + distance + ", index = " + currentStep);

//        managerJumpStep(distance);

        // Case
        if (distance<=100){          // Next step
            text += readyGoToNextStep();
        } else {                    // Prepare go to next Step
            text += preparingGoToNextStep(distance);
        }

        if (!text.equals("")){
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            new ReadText(text).run();
            Logger.i(mContext, TAG, text);
        }

        if (distance<70) managerJumpStep(distance);

        if (currentStep>=listSteps.size()) {
            return STATE_DIRECTION_IS_FINISHED;
        }
        return STATE_DIRECTION_IS_SPEAKING;
    }

    private String readyGoToNextStep(){
        if (!allowGoToNextStep) return "";

        String value = "Chuẩn bị " + directStep(currentStep);
        if (currentStep>=listSteps.size()-1){     // Ket thuc chi duong
            isDirecting = false;
            value += ". Đích đến ở phía trước";
        }
//        new ReadText(value).run();
//        Log.i(TAG, value);

        // Reset preIndex cho step moi, gan preIndex dat gia tri max
        preIndex = 999999;
        return value;
    }
    private String preparingGoToNextStep(int distance){
        int currentIndex = (int)(distance/DISTACE_SPEAK_AGAIN);

        // Index trung nhau -> Khong doc thong bao
        if (currentIndex>=preIndex) return "";

        // Khi ma index khac nhau -> dua ra thong bao
        String value = "Còn " + distance + " mét nữa thì " + directStep(currentStep);
//        new ReadText(value).run();
//        Log.i(TAG, value);

        // Gan gia tri currentIndex cho preIndex
        preIndex = currentIndex;

        return value;
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

    private String directStep(int pos){
        if (pos>=0 && pos<listSteps.size()) return listSteps.get(pos).getText();
        else {
            Logger.i(mContext, TAG, "Out of bound: " + pos);
            return "";
        }
    }



    //---------------------------------- Get ---------------------------------------------
    public ArrayList<Step> getListSteps(){
        return listSteps;
    }

    public String getOrigin(){
        return origin;
    }
    public String getDestination(){
        return destination;
    }
    public String getDistance(){
        return distance;
    }
    public String getDuration(){
        return duration;
    }
    public boolean getIsDirecting(){
        return isDirecting;
    }

    //------------------------------------ Set ------------------------------------------
    public void setInfomationOfDirection(String origin, String destination, String distance, String duration, LatLng tailDestination){
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.duration = duration;
        this.latLngOfTailDestination = tailDestination;
    }

    private void setIsDirecting(boolean isStart){
        isDirecting = isStart;
    }
    public void startDirecting(){
        setIsDirecting(true);
    }

    // ----------------------- Test ---------------------------------------
    private static final int DISTANCE_DECREASE  = 111;  // Trang thai: khoang cach giam dan
    private static final int DISTANCE_ASCEND    = 222;  // Trang thai: khoang cach tang dan

    private boolean allowGoToNextStep;
    private int preDistanceState;
    private boolean originIsSpeak;

    private LatLng latLngOfTailDestination;

    private void managerJumpStep(int distance){
        int currentDistanceState;
        if (preDistance>distance) currentDistanceState = DISTANCE_DECREASE; // Giam dan
        else currentDistanceState = DISTANCE_ASCEND;                        // Tang dan

        if (currentDistanceState!=preDistanceState) {                       // Neu thay doi trang thai thi nhay Step moi
            currentStep ++;
            allowGoToNextStep = true;
        }else{                                                              // Neu khong thay doi trang thai thi khong cho nhay Step moi
            allowGoToNextStep = false;
        }

        preDistance = distance;
        preDistanceState = currentDistanceState;
    }


}
