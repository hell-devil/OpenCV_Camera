package com.hitwh.opencv_camera;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;



import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btnCamera;

    private File mPhotoFile;
    private String mFilePath;
    private String mWorkSpacePath;
    private boolean FILE_FLAG=true;
    private static int REQ = 1;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //权限获取
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            //权限还没有授予，进行权限申请
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},1);
        }




        btnCamera = findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new CameraClickListener());
    }

    private class CameraClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.i(TAG, "按了相机");
            takePhoto();

        }
    }


    private void takePhoto() {
        //跳转到新的Activity去拍照片

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mFilePath = getSDPath()+"/TestPhoto";
            Log.i(TAG, "啊，mFilePath= "+mFilePath);
            try {
                File file;
                file = new File(mFilePath);
                if (!file.exists()) {
                    file.mkdir();
                }
            } catch (Exception e) {
                Log.i("error:", e+"");
            }
            Log.i(TAG, "啊，建立文件夹完毕"+FILE_FLAG);

            mWorkSpacePath=mFilePath;
            Log.i(TAG, "t啊，mWorkSpacePath="+mWorkSpacePath);
            mFilePath=mFilePath+ "/" +getPhotoFileName();
            mPhotoFile = new File(mFilePath);
            if (!mPhotoFile.exists()) {
                mPhotoFile.createNewFile();//创建新文件
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT,//Intent有了图片的信息
                    Uri.fromFile(mPhotoFile));
            startActivityForResult(intent, REQ);//跳转界面传回拍照所得数据
        } catch (Exception e) {
        }
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
        }
        return sdDir.toString();

    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ) {
            Log.i(TAG, "onActivityResult: 啊进入下一个Activity");
            Intent intent1 = new Intent();
            intent1.setClass(MainActivity.this, ImageEdit.class);
            intent1.putExtra("data", mFilePath);
            intent1.putExtra("workpath",mWorkSpacePath);
            startActivity(intent1);
        }
    }

}