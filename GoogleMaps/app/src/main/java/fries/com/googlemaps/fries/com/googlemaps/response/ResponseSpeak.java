package fries.com.googlemaps.fries.com.googlemaps.response;

import fries.com.googlemaps.ReadTextStream;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tmq on 11/05/15.
 */
public class ResponseSpeak extends ResponseService{
    private String answer;
    private String myLocation;

    public ResponseSpeak(JSONObject json){
        super(json);
    }

    @Override
    void analyzeJson(JSONObject json) throws JSONException {
        if (type.equals(TYPE_SPEAK)){
            answer = json.getString(TAG_ANSWER);
        }else if (type.equals(TYPE_MY_LOCATION)){
            myLocation = json.getString("address_formatted");
        }
    }

    public void speak(){
//        new ReadTextStream(answer).start(); // Neu nhieu cau cung doc se bi ngat giua chung
//        new ReadTextStream(answer).run(); // Giong noi bi doc chong len nhau khi thuc hien nhieu cau noi
        switch (type){
            case TYPE_SPEAK:
                new ReadTextStream(answer).start();
                break;
            case TYPE_MY_LOCATION:
                if (myLocation!=null) new ReadTextStream("Bạn đang ở " + myLocation).start();
                break;
            case TYPE_POST_TRAFFIC:
                new ReadTextStream("Cám ơn bạn đã phản hồi thông tin giao thông").start();
                break;
        }

    }
}
