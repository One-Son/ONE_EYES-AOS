package com.example.one_son.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class data_model {
        @SerializedName("kickscooters")
        @Expose
        private List kickscooters;

        @SerializedName("clustered_kickscooters")
        @Expose
        private List clustered_kickscooters;

        @SerializedName("service_area_centers")
        @Expose
        private List service_area_centers;




        public List getUserId(){
                return kickscooters;
        }

        public List getID(){
                return clustered_kickscooters;
        }

        public List getTitle(){
                return service_area_centers;
        }


}