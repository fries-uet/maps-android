package fries.com.googlemaps;

import android.content.Context;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by tmq on 10/26/15.
 */
public class StepsDirection {
    private Context mContext;
    private ArrayList<Step> listSteps = new ArrayList<>();


    private int preDistance;
    private int stateLocation;

    private static final int DISTANCE_MIN = 8;
    private static final int STATE_GO_TO_STEP   = 111111;     //
    private static final int STATE_LEFT_STEP    = 222222;     //

    private static boolean isDirecting;
    private static int currentStep;


    public StepsDirection(Context context){
        mContext = context;
        resetDirection();
    }

    public void resetDirection(){
        currentStep = -1;
        preDistance = 999999;
        stateLocation = STATE_GO_TO_STEP;
        isDirecting = false;
    }

    public StepsDirection(ArrayList<Step> list){
        this.listSteps = list;
    }

    public void addStepLatLng(Step step){
        listSteps.add(step);
        if (!isDirecting){
            currentStep = 0;
            isDirecting = true;
        }
    }

    public boolean checkLocationAndSpeak(LatLng currentLatLng){
        // If direction is empty
        if (!isDirecting) return false;

        LatLng currentLatLngStep = listSteps.get(currentStep).getLatLng();

        if (currentStep==0){
            new ReadText(directStep(currentStep)).run();
            currentStep ++;
        }

        // Get distance from my location to next step
        int distance = (int) distance(currentLatLng.latitude, currentLatLng.longitude, currentLatLngStep.latitude, currentLatLngStep.longitude);

//        if (distance<preDistance){
//
//        }

        // Case
        if (distance<=20){          // Next step
            goToNextStep();
            return true;
        } else if ((distance % 100)>=0 && (distance%100)<DISTANCE_MIN){       // Prepare go to next Step
            preparingGoToNextStep(distance);
            return true;
        }
        return false;
    }

    private void goToNextStep(){
        String value = "Chuẩn bị " + listSteps.get(currentStep).getText();
        Toast.makeText(mContext, "Direct: " + value, Toast.LENGTH_SHORT).show();
        if (currentStep+1 < listSteps.size()) currentStep++;
        new ReadText(value).run();
    }
    private void preparingGoToNextStep(int distance){
        String value = "Còn " + distance + " mét nữa thì " + listSteps.get(currentStep).getText();
        Toast.makeText(mContext, "Direct: " + value, Toast.LENGTH_SHORT).show();
        new ReadText(value).run();
    }

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
        return listSteps.get(pos).getText();
    }

}
