package com.example.one_son;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final Double MAX_DISTANCE = 200.0;

    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;
    private List<Marker> markerList = new ArrayList<Marker>();
    private boolean isCameraAnimated = false;
    private double minDistance = MAX_DISTANCE;

    private double userLat, userLng; // 사용자 좌표
    private List<Map<String ,Object>> mappoint;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        APIThread thread = new APIThread();
        thread.start();

        DistanceThread thread2 = new DistanceThread();
        thread2.start();

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


    /** API 쓰레드 구현
     *  60초 마다
     *  현재 백엔드 API endpoint: https://one-humqo.run.goorm.app/api/location
     *
     *  GET형식
     *  파라미터 lat, lng
     *  사용자 좌표 변수명 userLat, userLng
     * */
    private class APIThread extends Thread {


        public APIThread() {
            // 초기화 작업
        }

        public void run() {

            int second = 0;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                second++;
                try {
                    // 스레드에게 수행시킬 동작들 구현
                    Call<data_model> call;
                    call = retrofit_client.getApiService().test_api_get(userLat, userLng);//url 파라미터
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

                                Log.e("test2", "test2 "+str);

                                updataMapMarkers(result);
                            }
                        }

                        @Override
                        public void onFailure(Call<data_model> call, Throwable t) {
                        }
                    });//60초마다 실행
                    // 거리에 따른 진동 발생 (수정 예정)

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrateByDistance(minDistance, vibrator);
                    Log.e("vibrate", vibrator.hasVibrator() + "");

                    Thread.sleep(60000); // 5초간 Thread를 잠재운다
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i("경과된 시간 : ", Integer.toString(second));
            }

        }
    }


    private class DistanceThread extends Thread {


        public DistanceThread() {
            // 초기화 작업
        }

        public void run() {

            while (true) {

                try {

                    // 거리에 따른 진동 발생 (수정 예정)

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrateByDistance(minDistance, vibrator);
                    Log.e("vibrate", vibrator.hasVibrator() + "");

                    Thread.sleep(5000); // 5초간 Thread를 잠재운다
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }
    }

    private void updataMapMarkers(data_model result) {
        resetMarkerList();
        if(result.getResult() != null){
            mappoint = new ArrayList<>(result.getResult());
//            latng.addAll(result.getResult());
            for (Map<String, Object> scooter: mappoint) {
                scooter.get("lng");
                Marker marker = new Marker();
                marker.setPosition(new LatLng((Double) scooter.get("lat"), (Double) scooter.get("lng")));
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_location_on_24));
                marker.setAnchor(new PointF(0.5f, 1.0f));
                marker.setMap(naverMap);
                markerList.add(marker);
            }
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

        //현 위치 갱신
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener(){

            @Override
            public void onLocationChange(@NonNull Location location) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();

                Log.e("latlng", userLat + ", " + userLng);

                //현위치로 부터 가장 가까운 스쿠터 거리 계산
                minDistance = calcMinDistance(userLat, userLng, minDistance);

            }
        });
    }

    // 진동 발생 함수
    private void vibrateByDistance(Double minDistance, Vibrator vibrator) {
        if(minDistance >= MAX_DISTANCE) return;
        if(minDistance < 1) minDistance = 1.0;
        //vibrator.vibrate((int) Math.round(5000 - minDistance * 25)); // 1000이 1초간 진동
        long[] pattern = {(long) (minDistance * 10), (long) (minDistance * 10)};
        vibrator.vibrate(pattern, (int) (250 / minDistance / minDistance));
    }

    /**
     * 최소 거리 구하기
     * mappoint는 전역변수
     * @param userLat 유저 위도
     * @param userLng 유저 경도
     * @param minDistance 현재 최소 값
     * @return 최소 값
     */
    private Double calcMinDistance(Double userLat, Double userLng, Double minDistance) {
        if(userLat.equals(0.0d) || userLng.equals(0.0d) ||mappoint == null) return minDistance;
        Double result = minDistance;


        for (Map<String, Object> scooter: mappoint) {
            Double distance = distanceByHarversine(userLat, userLng,
                    (Double) scooter.get("lat"), (Double) scooter.get("lng"));
            if(distance < minDistance){
                result = distance;
            }
        }

        return result;
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
