package fries.com.googlemaps;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.*;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.android.volley.*;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import fries.com.googlemaps.fries.com.googlemaps.response.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
//        LocationSource.OnLocationChangedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener{

    private static final String TAG = "MapsActivity";
    private static final int KEY_CODE_RECOGNIZER_ACTIVITY = 1824;
    private static final int REQUEST_PLACE_PICKER = 1111;

    private GoogleMap mMap;

    // MediaManager tao mot queue de dong bo viec doc thong bao
    private MediaManager mediaMgr;

    private FloatingActionButton    fabRecordVoice;
    private LinearLayout    notificationDirection;
    private TextView        txtOrigin,
                            txtDestination,
                            txtDistance,
                            txtDuration;
    private ListView    listStep;
//    private ListStepsAdapter listStepsAdapter;
    private ImageButton btnCancelDirection,
                        btnAcceptDirection;


    private ResponseDirection direction;

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

        checkLocationEnable();
        mediaMgr = new MediaManager(this);
    }

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


        // ------------------------------ Tam thoi tat che do clik tren map----------------------------------------------------------------------------------------------------
        mMap.setOnMapClickListener(this);
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (direction==null || !direction.isDirecting) return;

                direction.checkLocationAndSpeakDirection(location);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                mMap.animateCamera(cameraUpdate);

            }
        });


        setLanguage();

        initViews();

        initViewsVoiceText();

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

        notificationDirection   = (LinearLayout) findViewById(R.id.notificationDirection);
        txtOrigin       = (TextView) findViewById(R.id.txtOrigin);
        txtDestination  = (TextView) findViewById(R.id.txtDestination);
        txtDistance     = (TextView) findViewById(R.id.txtDistance);
        txtDuration     = (TextView) findViewById(R.id.txtDuration);

//        listStepsAdapter = new ListStepsAdapter(this);
        listStep        = (ListView) findViewById(R.id.listSteps);

        btnCancelDirection  = (ImageButton) findViewById(R.id.btnCancelDirection);
        btnAcceptDirection  = (ImageButton) findViewById(R.id.btnAcceptDirection);


        // SetOnClick
        fabRecordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecognizerIntent();
            }
        });

//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()){
//                    case R.id.btnCancelDirection:
//                        notificationDirection.setVisibility(View.GONE);
//                        Animation myAni1 = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.anim_slide_out_top);
//                        notificationDirection.startAnimation(myAni1);
//                        //resetDirection
//
//                        break;
//                    case R.id.btnAcceptDirection:
//                        stepsDirection.startDirecting();    // Bat dau chi duong bang giong noi, voi du lieu da duoc nap san vao stepsDirection
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getMyLocationLatLng(),16));
//                        break;
//                }
//            }
//        };
//        btnAcceptDirection.setOnClickListener(onClickListener);
//        btnCancelDirection.setOnClickListener(onClickListener);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (checkLocationEnable())
        getMyAddress(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude());
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        mMap.clear();

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        StringBuilder address = getMyAddress(lat, lng);
        Toast.makeText(this, address, Toast.LENGTH_LONG).show();
    }


    //----------------------------------- Get -------------------------------------------------------------------------

    private LatLng getMyLocationLatLng(){
        checkLocationEnable();
        Location location = mMap.getMyLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private StringBuilder getMyAddress(double lat, double lng) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Logger.i(this, TAG, addresses.size() + "");
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    result.append(address.getAddressLine(i)).append(" ");
                }
                Logger.i(this, TAG, "LATITUDE: " + lat);
                Logger.i(this, TAG, "LONGITUDE: " + lng);
            }
        } catch (IOException e) {
            Logger.e(this, TAG, e.getMessage());
        }
        Toast.makeText(MapsActivity.this, "" + result, Toast.LENGTH_SHORT).show();
        return result;
    }

    private String getCurrentCity() {
        String result = "";
        Location location = mMap.getMyLocation();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                int size = address.getMaxAddressLineIndex();
                if (size>=1) result = address.getAddressLine(size-1);
                else result = address.getAddressLine(0);
                Logger.i(this, TAG, "City: " + result);
            }
        } catch (IOException e) {
            Logger.e(this, TAG, e.getMessage());
        }
        return result;
    }


