package com.shinow.qrscan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = SecondActivity.class.getSimpleName();

    public static boolean isLightOpen = false;
    private int REQUEST_IMAGE = 101;
    private LinearLayout lightLayout;
    private ImageView ivBack;
    private ImageView ivChoosePhoto;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener sensorEventListener;
    TextView tvTitle, tvContent, tvWarn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        CaptureFragment captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();

        tvContent = findViewById(R.id.tv_scanner_content);
        lightLayout = findViewById(R.id.scan_light);
        ivBack = findViewById(R.id.iv_back);
        ivChoosePhoto = findViewById(R.id.iv_choose_photo);
        tvTitle = findViewById(R.id.tv_scanner_title);
        tvWarn = findViewById(R.id.tv_warn);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorEventListener = new LightSensorEventListener(lightLayout);

        initView();
    }

    @Override
    protected void onResume() {
        // System.out.println("---------------------|||||||||||||---onResume---|||||||||||-------------------------");
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        // System.out.println("---------------------|||||||||||||---onPause---|||||||||||-------------------------");
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    private void initView() {
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String bottom = getIntent().getStringExtra("bottom");
        if (title != null) {
            tvTitle.setText(title);
        }
        if (content != null) {
            tvContent.setText(content);
        }
        if (bottom != null) {
            tvWarn.setText(bottom);
        }
        lightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLightOpen) {
                    CodeUtils.isLightEnable(true);
                    isLightOpen = true;
                } else {
                    CodeUtils.isLightEnable(false);
                    isLightOpen = false;
                }
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondActivity.this.finish();
            }
        });
        ivChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                SecondActivity.this.startActivityForResult(intent, REQUEST_IMAGE);

                //这里修改 调到flutter去选择照片
//                Intent intent = new Intent();
//                intent.setClass(SecondActivity.this, QrscanPlugin.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("m_intent", "choose_photo");
//                intent.putExtra("secondBundle", bundle);
//                setResult(Activity.RESULT_OK, intent);
                EventBus.getDefault().post("choose_photo");
                SecondActivity.this.finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                String path = ImageUtil.getImageAbsolutePath(SecondActivity.this, uri);
                Intent intent = new Intent();
                intent.setClass(SecondActivity.this, QrscanPlugin.class);
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                intent.putExtra("secondBundle", bundle);
                setResult(Activity.RESULT_OK, intent);
                SecondActivity.this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
        resultIntent.putExtras(bundle);
        SecondActivity.this.setResult(RESULT_OK, resultIntent);
        SecondActivity.this.finish();
    }

    private CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            SecondActivity.this.setResult(RESULT_OK, resultIntent);
            SecondActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            SecondActivity.this.setResult(RESULT_OK, resultIntent);
            SecondActivity.this.finish();
        }
    };

}
