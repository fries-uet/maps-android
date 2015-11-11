package fries.com.googlemaps;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by tmq on 10/10/15.
 */
public class ReadTextStream extends Thread{
//    private Context mContext;
    private String url;
    private MediaPlayer player;

    public ReadTextStream(String text){
//        mContext = c;
        String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        String urlEncoded = Uri.encode(text, ALLOWED_URI_CHARS);
        this.url = "http://118.69.135.22/synthesis/file?voiceType=female&text=" + urlEncoded;
    }

    @Override
    public void run() {
        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
//            player.prepareAsync();
//            Toast.makeText(mContext, "Play...", Toast.LENGTH_SHORT).show();
            player.start();

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
