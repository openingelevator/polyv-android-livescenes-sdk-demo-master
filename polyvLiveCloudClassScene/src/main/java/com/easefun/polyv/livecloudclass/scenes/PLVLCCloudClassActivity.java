package com.easefun.polyv.livecloudclass.scenes;

import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotEmpty;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.IPLVLCLinkMicLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.PLVLCLinkMicControlBar;
import com.easefun.polyv.livecloudclass.modules.media.IPLVLCMediaLayout;
import com.easefun.polyv.livecloudclass.modules.media.controller.PLVLCLiveLandscapeChannelController;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindowModule;
import com.easefun.polyv.livecloudclass.modules.pagemenu.IPLVLCLivePageMenuLayout;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCFloatingPPTLayout;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCPPTView;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chapter.di.PLVPlaybackChapterModule;
import com.easefun.polyv.livecommon.module.modules.commodity.di.PLVCommodityModule;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.di.PLVPlaybackCacheModule;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.popover.IPLVPopoverLayout;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.utils.PLVDialogFactory;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.File;

/**
 * date: 2020/10/12
 * author: HWilliamgo
 * ??????????????????????????? ??????????????????????????? ??? ???????????????
 * ????????????????????????????????????????????????????????????PPT(?????????????????????)???????????????????????????
 * ????????????????????????????????????????????????????????????PPT(?????????????????????)???
 */
