package com.hteck.playtube.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.fragment.BaseFragment;
import com.hteck.playtube.fragment.PlaylistVideosView;
import com.hteck.playtube.fragment.PlaylistsView;
import com.hteck.playtube.fragment.PopularView;
import com.hteck.playtube.fragment.YoutubePlayerBottomView;
import com.hteck.playtube.fragment.YoutubePlayerView;
import com.hteck.playtube.view.CustomRelativeLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private static MainActivity _this;

    public static MainActivity getInstance() {
        return _this;
    }

    private PopularView _popularView;
    long _timeExitPressed;
    public boolean mIsOrientationChanged = false;
    private float _oldX = 0, _oldY = 0, _dX, _dY, _posX, _posY, _prevX = 0,
            _prevY = 0;
    private boolean _isPlayerLayoutInitiated = false;
    private boolean _isPlayerShowing, _isSmallPlayer;
    private YoutubePlayerView _youtubePlayerApiView;
    private YoutubePlayerBottomView _youtubePlayerBottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _this = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvTitle = (TextView) toolbar.findViewById(R.id.toolbar_text);
        TextView tvExplore = (TextView) findViewById(R.id.main_activity_text_view_explore);
        TextView tvSearch = (TextView) findViewById(R.id.main_activity_text_view_search);
        TextView tvPlaylists = (TextView) findViewById(R.id.main_activity_text_view_playlists);
        TextView tvSettings = (TextView) findViewById(R.id.main_activity_text_view_settings);
        tvTitle.setText(getTitle());
        tvExplore.setText(Utils.getString(R.string.explore));
        tvSearch.setText(Utils.getString(R.string.search));
        tvPlaylists.setText(Utils.getString(R.string.playlists));
        tvSettings.setText(Utils.getString(R.string.settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);

        init();
        View layoutHot = findViewById(R.id.activity_main_layout_hot);
        layoutHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPopularView();
            }
        });

        View layoutSearch = findViewById(R.id.activity_main_layout_search);
        layoutSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectSearchView();
            }
        });

        View layoutChannel = findViewById(R.id.activity_main_layout_playlists);
        layoutChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPlaylistsView();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isRootLevel()) {
                    View vMain1 = findViewById(R.id.activity_main_layout_main1);
                    zoomImageFromThumb(vMain1);
                } else {
                    doBackStep();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        try {
            PlayTubeController.initImageLoader();

            initView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void zoomImageFromThumb(final View thumbView) {
        final ViewGroup layoutMain = findViewById(R.id.activity_main_layout_container);
        final ViewGroup layoutMain1 = (ViewGroup) findViewById(R.id.activity_main_layout_main1);
        final View vMain1Overlay = findViewById(R.id.activity_main_layout_main1_overlay);
        final View vMain10Overlay = findViewById(R.id.activity_main_layout_main10_overlay);

        Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_out_animation);
        animation.setRepeatMode(0);
        animation.setFillEnabled(true);
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                vMain1Overlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        layoutMain1.startAnimation(animation);
        vMain10Overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutMain1.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_in_animation);
                animation.setDuration(200);
                animation.setFillAfter(true);
                vMain1Overlay.setVisibility(View.GONE);

                layoutMain1.startAnimation(animation);
            }
        });
    }

    private void selectPopularView() {
        try {
            restoreMainAnimation();

            initView();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectSearchView() {
        try {
            restoreMainAnimation();

            com.hteck.playtube.fragment.SearchView searchView = com.hteck.playtube.fragment.SearchView.newInstance(_searchView);
            replaceFragment(searchView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void selectPlaylistsView() {
        try {
            restoreMainAnimation();

            showPlaylists();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void refreshPlaylistData() {
        try {
            for (int i = 0; i < getSupportFragmentManager().getFragments()
                    .size(); ++i) {
                Fragment fragment = getSupportFragmentManager().getFragments().get(i);
                if (fragment != null) {
                    if (fragment instanceof PlaylistsView) {
                        ((PlaylistsView) fragment).refreshData();
                    } else if (fragment instanceof PlaylistVideosView) {
                        ((PlaylistVideosView) fragment).resetData();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void restoreMainAnimation() {
        final ViewGroup layoutMain1 = (ViewGroup) findViewById(R.id.activity_main_layout_main1);
        final View vMain1Overlay = findViewById(R.id.activity_main_layout_main1_overlay);
        layoutMain1.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getInstance(), R.anim.zoom_in_animation);
        animation.setDuration(200);
        animation.setFillAfter(true);
        vMain1Overlay.setVisibility(View.GONE);

        layoutMain1.startAnimation(animation);
    }

    private static Vector<TimerTask> _timerCallback = null;

    public void runInNextLoopUI(final Runnable r, long delayMs) {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    runOnUiThread(r);

                    removeTimerTask(this);
                } catch (Throwable e) {
                }
            }
        };

        if (_timerCallback == null) {
            _timerCallback = new Vector<>(3);
        }
        synchronized (_timerCallback) {
            _timerCallback.add(tt);
        }
        t.schedule(tt, delayMs);
    }

    static void removeTimerTask(TimerTask tt) {
        synchronized (_timerCallback) {
            _timerCallback.remove(tt);
        }
    }

    public Fragment addFragment(Fragment fragment) {
        String loadingFragment = fragment.getClass().getName();
        if (fragment.getArguments() != null
                && fragment.getArguments().containsKey(
                Constants.MAIN_VIEW_ID)) {
            loadingFragment += fragment.getArguments().getString(
                    Constants.MAIN_VIEW_ID);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment existedfragment = fragmentManager
                .findFragmentByTag(loadingFragment);
        if (existedfragment == null) {

            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.activity_main_layout_content, fragment,
                    loadingFragment);

            fragmentTransaction.addToBackStack(loadingFragment);

            fragmentTransaction.commit();
        } else {
            fragmentManager.popBackStack(loadingFragment, 0);
//            updateHomeTitle(fragment);
        }

//        if (!(fragment instanceof SearchView)) {
//            collapseSearchView();
//            if (currentFragment instanceof SearchView) {
//                ((SearchView) currentFragment).setIsNotInEditMode();
//            }
//        }
        return existedfragment;
    }

    public void replaceFragment(Fragment fragment) {
        Fragment currentFragment = getCurrentFrag();
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }
        FragmentManager fragmentManager = MainActivity.getInstance()
                .getSupportFragmentManager();
        fragmentManager.popBackStack(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.replace(R.id.activity_main_layout_content, fragment, fragment
                .getClass().getName());

        fragmentTransaction.addToBackStack(fragment.getClass().getName());
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment, int controlId) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.replace(controlId, fragment);
        fragmentTransaction.commit();
    }

    private Fragment getCurrentFrag() {
        Object obj = getSupportFragmentManager().findFragmentById(
                R.id.activity_main_layout_content);
        if (obj instanceof Fragment) {
            return (Fragment) obj;
        }
        return null;
    }

    private void initView() {
        if (_popularView == null) {
            _popularView = PopularView.newInstance();
        }
        replaceFragment(_popularView);
    }

    private void showPlaylists() {
        PlaylistsView playlistsView = PlaylistsView.newInstance();
        replaceFragment(playlistsView);
    }

    @Override
    public void onBackPressed() {
        if (!doBackStep()) {
            exit();
        }
    }

    public boolean doBackStep() {

        if (isPlayerPlaying()) {
            minimizePlayer();
            return true;
        }
        boolean result = false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
            result = true;
            updateHomeIcon();
        }

        return result;
    }

    private void exit() {
        try {
//        if (_isPlayerShowing && mPlayerYoutubeView != null
//                && mPlayerYoutubeView.isPlaying()) {
//            exitForBackgroundPlaying();
//            return;
//        }
            boolean isOKToQuit = (new Date().getTime() - _timeExitPressed) <= 3000;
            if (isOKToQuit) {
                finish();
                System.exit(1);
                return;
            }
            _timeExitPressed = new Date().getTime();
            Utils.showMessage(Utils.getString(R.string.quit_msg));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void exitForBackgroundPlaying() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this,
                AlertDialog.THEME_HOLO_LIGHT);
        alert.setTitle(Utils.getString(R.string.exit_msg));
        alert.setMessage(Utils.getString(R.string.exit_confirm_msg));

        alert.setPositiveButton(Utils.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    finish();
                    System.exit(1);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNeutralButton(Utils.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dialog.dismiss();
                        } catch (Throwable e) {
                        }
                    }
                });
        alert.show();
    }

    public void playYoutube(YoutubeInfo youtubeInfo, ArrayList<YoutubeInfo> youtubeList, boolean isOutside,
                            boolean isRefreshHistory) {
        try {
            PlayTubeController.setPlayingInfo(youtubeInfo, youtubeList);
            _isPlayerShowing = true;
            if (isOutside) {
                _isSmallPlayer = false;
            }
            if (_youtubePlayerApiView == null) {
                _youtubePlayerBottomView = YoutubePlayerBottomView.newInstance();

                _youtubePlayerApiView = YoutubePlayerView
                        .newInstance(_youtubePlayerBottomView);

                replaceFragment(_youtubePlayerApiView, R.id.activity_main_player_video);
                replaceFragment(_youtubePlayerBottomView,
                        R.id.layout_player_bottom_fragment);

            } else {
                _youtubePlayerApiView.initAndPlayYoutube();
                if (_youtubePlayerBottomView == null) {
                    _youtubePlayerBottomView = YoutubePlayerBottomView.newInstance();
                } else {
                    _youtubePlayerBottomView.resetData();
                }

            }
            if (isOutside) {
                switchPlayerLayout(false);
            }
//            MainContext.increaseNumOfPlays();
//            RecentHelper.addVideoToHistory(youtubeInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void switchPlayerLayout(boolean isMiniPlayer) {
        _isSmallPlayer = isMiniPlayer;
        try {
            ViewGroup layoutPlayerVideoContent = (ViewGroup) findViewById(R.id.activity_main_player_video);
            View layoutPlayerBottom = findViewById(R.id.layout_player_bottom);
            View layoutPlayerAction = findViewById(R.id.main_activity_player_header);
            _layoutPlayerVideoContent = (RelativeLayout) getLayoutPlayerContent();
            RelativeLayout.LayoutParams layoutParamsBody = (RelativeLayout.LayoutParams) _layoutPlayerVideoContent
                    .getLayoutParams();
            ViewGroup.LayoutParams layoutParams = layoutPlayerVideoContent
                    .getLayoutParams();
            boolean isLandscapeScreen = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            if (isMiniPlayer) {
                layoutPlayerBottom.setVisibility(View.GONE);
                layoutPlayerAction.setVisibility(View.VISIBLE);

                layoutParams.height = (int) getResources().getDimension(R.dimen.mini_player_height);
                layoutParams.width = (int) getResources().getDimension(R.dimen.mini_player_width);

                layoutParamsBody.height = layoutParams.height + Utils.convertPointToPixels(32);
                layoutParamsBody.width = layoutParams.width;
                if (_prevX != 0 && _prevY != 0) {
                    layoutParamsBody.leftMargin = (int) _prevX;
                    layoutParamsBody.topMargin = (int) _prevY;
                }
                _layoutPlayerVideoContent.setLayoutParams(layoutParamsBody);

                getLayoutPlayer().setSizeChangedListener(onSizeChangedListener);
                getLayoutPlayer().setVisibility(View.VISIBLE);

                _layoutPlayerVideoContent.setClickable(true);

                _layoutPlayerVideoContent
                        .setOnTouchListener(m_touchImagListener);

//                initPlayerEvents();

                if (!_isPlayerLayoutInitiated) {
                    initPlayerLayout(getLayoutPlayer().getMeasuredWidth(),
                            getLayoutPlayer().getMeasuredHeight(),
                            getLayoutPlayer().getMeasuredWidth(),
                            getLayoutPlayer().getMeasuredHeight());
                }
                _youtubePlayerApiView.setFullScreen(false);
                layoutPlayerBottom.setPadding(0, 0, 0, 0);
            } else {
                layoutPlayerBottom.setVisibility(View.VISIBLE);
                layoutPlayerAction.setVisibility(View.GONE);

                if (isLandscapeScreen) {
//                    visibleActionBar(false);
                    _youtubePlayerApiView.setFullScreen(true);
                } else {
//                    if (!isActionBarShowing()) {
//                        visibleActionBar(true);
//                    }
                    _youtubePlayerApiView.setFullScreen(false);
                }

                layoutParams.height = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getPlayerHeightPortrait();
                layoutParams.width = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getScreenWidth();


                layoutParamsBody.width = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getScreenWidth();
                layoutParamsBody.height = isLandscapeScreen ? ViewGroup.LayoutParams.MATCH_PARENT
                        : Utils.getPlayerHeightPortrait();
                layoutPlayerBottom.setPadding(0,
                        Utils.getPlayerHeightPortrait(), 0, 0);
//                getLayoutPlayer().mEventListener = null;
                _layoutPlayerVideoContent.setOnTouchListener(null);
                layoutParamsBody.leftMargin = 0;
                layoutParamsBody.topMargin = 0;
                if (isLandscapeScreen) {
                    setNavVisibility(false);
                } else {
                    setNavVisibility(true);
                }
            }

            _youtubePlayerApiView.visibleFullScreenButton(!isMiniPlayer);
            getLayoutPlayer().setVisibility(View.VISIBLE);
            layoutPlayerVideoContent.setLayoutParams(layoutParams);
            _layoutPlayerVideoContent.setLayoutParams(layoutParamsBody);
//            updateHomeTitle(null);


//            visibleBannerAds();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void onYoutubeApiOrientationChanged(Configuration newConfig,
                                               boolean isFromPlay) {

        try {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (_youtubePlayerApiView != null && !_isSmallPlayer) {
                    _youtubePlayerApiView.setFullScreen(true);
                }
            } else {
                if (_youtubePlayerApiView != null && !_isSmallPlayer) {
                    _youtubePlayerApiView.setFullScreen(false);
                }
            }
            switchPlayerLayout(_isSmallPlayer);
//            visibleBannerAds(newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private CustomRelativeLayout _layoutPlayerVideo;
    private RelativeLayout _layoutPlayerVideoContent;

    private CustomRelativeLayout getLayoutPlayer() {
        if (_layoutPlayerVideo == null) {
            _layoutPlayerVideo = findViewById(R.id.layout_player);
        }
        return _layoutPlayerVideo;
    }

    private RelativeLayout getLayoutPlayerContent() {
        return (RelativeLayout) findViewById(R.id.layout_player_video);
    }

    private boolean _isOrientationChanged = false;

    CustomRelativeLayout.ISizeChangedListener onSizeChangedListener = new CustomRelativeLayout.ISizeChangedListener() {

        @Override
        public void onLayoutChanged(int xNew, int yNew, int xOld, int yOld) {
            initPlayerLayout(xNew, yNew, xOld, yOld);

        }
    };

    private void initPlayerLayout(int xNew, int yNew, int xOld, int yOld) {
        if (!_isSmallPlayer) {
            return;
        }
        try {
            RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) _layoutPlayerVideoContent
                    .getLayoutParams();
            if (!_isPlayerLayoutInitiated) {
                _oldX = getLayoutPlayer().getMeasuredWidth()
                        - getResources().getDimensionPixelSize(R.dimen.mini_player_width) - Utils.convertPointToPixels(24);
                _oldY = getLayoutPlayer().getMeasuredHeight()
                        - getResources().getDimensionPixelSize(R.dimen.mini_player_height) - Utils.convertPointToPixels(36)
                        - Utils.convertPointToPixels(112);

                layout.leftMargin = (int) _oldX;
                layout.topMargin = (int) _oldY;
                _layoutPlayerVideoContent.setLayoutParams(layout);
                return;
            }

            int x = layout.leftMargin;
            int y = layout.topMargin;

            if (x == 0 && y == 0) {
                layout.leftMargin = (int) _prevX;
                layout.topMargin = (int) _prevY;
            } else {
                if (_isOrientationChanged) {
                    layout.leftMargin = (x + _layoutPlayerVideoContent.getWidth() / 2)
                            * xNew / xOld
                            - _layoutPlayerVideoContent.getWidth() / 2;
                    layout.topMargin = (y + _layoutPlayerVideoContent.getHeight() / 2)
                            * yNew / yOld
                            - _layoutPlayerVideoContent.getHeight() / 2;
                }
                _isOrientationChanged = false;
            }
            if (_layoutPlayerVideoContent.getWidth() + layout.leftMargin > getLayoutPlayer()
                    .getMeasuredWidth()
                    && _layoutPlayerVideoContent.getWidth() < getLayoutPlayer()
                    .getMeasuredWidth()) {
                layout.leftMargin = getLayoutPlayer().getMeasuredWidth()
                        - _layoutPlayerVideoContent.getWidth();
            }
            if (layout.leftMargin < 0) {
                layout.leftMargin = 0;
            }

            if (_layoutPlayerVideoContent.getHeight() + layout.topMargin > getLayoutPlayer()
                    .getMeasuredHeight()
                    && getLayoutPlayer().getMeasuredHeight() > _layoutPlayerVideoContent
                    .getHeight()) {
                layout.topMargin = getLayoutPlayer().getMeasuredHeight()
                        - _layoutPlayerVideoContent.getHeight();
            }
            if (layout.topMargin < 0) {
                layout.topMargin = 0;
            }

            _layoutPlayerVideoContent.setLayoutParams(layout);
            _prevX = layout.leftMargin;
            _prevY = layout.topMargin;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    View.OnTouchListener m_touchImagListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:

                    _oldX = event.getX();
                    _oldY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_POINTER_UP:
                    break;

                case MotionEvent.ACTION_MOVE:
                    _dX = event.getX() - _oldX;
                    _dY = event.getY() - _oldY;

                    _posX = _prevX + _dX;
                    _posY = _prevY + _dY;
                    if (_posX < 0) {
                        _posX = 0;
                    }
                    if (_posY < 0) {
                        _posY = 0;
                    }
                    if ((_posX + v.getWidth()) > getLayoutPlayer().getWidth()) {
                        _posX = getLayoutPlayer().getWidth() - v.getWidth();
                    }
                    if ((_posY + v.getHeight()) > getLayoutPlayer().getHeight()) {
                        _posY = getLayoutPlayer().getHeight() - v.getHeight();
                    }

                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v
                            .getLayoutParams();
                    layoutParams.leftMargin = (int) _posX;
                    layoutParams.topMargin = (int) _posY;
                    v.setLayoutParams(layoutParams);
                    _prevX = _posX;
                    _prevY = _posY;
                    _isPlayerLayoutInitiated = true;
                    break;
            }
            return false;
        }
    };

    public boolean isPlayerPlaying() {
        return _isPlayerShowing && !_isSmallPlayer;
    }

    public void minimizePlayer() {
        try {
            switchPlayerLayout(true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsOrientationChanged = true;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setNavVisibility(false);
        } else {
            setNavVisibility(true);
        }

        if (!_isPlayerShowing) {
            return;
        }
        if (!_isSmallPlayer) {
            _isPlayerLayoutInitiated = false;
        }
        _isOrientationChanged = true;

        onYoutubeApiOrientationChanged(newConfig, false);

    }

    public void setNavVisibility(boolean visible) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (!visible) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        this.getWindow().setAttributes(attrs);
    }

    private boolean isRootLevel() {
        try {
            if (getSupportFragmentManager().getFragments() == null) {
                return true;
            }
            int count = 0;
            for (int i = 0; i < getSupportFragmentManager().getFragments()
                    .size(); ++i) {
                if (getSupportFragmentManager().getFragments().get(i) != null && getSupportFragmentManager().getFragments().get(i) instanceof BaseFragment) {
                    count++;
                }
            }
            return count <= 1;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateHomeIcon() {
        boolean isRoot = isRootLevel();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (isRoot) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        } else {
            getSupportActionBar().setHomeAsUpIndicator(0);
        }
        Fragment fragment = getCurrentFrag();
        if (fragment instanceof BaseFragment) {
            String title = ((BaseFragment) fragment).getTitle();
            setTitle(title);
            if (_searchView != null) {
                if (title.equals(Utils.getString(R.string.search))) {
                    _isInSearchMode = true;
                } else {
                    _isInSearchMode = false;
                }
                visibleSearchView();
            }
        }
    }

    public void setTitle(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvTitle = toolbar.findViewById(R.id.toolbar_text);
        tvTitle.setText(title);
    }

    private SearchView _searchView;
    private boolean _isInSearchMode;
    private Menu _menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _menu = menu;
        getMenuInflater().inflate(R.menu.main_search, menu);
        _searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
//        _searchView.setIconified(false);

        try {
    //            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
    //            searchField.setAccessible(true);
    //            ImageView searchCloseButton = (ImageView) searchField.get(_searchView);
    //            if (searchCloseButton != null) {
    //                searchCloseButton.setEnabled(false);
    //                searchCloseButton.setImageResource(0);
    //            }
            int searchPlateId = _searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
            View searchPlate = _searchView.findViewById(searchPlateId);
            if (searchPlate!=null) {
                searchPlate.setBackgroundColor (Color.WHITE);
                int searchTextId = searchPlate.getContext ().getResources ().getIdentifier ("android:id/search_src_text", null, null);

            }
//            View v = _searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
//            v.getBackground().setColorFilter(getResources().getColor(R.color.textColorTabSelected), PorterDuff.Mode.MULTIPLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        visibleSearchView();
        return true;
    }

    private void visibleSearchView() {
        if (_isInSearchMode) {
            _searchView.setVisibility(View.VISIBLE);
            _menu.findItem(R.id.action_search).setVisible(true);
        } else {
            _searchView.setVisibility(View.INVISIBLE);
            _menu.findItem(R.id.action_search).setVisible(false);
        }
    }
}
