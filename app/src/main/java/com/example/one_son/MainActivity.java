package com.example.one_son;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.one_son.Retrofit.data_model;
import com.example.one_son.Retrofit.retrofit_client;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;

    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;
    private List<Marker> markerList = new ArrayList<Marker>();
    private boolean isCameraAnimated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView textView;
        Call<data_model> call;


        textView = findViewById(R.id.txt_view);

        call = retrofit_client.getApiService().test_api_get("37.53505700000015","126.98862500000007");//url 파라미터
        Log.e("test2", "test2 "+call.request().url());

        call.enqueue(new Callback<data_model>(){
            //콜백 받는 부분
            @Override
            public void onResponse(Call<data_model> call, Response<data_model> response) {


                if(response.code()==200){
                    data_model result = response.body();
                    String str;
                    str= result.getIsSuccess() +"\n"+
                            result.getMessage()+"\n"+
                            result.getCode()+"\n"+
                            result.getResult()+"\n";
                    textView.setText(str);
                    Log.e("test2", "test2 "+str);
                    
                    updataMapMarkers(result);
                }
            }

            @Override
            public void onFailure(Call<data_model> call, Throwable t) {
            }
        });

        NaverMapOptions options = new NaverMapOptions()
                .locationButtonEnabled(true);
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(options);
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    private void updataMapMarkers(data_model result) {
        resetMarkerList();
        if(result.getResult() != null){
            List<Map<String,Object>> Mappoint = new ArrayList<>(result.getResult());
//            latng.addAll(result.getResult());
            for (Map<String, Object> scooter: Mappoint) {
                    scooter.get("lng");
                    Marker marker = new Marker();
                    marker.setPosition(new LatLng((Double) scooter.get("lat"), (Double) scooter.get("lng")));
                    marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_location_on_24));
                    marker.setAnchor(new PointF(0.5f, 1.0f));
                    marker.setMap(naverMap);
                    markerList.add(marker);
            }
            /*
            네이버 지도 api 현위치 찍기
            Toast는 밑에 알림같은거
            location.getLatitude(), location.getLongitude()가 위도 경도
             */
            naverMap.addOnLocationChangeListener(location ->
                    Toast.makeText(this,
                            location.getLatitude() + ", " + location.getLongitude(),
                            Toast.LENGTH_SHORT).show());
        }
    }



    private void resetMarkerList() {
        if(markerList != null && markerList.size() > 0){
            for(Marker marker : markerList){
                marker.setMap(null);
            }
            markerList.clear();
        }
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

        this.naverMap = naverMap;

        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
        }
    }

    /**
     * 위도 경도에 따른 거리 계산 (m단위)
     * @param lat1 위도1
     * @param lng1 경도1
     * @param lat2 위도2
     * @param lng2 경도2
     * @return 거리
     */
    private double distanceByHarversine(double lat1, double lng1, double lat2, double lng2){
        double r = 6371; // 지구 반지름
        double toRadian = Math.PI / 180;

        double deltaLatitude = Math.abs(lat1 - lat2) * toRadian;
        double deltaLongitude = Math.abs(lng1 - lng2) * toRadian;

        double sinDeltaLat = Math.sin(deltaLatitude / 2);
        double sinDeltaLng = Math.sin(deltaLongitude / 2);

        double squareRoot = Math.sqrt(
                sinDeltaLat * sinDeltaLat +
                        Math.cos(lat1 * toRadian) * Math.cos(lat2 * toRadian)
                                * sinDeltaLng * sinDeltaLng);

        double distance = 2 * r * Math.asin(squareRoot) * 1000;

        return distance;
    }
}
