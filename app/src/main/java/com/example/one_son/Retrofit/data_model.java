package com.example.one_son.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Result;

public class data_model {
        @SerializedName("isSuccess")
        @Expose
        private Boolean isSuccess;

        @SerializedName("code")
        @Expose
        private int code;

        @SerializedName("message")
        @Expose
        private String message;

        @SerializedName("result")
        @Expose
        private List result;


        public Boolean getIsSuccess(){
                return isSuccess;
        }

        public String getMessage(){
                return message;
        }

        public int getCode(){
                return code;
        }


        public List getResult(){
                return result;
        }


//        @SerializedName("kickscooters")
//        @Expose
//        private List kickscooters;
//
//        @SerializedName("clustered_kickscooters")
//        @Expose
//        private List clustered_kickscooters;
//
//        @SerializedName("service_area_centers")
//        @Expose
//        private List service_area_centers;
//
//        public List getMessage(){
//                return kickscooters;
//       }
//
//        public List getCode(){
//                return clustered_kickscooters;
//        }
//
//        public List getResult(){
//                return service_area_centers;
//        }
}