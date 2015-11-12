package fries.com.googlemaps.fries.com.googlemaps.response;

import android.content.Context;
import android.util.Log;
import fries.com.googlemaps.MediaManager;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tmq on 11/06/15.
 */
public class ResponseGetTraffic extends ResponseService{
    private static final String TAG = "ResponseGetTraffic";
    private static final String TYPE_CONGESTION = "congestion";
    private static final String TYPE_OPEN       = "open";

    private boolean dataIsInDatabase;
    private String typeTraffic;
    private String name;
    private String agoText;

    public ResponseGetTraffic(Context context, JSONObject json) {
        super(context, json);

    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {
        if (json.getInt("result") == 0){
            dataIsInDatabase = false;
            return;
        }

        dataIsInDatabase = true;
        JSONObject data = json.getJSONObject("data");

        typeTraffic = data.getString("type");
        name = data.getString("name");
        agoText = data.getString("ago_text");
    }

    public void speak(MediaManager mediaManager){
        String text = "";
        if (!dataIsInDatabase) text = "Không có thông tin về tuyến đường này, mong bạn thông cảm";
        else {
            if (typeTraffic.equals(TYPE_CONGESTION))    text = name + " đang bị tắc đường, theo thông tin cách đây " + agoText;
            else    text = name + " đang lưu thông bình thường";
        }
        Log.i(TAG, text);
        mediaManager.addToList(text);
    }

}
