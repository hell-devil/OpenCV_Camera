package com.hitwh.opencv_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ImageEdit extends AppCompatActivity {

    private File DisImageFile;
    TextView  TextSavePath;
    Button btnProcess;
    Button btnBlur;
    Button btnSrc;
    Button btnBlurMean;
    Button btnBlurGaussian;
    Button btnBlurMedian;
    Button btnSaveImage;
    SeekBar btnSeekBar;
    ProgressBar pgsbBlur;
    Bitmap srcBitmap;
    Bitmap grayBitmap;
    Bitmap blurBitmap;
    Bitmap disBitmap;
    Bitmap blurSrc;
    Bitmap mymap;
    ImageView imgHuaishi;
    private String mFilePath;
    private String mWorkSpacePath;
    private String DisImagePath;
    private int blur_size=1;
    private int Gussian_size=1;
    private int Median_size=1;
    private int BLUR_TYPE=3;
    private static boolean BLUR_FLAG = false;
    private static boolean IMG_FLAG=true;
    private static final String TAG = "ImageEdit";
    private static final int SRC_IMG=1;
    private static final int GRAY_IMG=2;
    private static final int BLUR_MEAN=3;
    private static final int BLUR_GAUSSIAN=4;
    private static final int BLUR_MEDIAN=5;
    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        Log.i(TAG, "啊进入下一个Edit成功");
        Intent intent =getIntent();
        mFilePath=intent.getStringExtra("data");
        mWorkSpacePath=intent.getStringExtra("workpath");
        Log.i(TAG, "啊图片显示路径:"+mFilePath);
        Log.i(TAG, "啊文件夹路径："+mWorkSpacePath);
        mymap=BitmapFactory.decodeFile(mFilePath);
        //按键定义
        btnSrc=findViewById(R.id.btn_src);
        btnProcess = findViewById(R.id.btn_gray_process);
        btnBlur=findViewById(R.id.btn_blur);
        btnBlurMean=findViewById(R.id.btn_blur_mean);
        btnBlurGaussian=findViewById(R.id.btn_blur_gaussian);
        btnBlurMedian=findViewById(R.id.btn_blur_median);
        btnSaveImage=findViewById(R.id.btn_save_img);
        //滑块定义
        btnSeekBar=findViewById(R.id.btn_seekbar);
        //圆圈定义
        pgsbBlur=findViewById(R.id.pgsb_blur);
        //图片显示定义
        imgHuaishi = findViewById(R.id.img_huaishi);
        //文本框定义
        TextSavePath=findViewById(R.id.text_save_path);
        //按键监听器
        btnProcess.setOnClickListener(new ProcessClickListener());
        btnBlur.setOnClickListener(new BlurClickListener());
        btnSrc.setOnClickListener(new SrcClickListener());
        btnBlurMean.setOnClickListener(new BlurMeanClickListener());
        btnBlurGaussian.setOnClickListener(new BlurGaussianClickListener());
        btnBlurMedian.setOnClickListener(new BlurMedianClickListener());
        imgHuaishi.setOnClickListener(new imgClickListener());
        btnSaveImage.setOnClickListener(new SaveImageClickListener());
        //滑块变化监听器
        btnSeekBar.setOnSeekBarChangeListener(seekBarChange);

        //变量初始化
        BLUR_FLAG = false;
        IMG_FLAG=true;

        //隐藏不需要控件
        btnSeekBar.setVisibility(View.GONE);
        btnBlurMean.setVisibility(View.GONE);
        btnBlurGaussian.setVisibility(View.GONE);
        btnBlurMedian.setVisibility(View.GONE);
        pgsbBlur.setVisibility(View.GONE);
        TextSavePath.setVisibility(View.GONE);

        DisImage(); //裁剪图片用于显示

    }
    public void DisImage(){
        Log.i(TAG, "initUI sucess...");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        // float scale = getResources().getDisplayMetrics().density;
        int screenWidth= dm.widthPixels;
        int screenHeight= dm.heightPixels*4/5;
        if(mymap.getWidth()>screenWidth  ){
            Bitmap bmp=Bitmap.createScaledBitmap(mymap, screenWidth, mymap.getHeight()*screenWidth/mymap.getWidth(), true);
            imgHuaishi.setImageBitmap(bmp);
            srcBitmap=bmp;
        }else if(mymap.getHeight()>screenHeight){
            Bitmap bmp=Bitmap.createScaledBitmap(mymap, mymap.getWidth()*screenHeight/mymap.getHeight(), screenHeight, true);
            imgHuaishi.setImageBitmap(bmp);
            srcBitmap=bmp;
        }else{
            imgHuaishi.setImageBitmap(mymap);
            srcBitmap=mymap;
        }
        disBitmap=Bitmap.createBitmap(srcBitmap);
    }

    public void procSrc2Gray(int IMG_EVENT){
        Mat rgbMat = new Mat();
        Mat tempMat = new Mat();
        switch (IMG_EVENT)
        {
            case SRC_IMG:
                disBitmap=Bitmap.createBitmap(srcBitmap);
                break;
            case GRAY_IMG:
                grayBitmap = Bitmap.createBitmap(disBitmap.getWidth(), disBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Utils.bitmapToMat(disBitmap, rgbMat);//convert original bitmap to Mat, R G B.
                Imgproc.cvtColor(rgbMat, tempMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
                Utils.matToBitmap(tempMat, grayBitmap); //convert mat to bitmap
                disBitmap=Bitmap.createBitmap(grayBitmap);
                break;
            case BLUR_MEAN:
                disBitmap=Bitmap.createBitmap(blurSrc);   //防止反复模糊
                blurBitmap = Bitmap.createBitmap(disBitmap.getWidth(), disBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Utils.bitmapToMat(disBitmap, rgbMat);//convert original bitmap to Mat, R G B.
                Imgproc.blur(rgbMat,tempMat,new Size(blur_size,blur_size));
                Utils.matToBitmap(tempMat,blurBitmap);
                disBitmap=Bitmap.createBitmap(blurBitmap);
                Log.i(TAG, "啊均值模糊成功");
                break;
            case BLUR_GAUSSIAN:
                disBitmap=Bitmap.createBitmap(blurSrc);
                blurBitmap = Bitmap.createBitmap(disBitmap.getWidth(), disBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Utils.bitmapToMat(disBitmap, rgbMat);//convert original bitmap to Mat, R G B.
                Imgproc.GaussianBlur(rgbMat,tempMat,new Size(Gussian_size,Gussian_size),0,0);
                Utils.matToBitmap(tempMat,blurBitmap);
                disBitmap=Bitmap.createBitmap(blurBitmap);
                Log.i(TAG, "啊高斯模糊成功");
                break;
            case BLUR_MEDIAN:
                disBitmap=Bitmap.createBitmap(blurSrc);
                blurBitmap = Bitmap.createBitmap(disBitmap.getWidth(), disBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Utils.bitmapToMat(disBitmap, rgbMat);//convert original bitmap to Mat, R G B.
                Imgproc.medianBlur(rgbMat,tempMat,Median_size);
                Utils.matToBitmap(tempMat,blurBitmap);
                disBitmap=Bitmap.createBitmap(blurBitmap);
                Log.i(TAG, "啊中值模糊成功");
                break;

        }
    }


    //点击图片显示和隐藏按键
    private class imgClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            if(IMG_FLAG) {
                btnSrc.setVisibility(View.INVISIBLE);
                btnProcess.setVisibility(View.INVISIBLE);
                btnBlur.setVisibility(View.INVISIBLE);
                btnSaveImage.setVisibility(View.INVISIBLE);
                if(BLUR_FLAG){
                    btnBlurMean.setVisibility(View.INVISIBLE);
                    btnBlurGaussian.setVisibility(View.INVISIBLE);
                    btnBlurMedian.setVisibility(View.INVISIBLE);
                    btnSeekBar.setVisibility(View.INVISIBLE);
                }
                IMG_FLAG=false;
                Log.i(TAG, "啊1、点击了图片");
            }
            else
            {
                btnSrc.setVisibility(View.VISIBLE);
                btnProcess.setVisibility(View.VISIBLE);
                btnBlur.setVisibility(View.VISIBLE);
                btnSaveImage.setVisibility(View.VISIBLE);
                if(BLUR_FLAG){
                    btnBlurMean.setVisibility(View.VISIBLE);
                    btnBlurGaussian.setVisibility(View.VISIBLE);
                    btnBlurMedian.setVisibility(View.VISIBLE);
                    btnSeekBar.setVisibility(View.VISIBLE);
                }
                IMG_FLAG=true;
                Log.i(TAG, "啊2、点击了图片");
            }
        }
    }


    //显示原图
    private class SrcClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            procSrc2Gray(SRC_IMG);
            imgHuaishi.setImageBitmap(disBitmap);
        }
    }
    //显示灰度图
    private class ProcessClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            procSrc2Gray(GRAY_IMG);
            imgHuaishi.setImageBitmap(disBitmap);
        }
    }

    private class BlurClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
