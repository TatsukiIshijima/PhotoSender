package com.io.tatsuki.photosender.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.io.tatsuki.photosender.APIs.PhotoSenderApi;
import com.io.tatsuki.photosender.Models.Result;
import com.io.tatsuki.photosender.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_CAMERA = 1001;
    private static final int REQUEST_PERMISSION = 1002;
    private static final String BASE_URL = "http://10.0.2.2:5050";

    private ImageView mTakePhotoImageView;
    private Button mShootButton;
    private Button mSendButton;

    private String mImagePath;
    private File mImageFile;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTakePhotoImageView = (ImageView) findViewById(R.id.take_photo_image_view);
        mShootButton = (Button) findViewById(R.id.shoot_button);
        mSendButton = (Button) findViewById(R.id.send_button);

        mShootButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
        else {
            // カメラ起動
            startActivityForResult(cameraIntent(), RESULT_CAMERA);
        }
    }

    /**
     * 結果の取得
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CAMERA) {
            // 画像を取得し、セットする
            if (mImageUri != null) {
                mTakePhotoImageView.setImageURI(mImageUri);
                registerDatabase(mImagePath);
            }
        }
    }

    /**
     * パーミッションの取得結果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(cameraIntent(), RESULT_CAMERA);
            } else {
                finish();
            }
        }
    }

    /**
     * パーミッションの確認
     */
    private void checkPermission(){
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            // カメラ起動
            startActivityForResult(cameraIntent(), RESULT_CAMERA);
        }
        // 拒否していた場合
        else{
            requestPermission();
        }
    }

    /**
     * パーミッションの要求
     */
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, REQUEST_PERMISSION);
        }
    }

    /**
     * 標準カメラ起動のIntent発行
     * @return intent
     */
    private Intent cameraIntent() {
        createImagePath();
        mImageFile = new File(mImagePath);
        mImageUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", mImageFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        return intent;
    }

    /**
     * 画像ファイルのパス作成
     * @return
     */
    private void createImagePath() {
        // 保存先のフォルダーを作成
        File cameraFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IMG");
        cameraFolder.mkdirs();

        // 保存ファイル名
        String fileName = new SimpleDateFormat("ddHHmmss").format(new Date());
        mImagePath = cameraFolder.getPath() +"/" + fileName + ".jpg";
    }

    /**
     * DBへパスを登録する
     * @param path
     */
    private void registerDatabase(String path) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    }

    /**
     * クリックイベント
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 撮影ボタン
            case R.id.shoot_button:
                startActivityForResult(cameraIntent(), RESULT_CAMERA);
                break;
            // 送信ボタン
            case R.id.send_button:
                if (mImageFile != null) {

                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), mImageFile);
                    //MultipartBody.Part part = MultipartBody.Part.createFormData("image", mImageFile.getName(), requestBody);

                    Gson gson = new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .registerTypeAdapter(Date.class, new DateTypeAdapter())
                            .create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    PhotoSenderApi photoSenderApi = retrofit.create(PhotoSenderApi.class);

                    Observer observer = new Observer<Result>() {

                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "onCompleted");
                            // TODO:送信完了時点で画面遷移
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError : "  + e.toString());
                        }

                        @Override
                        public void onNext(Result result) {
                            Log.d(TAG, "onNext Status : " + result.getStatus());
                            Log.d(TAG, "onNext Category : " + result.getCategory());
                            // TODO:レスポンスをもとに画像に描画
                        }
                    };

                    photoSenderApi.sendPhoto(requestBody)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(observer);

                } else {
                    Log.d(TAG, "Image File is NULL");
                }
                break;
        }
    }
}
