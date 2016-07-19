package com.example.gongzhiyao.oplayer.Controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gongzhiyao.oplayer.LocalPlayer;
import com.example.gongzhiyao.oplayer.R;

import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by 宫智耀 on 2016/6/24.
 */
public class VideoViewMediaController extends MediaController {

    private GestureDetector mGestureDetector;
    private ImageButton img_back;//返回键
    private ImageView img_Battery;//电池电量显示
    private TextView textViewTime;//时间提示
    private TextView textViewBattery;//文字显示电池
    private VideoView videoView;
    private Activity activity;
    private Context context;
    private int controllerWidth = 0;//设置mediaController高度为了使横屏时top显示在屏幕顶端


    private View mVolumeBrightnessLayout;
    private ImageView mOperationBg;
    private ImageView mOperationPercent;
    private AudioManager mAudioManager;
    /**
     * 最大声音
     */
    private int mMaxVolume;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    /**
     * 当前缩放模式
     */
    private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;


    //返回监听
    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            if (activity != null) {
                activity.finish();
            }
        }
    };


    //videoview 用于对视频进行控制的等，activity为了退出
    public VideoViewMediaController(Context context, VideoView videoView, Activity activity) {
        super(context);
        this.context = context;
        this.videoView = videoView;
        this.activity = activity;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        controllerWidth = wm.getDefaultDisplay().getWidth();




        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }

    @Override
    protected View makeControllerView() {
//        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("controller", "layout", getContext().getPackageName()), this);
        View v = ((LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(getResources()
                        .getIdentifier("controller", "layout", getContext().getPackageName()), this);
        v.setMinimumHeight(controllerWidth);
        img_back = (ImageButton) v.findViewById(getResources().getIdentifier("mediacontroller_top_back", "id", context.getPackageName()));
        img_Battery = (ImageView) v.findViewById(getResources().getIdentifier("mediacontroller_imgBattery", "id", context.getPackageName()));
        img_back.setOnClickListener(backListener);
        textViewBattery = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_Battery", "id", context.getPackageName()));
        textViewTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time", "id", context.getPackageName()));
//        mChangeScreen= (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_change_screen", "id", context.getPackageName()));
//        mChangeScreen.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                context.startActivity(new Intent(context, FullScreenPlay.class));
//            }
//        });


        mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);




        return v;

    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("MYApp-MyMediaController-dispatchKeyEvent");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }


    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                mVolumeBrightnessLayout.setVisibility(View.GONE);

                    break;
            }

        }
    };




    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //当收拾结束，并且是单击结束时，控制器隐藏/显示
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = activity.getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }


        //双击暂停或开始
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            playOrPause();
            return true;
        }
    }





    public void setTime(String time) {
        if (textViewTime != null)
            textViewTime.setText(time);
    }

    //显示电量，
    public void setBattery(String stringBattery) {
        if (textViewTime != null && img_Battery != null) {
            textViewBattery.setText(stringBattery + "%");
            int battery = Integer.valueOf(stringBattery);
            if (battery < 15)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_15));
            if (battery < 30 && battery >= 15)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_20));
            if (battery < 45 && battery >= 30)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_30));
            if (battery < 60 && battery >= 45)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_45));
            if (battery < 75 && battery >= 60)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_60));
            if (battery < 90 && battery >= 75)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_75));
            if (battery > 90)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_90));
        }
    }

    //隐藏/显示
    private void toggleMediaControlsVisiblity() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    //播放与暂停
    private void playOrPause() {
        if (videoView != null)
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
                * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        activity.getWindow().setAttributes(lpa);

        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (videoView != null)
            videoView.setVideoLayout(mLayout, 0);
        super.onConfigurationChanged(newConfig);

    }
}