//    private void showNotificationDirection(){//--------------------------------------------------------- Chu y ------------------------------------------------
//        // Set Layout
//        notificationDirection.setVisibility(View.VISIBLE);
//        Animation myAni2 = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.anim_slide_in_top);
//        notificationDirection.startAnimation(myAni2);
//
//        notificationDirection.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mMap.setPadding(0,notificationDirection.getHeight(),0,0);
//                Logger.i(MapsActivity.this, TAG, "Set padding for Map: top = " + notificationDirection.getHeight());
//            }
//        }, 2000);
//
//        // Set Component
//        txtOrigin.setText(stepsDirection.getOrigin());
//        txtDestination.setText(stepsDirection.getDestination());
//        txtDistance.setText(stepsDirection.getDistance());
//        txtDuration.setText(stepsDirection.getDuration());
//
//        listStepsAdapter.setListSteps(stepsDirection.getListSteps());
//        listStep.setAdapter(listStepsAdapter);
//    }

    //------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {


        switch (requestCode) {
            case KEY_CODE_RECOGNIZER_ACTIVITY: {
                if (resultCode == -1 && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // Result text from GG
                    final String result = text.get(0);
                    sendRequest(result);
                }
                break;
            }

            case REQUEST_PLACE_PICKER:          // Pick Location
                if (resultCode == Activity.RESULT_OK){}
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "vi-vn");
        try {
            startActivityForResult(intent, KEY_CODE_RECOGNIZER_ACTIVITY);
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Khong ho tro STT", Toast.LENGTH_SHORT).show();
            a.printStackTrace();
        }
    }


    // ---------------- new Code ------------------------------------------------------------------------

    private static final String URL_REQUEST = "http://tutran.net/v2/bot/chat";

    private void sendRequest(final String request){
        Log.i(TAG, "send: " + request);
        Location location = mMap.getMyLocation();
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("question", request);
            jsonObject.put("my_latitude", location.getLatitude());
            jsonObject.put("my_longitude", location.getLongitude());
            jsonObject.put("city", getCurrentCity());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, URL_REQUEST, jsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(MapsActivity.this, "Response: " + response.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Response: " + response.toString());

                try {
                    if (!response.getString(ResponseService.TAG_STATUS).equalsIgnoreCase("ok")){
                        Toast.makeText(MapsActivity.this, "Response is Error!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switch (response.getString(ResponseService.TAG_TYPE)){
                        case ResponseService.TYPE_SPEAK:
                        case ResponseService.TYPE_POST_TRAFFIC:
                        case ResponseService.TYPE_MY_LOCATION:
                            ResponseSpeak speak = new ResponseSpeak(MapsActivity.this, response);
                            speak.speak(mediaMgr);
                            break;
                        case ResponseService.TYPE_GET_TRAFFIC:
                            ResponseGetTraffic getTraffic = new ResponseGetTraffic(MapsActivity.this, response);
                            getTraffic.speak(mediaMgr);
                            break;
                        case ResponseService.TYPE_DIRECTION:
                        case ResponseService.TYPE_DIRECTION_COORDINATE_TO_TEXT:
                        case ResponseService.TYPE_DIRECTION_TEXT_TO_TEXT:
                            mMap.clear();
                            direction = new ResponseDirection(MapsActivity.this, response, mediaMgr);
                            direction.isDirecting = true;
                            direction.speakInformation();
                            mMap.addPolyline(direction.getPolylineOptions());
                            for (AddressMap point : direction.getWaypoints()){
                                mMap.addMarker(new MarkerOptions()
                                        .position(point.getLatLng())
                                        .title(point.getFullName())
                                        .flat(true));
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Logger.i(MapsActivity.this, TAG, "JsonException: GetString");
                }
            }
        },  new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "Error Response");
                }
            }
        );

        if (SmacApplication.getInstance() != null) {
            SmacApplication.getInstance().addToRequestQueue(jsObjRequest, "jsonobject_request");
        } else {
            Logger.i(MapsActivity.this, TAG, "SmacApplication is null");
        }

        Log.i(TAG, "Finish");
    }


    // nhap cau noi bang text
    ///////////////////////////////////////////
    private EditText edtVoice;
    private Button btnVoice;

    private void initViewsVoiceText(){
        edtVoice = (EditText) findViewById(R.id.edt_voice);
        btnVoice = (Button) findViewById(R.id.btn_speak);

        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edtVoice.getText().toString();

                sendRequest(text);
            }
        });
    }

}