public class PLVLCCloudClassActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = PLVLCCloudClassActivity.class.getSimpleName();
    // ?????? - ??????????????????????????????
    private static final String EXTRA_CHANNEL_ID = "channelId";   // ?????????
    private static final String EXTRA_VIEWER_ID = "viewerId";   // ?????????Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // ???????????????
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//?????????????????????
    private static final String EXTRA_VID = "vid";//????????????Id
    private static final String EXTRA_TEMP_STORE_FILE_ID = "temp_store_file_id";//????????????id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//??????????????????
    private static final String EXTRA_IS_LIVE = "is_live";//???????????????
    private static final String EXTRA_CHANNEL_TYPE = "channel_type";//????????????

    // ???????????????????????????????????????????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // ???????????????
    private IPLVLCMediaLayout mediaLayout;
    // ????????????????????????
    private IPLVLCLivePageMenuLayout livePageMenuLayout;
    // ??????PPT??????
    private IPLVLCFloatingPPTLayout floatingPPTLayout;
    // ????????????
    @Nullable
    private IPLVLCLinkMicLayout linkMicLayout;
    // ?????????????????????
    private PLVLCChatLandscapeLayout chatLandscapeLayout;

    //????????????
    private IPLVPopoverLayout popoverLayout;

    // ??????PPT?????? ??? ??????????????? ????????????
    private PLVViewSwitcher pptViewSwitcher = new PLVViewSwitcher();

    private PLVPlayerLogoView plvPlayerLogoView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????Activity?????????">

    /**
     * ???????????????
     *
     * @param activity    ?????????Activity
     * @param channelId   ?????????
     * @param channelType ????????????
     * @param viewerId    ??????ID
     * @param viewerName  ????????????
     * @return PLVLaunchResult.isSuccess=true?????????????????????PLVLaunchResult.isSuccess=false??????????????????
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchLive(@NonNull Activity activity,
                                             @NonNull String channelId,
                                             @NonNull PLVLiveChannelType channelType,
                                             @NonNull String viewerId,
                                             @NonNull String viewerName,
                                             @NonNull String viewerAvatar) {
        if (activity == null) {
            return PLVLaunchResult.error("activity ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId ??????????????????????????????????????????");
        }
        if (channelType == null) {
            return PLVLaunchResult.error("channelType ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName ??????????????????????????????????????????");
        }

        Intent intent = new Intent(activity, PLVLCCloudClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_IS_LIVE, true);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }

    /**
     * ??????????????????
     * ??????????????????vid??????????????????????????????????????????????????????????????????vid???????????????????????????vid????????????
     * ?????????????????????????????????????????????
     * ????????????????????????vid????????????????????????????????????????????????????????????
     * PLVLaunchResult.error("vid ??????????????????????????????????????????");?????????
     *
     * @param activity      ?????????Activity
     * @param channelId     ?????????
     * @param channelType   ????????????
     * @param vid           ??????ID
     * @param viewerId      ??????ID
     * @param viewerName    ????????????
     * @param videoListType ?????????????????? {@link PLVPlaybackListType}
     * @return PLVLaunchResult.isSuccess=true?????????????????????PLVLaunchResult.isSuccess=false??????????????????
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchPlayback(@NonNull Activity activity,
                                                 @NonNull String channelId,
                                                 @NonNull PLVLiveChannelType channelType,
                                                 @Nullable String vid,
                                                 @Nullable String tempStoreFileId,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String viewerAvatar,
                                                 PLVPlaybackListType videoListType) {
        if (activity == null) {
            return PLVLaunchResult.error("activity ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId ??????????????????????????????????????????");
        }
        if (channelType == null) {
            return PLVLaunchResult.error("channelType ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId ??????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName ??????????????????????????????????????????");
        }

        Intent intent = new Intent(activity, PLVLCCloudClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_TEMP_STORE_FILE_ID, tempStoreFileId);
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
        setContentView(R.layout.plvlc_cloudclass_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initPptTurnPageLandLayout();

        observeMediaLayout();
        observeLinkMicLayout();
        observePageMenuLayout();
        observePPTView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVFloatingPlayerManager.getInstance().runOnFloatingWindowClosed(new Runnable() {
            @Override
            public void run() {
                PLVFloatingPlayerManager.getInstance().clear();
                if (mediaLayout != null) {
                    mediaLayout.destroy();
                }
                if (linkMicLayout != null) {
                    linkMicLayout.destroy();
                }
                if (livePageMenuLayout != null) {
                    livePageMenuLayout.destroy();
                }
                if (popoverLayout != null) {
                    popoverLayout.destroy();
                }
                if (floatingPPTLayout != null) {
                    floatingPPTLayout.destroy();
                }
                if (popoverLayout != null) {
                    popoverLayout.destroy();
                }
                if (liveRoomDataManager != null) {
                    liveRoomDataManager.destroy();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (popoverLayout != null && popoverLayout.onBackPress()) {
            return;
        } else if (mediaLayout != null && mediaLayout.onBackPressed()) {
            return;
        } else if (livePageMenuLayout != null && livePageMenuLayout.onBackPressed()) {
            return;
        }

        //?????????????????????????????????
        PLVDialogFactory.createConfirmDialog(
                this,
                getResources().getString(
                        liveRoomDataManager.getConfig().isLive()
                                ? R.string.plv_live_room_dialog_exit_confirm_ask
                                : R.string.plv_playback_room_dialog_exit_confirm_ask
                ),
                getResources().getString(R.string.plv_common_dialog_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PLVLCCloudClassActivity.super.onBackPressed();
                    }
                }
        ).show();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVPlaybackCacheModule.instance)
                .addModule(PLVPlaybackChapterModule.instance)
                .addModule(PLVCommodityModule.instance)
                .addModule(PLVLCFloatingWindowModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void initParams() {
        // ??????????????????
        final Intent intent = getIntent();
        final boolean isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true);
        final PLVLiveChannelType channelType = (PLVLiveChannelType) intent.getSerializableExtra(EXTRA_CHANNEL_TYPE);
        final String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        final String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        final String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        final String viewerAvatar = intent.getStringExtra(EXTRA_VIEWER_AVATAR);
        final String vid = firstNotEmpty(intent.getStringExtra(EXTRA_VID), intent.getStringExtra(EXTRA_TEMP_STORE_FILE_ID));
        final PLVPlaybackListType videoListType = (PLVPlaybackListType) intent.getSerializableExtra(EXTRA_VIDEO_LIST_TYPE);

        // ??????Config??????
        PLVLiveChannelConfigFiller.setIsLive(isLive);
        PLVLiveChannelConfigFiller.setChannelType(channelType);
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, viewerAvatar,
                PLVLinkMicConfig.getInstance().getLiveChannelTypeNew() == PLVLiveChannelType.PPT
                        ? PLVSocketUserConstant.USERTYPE_SLICE : PLVSocketUserConstant.USERTYPE_STUDENT);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);

        PLVFloatingPlayerManager.getInstance().saveIntent(intent);
        // ???????????????????????????????????????
        if (isLive) {
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_live");
        } else { // ????????????
            PLVLiveChannelConfigFiller.setupVid(vid != null ? vid : "");
            PLVLiveChannelConfigFiller.setupVideoListType(videoListType != null ? videoListType : PLVPlaybackListType.PLAYBACK);

            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_" + (vid == null ? "playback" : vid));
        }

        initPlaybackParam(vid, channelId, viewerId, viewerName, viewerAvatar, channelType, videoListType);
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

        // ?????????????????????????????????????????????
        liveRoomDataManager.requestChannelSwitch();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????UI">
    private void initView() {
        // ?????????ViewStub
        ViewStub videoLyViewStub = findViewById(R.id.plvlc_video_viewstub);

        // ??????PPT??????
        floatingPPTLayout = findViewById(R.id.plvlc_ppt_floating_ppt_layout);

        if (liveRoomDataManager.getConfig().isLive()) {
            // ?????????????????????
            ViewStub landscapeChannelControllerViewStub = findViewById(R.id.plvlc_ppt_landscape_channel_controller);
            PLVLCLiveLandscapeChannelController liveLandscapeChannelController = (PLVLCLiveLandscapeChannelController) landscapeChannelControllerViewStub.inflate();

            // ???????????????
            videoLyViewStub.setLayoutResource(R.layout.plvlc_live_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setLandscapeControllerView(liveLandscapeChannelController);
            mediaLayout.startPlay();

            // ???????????????
            ViewStub linkmicControllerViewStub = findViewById(R.id.plvlc_ppt_linkmic_controller);
            PLVLCLinkMicControlBar linkMicControlBar = (PLVLCLinkMicControlBar) linkmicControllerViewStub.inflate();

            // ????????????
            ViewStub linkmicLayoutViewStub = findViewById(R.id.plvlc_linkmic_viewstub);
            linkMicLayout = (IPLVLCLinkMicLayout) linkmicLayoutViewStub.inflate();
            linkMicLayout.init(liveRoomDataManager, linkMicControlBar);
            linkMicLayout.hideAll();
        } else {
            // ???????????????
            videoLyViewStub.setLayoutResource(R.layout.plvlc_playback_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setPPTView(floatingPPTLayout.getPPTView().getPlaybackPPTViewToBindInPlayer());
            mediaLayout.startPlay();
        }

        // ????????????(?????????????????????????????????)
        ViewStub floatViewStub = findViewById(R.id.plvlc_popover_layout);
        popoverLayout = (IPLVPopoverLayout) floatViewStub.inflate();
        popoverLayout.init(PLVLiveScene.CLOUDCLASS, liveRoomDataManager);
        popoverLayout.setOnPointRewardListener(new OnPointRewardListener() {
            @Override
            public void pointRewardEnable(boolean enable) {
                liveRoomDataManager.getPointRewardEnableData().postValue(PLVStatefulData.success(enable));
            }
        });
        popoverLayout.setOnOpenInsideWebViewListener(new PLVInteractLayout2.OnOpenInsideWebViewListener() {
            boolean needShowControllerOnClosed = false;

            @Override
            public PLVInteractLayout2.OpenUrlParam onOpenWithParam(boolean isLandscape) {
                if (isLandscape) {
                    needShowControllerOnClosed = !needShowControllerOnClosed ? mediaLayout.hideController() : needShowControllerOnClosed;
                }
                return new PLVInteractLayout2.OpenUrlParam(((View) mediaLayout).getHeight() + ConvertUtils.dp2px(48), (ViewGroup) findViewById(R.id.plvlc_popup_container));
            }

            @Override
            public void onClosed() {
                if (needShowControllerOnClosed) {
                    mediaLayout.showController();
                    needShowControllerOnClosed = false;
                }
            }
        });

        // ??????????????????
        livePageMenuLayout = findViewById(R.id.plvlc_live_page_menu_layout);
        livePageMenuLayout.init(liveRoomDataManager);

        // ??????????????????
        livePageMenuLayout.getCardPushManager().registerView(mediaLayout.getCardEnterView(), mediaLayout.getCardEnterCdView(), mediaLayout.getCardEnterTipsView());
        livePageMenuLayout.getCardPushManager().setOnCardEnterClickListener(new PLVCardPushManager.OnCardEnterClickListener() {
            @Override
            public void onClick(PLVShowPushCardEvent event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showCardPush(event);
                }
            }
        });

        // ??????????????????????????????
        chatLandscapeLayout = mediaLayout.getChatLandscapeLayout();
        chatLandscapeLayout.init(livePageMenuLayout.getChatCommonMessageList());

        // ?????? ??????PPT?????? ??? ??????????????? ????????????
        pptViewSwitcher.registerSwitchView(floatingPPTLayout.getPPTSwitchView(), mediaLayout.getPlayerSwitchView());

        // ????????? ????????????
        if (ScreenUtils.isPortrait()) {
            PLVScreenUtils.enterPortrait(this);
        } else {
            PLVScreenUtils.enterLandscape(this);
        }
        plvPlayerLogoView = mediaLayout.getLogoView();
    }

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
                    mediaLayout.startPlay();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????">
    private void observeMediaLayout() {
        //??????view???????????????
        mediaLayout.setOnViewActionListener(new IPLVLCMediaLayout.OnViewActionListener() {
            @Override
            public void onClickShowOrHideSubTab(boolean toShow) {
                if (liveRoomDataManager.getConfig().isLive()) {
                    if (linkMicLayout == null) {
                        return;
                    }
                    if (linkMicLayout.isJoinChannel()) {
                        if (toShow) {
                            linkMicLayout.showAll();
                        } else {
                            linkMicLayout.hideLinkMicList();
                        }
                    } else {
                        if (toShow) {
                            floatingPPTLayout.show();
                        } else {
                            floatingPPTLayout.hide();
                        }
                    }
                } else {
                    if (toShow) {
                        floatingPPTLayout.show();
                    } else {
                        floatingPPTLayout.hide();
                    }
                }
            }

            @Override
            public void onShowMediaController(boolean show) {
                if (liveRoomDataManager.getConfig().isLive()) {
                    if (linkMicLayout == null) {
                        return;
                    }
                    if (show) {
                        linkMicLayout.showControlBar();
                    } else {
                        linkMicLayout.hideControlBar();
                    }
                }
            }

            @Override
            public Pair<Boolean, Integer> onSendChatMessageAction(String message) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                return livePageMenuLayout.getChatroomPresenter().sendChatMessage(localMessage);
            }

            @Override
            public void onShowBulletinAction() {
                if (liveRoomDataManager.getConfig().isLive() && popoverLayout != null) {
                    popoverLayout.getInteractLayout().showBulletin();
                }
            }

            @Override
            public void onShowRewardAction() {
                if (liveRoomDataManager.getConfig().isLive() && popoverLayout != null) {
                    popoverLayout.getRewardView().showPointRewardDialog(true);
                }
            }

            @Override
            public void onSendLikesAction() {
                livePageMenuLayout.getChatroomPresenter().sendLikeMessage();
            }

            @Override
            public void onPPTTurnPage(String type) {
                if (floatingPPTLayout != null && floatingPPTLayout.getPPTView() != null) {
                    floatingPPTLayout.getPPTView().turnPagePPT(type);
                }
            }

            @Override
            public void onWatchLowLatency(boolean watchLowLatency) {
                floatingPPTLayout.setIsLowLatencyWatch(watchLowLatency);
                if (linkMicLayout != null) {
                    linkMicLayout.setWatchLowLatency(watchLowLatency);
                }
            }

            @Override
            public void onRtcPauseResume(boolean toPause) {
                if (linkMicLayout == null) {
                    return;
                }
                if (toPause) {
                    linkMicLayout.pause();
                } else {
                    linkMicLayout.resume();
                }
            }

            @Override
            public boolean isRtcPausing() {
                return linkMicLayout.isPausing();
            }
        });

        //???????????? ?????? ?????????????????????PPT??????????????????
        mediaLayout.addOnPPTShowStateListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isPptVisible) {
                if (isPptVisible == null) {
                    return;
                }
                floatingPPTLayout.setServerEnablePPT(isPptVisible);
            }
        });
        //??????????????????
        mediaLayout.addOnPlayerStateListener(new IPLVOnDataChangedListener<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState playerState) {
                if (playerState == null) {
                    return;
                }
                if (liveRoomDataManager.getConfig().isLive()) {
                    //????????????????????????
                    switch (playerState) {
                        case PREPARED:
                            floatingPPTLayout.show();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.LIVE);
                            if (linkMicLayout != null) {
                                linkMicLayout.showAll();
                            }
                            break;
                        case LIVE_STOP:
                            if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                                //??????????????????????????????????????????PPT??????????????????????????????????????????
                                if (!floatingPPTLayout.isPPTInFloatingLayout()) {
                                    pptViewSwitcher.switchView();
                                }
                            }
                            floatingPPTLayout.hide();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.STOP);
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            break;
                        case NO_LIVE:
                        case LIVE_END:
                            if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                                //??????????????????????????????????????????PPT??????????????????????????????????????????
                                if (!floatingPPTLayout.isPPTInFloatingLayout()) {
                                    pptViewSwitcher.switchView();
                                }
                            }
                            floatingPPTLayout.hide();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.END);
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    //????????????????????????
                    switch (playerState) {
                        case PREPARED:
                            floatingPPTLayout.show();
                            livePageMenuLayout.onPlaybackVideoPrepared(mediaLayout.getSessionId(), liveRoomDataManager.getConfig().getChannelId());
                            break;
                        case IDLE:
                            floatingPPTLayout.hide();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        if (liveRoomDataManager.getConfig().isLive()) {
            //?????????????????????

            //???????????? ?????? ??????????????????????????????->?????????????????????????????????
            mediaLayout.addOnLinkMicStateListener(new IPLVOnDataChangedListener<Pair<Boolean, Boolean>>() {
                @Override
                public void onChanged(@Nullable Pair<Boolean/*??????????????????*/, Boolean/*?????????????????????*/> linkMicState) {
                    if (linkMicState == null) {
                        return;
                    }
                    boolean isLinkMicOpen = linkMicState.first;
                    boolean isAudio = linkMicState.second;
                    if (linkMicLayout == null) {
                        return;
                    }
                    linkMicLayout.setIsTeacherOpenLinkMic(isLinkMicOpen);
                    linkMicLayout.setIsAudio(isAudio);
                }
            });
            //???????????? ?????? ?????????????????????sei??????
            mediaLayout.addOnSeiDataListener(new IPLVOnDataChangedListener<Long>() {
                @Override
                public void onChanged(@Nullable Long aLong) {
                    if (aLong == null) {
                        return;
                    }
                    floatingPPTLayout.getPPTView().sendSEIData(aLong);
                }
            });
            mediaLayout.setOnRTCPlayEventListener(new IPolyvLiveListenerEvent.OnRTCPlayEventListener() {
                @Override
                public void onRTCLiveStart() {
                    if (linkMicLayout != null) {
                        linkMicLayout.setLiveStart();
                    }
                }

                @Override
                public void onRTCLiveEnd() {
                    if (linkMicLayout != null) {
                        linkMicLayout.setLiveEnd();
                    }
                }
            });
        } else {
            //?????????????????????

            mediaLayout.addOnPlayInfoVOListener(new IPLVOnDataChangedListener<PLVPlayInfoVO>() {
                @Override
                public void onChanged(@Nullable PLVPlayInfoVO plvPlayInfoVO) {
                    if (plvPlayInfoVO == null) {
                        return;
                    }
                    //???????????????????????????????????????PPT???
                    floatingPPTLayout.getPPTView().setPlaybackCurrentPosition(plvPlayInfoVO.getPosition());

                    //??????????????????????????????????????????????????????????????????
                    if (livePageMenuLayout.getPreviousPresenter() != null) {
                        livePageMenuLayout.getPreviousPresenter().updatePlaybackCurrentPosition(plvPlayInfoVO);
                    }
                }
            });

            mediaLayout.addOnSeekCompleteListener(new IPLVOnDataChangedListener<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    if (integer == null) {
                        return;
                    }
                    livePageMenuLayout.onPlaybackVideoSeekComplete(integer);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">
    private void observePageMenuLayout() {
        //??????view???????????????
        livePageMenuLayout.setOnViewActionListener(new IPLVLCLivePageMenuLayout.OnViewActionListener() {
            @Override
            public void onShowBulletinAction() {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showBulletin();
                }
            }

            @Override
            public void onSendDanmuAction(CharSequence message) {
                mediaLayout.sendDanmaku(message);
            }

            @Override
            public void onChangeVideoVidAction(String vid) {
                mediaLayout.updatePlayBackVideVidAndPlay(vid);
            }

            @Override
            public void onSeekToAction(int progress) {
                mediaLayout.seekTo(progress * 1000, mediaLayout.getDuration());
            }

            @Override
            public int getVideoCurrentPosition() {
                return mediaLayout.getVideoCurrentPosition();
            }

            @Override
            public void onAddedChatTab(boolean isChatPlaybackEnabled) {
                if (chatLandscapeLayout != null) {
                    chatLandscapeLayout.setIsChatPlaybackLayout(isChatPlaybackEnabled);
                    livePageMenuLayout.getChatPlaybackManager().addOnCallDataListener(chatLandscapeLayout.getChatPlaybackDataListener());
                    livePageMenuLayout.getChatroomPresenter().registerView(chatLandscapeLayout.getChatroomView());
                }
                if (mediaLayout != null) {
                    mediaLayout.setChatPlaybackEnabled(isChatPlaybackEnabled);
                }
            }

            @Override
            public void onShowRewardAction() {
                if (popoverLayout != null) {
                    popoverLayout.getRewardView().showPointRewardDialog(true);
                }
            }

            @Override
            public void onShowEffectAction(boolean isShow) {
                //????????????????????????????????????
                if (mediaLayout != null) {
                    mediaLayout.setLandscapeRewardEffectVisibility(isShow);
                }
            }

            @Override
            public void onClickChatMoreDynamicFunction(String event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().onCallDynamicFunction(event);
                }
            }
        });
        //???????????? ?????? ???????????????????????????????????????
        livePageMenuLayout.addOnViewerCountListener(new IPLVOnDataChangedListener<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                if (l == null) {
                    return;
                }
                mediaLayout.updateViewerCount(l);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - PPT">
    private void observePPTView() {
        //??????????????????????????????
        floatingPPTLayout.setOnFloatingViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pptViewSwitcher.switchView();
                initPptTurnPageLandLayout();
            }
        });
        //???????????????????????????????????????
        floatingPPTLayout.setOnClickCloseListener(new IPLVLCFloatingPPTLayout.IPLVOnClickCloseFloatingView() {
            @Override
            public void onClickCloseFloatingView() {
                mediaLayout.updateOnClickCloseFloatingView();
            }
        });
        if (liveRoomDataManager.getConfig().isLive()) {
            //????????????PPT???????????????
            floatingPPTLayout.getPPTView().initLivePPT(new IPLVLCPPTView.OnPLVLCLivePPTViewListener() {

                @Override
                public void onLiveSwitchPPTViewLocation(boolean toMainScreen) {
                    if (!liveRoomDataManager.getConfig().isPPTChannelType()) {
                        return;
                    }

                    //ppt??????????????????????????????
                    mediaLayout.onTurnPageLayoutChange(toMainScreen);

                    if (linkMicLayout == null || !linkMicLayout.isJoinChannel()) {
                        if (toMainScreen) {
                            if (!pptViewSwitcher.isViewSwitched()) {
                                pptViewSwitcher.switchView();
                            }
                        } else {
                            if (pptViewSwitcher.isViewSwitched()) {
                                pptViewSwitcher.switchView();
                            }
                        }
                    }
                }

                @Override
                public void onLiveChangeToLandscape(boolean toLandscape) {
                    if (toLandscape) {
                        PLVOrientationManager.getInstance().setLandscape(PLVLCCloudClassActivity.this);
                    } else {
                        PLVOrientationManager.getInstance().setPortrait(PLVLCCloudClassActivity.this);
                    }
                }

                @Override
                public void onLiveStartOrPauseVideoView(boolean toStart) {
                    if (toStart) {
                        mediaLayout.startPlay();
                    } else {
                        mediaLayout.stop();
                    }
                }

                @Override
                public void onLiveRestartVideoView() {
                    mediaLayout.startPlay();
                }

                @Override
                public void onLiveBackTopActivity() {
                    if (ScreenUtils.isLandscape()) {
                        PLVOrientationManager.getInstance().setPortrait(PLVLCCloudClassActivity.this);
                    } else {
                        finish();
                    }
                }

                @Override
                public void onLivePPTStatusChange(PLVPPTStatus plvpptStatus) {
                    //??????PPT??????
                    if (mediaLayout != null) {
                        mediaLayout.updatePPTStatusChange(plvpptStatus);
                    }
                }
            });
        } else {
            //????????????PPT????????????
            floatingPPTLayout.getPPTView().initPlaybackPPT(new IPLVLCPPTView.OnPLVLCPlaybackPPTViewListener() {
                @Override
                public void onPlaybackSwitchPPTViewLocation(boolean toMainScreen) {
                    pptViewSwitcher.switchView();
                }
            });
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ??????">
    private void observeLinkMicLayout() {
        if (!liveRoomDataManager.getConfig().isLive() || linkMicLayout == null) {
            return;
        }
        //?????? View ????????????????????????
        final PLVViewSwitcher linkMicItemSwitcher = new PLVViewSwitcher();
        linkMicLayout.setLogoView(plvPlayerLogoView);
        //???????????????????????????
        linkMicLayout.setOnPLVLinkMicLayoutListener(new IPLVLCLinkMicLayout.OnPLVLinkMicLayoutListener() {
            @Override
            public void onJoinRtcChannel() {
                if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                    //??????????????????????????????PPT??????????????????????????????PPT?????????????????????????????????????????????????????????????????????
                    if (floatingPPTLayout.isPPTInFloatingLayout()) {
                        pptViewSwitcher.switchView();
                        linkMicLayout.notifySwitchedPptToMainScreenOnJoinChannel();
                    }
                }
                //???????????????
                floatingPPTLayout.hide();
                //??????PPT??????????????????0
                floatingPPTLayout.getPPTView().notifyJoinRtcChannel();
                //?????????????????????
                mediaLayout.updateWhenJoinRTC(linkMicLayout.getLandscapeWidth());
            }

            @Override
            public void onLeaveRtcChannel() {
                //???????????????
                floatingPPTLayout.show();
                //??????PPT????????????
                floatingPPTLayout.getPPTView().notifyLeaveRtcChannel();
                //?????????????????????
                mediaLayout.updateWhenLeaveRTC();
            }

            @Override
            public void onChannelLinkMicOpenStatusChanged(boolean isOpen) {
                mediaLayout.updateWhenLinkMicOpenStatusChanged(isOpen);
            }

            @Override
            public void onRequestJoinLinkMic() {
                mediaLayout.updateWhenRequestJoinLinkMic(true);
            }

            @Override
            public void onCancelRequestJoinLinkMic() {
                mediaLayout.updateWhenRequestJoinLinkMic(false);
            }

            @Override
            public void onJoinLinkMic() {
                mediaLayout.updateWhenJoinLinkMic();
            }

            @Override
            public void onLeaveLinkMic() {
                mediaLayout.updateWhenLeaveLinkMic();
            }

            @Override
            public void onShowLandscapeRTCLayout(boolean show) {
                if (show) {
                    mediaLayout.setShowLandscapeRTCLayout();
                } else {
                    mediaLayout.setHideLandscapeRTCLayout();
                }
            }

            @Override
            public void onNetworkQuality(int quality) {
                mediaLayout.acceptNetworkQuality(quality);
            }

            @Override
            public void onChangeTeacherLocation(PLVViewSwitcher viewSwitcher, PLVSwitchViewAnchorLayout switchView) {
                viewSwitcher.registerSwitchView(switchView, mediaLayout.getPlayerSwitchView());
                viewSwitcher.switchView();
                mediaLayout.getPlayerSwitchView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaLayout != null && mediaLayout.getPlayerSwitchView() != null) {
                            //?????? constraint-layout ????????? 2.0.0+ ??????????????????????????????
                            mediaLayout.getPlayerSwitchView().requestLayout();
                        }
                    }
                });
            }

            @Override
            public void onClickSwitchWithMediaOnce(PLVSwitchViewAnchorLayout switchView) {
                linkMicItemSwitcher.registerSwitchView(switchView, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();
            }

            @Override
            public void onClickSwitchWithMediaTwice(PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
                //??????PPT??????????????????????????????
                linkMicItemSwitcher.registerSwitchView(switchViewHasMedia, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();

                //???????????????????????????item???PPT????????????
                linkMicItemSwitcher.registerSwitchView(switchViewGoMainScreen, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();
            }

            @Override
            public void onRTCPrepared() {
                mediaLayout.notifyRTCPrepared();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    @Override
    protected boolean enableRotationObserver() {
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PLVScreenUtils.enterLandscape(this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            PLVScreenUtils.enterPortrait(this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????ppt????????????">
    private void initPptTurnPageLandLayout() {
        if (floatingPPTLayout.isPPTInFloatingLayout()) {
            mediaLayout.onTurnPageLayoutChange(false);
        } else {
            mediaLayout.onTurnPageLayoutChange(true);
        }
    }
    // </editor-fold>
}
