package com.whispon.map;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ParseException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;

    ArrayList<Double> latList = new ArrayList<Double>();
    ArrayList<Double> lngList = new ArrayList<Double>();
    ArrayList<String> contentList = new ArrayList<String>();
    ArrayList<String> nameList = new ArrayList<String>();
    ArrayList<String> placeList = new ArrayList<String>();
    int indexNum2 = 0;

    private EditText editText;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment =
                ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        mMap = mapFragment.getMap();
       try {

            JSONObject obj = new JSONObject(loadJSONFile("fish.json"));
            JSONArray m_jArry = obj.getJSONArray("Marker");

            for (int i = 0; i < m_jArry.length(); i++)
            {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                double lat_value = jo_inside.getDouble("lat");
                double lng_value = jo_inside.getDouble("lng");
                String content_value = jo_inside.getString("content");
                latList.add(lat_value);
                lngList.add(lng_value);
                contentList.add(content_value);
                //Log.d("コンテント:",contentList.get(i));
            }

        }catch (JSONException e){
           e.printStackTrace();
        }

        //魚の生息地を入力
        try{
            JSONObject obj = new JSONObject(loadJSONFile("fishResponce.json"));
            JSONArray m_jArry = obj.getJSONArray("Marker");

            for(int i = 0; i < m_jArry.length();i++){
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                String name = jo_inside.getString("name");
                String place = jo_inside.getString("place");
                nameList.add(name);
                placeList.add(place);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        //placeListを配列にしてsplitする準備
        final String[] placeArray = placeList.toArray(new String[placeList.size()]);

        editText = (EditText)findViewById(R.id.editText);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                //markerOn();
                MarkerOptions options = new MarkerOptions();
                BitmapDescriptor bMapIcon = BitmapDescriptorFactory.fromResource(R.drawable.fish);
                options.icon(bMapIcon);
                if (nameList.contains(text) == true) {
                    Toast.makeText(MainActivity.this,text + "は存在します。",Toast.LENGTH_SHORT).show();

                    int indexNum = nameList.indexOf(text);
                    //複数ある生息地を分割する
                    String[] placeArray2 = placeArray[indexNum].split(",");
                    for(int i=0;i<placeArray2.length;i++){
                        System.out.println(placeArray2[i]);
                        indexNum2 = contentList.indexOf(placeArray2[i]);
                        if(indexNum2 != -1) {
                            mMap.addMarker(options.position(new LatLng(latList.get(indexNum2), lngList.get(indexNum2))).
                                    title(contentList.get(indexNum2)));
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,text + "は存在しません。",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void markerOn(){
        try {
            MarkerOptions options = new MarkerOptions();
            BitmapDescriptor bMapIcon = BitmapDescriptorFactory.fromResource(R.drawable.fish);
            options.icon(bMapIcon);
            mMap.addMarker(options.position(new LatLng(40, 141)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void onResume() {
        super.onResume();

    }

    public void mapReset(View view){
        if (!checkReady()) {
            return;
        }
        mMap.clear();
    }


    public void onMapReady(GoogleMap map) {
        mMap = map;
        enableMyLocation();
        MarkerOptions options = new MarkerOptions();
        BitmapDescriptor bMapIcon = BitmapDescriptorFactory.fromResource(R.drawable.fish);
        options.icon(bMapIcon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.0594286, 141.3004599), 7));
        /*
        for(int i=0;latList.size()>i;i++) {
            map.addMarker(options.position(new LatLng(latList.get(i), lngList.get(i))).title(contentList.get(i)));
        }
        */
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    public String loadJSONFile(String filename){
        String jsonStr = null;
        try{
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer,"UTF-8");
        }catch (IOException ex){
            ex.printStackTrace();
            return null;
        }

        return jsonStr;
    }

    private void changeTheInsectMap(){

    }
}
