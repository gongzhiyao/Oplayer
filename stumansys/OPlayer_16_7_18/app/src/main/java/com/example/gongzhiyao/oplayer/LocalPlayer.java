package com.example.gongzhiyao.oplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.gongzhiyao.oplayer.Controller.VideoViewMediaController;
import com.example.gongzhiyao.oplayer.Log.L;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.StreamHandler;

import io.vov.vitamio.widget.VideoView;

public class LocalPlayer extends AppCompatActivity implements Runnable {

    private VideoView mVideoView;
    private VideoViewMediaController controller;
    private static final int TIME = 0;
    private static final int BATTERY = 1;
    private L log;




    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME:
                    controller.setTime(msg.obj.toString());
                    break;
                case BATTERY:
                    controller.setBattery(msg.obj.toString());

                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = LocalPlayer.this.getWindow();
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_local_player);
        initView();
        Intent i = getIntent();
        String path = i.getStringExtra("path");
        log.d("得到的路径是" + path);
        mVideoView.setVideoPath(path);
        controller = new VideoViewMediaController(this, mVideoView, this);

        mVideoView.setMediaController(controller);
        /**
         * 这里有个高画质，可以不用写，因为是本地
         */

        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH, 0);
        mVideoView.requestFocus();
        registerBoradcastReceiver();
        new Thread(this).start();


    }

    public void registerBoradcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryBroadcastReceiver, intentFilter);

    }


    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                Message msg = new Message();
                msg.obj = (level * 100) / scale + "";
                msg.what = BATTERY;
                mHandler.sendMessage(msg);
            }


        }
    };


    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.videoView_Local);
        log = new L();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        if (mVideoView != null) {
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);

        }
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void run() {
        while (true){
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
            String str=sdf.format(new Date());
            Message msg=new Message();
            msg.what=TIME;
            msg.obj=str;
            mHandler.sendMessage(msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(batteryBroadcastReceiver);
        } catch (IllegalArgumentException ex) {

        }
    }
}
