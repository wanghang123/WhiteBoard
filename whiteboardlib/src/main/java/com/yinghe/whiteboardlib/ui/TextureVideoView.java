package com.yinghe.whiteboardlib.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

/**
 * 视频播放View
 * @author wang
 * @time on 2016/10/21.
 */
public class TextureVideoView extends TextureView {
    Surface surface = null;
    private MediaPlayer mMediaPlayer;
    private MySurfaceTextureListener mMySurfaceTextureListener;

    private OnVideoCompletionListener mOnVideoCompletionListener;// 播放完成监听，供上层调用

    public TextureVideoView(Context context) {
        super(context);
        initView();
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TextureVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        initPlayer();
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            if (null != surface) {
                mMediaPlayer.setSurface(surface);
            }
        } else {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    /**
     * 开始播放
     *
     * @param path
     */
    private void startPlayer(String path) {
        Log.i("video", "setDataSource-------------------");
        initPlayer();

        try {
            mMediaPlayer.reset();

            if(null!=surface){
                mMediaPlayer.setSurface(surface);
            }
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);// 播放完成监听
        } catch (IOException e) {
            Log.d("video", e.getMessage());
        }
    }

    /**
     * 外部调用开始播放
     *
     * @param path
     */
    public void onStartPlay(String path){
        initPlayer();

        if (mMySurfaceTextureListener == null){
            mMySurfaceTextureListener = new MySurfaceTextureListener(path);
        }

        mMySurfaceTextureListener.setPath(path);
        setSurfaceTextureListener(mMySurfaceTextureListener);
    }

    public void setOnVideoCompletionListener(OnVideoCompletionListener onVideoCompletionListener) {
        mOnVideoCompletionListener = onVideoCompletionListener;
    }

    /**
     * 视频播放完成
     */
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mOnVideoCompletionListener != null){
                mOnVideoCompletionListener.onCompletion();
            }
        }
    };

    /**
     * 监听是否可播放
     */
    public class MySurfaceTextureListener implements SurfaceTextureListener {
        private String path;
        public MySurfaceTextureListener(String path){
            this.path = path;
        }
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);
            startPlayer(path);// 开始播放
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            releaseMedia();

            surfaceTexture.release();
            surfaceTexture = null;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    /**
     * 释放播放资源
     */
    private void releaseMedia(){
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }

            mMediaPlayer.reset();
            mMediaPlayer.release();
//            mMediaPlayer.setSurface(null);
            mMediaPlayer = null;
        }

        if (surface != null){
            surface.release();
            surface = null;
        }
    }

    public void onStopPlay() {
        releaseMedia();
        mMySurfaceTextureListener = null;
    }

    /**
     * 视频播放完成监听
     * @author wang
     * @time on 2016/10/24.
     */
    public interface OnVideoCompletionListener {
        void onCompletion();
    }

}