//
            if(BLUR_FLAG == false) {

                blurSrc=Bitmap.createBitmap(disBitmap);
                btnProcess.setEnabled(false);
                btnSrc.setEnabled(false);
                btnSaveImage.setEnabled(false);
                btnBlur.setText("确定");
                btnBlurMean.setVisibility(View.VISIBLE);
                btnBlurGaussian.setVisibility(View.VISIBLE);
                btnBlurMedian.setVisibility(View.VISIBLE);
                btnSeekBar.setVisibility(View.VISIBLE);
                btnSeekBar.setProgress(0);
                BLUR_FLAG=true;
            }
            else {
                blurSrc=Bitmap.createBitmap(disBitmap);
                btnProcess.setEnabled(true);
                btnSrc.setEnabled(true);
                btnSaveImage.setEnabled(true);
                btnBlur.setText("模糊");
                btnBlurMean.setVisibility(View.GONE);
                btnBlurGaussian.setVisibility(View.GONE);
                btnBlurMedian.setVisibility(View.GONE);
                btnSeekBar.setVisibility(View.GONE);
                BLUR_FLAG=false;
            }
            //procSrc2Gray(BLUR_IMG);
            //imgHuaishi.setImageBitmap(disBitmap);
        }
    }

    private class BlurMeanClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
