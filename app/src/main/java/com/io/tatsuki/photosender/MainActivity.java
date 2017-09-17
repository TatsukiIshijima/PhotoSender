package com.io.tatsuki.photosender;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;

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
                    UploadService service = ServiceGenerator.createService(UploadService.class);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), mImageFile);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("POST先フィールド名", mImageFile.getName(), requestFile);

                    Call<ResponseBody> call = service.upload(body);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.d(TAG, "onSuccess : " + response);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.d(TAG, "onFailure : " + t);
                        }
                    });

                    /*
                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), mImageFile);
                    Call<String> call = service.upload(requestBody);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.d(TAG, "onSuccess : " + response);
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d(TAG, "onFailure");
                        }
                    });
                    */
                } else {
                    Log.d(TAG, "Image File is NULL");
                }
                break;
        }
    }
}
