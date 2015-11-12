package fries.com.googlemaps;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.*;
import java.net.*;

/**
 * Created by tmq on 11/05/15.
 */
public class ReadTextDownload{
    public final int stateMP_Error = 0;
    public final int stateMP_NotStarter = 1;
    public int stateMediaPlayer;
    public MediaPlayer mediaPlayer;

//    private String text;
    private Context mContext;

    private static final String mHostTTS = "http://118.69.135.22";
    private static final String TAG = "ReadTextDownload";

    public void speakText(Context context, final String text){
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                speakTTS(text);
            }
        }).run();
    }

    private void speakTTS(String msg) {
//        String URL = mHostTTS + "/synthesis/file?voiceType=\"female\"&text=\"" + URLEncoder.encode(msg) + "\"";

        String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        String urlEncoded = Uri.encode(msg, ALLOWED_URI_CHARS);
        String URL = "http://118.69.135.22/synthesis/file?voiceType=female&text=" + urlEncoded;
        Logger.i(mContext, TAG, "Da nhan text");
        downloadFile(URL, "sdcard/sound.wav");
    }

    private void downloadFile(final String sURL, final String filePath) {
        try {

//            URL url = new URL(sURL);
//            Logger.e(mContext, TAG, "Download URL: " + url.toString());
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setRequestProperty("accept-charset", "UTF-8");
//            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");
//            urlConnection.setDoOutput(true);
//            urlConnection.connect();
//
//            InputStream inputStream = urlConnection.getInputStream();

            URL url = new URL(sURL);
            URLConnection conection = url.openConnection();
            conection.connect();
            int lenghtOfFile = conection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(filePath);

            final File file = new File(filePath);
//            FileOutputStream fileOutput = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//            int bufferLength = 0;
//            while ((bufferLength = inputStream.read(buffer)) > 0) {
//                fileOutput.write(buffer, 0, bufferLength);
//            }
//            Logger.i(mContext, TAG, "Ghi file vao bo nho thanh cong");
//            speakVi(file.getAbsolutePath());
//            fileOutput.close();

            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = input.read(buffer)) > 0) {
                output.write(buffer, 0, bufferLength);
            }
            Logger.i(mContext, TAG, "Ghi file vao bo nho thanh cong");
            speakVi(file.getAbsolutePath());
            output.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void speakVi(final String filePath) {
        initMediaPlayer(filePath);
        mediaPlayer.start();
    }

    private void initMediaPlayer(String path) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            stateMediaPlayer = stateMP_NotStarter;
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
            stateMediaPlayer = stateMP_Error;
        }
    }
}
