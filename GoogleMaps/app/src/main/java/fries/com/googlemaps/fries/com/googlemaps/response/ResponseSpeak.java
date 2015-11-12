package fries.com.googlemaps.fries.com.googlemaps.response;

import android.content.Context;
import fries.com.googlemaps.MediaManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tmq on 11/05/15.
 */
public class ResponseSpeak extends ResponseService{
    private String answer;
    private String myLocation;

    public ResponseSpeak(Context context, JSONObject json){
        super(context, json);
    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {
        if (type.equals(TYPE_SPEAK)){
            answer = json.getString(TAG_ANSWER);
        }else if (type.equals(TYPE_MY_LOCATION)){
            myLocation = json.getString("address_formatted");
        }
    }

    public void speak(MediaManager mediaMgr){
        switch (type){
            case TYPE_SPEAK:
                mediaMgr.addToList(answer);
                break;
            case TYPE_MY_LOCATION:
                if (myLocation!=null) mediaMgr.addToList("Bạn đang ở " + myLocation);
                break;
            case TYPE_POST_TRAFFIC:
                mediaMgr.addToList("Cám ơn bạn đã phản hồi thông tin giao thông");
                break;
        }

    }
}
