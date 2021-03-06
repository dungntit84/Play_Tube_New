package com.hteck.playtube.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.HistoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class YoutubePlayerView extends YouTubePlayerSupportFragment {
    private YouTubePlayer _youtubePlayer;
    private boolean _isFullScreen;
    private YoutubePlayerBottomView _youtubePlayerBottomView;
    private Timer _timer;
    private TimerTask _task;
    private boolean isRotationChanged = false;
    private boolean _isInit = false;

    public static YoutubePlayerView newInstance(YoutubePlayerBottomView youtubePlayerBottomView) {

        YoutubePlayerView v = new YoutubePlayerView();
        v._youtubePlayerBottomView = youtubePlayerBottomView;

        return v;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        initPlayer();
    }

    private void initPlayer() {
        if (_youtubePlayer == null) {
            try {
                OnInitializedListener initializedListener =
                        new OnInitializedListener() {

                            @Override
                            public void onInitializationFailure(Provider arg0,
                                                                YouTubeInitializationResult arg1) {
                            }

                            @Override
                            public void onInitializationSuccess(
                                    YouTubePlayer.Provider provider,
                                    final YouTubePlayer player, boolean wasRestored) {

                                try {
                                    if (!wasRestored) {
                                        try {
                                            onPause();
                                            onResume();
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    _youtubePlayer = player;

                                    _youtubePlayer
                                            .setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                                    if (!wasRestored) {
                                        playYoutube();
                                    }
                                    _youtubePlayer
                                            .setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

                                    LayoutParams layoutParams = getView().getLayoutParams();
                                    layoutParams.height = LayoutParams.MATCH_PARENT;
                                    layoutParams.width = LayoutParams.MATCH_PARENT;
                                    getView().setLayoutParams(layoutParams);

                                    initEventListener();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }

                            }
                        };
                initialize(PlayTubeController.getConfigInfo().youtubeDevID, initializedListener);

            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            try {
                playYoutube();
            } catch (Throwable e) {
                e.printStackTrace();
                if (e instanceof IllegalStateException) {
                    _youtubePlayer = null;
                    initPlayer();
                }
            }
        }
    }

    private void playYoutube() {
        _youtubePlayer
                .loadVideos(
                        getYoutubeIds(PlayTubeController.getPlayingInfo().getYoutubeList()),
                        PlayTubeController.getPlayingInfo().getCurrentIndex(),
                        0);
    }

    public void visibleFullScreenButton(boolean isVisible) {
        if (_youtubePlayer != null) {
            _youtubePlayer.setShowFullscreenButton(isVisible);
        }
    }

    public void reinitPlayer() {
        stop();
        initPlayer();
    }

    public void stop() {
        if (_youtubePlayer == null) {
            return;
        }
        try {
            _youtubePlayer.pause();
            _youtubePlayer.release();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private List<String> getYoutubeIds(ArrayList<YoutubeInfo> youtubeList) {
        List<String> result = new Vector<String>();
        for (YoutubeInfo youtubeInfo : youtubeList) {
            if (youtubeInfo != null) {
                result.add(youtubeInfo.id);
            }
        }
        return result;
    }

    public boolean isPlaying() {
        try {
            if (_youtubePlayer != null) {
                return _youtubePlayer.isPlaying();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            handleError(e);
        }
        return false;
    }

    public void start() {
        try {
            if (_youtubePlayer != null) {
                _youtubePlayer.play();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initEventListener() {
        _youtubePlayer.setPlaylistEventListener(new PlaylistEventListener() {

            @Override
            public void onPrevious() {
                PlayTubeController.getPlayingInfo().doPrevious();
                updateNewVideoOnPlayer(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo());
            }

            @Override
            public void onPlaylistEnded() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNext() {
                PlayTubeController.getPlayingInfo().doNext();
                updateNewVideoOnPlayer(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo());
            }
        });

        _youtubePlayer.setOnFullscreenListener(new OnFullscreenListener() {

            @Override
            public void onFullscreen(boolean isFullScreen) {
                if (_isInit && _isFullScreen == isFullScreen) {
                    isRotationChanged = false;
                    return;
                }
                if (isFullScreen) {
                    if (!isRotationChanged) {
                        MainActivity
                                .getInstance()
                                .setRequestedOrientation(
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    }
                } else {
                    MainActivity.getInstance().setRequestedOrientation(
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    MainActivity.getInstance().mIsOrientationChanged = false;
                    if (_isFullScreen && !isRotationChanged) {
                        handle();
                    }
                }
                isRotationChanged = false;
                _isFullScreen = isFullScreen;
            }
        });
    }

    private void handle() {
        if (_timer != null) {
            _timer.cancel();
            _timer = null;
            _task.cancel();
            _task = null;
        }
        if (_timer == null) {
            _timer = new Timer();
            _task = new TimerTask() {
                @Override
                public void run() {
                    if (_timer != null) {
                        _timer.cancel();
                        _timer = null;
                        _task.cancel();
                        _task = null;
                    }
                    if (!MainActivity.getInstance().mIsOrientationChanged) {
                        MainActivity.getInstance().runOnUiThread(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        MainActivity.getInstance().minimizePlayer();
                                    }
                                });
                    }
                }
            };

            _timer.schedule(_task, 100);
        }
    }

    public boolean isFullScreen() {
        if (_youtubePlayer != null) {
            return _isFullScreen;
        }
        return false;
    }

    public void setFullScreen(boolean isFullScreen) {
        if (_youtubePlayer == null) {
            return;
        }
        if (_isInit && _isFullScreen == isFullScreen) {
            isRotationChanged = false;
            return;
        }
        _isInit = true;
        isRotationChanged = true;
        try {
            _youtubePlayer.setFullscreen(isFullScreen);
        } catch (Throwable e) {
            handleError(e);
            e.printStackTrace();
        }
    }

    public void updateNewVideoOnPlayer(YoutubeInfo youtubeInfo) {
        if (_youtubePlayerBottomView != null) {
            HistoryService.addYoutubeToHistory(youtubeInfo);
            _youtubePlayerBottomView.refreshData();
            MainActivity.getInstance().refreshHistoryData();
        }
    }

    private void handleError(Throwable e) {
        try {
            if (e instanceof IllegalStateException) {
                _youtubePlayer = null;
                initPlayer();
                MainActivity.getInstance().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}