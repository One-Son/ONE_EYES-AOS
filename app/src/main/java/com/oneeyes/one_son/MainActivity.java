package com.oneeyes.one_son;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.widget.LocationButtonView;
import com.oneeyes.one_son.Retrofit.data_model;
import com.oneeyes.one_son.Retrofit.retrofit_client;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final int ACCESS_LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final Double MAX_DISTANCE = 5.0;

    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private List<Marker> markerList = new ArrayList<Marker>();
    private boolean isCameraAnimated = false;
    private double minDistance = MAX_DISTANCE;

    private Context cThis;//context 설정
    private Button addressbtn;
    private Button searchbtn;
    private String userLat, userLng; // 사용자 좌표
    private List<Map<String ,Object>> mappoint;
    private List<Map<String ,Object>> mapsearchpoint;
    //음성 출력용
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTheme(R.style.Theme_ONESoN);
        APIThread thread = new APIThread();
        thread.start();

        NaverMapOptions options = new NaverMapOptions()
                .locationButtonEnabled(true);
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(options);
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        addressbtn = (Button)findViewById(R.id.address);
        addressbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userLat == null || userLng == null) return;
                getAddress(Double.parseDouble(userLat),Double.parseDouble(userLng));
            }
        });

        searchbtn = (Button) findViewById(R.id.search);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchlocationActivity.class);
                getSearchActivityResult.launch(intent);
            }
        });

        //음성출력 생성, 리스너 초기화
        tts=new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        DistanceThread thread2 = new DistanceThread();
        thread2.start();
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
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {

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
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                    // 거리에 따른 진동 발생
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrateByDistance(minDistance, vibrator);

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
                Marker marker = new Marker();
                marker.setPosition(new LatLng( Double.parseDouble(scooter.get("lat").toString()), Double.parseDouble(scooter.get("lng").toString())));
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_scooter));
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

        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(33.38, 126.55),17);  // 위치 지정                           // 줌 레벨

        naverMap.setCameraPosition(cameraPosition);
        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSION_REQUEST_CODE);
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(false);//현위치 버튼
        uiSettings.setCompassEnabled(false);//나침반
        uiSettings.setScaleBarEnabled(false);//축적바
        uiSettings.setZoomControlEnabled(false); // 줌버튼

        LocationButtonView locationButtonView = findViewById(R.id.currentLocationButton);
        locationButtonView.setMap(naverMap);

        //현 위치 갱신
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener(){

            @Override
            public void onLocationChange(@NonNull Location location) {
                userLat = Double.toString(location.getLatitude());
                userLng = Double.toString(location.getLongitude());

                Log.e("latlng", userLat + ", " + userLng);

                if(userLat == null || userLng == null) return;
                //현위치로 부터 가장 가까운 스쿠터 거리 계산
                minDistance = calcMinDistance(Double.parseDouble(userLat), Double.parseDouble(userLng));

            }
        });
        naverMap.addOnOptionChangeListener(() -> {
            locationSource.setCompassEnabled(true);
        });
    }

    // 진동 발생 함수
    private void vibrateByDistance(Double minDistance, Vibrator vibrator) {
        vibrator.cancel();
        if (minDistance >= MAX_DISTANCE) {
            return;
        }
        double timeDelay = minDistance * 400;
        //vibrator.vibrate((int) Math.round(5000 - minDistance * 25)); // 1000이 1초간 진동
        long[] pattern = {(long) (timeDelay + 100), (long) (timeDelay + 100)};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
        else {
            vibrator.vibrate(pattern, 0);
        }
    }

        /**
     * 최소 거리 구하기
     * mappoint는 전역변수
     * @param userLat 유저 위도
     * @param userLng 유저 경도
     * @return 최소 값
     */
    private Double calcMinDistance(Double userLat, Double userLng) {
        if(userLat.equals(0.0d) || userLng.equals(0.0d) || mappoint == null) return MAX_DISTANCE;
        Double result = MAX_DISTANCE;
        for (Map<String, Object> scooter: mappoint) {
            double scooterLat = Double.parseDouble(scooter.get("lat").toString());
            double scooterLng = Double.parseDouble(scooter.get("lng").toString());
            double boundaryLange = 0.0001; // 대략 10m

            if(Math.abs(userLat - scooterLat) > boundaryLange ||
                    Math.abs(userLng - scooterLng) > boundaryLange) {
                continue;
            }

            Double distance = distanceByHarversine(userLat, userLng, scooterLat, scooterLng);
            if(distance < result){
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

    public String getAddress(double lat, double lng){
        String nowAddr ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> address;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    nowAddr = address.get(0).getAddressLine(0).toString();
                    Toast.makeText(this,"해당 주소는 "+nowAddr+"입니다.", Toast.LENGTH_LONG).show();
                    FuncVoiceOut("해당 주소는 "+nowAddr+"입니다.");// 음성 출력
                }
            }
        }
        catch (Exception e) {
            Log.e("error", "NULL 에러 " + e.getMessage());
            e.printStackTrace();
        }
        return nowAddr;
    }

    //음성 메세지 출력용
    private void FuncVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null);
    }

    private final ActivityResultLauncher<Intent> getSearchActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    String VoiceMsg = result.getData().getStringExtra("VoiceMsg");

                    //주소 검색 API
                    Call<data_model> call;
                    call = retrofit_client.getKakaoApiService().kakao_api_get("https://map.kakao.com/",VoiceMsg);//url 파라미터

                    Log.e("test2", "test2 "+call.request().url());
                    call.enqueue(new Callback<data_model>(){
                        //콜백 받는 부분
                        @Override
                        public void onResponse(Call<data_model> call, Response<data_model> response) {

                            //여기 수정
                            if(response.code()==200){
                                String str;
                                data_model result = response.body();
                                str= result.getPlace()+"\n";
                                Log.e("test", "test "+str);
                                mapsearchpoint = new ArrayList<>(result.getPlace());

                                Double.parseDouble(mapsearchpoint.get(0).get("lat").toString());
                                Double.parseDouble(mapsearchpoint.get(0).get("lon").toString());
                                Double distance = distanceByHarversine(Double.parseDouble(userLat), Double.parseDouble(userLng), Double.parseDouble(mapsearchpoint.get(0).get("lat").toString()), Double.parseDouble(mapsearchpoint.get(0).get("lon").toString()));
                                FuncVoiceOut("현재"+ VoiceMsg +"과의 거리는"+distance.intValue()+"미터 떨어져 있습니다.");// 음성 출력
                            }
                        }
                        @Override
                        public void onFailure(Call<data_model> call, Throwable t) {
                            FuncVoiceOut("현재 위치 주소를 찾을 수 없습니다.");// 음성 출력
                        }
                    });

                    Log.e("VoiceMsg",result.getData().getStringExtra("VoiceMsg"));
                    //FuncVoiceOut("해당 주소는 "+result.getData().getStringExtra("VoiceMsg")+"입니다.");// 음성 출력
                }
            }
    );
}
