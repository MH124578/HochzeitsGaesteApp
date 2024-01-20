// com.example.greeting_screen.network.ApiService

package com.example.greeting_screen.network;

import com.example.greeting_screen.model.HomeInformationEntry;
import com.example.greeting_screen.model.HomeInformationEntryCreate;
import com.example.greeting_screen.model.User;
import com.example.greeting_screen.model.UserBase;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/add_user_email/")
    Call<User> addUserEmail(@Body UserBase user);

    @Headers("Content-Type: application/json")
    @POST("/fill_out_email_user/")
    Call<User> fillOutEmailUser(@Body Map<String, String> requestBody);

    @GET("/check_user_email/")
    Call<Boolean> checkUserEmail(@Query("email") String email);

    @GET("/home_information/")
    Call<List<HomeInformationEntry>> getHomeInformationEntries(
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @POST("/home_information/")
    Call<HomeInformationEntryCreate> postHomeInformationEntry(
            @Body HomeInformationEntryCreate homeInformationEntryCreate,
            @Query("user_id") int user_id
    );

}