//
            btnSeekBar.setProgress(0);
            blur_size=1;
            BLUR_TYPE=BLUR_MEAN;
            imgHuaishi.setImageBitmap(blurSrc);
        }
    }

    private class BlurGaussianClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
//
            btnSeekBar.setProgress(0);
            Gussian_size=1;
            BLUR_TYPE=BLUR_GAUSSIAN;
            imgHuaishi.setImageBitmap(blurSrc);
        }
    }

    private class BlurMedianClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){

            btnSeekBar.setProgress(0);
            Median_size=1;
            BLUR_TYPE=BLUR_MEDIAN;
            imgHuaishi.setImageBitmap(blurSrc);

        }
    }



    private OnSeekBarChangeListener seekBarChange = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            //模糊中禁用按键，显示进度条
            pgsbBlur.setVisibility(View.VISIBLE);
            btnBlur.setEnabled(false);
            btnBlurMean.setEnabled(false);
            btnBlurGaussian.setEnabled(false);
            btnBlurMedian.setEnabled(false);
            btnSeekBar.setEnabled(false);
            imgHuaishi.setEnabled(false);
            new Thread(new Runnable() {
                //					        开启一个线程处理逻辑，然后在线程中在开启一个UI线程，当子线程中的逻辑完成之后，
                //					        就会执行UI线程中的操作，将结果反馈到UI界面。
                @Override
                public void run() {
                    // 耗时的操作，在子线程中进行。
                    procSrc2Gray(BLUR_TYPE);
                    // 更新主线程ＵＩ，跑在主线程。
                    ImageEdit.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 重新启用按键，隐藏进度条
                            pgsbBlur.setVisibility(View.GONE);
                            btnBlur.setEnabled(true);
                            btnBlurMean.setEnabled(true);
                            btnBlurGaussian.setEnabled(true);
                            btnBlurMedian.setEnabled(true);
                            btnSeekBar.setEnabled(true);
                            imgHuaishi.setEnabled(true);
                            imgHuaishi.setImageBitmap(disBitmap);
                        }
                    });
                }
            }).start();

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG, "啊开始拖动进度条");
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (BLUR_TYPE){
                case BLUR_MEAN:     blur_size   = progress+1;               break;
                case BLUR_GAUSSIAN: Gussian_size= 2*progress+1;             break;
                case BLUR_MEDIAN:   Median_size = 2*progress+1;             break;
            }
            Log.i(TAG, "啊正在拖动！");
        }
    };


    private class SaveImageClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            DisImagePath=mWorkSpacePath+ "/" +getPhotoFileName();
//            DisImageFile = new File(DisImagePath);
//            if (!DisImageFile.exists()) {
//                try {
//                    DisImageFile.createNewFile();//创建新文件
//                    Log.i(TAG, "啊，成功save"+DisImagePath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            try {
                DisImageFile = new File(DisImagePath);
                FileOutputStream out = new FileOutputStream(DisImageFile);
                disBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "啊，成功save"+DisImagePath);

//            btnSaveImage.setText("保存成功");
            TextSavePath.setText("存储路径"+DisImageFile);
            TextSavePath.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**
                     *要执行的操作
                     */
                    Log.i(TAG, "啊，延时开始");
                    TextSavePath.setVisibility(View.GONE);
                }
            }, 1000);//3秒后执行Runnable中的run方法
            Log.i(TAG, "啊，延时结束");
        }
    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Log.i(TAG, "啊执行了第二个的ActivityResult");
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}

