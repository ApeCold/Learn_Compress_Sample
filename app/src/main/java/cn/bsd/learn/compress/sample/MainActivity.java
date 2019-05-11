package cn.bsd.learn.compress.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import cn.bsd.learn.compress.library.CompressImageManager;
import cn.bsd.learn.compress.library.bean.Photo;
import cn.bsd.learn.compress.library.config.CompressConfig;
import cn.bsd.learn.compress.library.listener.CompressImage;
import cn.bsd.learn.compress.library.utils.CachePathUtils;
import cn.bsd.learn.compress.library.utils.CommonUtils;
import cn.bsd.learn.compress.library.utils.Constants;
import cn.bsd.learn.compress.sample.utils.UriParseUtils;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity implements CompressImage.CompressListener {
    private CompressConfig compressConfig; //压缩配置
    private ProgressDialog dialog;//压缩进度加载框
    private String cameraCachePath;//拍照源文件路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED || checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }

//        compressConfig = CompressConfig.getDefaultConfig();
        compressConfig = CompressConfig.builder()
                .setUnCompressMinPixel(1000)//最小像素不压缩，默认值：1000
                .setUnCompressNormalPixel(2000)//标准像素不压缩，默认值：2000
                .setMaxPixel(1000) //长或宽不超过的最大像素（单位px），默认值：1200
                .setMaxSize(100 * 1024)//压缩到的最大大小（单位B）,默认值：200*1024 = 200KB
                .enablePixelCompress(true)//是否启用像素压缩，默认值：true
                .enableQualityCompress(true)//是否启用质量压缩，默认值：true
                .enableReserveRaw(true)//是否保留源文件，默认值：true
                .setCacheDir("")//压缩后缓存图片路径，默认值：Constants.COMPRESS_CACHE
                .setShowCompressDialog(true)//是否显示压缩进度条，默认值：false
                .create();
//        testLuban();
    }

    private void testLuban() {
        String mCacheDir = Constants.BASE_CACHE_PATH + getPackageName() + "/cache/" + Constants.COMPRESS_CACHE;
        Log.e("learn >>> ", mCacheDir);

        Luban.with(this)
                .load("/storage/emulated/0/DCIM/Camera/IMG20190428145516.jpg") //源文件
                .ignoreBy(100) //忽略100KB不压缩
                .setTargetDir(mCacheDir) //压缩后存放路径
                .filter(new CompressionPredicate() { //过滤
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e("learn >>> ", "onStart");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("learn >>> ", file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("learn >>> ", e.toString());
                    }
                }).launch();
    }

    //拍照
    public void camera(View view) {
        //Android 7.0 File路径的变更，需要使用FileProvider来做
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        cameraCachePath = file.getAbsolutePath();
        //启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }

    //相册
    public void album(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //拍照返回
        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {
            //压缩（集合？单张）
            preCompress(cameraCachePath);
        }

        //相册返回
        if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = UriParseUtils.getPath(this, uri);
                //压缩（集合？单张）
                preCompress(path);
            }
        }
    }

    //准备压缩，封装图片集合
    private void preCompress(String photoPath) {
        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo(photoPath));
        if (!photos.isEmpty()) {
            compress(photos);
        }
    }

    private void compress(ArrayList<Photo> photos) {
        if (compressConfig.isShowCompressDialog()) {//配置了开启Dialog
            Log.e("learn >>> ", "open dialog");
            dialog = CommonUtils.showProgressDialog(this, "图片骚动中......");
        }
        //真正的压缩
        CompressImageManager.build(this,compressConfig,photos,this).compress();
    }


    @Override
    public void onCompressSuccess(ArrayList<Photo> images) {
        Log.e("learn >>> ", "compress successful");
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCompressFailed(ArrayList<Photo> images, String error) {
        Log.e("learn >>> ", error);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
