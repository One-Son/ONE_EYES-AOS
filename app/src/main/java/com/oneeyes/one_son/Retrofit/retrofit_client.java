package com.oneeyes.one_son.Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class retrofit_client {
    private static final String BASE_URL = "https://one-eyes.run.goorm.site/api/"; // api url 주소
    private static final String KAKAO_URL = "https://search.map.kakao.com/mapsearch/"; // api url 주소


    public static Retrofit_interface getApiService(){return getInstance().create(Retrofit_interface.class);}
    public static Retrofit_interface getKakaoApiService(){return getKakaoInstance().create(Retrofit_interface.class);}

    private static Retrofit getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static Retrofit getKakaoInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(KAKAO_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}
