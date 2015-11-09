package fries.com.googlemaps.fries.com.googlemaps.response;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tmq on 11/05/15.
 */
public abstract class ResponseService {
    // TAG Component of Response
    public static final String TAG_STATUS  = "status";
    public static final String TAG_TYPE    = "type";
    public static final String TAG_QUESTION= "question";
    public static final String TAG_ANSWER  = "answer";

    // TYPE of Response
    public static final String TYPE_SPEAK           = "speak";
    public static final String TYPE_GET_TRAFFIC     = "get_traffic";
    public static final String TYPE_POST_TRAFFIC    = "post_traffic";
    public static final String TYPE_MY_LOCATION     = "my_location";
    public static final String TYPE_DIRECTION                       = "direction";
    public static final String TYPE_DIRECTION_COORDINATE_TO_TEXT    = "coor_text";
    public static final String TYPE_DIRECTION_TEXT_TO_TEXT          = "text_text";

    protected Context mContext;
    protected String type;

//    public ResponseService(){
//        type = "";
//    }

    public ResponseService(Context context, JSONObject json){
        try {
            mContext = context;
            this.type = json.getString(TAG_TYPE);
            analyzeJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ------------------------- GET -----------------------------------
    public String getType(){
        return type;
    }
    // ------------------------ Abstract -------------------------------
    abstract void analyzeJson(JSONObject json) throws JSONException;
}
