package com.io.tatsuki.photosender.APIs;

import com.io.tatsuki.photosender.Models.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * 画像アップロードAPI
 */

public interface PhotoSenderApi {

    @Multipart
    @POST("image/upload")
    public Observable<Result> sendPhoto(@Part("file\"; filename=\"send.jpg\" ") RequestBody file);
}
