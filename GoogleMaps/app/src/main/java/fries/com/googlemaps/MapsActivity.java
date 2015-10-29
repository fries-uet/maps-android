package fries.com.googlemaps;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.*;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationSource.OnLocationChangedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener{

    private static final String TAG_NOTIFICATION_CONGESTION = "tacduong";
    private static final String TAG_NOTIFICATION_OPEN = "hettacduong";
    private GoogleMap mMap;
    private static String TAG = "MapsActivity";

    private static final int REQUEST_PLACE_PICKER = 1111;

    private static String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";    // Encode URL

    // API: TEXT - TEXT
    private static String PRE_URL_1 = "http://tutran.net/v1/direction/byText/";
    private static String ORIGIN_URL_1 = "origin=";
    private static String DESTINATION_URL_1 = "&destination=";

    // API: LATLNG - LATLNG
    // http://tutran.net/v1/direction/byCoordinates/origin=21.033205,105.745402&destination=21.033205,105.758126
    private static String PRE_URL = "http://tutran.net/v1/direction/byCoordinates/";
    private static String ORIGIN_URL = "origin=";
    private static String DESTINATION_URL = "&destination=";

    // API: LATLNG - TEXT
    // http://tutran.net/v1/direction/byMixed/origin=21.033196,105.745296&destination=Quan%20Hoa,%20C%E1%BA%A7u%20Gi%E1%BA%A5y
    private static String PRE_URL_3 = "http://tutran.net/v1/direction/byMixed/";
    private static String ORIGIN_URL_3 = "origin=";
    private static String DESTINATION_URL_3 = "&destination=";

    // API: Thong bao tac duong
    //http://tutran.net/v1/traffic/postStatus/open/location=21.036276,105.761516
    private static String PRE_URL_4 = "http://tutran.net/v1/traffic/postStatus/";
    private static String LOCATION_URL = "location=";

    private ArrayList<LatLng> listLatLng = new ArrayList<>();

    private PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLACK).geodesic(true);

    private LatLng originLatLng = null, destionationLatLng = null;
    private String originName = "",     destionationName = "";


    private FloatingActionButton    fabRecordVoice,
                                    fabPickLocation;

    private StepsDirection stepsDirection = new StepsDirection(MapsActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        View view = mapFragment.getView();
        if (view!=null) view.setVisibility(View.VISIBLE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(MapsActivity.this);
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }


        mMap.setOnMapClickListener(this);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
//                mMap.animateCamera(cameraUpdate);
                stepsDirection.checkLocationAndSpeak(currentLatLng);
            }
        });

        setLanguage();

        initViews();

        // Move to Ha Noi
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(21.0201531, 105.7988345))
                .zoom(12)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }


    //-------------------------- Setting -------------------------------------------------------------------
    private boolean checkLocationEnable(){
        final boolean[] result = {false};
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Location is Unable!");
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MapsActivity.this, "Location is Unable", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    mMap.setOnMyLocationButtonClickListener(MapsActivity.this);
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                    result[0] = true;
                }
            });
            dialog.show();
        }
        return result[0];
    }

    private void setLanguage() {
        String languageToLoad = "vi_VN";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());
    }

    private void initViews(){
        fabRecordVoice      = (FloatingActionButton) findViewById(R.id.fabRecordVoice);
        fabPickLocation     = (FloatingActionButton) findViewById(R.id.fabPickLocation);

        // SetOnClick
        fabRecordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "vi-vn");
                try {
                    startActivityForResult(intent, KEY_CODE_RECOGNIZER_ACTIVITY);
                } catch (Exception a) {
                    Toast.makeText(MapsActivity.this, "Khong ho tro STT", Toast.LENGTH_SHORT).show();
                    a.printStackTrace();
                }
            }
        });
        fabPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickButtonClick(v);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(this, "Location Changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (checkLocationEnable())
            getMyAddress();
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        mMap.clear();

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        StringBuilder address = getMyAddress(lat, lng);
//        Toast.makeText(this, "Location = " + address, Toast.LENGTH_LONG).show();

        if (originLatLng == null){                                      // First Click
            // Clear MArker and Polyline
            mMap.clear();
//            if (polylineFinal!=null) polylineFinal.remove();

            originLatLng    = latLng;
            originName      = address.toString();

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(originName)
                    .flat(true));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(latLng)
                    .zoom(15)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            listLatLng.add(latLng);

        }else if (destionationLatLng == null){                          // Second Click
            destionationLatLng  = latLng;
            destionationName    = address.toString();

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(destionationName)
                    .flat(true));

