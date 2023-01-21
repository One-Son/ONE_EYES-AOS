package com.example.one_son.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Retrofit_interface {

        @GET("kickscooters")
        Call<data_model> test_api_get(
                //@Path("UserID") String userid);
                //@Query("id") String id
                @Query("version") String version,
                @Query("lat") String lat,
               @Query("lng") String lng,
               @Query("zoom") String zoom

        );
}