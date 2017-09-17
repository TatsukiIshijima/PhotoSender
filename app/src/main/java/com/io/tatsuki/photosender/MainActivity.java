package com.io.tatsuki.photosender;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_CAMERA = 1001;

    private ImageView mTakePhotoImageView;
    private Button mShootButton;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTakePhotoImageView = (ImageView) findViewById(R.id.take_photo_image_view);
        mShootButton = (Button) findViewById(R.id.shoot_button);
        mSendButton = (Button) findViewById(R.id.send_button);

        mShootButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);

        // カメラ起動
        startActivityForResult(cameraIntent(), RESULT_CAMERA);
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
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            mTakePhotoImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 標準カメラ起動のIntent発行
     * @return intent
     */
    private Intent cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent;
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
                break;
        }
    }
}
