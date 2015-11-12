package fries.com.googlemaps;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
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
        mContext = context;

        media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "Noi xong!");
            media.reset();
            if (listSong.size() > 0) {
                speak(listSong.get(0));
            }
            }
        });
    }

    private void speak(String text) {
        if (media.isPlaying()) {
            return;
        }

        try {
            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
            String urlEncoded = Uri.encode(text, ALLOWED_URI_CHARS);
            String urlMedia = "http://118.69.135.22/synthesis/file?voiceType=female&text=" + urlEncoded;
            media.setDataSource(urlMedia);
            media.prepare();
            media.start();
            listSong.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void speakFromURI(Uri uri) {
//        if (media.isPlaying()) {
//            return;
//        }
//
//        try {
//            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            media.setDataSource(mContext, uri);
//            media.prepare();
//            media.start();
//            listSong.remove(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void addToList(String text) {
        listSong.add(text);
        speak(text);
    }


}