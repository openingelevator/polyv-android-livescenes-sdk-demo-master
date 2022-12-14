package com.easefun.polyv.liveecommerce.scenes;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.transformList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chapter.di.PLVPlaybackChapterModule;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.playback.di.PLVPlaybackCacheModule;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.popover.IPLVPopoverLayout;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.IPLVECVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECLiveVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECPlaybackVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindowModule;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECCommonHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECEmptyFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveDetailFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECPalybackHomeFragment;
import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.socket.user.PLVSocketUserConstant;

import java.io.File;
import java.util.List;

/**
 * ?????????????????????????????? ??????????????????????????? ??? ???????????????
 * ????????????????????????????????????????????????????????????????????????????????????
 * ???????????????????????????????????????
 */
public class PLVECLiveEcommerceActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = PLVECLiveEcommerceActivity.class.getSimpleName();
    // ?????? - ??????????????????????????????
    private static final String EXTRA_CHANNEL_ID = "channelId";   // ?????????
    private static final String EXTRA_VIEWER_ID = "viewerId";   // ?????????Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // ???????????????
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//?????????????????????
    private static final String EXTRA_VID = "vid";//??????Id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//??????????????????
    private static final String EXTRA_IS_LIVE = "is_live";//???????????????

    // ???????????????????????????????????????????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // ???????????????
    private IPLVECVideoLayout videoLayout;
    // ?????? - ???????????? - ????????? viewpager ????????????????????????fragment
    private ViewPager viewPager;
    // ??????????????? ???????????????????????? fragment
    private PLVECLiveDetailFragment liveDetailFragment;
    // ??????????????? ?????? fragment
    private PLVECCommonHomeFragment commonHomeFragment;
    // ??????????????? ????????? fragment ??????fragment???????????????????????????????????????????????????
    private PLVECEmptyFragment emptyFragment;
    //??????Layout
    private IPLVPopoverLayout popoverLayout;

    // ????????????
    private PLVECFloatingWindow floatingWindow;
    //?????????????????????????????????
    private boolean isUserCloseFloatingWindow;
    //?????????????????????
    protected GestureDetector gestureScanner;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????Activity?????????">

    /**
     * ???????????????????????????
     *
     * @param activity   ?????????Activity
     * @param channelId  ?????????
     * @param viewerId   ??????ID
     * @param viewerName ????????????
     * @return PLVLaunchResult.isSuccess=true?????????????????????PLVLaunchResult.isSuccess=false??????????????????
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchLive(@NonNull Activity activity, @NonNull String channelId, @NonNull String viewerId, @NonNull String viewerName,@NonNull String viewerAvatar) {
        if (activity == null) {
            return PLVLaunchResult.error("activity ?????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName ??????????????????????????????????????????");
        }
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_IS_LIVE, true);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }

    /**
     * ???????????????????????????
     *
     * ??????????????????vid??????????????????????????????????????????????????????????????????vid???????????????????????????vid????????????
     * ?????????????????????????????????????????????
     * ????????????????????????vid????????????????????????????????????????????????????????????
     * PLVLaunchResult.error("vid ??????????????????????????????????????????")?????????
     *
     * @param activity      ?????????Activity
     * @param channelId     ?????????
     * @param vid           ??????ID
     * @param viewerId      ??????ID
     * @param viewerName    ????????????
     * @param videoListType ?????????????????? {@link PLVPlaybackListType}
     * @return PLVLaunchResult.isSuccess=true?????????????????????PLVLaunchResult.isSuccess=false??????????????????
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchPlayback(@NonNull Activity activity, @NonNull String channelId, @NonNull String vid, @NonNull String viewerId, @NonNull String viewerName, @NonNull String viewerAvatar, PLVPlaybackListType videoListType) {
        if (activity == null) {
            return PLVLaunchResult.error("activity ?????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId ??????????????????????????????????????????");
        }
//        if (TextUtils.isEmpty(vid)) {
//            return PLVLaunchResult.error("vid ??????????????????????????????????????????");
//        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName ??????????????????????????????????????????");
        }
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_VIDEO_LIST_TYPE, videoListType);
        intent.putExtra(EXTRA_IS_LIVE, false);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependency();
        setContentView(R.layout.plvec_live_ecommerce_page_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initFloatingWindowSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUserCloseFloatingWindow) {
            videoLayout.setPlayerVolume(100);
        }
        isUserCloseFloatingWindow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVFloatingPlayerManager.getInstance().runOnFloatingWindowClosed(new Runnable() {
            @Override
            public void run() {
                PLVFloatingPlayerManager.getInstance().clear();
                if(popoverLayout != null){
                    popoverLayout.destroy();
                }
                if (videoLayout != null) {
                    videoLayout.destroy();
                }
                if (liveRoomDataManager != null) {
                    liveRoomDataManager.destroy();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(popoverLayout != null && popoverLayout.onBackPress()){
            return;
        }
        super.onBackPressed();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (videoLayout != null) {
            videoLayout.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">

    private void injectDependency() {
        PLVDependManager.getInstance().switchStore(this)
                .addModule(PLVPlaybackCacheModule.instance)
                .addModule(PLVPlaybackChapterModule.instance)
                .addModule(PLVECFloatingWindowModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void initParams() {
        // ??????????????????
        final Intent intent = getIntent();
        final boolean isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true);
        final String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        final String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        final String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        final String viewerAvatar = intent.getStringExtra(EXTRA_VIEWER_AVATAR);
        final String vid = intent.getStringExtra(EXTRA_VID);
        final PLVPlaybackListType videoListType = (PLVPlaybackListType) intent.getSerializableExtra(EXTRA_VIDEO_LIST_TYPE);

        // ??????Config??????
        PLVLiveChannelConfigFiller.setIsLive(isLive);
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, viewerAvatar,
                PolyvLinkMicConfig.getInstance().getLiveChannelType() == PolyvLiveChannelType.PPT
                        ? PLVSocketUserConstant.USERTYPE_SLICE : PLVSocketUserConstant.USERTYPE_STUDENT);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);

        PLVFloatingPlayerManager.getInstance().saveIntent(intent);
        // ???????????????????????????????????????
        if (isLive) {
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_live");
        } else { // ????????????
            PLVLiveChannelConfigFiller.setupVid(vid);
            PLVLiveChannelConfigFiller.setupVideoListType(videoListType != null ? videoListType : PLVPlaybackListType.PLAYBACK);
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_" + (vid == null ? "playback" : vid));
        }

        initPlaybackParam(vid, channelId, viewerId, viewerName, viewerAvatar, PLVLiveChannelType.ALONE, videoListType);
    }

    private void initPlaybackParam(
            final String vid,
            final String channelId,
            final String viewerId,
            final String viewerName,
            final String viewerAvatar,
            final PLVLiveChannelType channelType,
            final PLVPlaybackListType playbackListType
    ) {
        PLVDependManager.getInstance().get(PLVPlaybackCacheConfig.class)
                .setApplicationContext(getApplicationContext())
                .setDatabaseNameByViewerId(viewerId)
                .setDownloadRootDirectory(new File(PLVPlaybackCacheConfig.defaultPlaybackCacheDownloadDirectory(this)));
        PLVDependManager.getInstance().get(PLVPlaybackCacheVideoConfig.class)
                .setVid(vid)
                .setVideoPoolIdByVid(vid)
                .setChannelId(channelId)
                .setViewerId(viewerId)
                .setViewerName(viewerName)
                .setViewerAvatar(viewerAvatar)
                .setChannelType(channelType)
                .setPlaybackListType(playbackListType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????????????????">
    private void initLiveRoomManager() {
        // ??????PLVLiveChannelConfigFiller?????????????????????????????????????????????????????????????????????
        liveRoomDataManager = new PLVLiveRoomDataManager(PLVLiveChannelConfigFiller.generateNewChannelConfig());

        // ?????????????????????????????????????????????
        liveRoomDataManager.requestPageViewer();

        // ?????????????????????????????????????????????
        liveRoomDataManager.requestChannelDetail();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????UI">
    private void initView() {
        // ??????????????????
        findViewById(R.id.close_page_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ????????????ViewPage
        viewPager = findViewById(R.id.watch_info_vp);
        initEmptyFragment();
        initLiveDetailFragment();
        initCommonHomeFragment();
        // ??????fragment???????????????ViewPage???
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                viewPager,
                1,
                liveDetailFragment,
                commonHomeFragment,
                emptyFragment
        );

        if (liveRoomDataManager.getConfig().isLive()) {
            // ?????????????????????
            videoLayout = new PLVECLiveVideoLayout(this);
        } else {
            // ?????????????????????
            videoLayout = new PLVECPlaybackVideoLayout(this);
        }
        // ???????????????
        FrameLayout videoContainer = findViewById(R.id.plvec_fl_video_container);
        // ???????????????????????????
        videoContainer.addView((View) videoLayout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        // ???????????????????????????
        videoLayout.init(liveRoomDataManager);
        videoLayout.setOnViewActionListener(new IPLVECVideoLayout.OnViewActionListener() {
            @Override
            public void onCloseFloatingAction() {
                //???????????????????????????????????????
                videoLayout.setPlayerVolume(0);
                isUserCloseFloatingWindow = true;
            }

            @Override
            public void onShowMoreLayoutAction() {
                if (commonHomeFragment != null) {
                    commonHomeFragment.showMorePopupWindow();
                }
            }

            @Override
            public void acceptOnLowLatencyChange(boolean isLowLatency) {
                if (commonHomeFragment != null) {
                    commonHomeFragment.acceptOnLowLatencyChange(isLowLatency);
                }
            }

            @Override
            public void acceptNetworkQuality(int networkQuality) {
                if (commonHomeFragment != null) {
                    commonHomeFragment.acceptNetworkQuality(networkQuality);
                }
            }
        });
        //??????activity ?????????????????????????????????
        initGesture();

        videoLayout.startPlay();

        final String vid = liveRoomDataManager.getConfig().getVid();
        final boolean isPlayback = !liveRoomDataManager.getConfig().isLive();
        if (isPlayback && TextUtils.isEmpty(vid)) {
            observePreviousPage();
        }
    }

    //?????????????????????????????????????????????
    private void initGesture() {
        gestureScanner = new GestureDetector(PLVECLiveEcommerceActivity.this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                PLVCommonLog.d(TAG,"onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                PLVCommonLog.d(TAG,"onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                PLVCommonLog.d(TAG,"onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (videoLayout.isSubVideoViewShow()) {
                    if (!videoLayout.isSubVideoViewShow()) {
                        videoPause();
                    }
                    if (!videoLayout.getSubVideoViewHerf().isEmpty()) {
                        PLVWebUtils.openWebLink(videoLayout.getSubVideoViewHerf(), PLVECLiveEcommerceActivity.this);
                    }
                } else {
                    if (!videoLayout.isSubVideoViewShow()) {
                        videoResume();
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!videoLayout.isSubVideoViewShow()) {
                    if (videoLayout.isPlaying()) {
                        videoPause();
                    } else {
                        videoResume();
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
    }

    private void initEmptyFragment() {
        emptyFragment = new PLVECEmptyFragment();
    }

    private void initLiveDetailFragment() {
        liveDetailFragment = new PLVECLiveDetailFragment();
        // ??????liveDetailFragment???view???????????????
        liveDetailFragment.setOnViewActionListener(liveDetailViewActionListener);
    }

    private void initCommonHomeFragment() {
        if (liveRoomDataManager.getConfig().isLive()) {
            // ?????????????????????fragment
            commonHomeFragment = new PLVECLiveHomeFragment();
            // ??????view?????????????????????
            commonHomeFragment.setOnViewActionListener(liveHomeViewActionListener);
        } else {
            // ?????????????????????fragment
            commonHomeFragment = new PLVECPalybackHomeFragment();
            // ??????view?????????????????????
            commonHomeFragment.setOnViewActionListener(playbackHomeViewActionListener);
        }
        // ??????LiveRoomDataManager
        commonHomeFragment.init(liveRoomDataManager);
        // ????????????
        commonHomeFragment.getCardPushManager().setOnCardEnterClickListener(new PLVCardPushManager.OnCardEnterClickListener() {
            @Override
            public void onClick(PLVShowPushCardEvent event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showCardPush(event);
                }
            }
        });
    }

    /**
     * ?????????????????? - ????????????????????????
     */
    private void startPlaybackOnHasRecordFile() {
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                if (statefulData == null || !statefulData.isSuccess() || statefulData.getData() == null || statefulData.getData().getData() == null) {
                    return;
                }
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                final PLVLiveClassDetailVO liveClassDetailVO = statefulData.getData();
                final boolean hasRecordFile = liveClassDetailVO.getData().isPlaybackEnabled() && liveClassDetailVO.getData().getRecordFileSimpleModel() != null;
                if (hasRecordFile) {
                    videoLayout.startPlay();
                }
            }
        });
    }

    /**
     * ????????????
     */
    private void observePreviousPage() {
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable final PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                final List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenus = nullable(new PLVSugarUtil.Supplier<List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean>>() {
                    @Override
                    public List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> get() {
                        return statefulData.getData().getData().getChannelMenus();
                    }
                });
                final List<String> channelMenuTypes = transformList(channelMenus, new PLVSugarUtil.Function<PLVLiveClassDetailVO.DataBean.ChannelMenusBean, String>() {
                    @Override
                    public String apply(PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
                        return channelMenusBean.getMenuType();
                    }
                });
                if (channelMenuTypes != null && commonHomeFragment != null) {
                    commonHomeFragment.onHasPreviousPage(channelMenuTypes.contains(PLVLiveClassDetailVO.MENUTYPE_PREVIOUS));
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????Layout - ???????????????????????????">
    private void setupPopoverLayout(){
        if (popoverLayout == null) {
            ViewStub floatViewStub = findViewById(R.id.plvec_popover_layout);
            popoverLayout = (IPLVPopoverLayout) floatViewStub.inflate();
            popoverLayout.init(PLVLiveScene.ECOMMERCE, liveRoomDataManager);
            popoverLayout.setOnPointRewardListener(new OnPointRewardListener() {
                @Override
                public void pointRewardEnable(boolean enable) {
                    liveRoomDataManager.getPointRewardEnableData().postValue(PLVStatefulData.success(enable));
                }
            });
            popoverLayout.setOnOpenInsideWebViewListener(new PLVInteractLayout2.OnOpenInsideWebViewListener() {
                @Override
                public PLVInteractLayout2.OpenUrlParam onOpenWithParam(boolean isLandscape) {
                    ViewGroup containerView = findViewById(R.id.plvec_popup_container);
                    return new PLVInteractLayout2.OpenUrlParam((int) (containerView.getHeight() * 0.3f), containerView);
                }

                @Override
                public void onClosed() {
                }
            });
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????????????????">
    private void initFloatingWindowSetting() {
        floatingWindow = PLVDependManager.getInstance().get(PLVECFloatingWindow.class);
        floatingWindow.bindContentView(videoLayout.getPlayerSwitchAnchorLayout());
        floatingWindow.setLiveRoomData(liveRoomDataManager);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ??????????????????">
    private void observerDataToLiveDetailFragment() {
        // ???????????? ?????? ????????????????????????????????????????????????????????????
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> liveClassDetailVO) {
                if (liveClassDetailVO != null && liveClassDetailVO.isSuccess()) {
                    liveDetailFragment.setClassDetailVO(liveClassDetailVO.getData());
                }
            }
        });
        // ???????????? ?????? ?????????????????????????????????????????????
        commonHomeFragment.getBulletinVO().observe(this, new Observer<PolyvBulletinVO>() {
            @Override
            public void onChanged(@Nullable PolyvBulletinVO bulletinVO) {
                liveDetailFragment.setBulletinVO(bulletinVO);
            }
        });
    }

    private void observerDataToLiveHomeFragment() {
        // ???????????? ?????? ?????????????????????????????????????????????
        videoLayout.getPlayerState().observe(this, new Observer<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState state) {
                commonHomeFragment.setPlayerState(state);
            }
        });
    }

    private void observerDataToPlaybackHomeFragment() {
        // ???????????? ?????? ?????????????????????????????????????????????
        videoLayout.getPlayerState().observe(this, new Observer<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState state) {
                commonHomeFragment.setPlayerState(state);
                if (PLVPlayerState.PREPARED.equals(state)) {
                    commonHomeFragment.onPlaybackVideoPrepared(videoLayout.getSessionId(), liveRoomDataManager.getConfig().getChannelId());
                }
            }
        });

        // ???????????? ?????? ??????????????????seek??????????????????
        videoLayout.addOnSeekCompleteListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                commonHomeFragment.onPlaybackVideoSeekComplete(integer);
            }
        });

        // ???????????? ?????? ???????????????????????????????????????????????????
        if (videoLayout.getPlaybackPlayInfoVO() != null) {
            videoLayout.getPlaybackPlayInfoVO().observe(this, new Observer<PLVPlayInfoVO>() {
                @Override
                public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                    commonHomeFragment.setPlaybackPlayInfo(playInfoVO);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????? - view?????????????????????">
    private void videoResume() {
        videoLayout.resume();
    }

    private void videoPause() {
        videoLayout.pause();
    }

    private PLVECLiveDetailFragment.OnViewActionListener liveDetailViewActionListener = new PLVECLiveDetailFragment.OnViewActionListener() {
        @Override
        public void onViewCreated() {
            observerDataToLiveDetailFragment();
        }
    };

    private PLVECLiveHomeFragment.OnViewActionListener liveHomeViewActionListener = new PLVECLiveHomeFragment.OnViewActionListener() {
        @Override
        public void onChangeMediaPlayModeClick(View view, int mediaPlayMode) {
            videoLayout.changeMediaPlayMode(mediaPlayMode);
        }

        @Override
        public void onChangeLinesClick(View view, int linesPos) {
            videoLayout.changeLines(linesPos);
        }

        @Override
        public Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view) {
            return new Pair<>(videoLayout.getBitrateVO(), videoLayout.getBitratePos());
        }

        @Override
        public void onDefinitionChangeClick(View view, int definitionPos) {
            videoLayout.changeBitRate(definitionPos);
        }

        @Override
        public int onGetMediaPlayModeAction() {
            return videoLayout.getMediaPlayMode();
        }

        @Override
        public int onGetLinesCountAction() {
            return videoLayout.getLinesCount();
        }

        @Override
        public int onGetLinesPosAction() {
            return videoLayout.getLinesPos();
        }

        @Override
        public int onGetDefinitionAction() {
            return videoLayout.getBitratePos();
        }

        @Override
        public void onSetVideoViewRectAction(Rect videoViewRect) {
            videoLayout.setVideoViewRect(videoViewRect);
        }

        @Override
        public void onShowRewardAction() {
            if(popoverLayout != null){
                popoverLayout.getRewardView().showPointRewardDialog(true);
            }
        }

        @Override
        public boolean isCurrentLowLatencyMode() {
            return videoLayout.isCurrentLowLatencyMode();
        }

        @Override
        public void switchLowLatencyMode(boolean isLowLatency) {
            videoLayout.switchLowLatencyMode(isLowLatency);
        }

        @Override
        public void onViewCreated() {
            observerDataToLiveHomeFragment();
            setupPopoverLayout();
        }
    };

    private PLVECPalybackHomeFragment.OnViewActionListener playbackHomeViewActionListener = new PLVECPalybackHomeFragment.OnViewActionListener() {
        @Override
        public boolean onPauseOrResumeClick(View view) {
            if (!videoLayout.isSubVideoViewShow()) {
                if (videoLayout.isPlaying()) {
                    videoPause();
                    return false;
                } else {
                    videoResume();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onChangeSpeedClick(View view, float speed) {
            videoLayout.setSpeed(speed);
        }

        @Override
        public void onSeekToAction(int progress, int max) {
            videoLayout.seekTo(progress, max);
        }

        @Override
        public int onGetDurationAction() {
            return videoLayout.getDuration();
        }

        @Override
        public int getVideoCurrentPosition() {
            return videoLayout.getVideoCurrentPosition();
        }

        @Override
        public void onSetVideoViewRectAction(Rect videoViewRect) {
            videoLayout.setVideoViewRect(videoViewRect);
        }

        @Override
        public float onGetSpeedAction() {
            return videoLayout.getSpeed();
        }

        @Override
        public void onChangePlaybackVidAndPlay(String vid) {
            videoLayout.changePlaybackVidAndPlay(vid);
        }

        @Override
        public void onViewCreated() {
            observerDataToPlaybackHomeFragment();
            setupPopoverLayout();
        }
    };
    // </editor-fold>

}
