package com.oneeyes.one_son.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Retrofit_interface {

        @GET("location") // url 주소중 마지막 경로
        Call<data_model> test_api_get(
                //@Path("UserID") String userid);
                //@Query("id") String id
                @Query("lat") String lat, //파리미터들
                @Query("lng") String lng //파리미터들
//                @Query("version") String version,
//                @Query("lat") String lat,
//                @Query("lng") String lng,
//                @Query("zoom") String zoom


        );
}