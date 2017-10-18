package com.io.tatsuki.photosender.APIs;

import com.io.tatsuki.photosender.Models.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by TatsukiIshijima on 2017/10/18.
 */

public interface PhotoSenderApi {

    @Multipart
    @POST("image/upload")
    public Observable<Result> sendPhoto(@Part MultipartBody.Part image);
}