//            listLatLng.add(latLng);
//            drawPolyLines();
            new getData().execute(getUrlFromOriginAndDestionation());
            speakText("Đi từ " + originName + " đến, " + destionationName);
        }

    }



    //----------------------------------- Get -------------------------------------------------------------------------

    private void getMyAddress() {
        Location mLocation = mMap.getMyLocation();

        double lat = mLocation.getLatitude();
        double lng = mLocation.getLongitude();

        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 10);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.i(TAG, addresses.size() + "");
//                Toast.makeText(this, addresses.size() + "", Toast.LENGTH_LONG).show();
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    result.append(address.getAddressLine(i)).append("\n");
                }
                Log.i(TAG, "LATITUDE: " + lat);
                Log.i(TAG, "LONGITUDE: " + lng);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Toast.makeText(MapsActivity.this, "" + result, Toast.LENGTH_SHORT).show();
    }

    private StringBuilder getMyAddress(double lat, double lng) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.i(TAG, addresses.size() + "");
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    result.append(address.getAddressLine(i)).append(" ");
                }
                Log.i(TAG, "LATITUDE: " + lat);
                Log.i(TAG, "LONGITUDE: " + lng);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        Toast.makeText(MapsActivity.this, "" + result, Toast.LENGTH_SHORT).show();
        return result;
    }



    private String getUrlFromOriginAndDestionation(){
        String url;

        url =   PRE_URL +
                ORIGIN_URL +
                originLatLng.latitude + "," + originLatLng.longitude +
                DESTINATION_URL +
                destionationLatLng.latitude + "," + destionationLatLng.longitude;

        Toast.makeText(this, "url = " + url, Toast.LENGTH_SHORT).show();
        return url;
    }

    private void drawPolyLines(){
        Toast.makeText(this, "Size = " + listLatLng.size(), Toast.LENGTH_SHORT).show();
        if (listLatLng.size()<=0) return;

        for (LatLng i: listLatLng) {
            polylineOptions.add(i);
        }
        mMap.addPolyline(polylineOptions);
    }

    private void deleteMarkerAndPolyline(){
        mMap.clear();
        polylineOptions = new PolylineOptions().color(Color.BLACK).geodesic(true);
    }




    //------------------------------------- AsynTask ------------------------------------------------------------------------

    public class getData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {
            if (args[0]==null || args[0].equals("")) {
                Toast.makeText(MapsActivity.this, "url is fail", Toast.LENGTH_SHORT).show();
                return "";
            }

//            String u = args[0];
//            Log.i(TAG, u);
//            Toast.makeText(getBaseContext(), "url = " + args[0], Toast.LENGTH_SHORT).show();

            Log.i(TAG, "args[0] = " + args[0]);

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
//                Toast.makeText(getBaseContext(), "res = " + urlConnection.getResponseCode(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "response= " + urlConnection.getResponseCode());
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
                Log.i(TAG, "No data in doInBackGround from URL");
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("")){
                Toast.makeText(MapsActivity.this, "Cannot receive data from server!" + result, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject json = new JSONObject(result);

                String status = json.getString("status");
                if (!status.equals("OK")) {
                    Log.i(TAG,"Direction is NULL");
                    return;
                }

                if (json.getString("type").equalsIgnoreCase("coordinates")){
                    // New Polyline
                    polylineOptions = new PolylineOptions().color(Color.BLACK).geodesic(true);

                    JSONObject origin = json.getJSONObject("origin");
                    JSONObject destination = json.getJSONObject("destination");
                    Toast.makeText(MapsActivity.this,
                            origin.getString("text") +
                                    " ----->> " +
                                    destination.getString("text")
                            , Toast.LENGTH_LONG
                    ).show();

                    JSONObject info = json.getJSONObject("info");
                    Toast.makeText(MapsActivity.this,
                            "info:\nsummary = " + info.getString("summary") +
                                    "\n distance = " + info.getString("distance") +
                                    "\n duration = " + info.getString("duration")
                            , Toast.LENGTH_LONG
                    ).show();

                    drawMarkerOriginAndDestination(json);

                    JSONArray steps = json.getJSONArray("steps");
                    for (int i=0; i<steps.length(); i++){
                        JSONObject step = steps.getJSONObject(i);
                        JSONObject instructions = step.getJSONObject("instructions");
                        Toast.makeText(MapsActivity.this,
                                "Step " + i +
                                        "\nDistance = " + step.getString("distance") +
                                        "\nManeuver = " + step.getString("maneuver") +
                                        "\nText = "     + instructions.getString("text"),
                                Toast.LENGTH_LONG
                        ).show();

                        drawPolyLineDirection(instructions.getString("text"), step.getJSONArray("polyline"));
                    }
//                polylineFinal =
                    mMap.addPolyline(polylineOptions);
                }

                if (json.getString("type").equalsIgnoreCase("post_traffic")){
                    Toast.makeText(MapsActivity.this, "Da thong bao trang thai thanh cong", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "Cannot convert Result to Json");
            }

        }

        private void drawPolyLineDirection(String text, JSONArray array) throws JSONException{
            if (array.length()<=0) return;
            for (int i=0; i<array.length(); i++){
                JSONObject point = array.getJSONObject(i);
                LatLng latLngPoint = new LatLng(point.getDouble("lat"), point.getDouble("lng"));
                polylineOptions.add(latLngPoint);
            }
            JSONObject point = array.getJSONObject(array.length()-1);
            LatLng latLngPoint = new LatLng(point.getDouble("lat"), point.getDouble("lng"));
            stepsDirection.addStepLatLng(new Step(latLngPoint, text));
        }

        private void drawMarkerOriginAndDestination(JSONObject json) throws JSONException{
            String typeOfSourceData = json.getString("type");
            if (typeOfSourceData.equals("coordinates")){
//                return;
            }

            // Clear marker
            mMap.clear();
            // Origin
            JSONObject origin = json.getJSONObject("origin");
            JSONObject geo = origin.getJSONObject("geo");
            String name = origin.getString("text");
            LatLng latLng = new LatLng(geo.getDouble("lat"), geo.getDouble("lng"));
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(name)
                    .flat(true));

            // Destination
            origin = json.getJSONObject("destination");
            geo = origin.getJSONObject("geo");
            name = origin.getString("text");
            latLng = new LatLng(geo.getDouble("lat"), geo.getDouble("lng"));
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(name)
                    .flat(true));
        }

    }

    //------------------------------------------------------------------------------------------
    public void onPickButtonClick(View v) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(MapsActivity.this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            // ...
        } catch (GooglePlayServicesNotAvailableException e) {
            // ...
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {


        switch (requestCode) {
            case KEY_CODE_RECOGNIZER_ACTIVITY: {
                if (resultCode == -1 && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // Result text from GG
                    final String result = text.get(0);

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

            case REQUEST_PLACE_PICKER:          // Pick Location
                if (resultCode == Activity.RESULT_OK){
                    // The user has selected a place. Extract the name and address.
                    final Place place = PlacePicker.getPlace(data, this);

                    final CharSequence name = place.getName();
                    final CharSequence address = place.getAddress();
                    String attributions = PlacePicker.getAttributions(data);
                    if (attributions == null) {
                        attributions = "";
                    }
        //            mViewName.setText(name);
        //            mViewAddress.setText(address);
        //            mViewAttributions.setText(Html.fromHtml(attributions));
                    Toast.makeText(this, "Name = " + name + "\nAddress = " + "\nAttributions = " + Html.fromHtml(attributions), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //-------------------------------------------------- MInh
    // cho----------------------------------
    private static final int KEY_CODE_RECOGNIZER_ACTIVITY = 1824;
    public final int stateMP_Error = 0;
    public final int stateMP_NotStarter = 1;
    public int stateMediaPlayer;
    public MediaPlayer mediaPlayer;

    private String mToken = "775ced42-8100-48ef-add1-a7cc6be261ab";
    private String mBotId = "562fa5e6e4b07d327ad85788";
    private String mHostAIML = "http://118.69.135.27";
    private String mHostTTS = "http://118.69.135.22";

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

    private String TAG_FIND_ROAD = "abcdxyz";
    public void getAnswer(String question) {
        String s = getBotChatApi(question);
        if (s == null) return;
//        Log.i(TAG, s);
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET,
                s, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MapsActivity.this, response.toString(),
                                    Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Response JSON = " + response.toString());
//                        int start = response.toString().indexOf("response") + 11;
//                        int end = response.toString().indexOf("botname") - 3;
//                        final String botAnswer = response.toString().substring(start,end);
                            try {
                                // lay dia diem (text: day, dia diem)

                                final String botAnswer = response.getString("response");
                                Log.i(TAG, "bot Answer = " + botAnswer);

                                if(botAnswer.contains(TAG_FIND_ROAD)){
                                    findRoadBotChat(botAnswer);
                                } else if (botAnswer.contains(TAG_NOTIFICATION_CONGESTION)){
                                    notificationRoadStatus(TAG_NOTIFICATION_CONGESTION);
                                } else if (botAnswer.contains(TAG_NOTIFICATION_OPEN)){
                                    notificationRoadStatus(TAG_NOTIFICATION_OPEN);
                                }else{
                                    Log.i(TAG, "Khong tim thay dia diem!");
                                    speakText(botAnswer);
                                }
                                // Noi TTS
//                            speakText(botAnswer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
        if (SmacApplication.getInstance() != null) {
            SmacApplication.getInstance().addToRequestQueue(jsonObjRequest, "jsonobject_request");
        } else {
            Log.i(TAG, "SmacApplication is null");
        }
    }

    private void findRoadBotChat(String botAnswer){
        // xu li lo cation
        // abcxyzDay , Cau Giay
        // abcxyzTu liem , Ha Noi
        String start = botAnswer.substring(TAG_FIND_ROAD.length(), botAnswer.indexOf(",") - 1);
        String end = botAnswer.substring(botAnswer.indexOf(start) + start.length() + 3);
        String url = "";
        if (start.equalsIgnoreCase("đây")){
            Location location = mMap.getMyLocation();
            url =  PRE_URL_3 +
                    ORIGIN_URL_3 +
                    location.getLatitude() + "," + location.getLongitude() +
                    DESTINATION_URL_3 +
                    Uri.encode(end, ALLOWED_URI_CHARS);
            speakText("Đi đến " + end);
        }else {
            url =  PRE_URL_1 +
                    ORIGIN_URL_1 +
                    Uri.encode(start, ALLOWED_URI_CHARS) +
                    DESTINATION_URL_1 +
                    Uri.encode(end, ALLOWED_URI_CHARS);
            speakText("Đi từ " + start + " đến " + end);
        }
        Log.i(TAG, "Direction: " + start + " - " + end);
        Log.i(TAG, "url = " + url);
        new getData().execute(url);
        //------------------------------------------------------- Test Dialog-------------------------------------------------------
        AlertDialog.Builder alerDialog = new AlertDialog.Builder(this);
        alerDialog.setTitle("Direction:");
        alerDialog.setMessage(start + " -> " + end);
//        alerDialog.show();
        //--------------------------------------------------
    }

    public void speakText(final String text){
        new Thread(new Runnable() {
            @Override
            public void run() {
                speakTTS(text);
            }
        }).start();
    }

    public void stopSpeakVi() {
        mediaPlayer.stop();
    }

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

    private void notificationRoadStatus(String status){
        Location mLocation = mMap.getMyLocation();

        String url = PRE_URL_4 + status + "/" + LOCATION_URL + mLocation.getLatitude() + "," + mLocation.getLongitude();
        new getData().execute(url);
    }

}
