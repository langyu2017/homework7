package com.bytedance.videoplayer;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.VolumeShaper;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnVideoSizeChangedListener, SeekBar.OnSeekBarChangeListener {

    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SeekBar seekbar;
    private Button button;
    private TextView textView;

    private int time;

    private boolean isPlaying;


    private int surfaceWidth;
    private int surfaceHeight;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            seekbar.setProgress(time*1000);

            int hour = time/3600;
            int min = (time%3600)/60;
            int sec = time%60;

            String hourStr=String.valueOf(hour);
            if(hour<10){
                hourStr="0"+hourStr;
            }
            String mintStr=String.valueOf(min);
            if(min<10){
                mintStr="0"+mintStr;
            }
            String sedStr=String.valueOf(sec);
            if(sec<10){
                sedStr="0"+sedStr;
            }
            String timeString = hourStr+":"+mintStr+":"+sedStr;

            textView.setText(timeString);
            Message message = Message.obtain();
            time++;
            handler.sendMessageDelayed(message,1000);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        textView = findViewById(R.id.time);

        seekbar = findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(this);

        button = findViewById(R.id.buttonPause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(isPlaying == false){
                   isPlaying=true;
                   player.start();
                   setSeekbar();
                   button.setText("Pause");

               }
               else{
                   player.pause();
                   handler.removeCallbacksAndMessages(null);
                   button.setText("Play");
                   isPlaying=false;
               }
            }
        });
    }


    private void setSeekbar(){
        player.seekTo(seekbar.getProgress());
        handler.removeCallbacksAndMessages(null);
        Message message = Message.obtain();
        time = (seekbar.getProgress()/1000);
        handler.sendMessage(message);
    }

    public void changeVideoSize( ) {

        int videoWidth = player.getVideoWidth();
        int videoHeight = player.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);

        } else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
    }


    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
        changeVideoSize();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        player = new MediaPlayer();
        player.setOnVideoSizeChangedListener(this);

        if(getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            surfaceWidth=surfaceView.getWidth();
            surfaceHeight=surfaceView.getHeight();
        }else {
            surfaceWidth=surfaceView.getHeight();
            surfaceHeight=surfaceView.getWidth();
        }

        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.setLooping(true);

                int duration = player.getDuration();
                seekbar.setMax(duration);
                player.start();
                Message message = Message.obtain();
                message.arg1 = time;
                handler.sendMessage(message);
                isPlaying =true;
            }
        });

        player.setDisplay(surfaceHolder);
        player.prepareAsync();


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        player.release();
        player = null;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setSeekbar();

    }



}
