package com.io.tatsuki.photosender;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 通信クラス
 */

public interface UploadService {

    @Multipart
    @POST("image/upload")
    Call<ResponseBody> upload(@Part MultipartBody.Part file);
}
