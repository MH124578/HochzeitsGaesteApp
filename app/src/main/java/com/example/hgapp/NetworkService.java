package com.example.hgapp;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.GET;
import java.util.List;

public class NetworkService {

    public interface ApiService {
        @Multipart
        @POST("/upload_image/{user_id}")
        Call<ResponseBody> uploadImage(@Path("user_id") int userId, @Part MultipartBody.Part file, @Part("text") RequestBody text);

        @DELETE("/rm_image/{entry_id}")
        Call<ResponseBody> deleteImage(@Path("entry_id") int entryId);

        @FormUrlEncoded
        @PUT("/edit_image/{entry_id}")
        Call<ResponseBody> editImageText(@Path("entry_id") int entryId, @Field("text") String newText);

        @GET("/all_images/")
        Call<List<ImageData>> getAllImages();
    }

    private ApiService apiService;

    public NetworkService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public ApiService getApiService() {
        return apiService;
    }
}
