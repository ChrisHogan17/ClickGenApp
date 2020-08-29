package edu.washington.hoganc17.clickgen;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServerAPI {

    @Multipart
    @POST("/generate")
    Call<String> postSong(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part file
    );
}
