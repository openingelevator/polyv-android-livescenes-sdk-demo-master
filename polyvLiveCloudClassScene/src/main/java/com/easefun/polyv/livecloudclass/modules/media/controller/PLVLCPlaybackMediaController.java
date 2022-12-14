package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCPlaybackMoreLayout;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.imageview.IPLVVisibilityChangedListener;
import com.easefun.polyv.livecommon.ui.widget.imageview.PLVSimpleImageView;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * ??????????????????????????????????????? IPLVLCPlaybackMediaController ??????
 */
public class PLVLCPlaybackMediaController extends FrameLayout implements IPLVLCPlaybackMediaController, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = "PLVLCPlaybackMediaController";
    private static final int SHOW_TIME = 5000;//?????????????????????

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    /**** ??????View **/
    private ImageView ivPlayPauseLand;
    private TextView tvCurrentTimeLand;
    private TextView tvTotalTimeLand;
    private ImageView ivSubviewShowLand;
    private SeekBar sbPlayProgressLand;
    private ImageView btnMoreLand;
    private ImageView ivBackLand;
    private TextView tvVideoNameLand;
    private PLVLCLikeIconView ivLikesLand;
    private TextView tvStartSendMessageLand;
    private ImageView ivDanmuSwitchLand;
    private RelativeLayout rlRootLand;
    private PLVSimpleImageView controllerCommodityLandIv;
    private PLVSimpleImageView cardEnterLandView;
    private TextView cardEnterCdLandTv;
    private PLVTriangleIndicateTextView cardEnterTipsLandView;
    private View likesReferView;
    private View cardEnterReferView;
    /**** ??????View **/
    private ImageView ivPlayPausePort;
    private TextView tvCurrentTimePort;
    private TextView tvTotalTimePort;
    private SeekBar sbPlayProgressPort;
    private ImageView iVFullScreenPort;
    private ImageView ivSubviewShowPort;
    private ImageView btnMorePort;
    private ImageView ivBackPort;
    private ImageView ivTopGradientPort;
    private TextView tvVideoNamePort;
    private RelativeLayout rlRootPort;
    /**** ???????????? **/
    private PLVLCPlaybackMoreLayout moreLayout;
    //???????????????????????????
    private TextView tvReopenFloatingViewTip;

    /**** status **/
    // ????????????????????????????????????
    private boolean isPbDragging;
    //???????????????"??????????????????????????????"????????????
    private boolean hasShowReopenFloatingViewTip = false;

    //????????????"??????????????????????????????"
    private Disposable reopenFloatingDelay;

    //player presenter
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playerPresenter;

    //????????????PPT??????
    private boolean isServerEnablePPT;

    //Listener
    private OnViewActionListener onViewActionListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVLCPlaybackMediaController(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPlaybackMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPlaybackMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????view">
    private void initView() {
        //create and add view
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_controller_layout, this, true);

        //find all view
        //land layout
        ivPlayPauseLand = findViewById(R.id.plvlc_playback_controller_land_iv_playpause);
        tvCurrentTimeLand = findViewById(R.id.plvlc_playback_controller_land_tv_currenttime);
        tvTotalTimeLand = findViewById(R.id.plvlc_playback_controller_land_tv_totaltime);
        ivSubviewShowLand = findViewById(R.id.plvlc_playback_controller_land_iv_subview_show_land);
        sbPlayProgressLand = findViewById(R.id.plvlc_playback_controller_land_sb_playprogress);
        btnMoreLand = findViewById(R.id.plvlc_playback_controller_land_bt_more);
        ivBackLand = findViewById(R.id.plvlc_playback_controller_land_iv_back);
        tvVideoNameLand = findViewById(R.id.plvlc_playback_controller_land_tv_video_name);
        ivLikesLand = findViewById(R.id.plvlc_playback_controller_land_iv_likes);
        tvStartSendMessageLand = findViewById(R.id.plvlc_playback_controller_land_tv_start_send_message);
        ivDanmuSwitchLand = findViewById(R.id.plvlc_playback_controller_land_iv_danmu_switch);
        rlRootLand = findViewById(R.id.plvlc_playback_controller_land_rl_root);
        controllerCommodityLandIv = findViewById(R.id.plvlc_controller_commodity_land_iv);
        cardEnterLandView = findViewById(R.id.plvlc_card_enter_land_view);
        cardEnterCdLandTv = findViewById(R.id.plvlc_card_enter_cd_land_tv);
        cardEnterTipsLandView = findViewById(R.id.plvlc_card_enter_tips_land_view);
        likesReferView = findViewById(R.id.plvlc_refer_view_1);
        cardEnterReferView = findViewById(R.id.plvlc_refer_view_2);

        //port layout
        ivPlayPausePort = findViewById(R.id.plvlc_playback_controller_port_iv_play_pause);
        tvCurrentTimePort = findViewById(R.id.plvlc_playback_controller_port_tv_currenttime);
        tvTotalTimePort = findViewById(R.id.plvlc_playback_controller_port_tv_totaltime);
        sbPlayProgressPort = findViewById(R.id.plvlc_playback_controller_port_sb_playprogress);
        iVFullScreenPort = findViewById(R.id.plvlc_playback_controller_port_iv_full_screen);
        ivSubviewShowPort = findViewById(R.id.plvlc_playback_controller_port_iv_subview_show);
        btnMorePort = findViewById(R.id.plvlc_playback_controller_port_btn_controller_more);
        ivBackPort = findViewById(R.id.plvlc_playback_controller_port_iv_back);
        ivTopGradientPort = findViewById(R.id.plvlc_playback_controller_port_iv_top_gradient);
        tvVideoNamePort = findViewById(R.id.plvlc_playback_controller_port_tv_video_name);
        rlRootPort = findViewById(R.id.plvlc_playback_controller_port_rl_root);

        tvReopenFloatingViewTip = findViewById(R.id.plvlc_playback_player_controller_tv_reopen_floating_view);

        ivBackLand.setOnClickListener(this);
        ivPlayPauseLand.setOnClickListener(this);
        ivPlayPausePort.setOnClickListener(this);
        iVFullScreenPort.setOnClickListener(this);
        sbPlayProgressLand.setOnSeekBarChangeListener(playProgressChangeListener);
        sbPlayProgressPort.setOnSeekBarChangeListener(playProgressChangeListener);
        ivSubviewShowLand.setOnClickListener(this);
        ivSubviewShowPort.setOnClickListener(this);
        btnMoreLand.setOnClickListener(this);
        btnMorePort.setOnClickListener(this);
        ivBackPort.setOnClickListener(this);
        ivLikesLand.setOnButtonClickListener(this);
        tvStartSendMessageLand.setOnClickListener(this);
        controllerCommodityLandIv.setOnClickListener(this);

        //more layout
        initMoreLayout();

        //choose right orientation
        if (PLVScreenUtils.isLandscape(getContext())) {
            setLandscapeController();
        } else {
            setPortraitController();
        }

        observeCommodityStatus();
        observeForFitRightBottomViewLocation();
    }

    private void initMoreLayout() {
        moreLayout = new PLVLCPlaybackMoreLayout(this);
        moreLayout.setOnSpeedSelectedListener(new PLVLCPlaybackMoreLayout.OnSpeedSelectedListener() {
            @Override
            public void onSpeedSelected(Float speed, int pos) {
                playerPresenter.setSpeed(speed);
            }
        });
    }

    private void observeCommodityStatus() {
        commodityViewModel.getCommodityUiStateLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVCommodityUiState>() {
                    boolean needShowControllerOnClosed = false;

                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState uiState) {
                        if (uiState == null) {
                            return;
                        }
                        controllerCommodityLandIv.setVisibility(uiState.hasProductView ? View.VISIBLE : View.GONE);
                        if (uiState.showProductViewOnLandscape) {
                            if (!needShowControllerOnClosed) {
                                needShowControllerOnClosed = isShowing();
                                hide();
                            }
                        } else {
                            if (needShowControllerOnClosed) {
                                show();
                                needShowControllerOnClosed = false;
                            }
                        }
                    }
                });
    }

    private void observeForFitRightBottomViewLocation() {
        ivLikesLand.setVisibilityChangedListener(new IPLVVisibilityChangedListener() {
            @Override
            public void onChanged(int visibility) {
                processRightBottomViewVisibilityChanged(visibility, true);
            }
        });
        cardEnterLandView.setVisibilityChangedListener(new IPLVVisibilityChangedListener() {
            @Override
            public void onChanged(int visibility) {
                processRightBottomViewVisibilityChanged(visibility, false);
            }
        });
        controllerCommodityLandIv.setVisibilityChangedListener(new IPLVVisibilityChangedListener() {
            @Override
            public void onChanged(int visibility) {
                processRightBottomViewVisibilityChanged(visibility, false);
            }
        });
    }

    private void processRightBottomViewVisibilityChanged(int visibility, boolean isLikesView) {
        if (likesReferView.getLayoutParams() == null) {
            return;
        }
        boolean isVisible = visibility == View.VISIBLE;
        if (isLikesView) {
            if (isVisible || !hasRightBottomViewVisibleExcludeLikesView()) {
                likesReferView.getLayoutParams().width = ConvertUtils.dp2px(60);
            } else {
                likesReferView.getLayoutParams().width = ConvertUtils.dp2px(4);
            }
        } else {
            if (isVisible) {
                if (ivLikesLand.getVisibility() != View.VISIBLE) {
                    likesReferView.getLayoutParams().width = ConvertUtils.dp2px(4);
                }
            } else if (!hasRightBottomViewVisibleExcludeLikesView()) {
                likesReferView.getLayoutParams().width = ConvertUtils.dp2px(60);
            }
        }
        MarginLayoutParams mlp = (MarginLayoutParams) cardEnterLandView.getLayoutParams();
        if (mlp != null) {
            mlp.rightMargin = ConvertUtils.dp2px(isRightBottomOnlyCardEnterViewVisible() ? 44 : 20);
        }
        MarginLayoutParams cardEnterReferMlp = (MarginLayoutParams) cardEnterReferView.getLayoutParams();
        if (cardEnterReferMlp != null) {
            cardEnterReferMlp.rightMargin = ConvertUtils.dp2px(isRightBottomOnlyCardEnterViewVisible() ? 34 : 10);
        }
        likesReferView.requestLayout();
    }

    private boolean isRightBottomOnlyCardEnterViewVisible() {
        return cardEnterLandView.getVisibility() == View.VISIBLE && controllerCommodityLandIv.getVisibility() != View.VISIBLE && ivLikesLand.getVisibility() != View.VISIBLE;
    }

    private boolean hasRightBottomViewVisibleExcludeLikesView() {
        return cardEnterLandView.getVisibility() == View.VISIBLE || controllerCommodityLandIv.getVisibility() == View.VISIBLE;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ??????IPLVLCLiveMediaController?????????IPolyvMediaController???????????????">
    @Override
    public void onPrepared(PolyvPlaybackVideoView videoView) {
        updateTotalTimeView();
        updateMoreLayout();
        updateVideoName();
    }

    @Override
    public void onLongBuffering(String tip) {

    }

    @Override
    public void hide() {
        setVisibility(GONE);
    }

    @Override
    public boolean isShowing() {
        return isShown();
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ??????IPLVLCPlaybackMediaController???????????????">
    @Override
    public void setPlaybackPlayerPresenter(IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playerPresenter) {
        this.playerPresenter = playerPresenter;
        observePlayInfoVO();
    }

    @Override
    public View getLandscapeDanmuSwitchView() {
        return ivDanmuSwitchLand;
    }

    @Override
    public ImageView getCardEnterView() {
        return cardEnterLandView;
    }

    @Override
    public TextView getCardEnterCdView() {
        return cardEnterCdLandTv;
    }

    @Override
    public PLVTriangleIndicateTextView getCardEnterTipsView() {
        return cardEnterTipsLandView;
    }

    @Override
    public void setOnLikesSwitchEnabled(boolean isSwitchEnabled) {
        ivLikesLand.setVisibility(isSwitchEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setServerEnablePPT(boolean enable) {
        this.isServerEnablePPT = enable;
        ivSubviewShowPort.setVisibility(enable ? View.VISIBLE : View.GONE);
        ivSubviewShowLand.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    @Override
    public void playOrPause() {
        if (playerPresenter.isPlaying()) {
            playerPresenter.pause();
            ivPlayPausePort.setSelected(false);
            ivPlayPauseLand.setSelected(false);
        } else {
            playerPresenter.resume();
            ivPlayPausePort.setSelected(true);
            ivPlayPauseLand.setSelected(true);
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void updateOnClickCloseFloatingView() {
        ivSubviewShowPort.performClick();
        if (!hasShowReopenFloatingViewTip) {
            hasShowReopenFloatingViewTip = true;
            tvReopenFloatingViewTip.setVisibility(VISIBLE);
            dispose(reopenFloatingDelay);
            reopenFloatingDelay = PLVRxTimer.delay(3000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    tvReopenFloatingViewTip.setVisibility(GONE);
                }
            });
        }
    }

    @Override
    public void dispatchDanmuSwitchOnClicked(View v) {
        this.onClick(v);
    }

    @Override
    public void setChatPlaybackEnabled(boolean isChatPlaybackEnabled) {
        if (tvStartSendMessageLand != null) {
            if (isChatPlaybackEnabled) {
                tvStartSendMessageLand.setText("?????????????????????");
                tvStartSendMessageLand.setEnabled(false);
            } else {
                tvStartSendMessageLand.setText("????????????????????????~");
                tvStartSendMessageLand.setEnabled(true);
            }
        }
    }

    @Override
    public void notifyChatroomStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus) {
        if (tvStartSendMessageLand != null) {
            tvStartSendMessageLand.setText(isCloseRoomStatus ? "??????????????????" : (isFocusModeStatus ? "????????????????????????????????????" : "????????????????????????~"));
            tvStartSendMessageLand.setOnClickListener((!isCloseRoomStatus && !isFocusModeStatus) ? this : null);
        }
    }

    @Override
    public void clean() {
        if (moreLayout != null) {
            moreLayout.hide();
        }

        dispose(reopenFloatingDelay);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ???????????????????????????view">
    private void updateMoreLayout() {
        moreLayout.updateViewWithPlayInfo(playerPresenter.getSpeed());
    }

    private void updateTotalTimeView() {
        String totalTime = "/" + PLVTimeUtils.generateTime(playerPresenter.getDuration(), true);
        tvTotalTimePort.setText(totalTime);
        tvTotalTimeLand.setText(totalTime);
    }

    private void updateVideoName() {
        tvVideoNamePort.setText(playerPresenter.getVideoName());
        tvVideoNameLand.setText(playerPresenter.getVideoName());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ???????????????????????????">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            show();
            isPbDragging = true;
            int seekPosition = (int) ((long) playerPresenter.getDuration() * progress / seekBar.getMax());
            tvCurrentTimePort.setText(PLVTimeUtils.generateTime(seekPosition, true));
            tvCurrentTimeLand.setText(PLVTimeUtils.generateTime(seekPosition, true));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPbDragging = false;
            playerPresenter.seekTo(seekBar.getProgress(), seekBar.getMax());
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeController();
        } else {
            setPortraitController();
        }
    }

    private void setLandscapeController() {
        post(new Runnable() {
            @Override
            public void run() {
                rlRootPort.setVisibility(GONE);
                rlRootLand.setVisibility(VISIBLE);
            }
        });
    }

    private void setPortraitController() {
        post(new Runnable() {
            @Override
            public void run() {
                rlRootPort.setVisibility(VISIBLE);
                rlRootLand.setVisibility(GONE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ????????????????????????">
    private void observePlayInfoVO() {
        //????????????????????????
        playerPresenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                if (playInfoVO == null) {
                    return;
                }
                int position = playInfoVO.getPosition();
                int totalTime = playInfoVO.getTotalTime();
                int bufPercent = playInfoVO.getBufPercent();
                boolean isPlaying = playInfoVO.isPlaying();
                //?????????????????????????????????????????????
                if (!isPbDragging) {
                    tvCurrentTimePort.setText(PLVTimeUtils.generateTime(position, true));
                    tvCurrentTimeLand.setText(PLVTimeUtils.generateTime(position, true));
                    if (totalTime > 0) {
                        sbPlayProgressPort.setProgress((int) ((long) sbPlayProgressPort.getMax() * position / totalTime));
                        sbPlayProgressLand.setProgress((int) ((long) sbPlayProgressLand.getMax() * position / totalTime));
                    } else {
                        sbPlayProgressPort.setProgress(0);
                        sbPlayProgressLand.setProgress(0);
                    }
                }
                sbPlayProgressPort.setSecondaryProgress(sbPlayProgressPort.getMax() * bufPercent / 100);
                sbPlayProgressLand.setSecondaryProgress(sbPlayProgressPort.getMax() * bufPercent / 100);
                if (isPlaying) {
                    ivPlayPausePort.setSelected(true);
                    ivPlayPauseLand.setSelected(true);
                } else {
                    ivPlayPausePort.setSelected(false);
                    ivPlayPauseLand.setSelected(false);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvlc_playback_controller_port_iv_back) {
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.plvlc_playback_controller_land_iv_back) {
            //??????????????????
            PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
        } else if (id == R.id.plvlc_playback_controller_land_iv_playpause || id == R.id.plvlc_playback_controller_port_iv_play_pause) {
            //????????????/??????
            playOrPause();
        } else if (id == R.id.plvlc_playback_controller_port_iv_full_screen) {
            //??????????????????
            PLVOrientationManager.getInstance().setLandscape((Activity) getContext());
        } else if (id == R.id.plvlc_playback_controller_land_iv_subview_show_land || id == R.id.plvlc_playback_controller_port_iv_subview_show) {
            //selected == true????????????????????????false??????????????????
            boolean isNotShowState = ivSubviewShowPort.isSelected();
            boolean isShowState = !isNotShowState;
            ivSubviewShowPort.setSelected(isShowState);
            ivSubviewShowLand.setSelected(isShowState);
            if (onViewActionListener != null) {
                onViewActionListener.onClickShowOrHideSubTab(isNotShowState);
            }
        } else if (id == R.id.plvlc_playback_controller_land_bt_more || id == R.id.plvlc_playback_controller_port_btn_controller_more) {
            hide();
            if (ScreenUtils.isPortrait()) {
                moreLayout.showWhenPortrait(getHeight());
            } else {
                moreLayout.showWhenLandscape();
            }
        } else if (id == R.id.plvlc_playback_controller_land_iv_likes) {
            show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivLikesLand.addLoveIcon(1);
                }
            }, 200);
            if (onViewActionListener != null) {
                onViewActionListener.onSendLikesAction();
            }
        } else if (id == R.id.plvlc_playback_controller_land_tv_start_send_message) {
            hide();
            if (onViewActionListener != null) {
                onViewActionListener.onStartSendMessageAction();
            }
        } else if (id == controllerCommodityLandIv.getId()) {
            commodityViewModel.showProductLayoutOnLandscape();
        }
    }
    // </editor-fold>
}
