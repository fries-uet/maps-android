package fries.com.googlemaps.fries.com.googlemaps.response;

import fries.com.googlemaps.ReadTextStream;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tmq on 11/05/15.
 */
public class ResponseSpeak extends ResponseService{

    private String answer;

//    public ResponseSpeak(){
//        super();
//        answer = "";
//    }

    public ResponseSpeak(JSONObject json){
        super(json);
        try {
            answer = json.getString(TAG_ANSWER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {

    }

    public void speak(){
//        new ReadTextStream(answer).start(); // Neu nhieu cau cung doc se bi ngat giua chung
//        new ReadTextStream(answer).run(); // Giong noi bi doc chong len nhau khi thuc hien nhieu cau noi
        switch (type){
            case TYPE_SPEAK:
                new ReadTextStream(answer).start();     break;
            case TYPE_POST_TRAFFIC:
                new ReadTextStream("Cám ơn bạn đã phản hồi thông tin giao thông");
                break;
        }
    }
}
