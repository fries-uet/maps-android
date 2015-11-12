package fries.com.googlemaps;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.google.android.gms.internal.zzid.*;

/**
 * Created by TooNies1810 on 11/11/15.
 */
public class MediaManager {
    private static final String TAG = "MediaManager";
    private MediaPlayer media = new MediaPlayer();
    private ArrayList<String> listSong = new ArrayList<>();
    private ArrayList<String> listPath = new ArrayList<>();
    private Context mContext;

    public MediaManager(Context context) {
        mContext = context;

//        media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.i(TAG, "Noi xong!");
//                media.reset();
//                if (listSong.size() > 0) {
//                    speak(listSong.get(0));
//                }
//            }
//        });

        media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                File file = new File(listPath.get(0));
//                file.delete();
                listPath.remove(0);
                Log.i(TAG, "Noi xong!");
                media.reset();
                if (listPath.size() > 0) {
                    speak(listPath.get(0));
                }
            }
        });
    }

//    private void speak(String text) {
//        if (media.isPlaying()) {
//            return;
//        }
//
//        try {
//            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
//            String urlEncoded = Uri.encode(text, ALLOWED_URI_CHARS);
//            String urlMedia = "http://118.69.135.22/synthesis/file?voiceType=female&text=" + urlEncoded;
//            media.setDataSource(urlMedia);
//            media.prepare();
//            media.start();
//            listSong.remove(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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

//    public void addToList(String text) {
//        listSong.add(text);
//        speak(text);
//    }


    public void addToList(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFileFromText(text);
            }
        }).start();

//        downloadFileFromText(text);
    }

    private void speak(final String path) {
        if (media.isPlaying()) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Path play: " + path);
                    media.setDataSource(path);
                    media.prepare();
                    media.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //download file and read
    ////////////////////////////////////////

    private String mHost = "http://118.69.135.22";

    public static final String PATH_DOWNLOAD_AUDIO = Environment.getExternalStorageDirectory().getPath() +
            "/" + Environment.DIRECTORY_MUSIC + "/";

    public static final String FILE_NAME_DEFAULT = "audio_map";
    public static final String FILE_EXTENTION_DEFAULT = ".wav";

    public void downloadFileFromText(String text) {
        String mUrl = mHost + "/synthesis/file?voiceType=\"female\"&text=\"" + URLEncoder.encode(text) + "\"";
        String filePath = PATH_DOWNLOAD_AUDIO + FILE_NAME_DEFAULT + FILE_EXTENTION_DEFAULT;
        try {
            URL url = new URL(mUrl);
            Log.i(TAG, "Download URL: " + url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("accept-charset", "UTF-8");
            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.i(TAG, "Connected to server'");

            InputStream inputStream = urlConnection.getInputStream();
            File file = new File(filePath);
//            Log.i(TAG, "Download path: " + filePath);
            int count = 0;
            while (file.exists()) {
                count++;
                String fileName = FILE_NAME_DEFAULT + count;
                filePath = PATH_DOWNLOAD_AUDIO + fileName + FILE_EXTENTION_DEFAULT;
                file = new File(filePath);
            }
            Log.i(TAG, "Download path: " + filePath);
            file.createNewFile();
            FileOutputStream fileOutput = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            Log.i(TAG, "Download file thanh cong: " + file.getAbsolutePath());


            // nhet file.getAbsolutePath() vao hang doi
//            speakVi(file.getAbsolutePath());
            listPath.add(file.getAbsolutePath());
            speak(file.getAbsolutePath());

            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}