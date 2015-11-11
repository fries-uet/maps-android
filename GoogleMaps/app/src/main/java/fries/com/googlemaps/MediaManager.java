package fries.com.googlemaps;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by TooNies1810 on 11/11/15.
 */
public class MediaManager {
    private static final String TAG = "MediaManager";
    private MediaPlayer media = new MediaPlayer();
    private ArrayList<String> listSong = new ArrayList<>();
    private Context mContext;

    public MediaManager(Context context) {
        mContext=context;
        media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "Noi xong!");
                media.reset();
                if (listSong.size() > 0){
//                    speak(listSong.get(0));
                    speakFromURI(Uri.parse(listSong.get(0)));
//                    listSong.remove(0);
                }
            }
        });
    }

    private void speak(String path){
        if (media.isPlaying()){
            return;
        }

        try {
            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
            media.setDataSource(path);
            media.prepare();
            media.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void speakFromURI(Uri uri){
        if (media.isPlaying()){
            return;
        }

        try {
            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
            media.setDataSource(mContext, uri);
            media.prepare();
            media.start();
            listSong.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToList(String path){
        listSong.add(path);
//        speak(path);
        speakFromURI(Uri.parse(path));
//        listSong.remove(0);
    }


}