package com.itcast.zz.longpresssaveimagetolocal;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.iv)
    ImageView mIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("保存图片？");
                builder.setNegativeButton("取消",null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveImageToLocal(getViewBitmap(v));
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    private void saveImageToLocal(Bitmap bitmap) {
        Observable.create(new SaveObserver(bitmap))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SaveSubscriber());
    }

    private Bitmap getViewBitmap(View v) {
        if(v==null){
            return null;
        }
        Bitmap bitmap=Bitmap.createBitmap(v.getWidth(),v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private class SaveObserver implements Observable.OnSubscribe<String>{
        private Bitmap mBitmap=null;
        public SaveObserver(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        @Override
        public void call(Subscriber<? super String> subscriber) {
            if (mBitmap == null) {
                subscriber.onError(new NullPointerException("image获取为null"));
            } else {
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), "bitmap1.jpg");
                    FileOutputStream outputStream = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    subscriber.onNext(Environment.getExternalStorageDirectory().getPath());
                    subscriber.onCompleted();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class SaveSubscriber extends Subscriber<String>{

        @Override
        public void onCompleted() {
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(String s) {
            Toast.makeText(getApplicationContext(),"保存的路径为"+s,Toast.LENGTH_SHORT).show();
        }
    }

}
