package com.example.map_uet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements OnClickListener {
    private static final int KEY_CODE_RECOGNIZER_ACTIVITY = 1824;
    /**
     * Called when the activity is first created.
     */
    private Button btnClick, btnClear;
    private ListView lvChatMain;
    private ListViewChatAdapter mAdapter;
    private String TAG = "MainActivity";
    private String mToken = "775ced42-8100-48ef-add1-a7cc6be261ab";
    private String mBotId = "562677f5e4b07d327ad8358f";
    private String mHostAIML = "http://118.69.135.27";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initViews();
    }

    private void initViews() {
        btnClick = (Button) findViewById(R.id.btn_click);
        btnClear = (Button) findViewById(R.id.btn_clear);
        lvChatMain = (ListView) findViewById(R.id.lv_chatmain);

        mAdapter = new ListViewChatAdapter(this);
        lvChatMain.setAdapter(mAdapter);
        btnClick.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_click:
//                addTextToConversation("ok",true);
//                addTextToConversation("no",false);

                //Nhan dang cau noi vua nhan vao -> text
//                startRecognizerIntent();

//                demo
                final String result = "hi";
                addTextToConversation(result, true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!result.equals("")) {
                            getAnswer(result);
                        }
                    }
                }).start();

                break;

            case R.id.btn_clear:
                mAdapter.clearData();
                break;
        }
    }

    private void addTextToConversation(String text, boolean type) {
        mAdapter.addItem(text, type);
    }

    private void startRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "vi");
        try {
            startActivityForResult(intent, KEY_CODE_RECOGNIZER_ACTIVITY);
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Khong ho tro STT", Toast.LENGTH_SHORT).show();
            a.printStackTrace();
        }
    }

    //Lay cau text tra ve cua nguoi noi
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case KEY_CODE_RECOGNIZER_ACTIVITY: {
                if (resultCode == -1 && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // Result text from GG
                    final String result = text.get(0);

                    //add My text to conversation
                    addTextToConversation(result, true);

                    //Get answer of Bot
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!result.equals("")) {
                                getAnswer(result);
                            }
                        }
                    }).start();

                }
                break;
            }
        }
    }

    private String getBotChatApi(final String message) {
        try {
            return new StringBuilder().append(mHostAIML).append("/AIML/api/bots/")
                    .append(mBotId).append("/chat?request=").append(URLEncoder.encode(message, "UTF-8"))
                    .append("&token=").append(mToken).toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getAnswer(String question) {
        String s = getBotChatApi(question);
        if (s == null) return;
        Log.i(TAG, s);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Method.GET,
                s, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this, response.toString(),
                                Toast.LENGTH_LONG).show();
                        Log.i(TAG, response.toString());
                        int start = response.toString().indexOf("response") + 11;
                        int end = response.toString().indexOf("botname") - 3;
                        final String botAnswer = response.toString().substring(start,end);
                        addTextToConversation(botAnswer,false);
//                        speakTTS(botAnswer);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                speakTTS(botAnswer);
                            }
                        }).start();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage() + "");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        if (SmacApplication.getInstance() != null)
        {
            SmacApplication.getInstance().addToRequestQueue(jsonObjRequest, "jsonobject_request");
        } else {
            Log.i(TAG, "SmacApplication is null");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // TTS

    public void stopSpeakVi() {
        mediaPlayer.stop();
    }

    private String mHostTTS = "http://118.69.135.22"; //118.69.135.22

    @SuppressWarnings("deprecation")
    public void speakTTS(String msg) {
        String URL = mHostTTS + "/synthesis/file?voiceType=\"female\"&text=\"" + URLEncoder.encode(msg) + "\"";
        Log.i(TAG, "Da nhan text");
        downloadFile(URL, "sdcard/sound.wav");
    }
    public void speakVi(final String filePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initMediaPlayer(filePath);
                mediaPlayer.start();
            }
        });
    }
    public void downloadFile(final String sURL, final String filePath) {
        try {
            URL url = new URL(sURL);
            Log.e(TAG, "Download URL: " + url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("accept-charset", "UTF-8");
            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            final File file = new File(filePath);
            FileOutputStream fileOutput = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            Log.i(TAG, "Ghi file vao bo nho thanh cong");
            speakVi(file.getAbsolutePath());
            fileOutput.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int stateMediaPlayer;
    public final int stateMP_Error = 0;
    public final int stateMP_NotStarter = 1;
    public MediaPlayer mediaPlayer;
    public void initMediaPlayer(String path) {
        String PATH_TO_FILE = path;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(PATH_TO_FILE);
            mediaPlayer.prepare();
            stateMediaPlayer = stateMP_NotStarter;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            stateMediaPlayer = stateMP_Error;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            stateMediaPlayer = stateMP_Error;
        } catch (IOException e) {
            e.printStackTrace();
            stateMediaPlayer = stateMP_Error;
        }
    }
}




