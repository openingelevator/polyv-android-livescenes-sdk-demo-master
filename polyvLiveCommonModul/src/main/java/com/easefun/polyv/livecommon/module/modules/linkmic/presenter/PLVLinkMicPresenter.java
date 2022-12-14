package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowModeGetter;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMsgHandler;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMuteCacheList;
import com.easefun.polyv.livescenes.linkmic.IPolyvLinkMicManager;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicEventListener;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicListener;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicManagerFactory;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.log.IPLVLinkMicTraceLogSender;
import com.plv.linkmic.log.PLVLinkMicTraceLogSender;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.vo.PLVLinkMicEngineParam;
import com.plv.livescenes.log.linkmic.PLVLinkMicELog;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * date: 2020/7/16
 * author: hwj
 * description: ??????Presenter
 */
public class PLVLinkMicPresenter implements IPLVLinkMicContract.IPLVLinkMicPresenter {
    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = PLVLinkMicPresenter.class.getSimpleName();

    //????????????
    private static final int LINK_MIC_UNINITIATED = 1;
    //???????????????
    private static final int LINK_MIC_INITIATED = 3;

    //??????????????????
    private static final int TIME_OUT_JOIN_CHANNEL = 20 * 1000;
    //??????1?????????????????????
    private static final int DELAY_TO_GET_LINK_MIC_LIST = 1000;
    //???20s??????????????????
    private static final int INTERVAL_TO_GET_LINK_MIC_LIST = 20 * 1000;

    /**** View ****/
    @Nullable
    private IPLVLinkMicContract.IPLVLinkMicView linkMicView;

    /**** Model ****/
    private IPolyvLinkMicManager linkMicManager;
    @Nullable
    private PLVLinkMicMsgHandler linkMicMsgHandler;
    @Nullable
    private IPLVRTCInvokeStrategy rtcInvokeStrategy;
    //????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    /**** ?????????????????? ****/
    //?????????????????????
    private int linkMicInitState = LINK_MIC_UNINITIATED;
    private String myLinkMicId = "";
    private boolean isAudioLinkMic;
    //socket?????????????????????????????????
    private long socketRefreshOpenStatusData = -1;
    private boolean isTeacherOpenLinkMic;
    //??????????????????????????????????????????
    private boolean hasInitFirstScreenUser = false;
    //???????????????????????????????????????????????????????????????????????????????????????
    private boolean hasInitFirstTeacherLocation = false;
    //??????????????????????????????????????????????????????Id
    private String mainTeacherLinkMicId;
    //????????????
    private List<PLVLinkMicItemDataBean> linkMicList = new LinkedList<>();
    //mute????????????????????????????????????Mute???????????????????????????????????????????????????????????????mute????????????????????????
    private PLVLinkMicMuteCacheList muteCacheList = new PLVLinkMicMuteCacheList();
    //?????????????????????????????????????????????????????????
    private String avConnectMode = "";
    // ??????rtc??????
    private boolean isWatchRtc;
    // ????????????mute??????
    private boolean isMuteAllAudio;
    private boolean isMuteAllVideo;

    /**** Disposable ****/
    private Disposable getLinkMicListDelay;
    private Disposable getLinkMicListTimer;
    private Disposable linkJoinTimer;
    @Nullable
    private List<Runnable> actionAfterLinkMicEngineCreated;

    /**** Listener ****/
    //rtc???????????????
    private PolyvLinkMicEventListener eventListener = new PolyvLinkMicEventListenerImpl();
    //socket???????????????
    private PolyvLinkMicSocketEventListener socketEventListener = new PolyvLinkMicSocketEventListener();
    //socket?????????
    private PLVSocketMessageObserver.OnMessageListener messageListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVLinkMicPresenter(final IPLVLiveRoomDataManager liveRoomDataManager, @Nullable final IPLVLinkMicContract.IPLVLinkMicView view) {
        this.liveRoomDataManager = liveRoomDataManager;
        //??????
        String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        PolyvLinkMicConfig.getInstance().init(viewerId, false);
        //view
        this.linkMicView = view;
        //model
        actionAfterLinkMicEngineCreated = new ArrayList<>();
        linkMicManager = PolyvLinkMicManagerFactory.createNewLinkMicManager();

        final PLVLinkMicEngineParam param = new PLVLinkMicEngineParam()
                .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                .setViewerId(liveRoomDataManager.getConfig().getUser().getViewerId())
                .setViewerType(liveRoomDataManager.getConfig().getUser().getViewerType())
                .setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        linkMicManager.initEngine(param, new PolyvLinkMicListener() {
            @Override
            public void onLinkMicEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "?????????????????????");
                linkMicInitState = LINK_MIC_INITIATED;
                linkMicManager.addEventHandler(eventListener);
                if (actionAfterLinkMicEngineCreated != null) {
                    for (Runnable action : actionAfterLinkMicEngineCreated) {
                        action.run();
                    }
                    actionAfterLinkMicEngineCreated = null;
                }
            }

            @Override
            public void onLinkMicError(int errorCode, Throwable throwable) {
                linkMicInitState = LINK_MIC_UNINITIATED;
                if (linkMicView != null) {
                    linkMicView.onLinkMicError(errorCode, throwable);
                }
            }
        });
        myLinkMicId = linkMicManager.getLinkMicUid();
        if (TextUtils.isEmpty(myLinkMicId)) {
            if (linkMicView != null) {
                linkMicView.onLinkMicError(-1, new Throwable("???????????????linkMicId"));
            }
            return;
        }
        //??????socket????????????
        linkMicMsgHandler = new PLVLinkMicMsgHandler(myLinkMicId);
        linkMicMsgHandler.addLinkMicMsgListener(socketEventListener);
        //?????????RTC??????????????????
        isWatchRtc = PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch();
        initRTCInvokeStrategy();
        //Socket????????????
        messageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if(event == null){
                    return;
                }
                switch (event){
                    case PLVEventConstant.MESSAGE_EVENT_RELOGIN:{
                        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        if(isJoinLinkMic()){
                            leaveLinkMic();
                        }
                        break;
                    }
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(messageListener);
    }

    private void initRTCInvokeStrategy() {
        if (PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch() && isWatchRtc) {
            //RTC ???????????????
            rtcInvokeStrategy = new PLVRTCWatchEnabledStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new PLVRTCWatchEnabledStrategy.OnJoinRTCChannelWatchListener() {
                        @Override
                        public void onJoinRTCChannelWatch() {
                            if (linkMicView != null) {
                                linkMicView.onJoinRtcChannel();
                            }
                            stopJoinTimeoutCount();
                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVRxTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    requestLinkMicListFromServer();
                                }
                            });
                        }
                    },
                    new IPLVRTCInvokeStrategy.OnJoinLinkMicListener() {

                        @Override
                        public void onJoinLinkMic(PLVLinkMicJoinSuccess data) {
                            PLVLinkMicItemDataBean selfDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(data);
                            //?????????????????????????????????
                            boolean selfExist = false;
                            for (PLVLinkMicItemDataBean bean : linkMicList) {
                                if (selfDataBean.getLinkMicId().equals(bean.getLinkMicId())) {
                                    selfExist = true;
                                    break;
                                }
                            }
                            if (!selfExist) {
                                //????????????
                                if (linkMicList.isEmpty()) {
                                    linkMicList.add(selfDataBean);
                                } else {
                                    linkMicList.add(1, selfDataBean);//????????????
                                }
                            }

                            if (linkMicView != null) {
                                linkMicView.onChangeListShowMode(PLVLinkMicListShowModeGetter.getJoinedMicShowMode(isAudioLinkMic));
                                linkMicView.onJoinLinkMic();
                                linkMicView.updateAllLinkMicList();
                            }

                            loadLinkMicConnectMode(avConnectMode);
                        }
                    });
            rtcInvokeStrategy.setOnLeaveLinkMicListener(new IPLVRTCInvokeStrategy.OnLeaveLinkMicListener() {
                @Override
                public void onLeaveLinkMic() {
                    Iterator<PLVLinkMicItemDataBean> it = linkMicList.iterator();//????????????
                    while (it.hasNext()) {
                        PLVLinkMicItemDataBean dataBean = it.next();
                        if (dataBean.getLinkMicId().equals(myLinkMicId)) {
                            if (linkMicView != null) {
                                linkMicView.onUsersLeave(Collections.singletonList(myLinkMicId));
                            }
                            it.remove();
                            break;
                        }
                    }

                    if (linkMicView != null) {
                        linkMicView.onChangeListShowMode(PLVLinkMicListShowModeGetter.getLeavedMicShowMode());
                        linkMicView.onLeaveLinkMic();
                    }
                }
            });
        } else {
            //???RTC???????????????
            rtcInvokeStrategy = new PLVRTCWatchDisabledStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new IPLVRTCInvokeStrategy.OnJoinLinkMicListener() {
                        @Override
                        public void onJoinLinkMic(PLVLinkMicJoinSuccess data) {
                            stopJoinTimeoutCount();
                            if (!linkMicList.isEmpty()) {
                                //??????????????????????????????????????????????????????????????????????????????RTC???????????????????????????RTC????????????????????????????????????????????????????????????????????????
                                PLVCommonLog.w(TAG, "????????????????????????????????????????????????????????????????????????????????????????????????????????????\n" + linkMicList.toString());
                                cleanLinkMicListData();
                            }
                            //?????????????????????????????????rtc????????????????????????????????????
                            linkMicList.add(0, PLVLinkMicDataMapper.map2LinkMicItemData(data));//????????????
                            //???????????????????????????????????????rtc?????????????????????????????????????????????????????????
                            if (linkMicView != null) {
                                linkMicView.onJoinRtcChannel();
                                linkMicView.onJoinLinkMic();
                            }

                            loadLinkMicConnectMode(avConnectMode);

                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVRxTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    requestLinkMicListFromServer();
                                }
                            });
                        }
                    });
            rtcInvokeStrategy.setOnLeaveLinkMicListener(new IPLVRTCInvokeStrategy.OnLeaveLinkMicListener() {
                @Override
                public void onLeaveLinkMic() {
                    if (linkMicView != null) {
                        linkMicView.onLeaveLinkMic();
                    }
                }
            });
        }

        rtcInvokeStrategy.setOnBeforeJoinChannelListener(new IPLVRTCInvokeStrategy.OnBeforeJoinChannelListener() {
            @Override
            public void onBeforeJoinChannel(PLVLinkMicListShowMode linkMicListShowMode) {
                startJoinTimeoutCount(new Runnable() {
                    @Override
                    public void run() {
                        if (linkMicView != null) {
                            linkMicView.onJoinChannelTimeout();
                        }
                    }
                });
                if (linkMicView != null) {
                    linkMicView.onPrepareLinkMicList(myLinkMicId, linkMicListShowMode, linkMicList);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API-presenter????????????">
    @Override
    public void destroy() {
        //???????????????????????????????????????????????????????????????joinLeave
        if(isJoinLinkMic()){
            //??????joinLeave
            linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        }
        leaveChannel();
        dispose(getLinkMicListDelay);
        dispose(getLinkMicListTimer);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(messageListener);
        linkMicList.clear();
        muteCacheList.clear();
        myLinkMicId = "";
        this.linkMicView = null;
        linkMicInitState = LINK_MIC_UNINITIATED;
        linkMicManager.destroy();
        if (linkMicMsgHandler != null) {
            linkMicMsgHandler.destroy();
        }
        PolyvLinkMicConfig.getInstance().clear();
    }

    @Override
    public void requestJoinLinkMic() {
        requestPermissionAndJoin();
    }

    @Override
    public void cancelRequestJoinLinkMic() {
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
    }

    @Override
    public void leaveLinkMic() {
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setLeaveLinkMic();
        }
    }

    @Override
    public void muteAudio(boolean mute) {
        PLVLinkMicMedia linkMicMedia = new PLVLinkMicMedia();
        linkMicMedia.setType("audio");
        linkMicMedia.setMute(mute);

        linkMicManager.sendMuteEventMsg(linkMicMedia);
        linkMicManager.muteLocalAudio(mute);
        for (int i = 0; i < linkMicList.size(); i++) {
            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
            if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                plvLinkMicItemDataBean.setMuteAudio(mute);
                if (linkMicView != null) {
                    linkMicView.onUserMuteAudio(myLinkMicId, mute, i);
                }
                break;
            }
        }
    }

    @Override
    public void muteVideo(boolean mute) {
        PLVLinkMicMedia linkMicMedia = new PLVLinkMicMedia();
        linkMicMedia.setType("video");
        linkMicMedia.setMute(mute);
        linkMicManager.sendMuteEventMsg(linkMicMedia);
        linkMicManager.muteLocalVideo(mute);
        for (int i = 0; i < linkMicList.size(); i++) {
            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
            if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                plvLinkMicItemDataBean.setMuteVideo(mute);
                if (linkMicView != null) {
                    linkMicView.onUserMuteVideo(myLinkMicId, mute, i);
                }
                break;
            }
        }
    }

    @Override
    public void muteAudio(String linkMicId, boolean mute) {
        if (myLinkMicId != null && myLinkMicId.equals(linkMicId)) {
            muteAudio(mute);
        } else {
            linkMicManager.muteRemoteAudio(linkMicId, mute);
        }
    }

    @Override
    public void muteVideo(String linkMicId, boolean mute) {
        if (myLinkMicId != null && myLinkMicId.equals(linkMicId)) {
            muteVideo(mute);
        } else {
            linkMicManager.muteRemoteVideo(linkMicId, mute);
        }
    }

    @Override
    public void muteAllAudio(boolean mute) {
        isMuteAllAudio = mute;
        linkMicManager.muteAllRemoteAudio(mute);
    }

    @Override
    public void muteAllVideo(boolean mute) {
        isMuteAllVideo = mute;
        linkMicManager.muteAllRemoteVideo(mute);
    }

    @Override
    public void switchCamera() {
        linkMicManager.switchCamera();
    }

    @Override
    public void setPushPictureResolutionType(int type) {
        linkMicManager.setPushPictureResolutionType(type);
    }

    @Override
    public SurfaceView createRenderView(Context context) {
        return linkMicManager.createRendererView(context);
    }

    @Override
    public String getLinkMicId() {
        return linkMicManager.getLinkMicUid();
    }

    @Override
    public String getMainTeacherLinkMicId() {
        return mainTeacherLinkMicId;
    }

    @Override
    public void setupRenderView(SurfaceView renderView, String linkMicId) {
        if (linkMicManager.getLinkMicUid().equals(linkMicId)) {
            if (liveRoomDataManager.isOnlyAudio()) {
                linkMicManager.setupLocalVideo(renderView, PLVStreamerConfig.RenderMode.RENDER_MODE_NONE);
            }
            linkMicManager.setupLocalVideo(renderView, linkMicId);
        } else {
            linkMicManager.setupRemoteVideo(renderView, linkMicId);
            if (isMuteAllAudio) {
                linkMicManager.muteRemoteAudio(linkMicId, true);
            }
            if (isMuteAllVideo) {
                linkMicManager.muteRemoteVideo(linkMicId, true);
            }
        }
    }

    @Override
    public void releaseRenderView(SurfaceView renderView) {
        linkMicManager.releaseRenderView(renderView);
    }

    @Override
    public boolean isJoinLinkMic() {
        if (rtcInvokeStrategy != null) {
            return rtcInvokeStrategy.isJoinLinkMic();
        } else {
            return false;
        }
    }

    @Override
    public boolean isJoinChannel() {
        if (rtcInvokeStrategy != null) {
            return rtcInvokeStrategy.isJoinChannel();
        } else {
            return false;
        }
    }

    @Override
    public void setIsAudioLinkMic(boolean isAudioLinkMic) {
        long interval = (System.currentTimeMillis() - socketRefreshOpenStatusData) / 1000;
        //???????????????????????????????????????????????????socket?????????????????????????????????10?????????????????????????????????
        if (interval < 10) {
            return;
        }
        this.isAudioLinkMic = isAudioLinkMic;//?????????????????????????????????????????????
    }

    @Override
    public boolean getIsAudioLinkMic() {
        return isAudioLinkMic;
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        this.isTeacherOpenLinkMic = isTeacherOpenLinkMic;
        if (isJoinLinkMic() && !isTeacherOpenLinkMic) {
            leaveLinkMic();
        }
    }

    @Override
    public boolean isTeacherOpenLinkMic() {
        return isTeacherOpenLinkMic;
    }

    @Override
    public boolean isAloneChannelTypeSupportRTC() {
        return liveRoomDataManager.getConfig().isAloneChannelType() && liveRoomDataManager.isSupportRTC();
    }

    @Override
    public void setLiveStart() {
        pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
            @Override
            public void run() {
                if (rtcInvokeStrategy != null) {
                    rtcInvokeStrategy.setLiveStart();
                }
            }
        });
    }


    @Override
    public void setLiveEnd() {
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setLiveEnd();
        }
    }

    @Override
    public void setWatchRtc(boolean watchRtc) {
        if (isWatchRtc == watchRtc) {
            return;
        }
        setLiveEnd();
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.destroy();
        }
        isWatchRtc = watchRtc;
        initRTCInvokeStrategy();
        setLiveStart();
    }

    @Override
    public int getRTCListSize() {
        return linkMicList.size();
    }

    @Override
    public void resetRequestPermissionList(ArrayList<String> permissions) {
        linkMicManager.resetRequestPermissionList(permissions);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="http??????">

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????server??????????????????????????????
     * ???????????????????????????????????????
     * 1. ???????????????server??????????????????????????????????????????????????????????????????????????????????????????
     * 2. ????????????????????????????????????ID
     * 3. ?????????????????????????????????
     * 4. ????????????????????????????????????????????????????????????????????????????????????
     * 5. ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void requestLinkMicListFromServer() {
        String sessionId = liveRoomDataManager.getSessionId();
        //rtc?????????????????????????????????onPrepared?????????????????????sessionId???
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        linkMicManager.getLinkStatus(sessionId, new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
            @Override
            public void onSuccess(PLVLinkMicJoinStatus data) {
                PLVCommonLog.d(TAG, "PLVLinkMicPresenter.requestLinkMicListFromServer.onSuccess->\n" + PLVGsonUtil.toJson(data));
                if (data.getJoinList().isEmpty()) {
                    return;
                }
                List<String> newJoinUserList = new ArrayList<>();
                //????????????ID
                String teacherLinkMicId = "";
                //????????????ID
                String guestLinkMicId = "";

                //????????????????????????????????????????????????????????????????????????????????????????????????voice????????????=1???????????????????????????????????????????????????data????????????
                Iterator<PLVJoinInfoEvent> joinInfoEventIterator = data.getJoinList().iterator();
                while (joinInfoEventIterator.hasNext()) {
                    PLVJoinInfoEvent plvJoinInfoEvent = joinInfoEventIterator.next();
                    if (PLVSocketUserConstant.USERTYPE_GUEST.equals(plvJoinInfoEvent.getUserType()) && !plvJoinInfoEvent.getClassStatus().isVoice()) {
                        joinInfoEventIterator.remove();
                    }
                }


                //1. ???????????????server??????????????????????????????????????????????????????????????????????????????????????????
                for (PLVJoinInfoEvent plvJoinInfoEvent : data.getJoinList()) {
                    //??????????????????????????????
                    boolean isThisUserExistInLinkMicList = false;
                    String userId = plvJoinInfoEvent.getUserId();
                    for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                        if (userId.equals(itemDataBean.getLinkMicId())) {
                            isThisUserExistInLinkMicList = true;
                            break;
                        }
                    }
                    //???????????????????????????????????????????????????????????????????????????
                    if (!isThisUserExistInLinkMicList) {
                        PLVLinkMicItemDataBean itemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(plvJoinInfoEvent);
                        //??????????????????????????????
                        linkMicList.add(itemDataBean);
                        muteCacheList.updateUserMuteCacheWhenJoinList(itemDataBean);
                        newJoinUserList.add(plvJoinInfoEvent.getUserId());
                    }

                    String userType = plvJoinInfoEvent.getUserType();
                    if (userType != null) {
                        switch (userType) {
                            case PLVSocketUserConstant.USERTYPE_TEACHER:
                                teacherLinkMicId = plvJoinInfoEvent.getUserId();
                                break;
                            case PLVSocketUserConstant.USERTYPE_GUEST:
                                if (TextUtils.isEmpty(guestLinkMicId)) {
                                    guestLinkMicId = plvJoinInfoEvent.getUserId();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (TextUtils.isEmpty(teacherLinkMicId)) {
                    PLVCommonLog.d(TAG, "???????????????????????????");
                }
                if (TextUtils.isEmpty(guestLinkMicId)) {
                    PLVCommonLog.d(TAG, "???????????????????????????");
                }

                //2. ????????????????????????????????????ID
                String firstScreenLinkMicId = data.getMaster();
                //      ?????????????????????????????????????????????????????????????????????
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = teacherLinkMicId;
                }
                //      ??????????????????????????????????????????????????????
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = guestLinkMicId;
                }
                //      ?????????????????????????????????????????????????????????????????????????????????
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = data.getJoinList().get(0).getUserId();
                }
                PLVCommonLog.d(TAG, "????????????:" + firstScreenLinkMicId);

                if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
                    rtcInvokeStrategy.setFirstScreenLinkMicId(firstScreenLinkMicId, isMuteAllVideo);
                    if (linkMicView != null) {
                        //????????????-1?????????????????????????????????View???????????????????????????????????????id?????????
                        linkMicView.updateFirstScreenChanged(firstScreenLinkMicId, -1, -1);
                    }

                    if (linkMicList.size() > 0
                            && !linkMicList.get(0).getLinkMicId().equals(firstScreenLinkMicId)) {
                        //???????????????????????????????????????????????????
                        PLVLinkMicItemDataBean firstScreenDataBean = null;
                        for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                            if (plvLinkMicItemDataBean.getLinkMicId().equals(firstScreenLinkMicId)) {
                                firstScreenDataBean = plvLinkMicItemDataBean;
                                linkMicList.remove(plvLinkMicItemDataBean);
                                break;
                            }
                        }
                        if (firstScreenDataBean != null) {
                            linkMicList.add(0, firstScreenDataBean);
                        }
                    }
                }

                //3. ?????????????????????????????????
                if (!newJoinUserList.isEmpty()) {
                    if (linkMicView != null) {
                        linkMicView.onUsersJoin(newJoinUserList);
                    }
                }

                //4. ????????????????????????????????????????????????????????????????????????????????????
                if (liveRoomDataManager.getConfig().isAloneChannelType()
                        && teacherLinkMicId != null
                        && newJoinUserList.contains(teacherLinkMicId)) {
                    mainTeacherLinkMicId = teacherLinkMicId;
                    if (linkMicView != null) {
                        for (int i = 0; i < linkMicList.size(); i++) {
                            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                            if (teacherLinkMicId.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                                final String finalFirstScreenLinkMicId = firstScreenLinkMicId;
                                final String finalTeacherLinkMicId = teacherLinkMicId;
                                linkMicView.onAdjustTeacherLocation(teacherLinkMicId, i, liveRoomDataManager.isSupportRTC(), new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!finalFirstScreenLinkMicId.equals(finalTeacherLinkMicId)) {
                                            //?????????????????????????????????????????????????????????????????????????????????
                                            socketEventListener.onSwitchFirstScreen(finalFirstScreenLinkMicId);
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }
                }

                //5. ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                List<String> usersToRemove = new ArrayList<>();
                Iterator<PLVLinkMicItemDataBean> itemDataBeanIterator = linkMicList.iterator();
                while (itemDataBeanIterator.hasNext()) {
                    PLVLinkMicItemDataBean itemDataBean = itemDataBeanIterator.next();
                    //???????????????????????????????????????????????????????????????
                    boolean isLocalUserExistInServerList = false;
                    String linkMicId = itemDataBean.getLinkMicId();
                    for (PLVJoinInfoEvent plvJoinInfoEvent : data.getJoinList()) {
                        if (linkMicId.equals(plvJoinInfoEvent.getUserId())) {
                            isLocalUserExistInServerList = true;
                            break;
                        }
                    }
                    if (!isLocalUserExistInServerList) {
                        usersToRemove.add(itemDataBean.getLinkMicId());
                        itemDataBeanIterator.remove();
                    }
                }
                if (!usersToRemove.isEmpty()) {
                    if (linkMicView != null) {
                        linkMicView.onUsersLeave(usersToRemove);
                    }
                    if (usersToRemove.contains(myLinkMicId)) {
                        //?????????????????????????????????????????????(?????????????????????????????????????????????????????????????????????)??????????????????????????????
                        if (linkMicView != null) {
                            PLVCommonLog.d(TAG, "onNotInLinkMicList");
                            linkMicView.onNotInLinkMicList();
                        }
                    }
                }
            }

            @Override
            public void onFail(PLVLinkMicHttpRequestException throwable) {
                super.onFail(throwable);
                if (linkMicView != null) {
                    linkMicView.onLinkMicError(throwable.getErrorCode(), throwable);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket????????????">
    private void handleTeacherAllowJoin(boolean isAudioLinkMic) {
        //????????????????????????
        linkMicManager.enableLocalVideo(!isAudioLinkMic);
        //????????????
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setJoinLinkMic();
        }
        if (linkMicView != null) {
            linkMicView.onTeacherAllowJoin();
        }
    }

    private void handleTeacherCloseLinkMic() {
        if (isTeacherOpenLinkMic) {
            isTeacherOpenLinkMic = false;
            if (linkMicView != null) {
                linkMicView.onTeacherCloseLinkMic();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????rtc??????">
    //?????????????????????????????????
    private void startJoinTimeoutCount(final Runnable timeout) {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
        }
        linkJoinTimer = PLVRxTimer.delay(TIME_OUT_JOIN_CHANNEL, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                timeout.run();
            }
        });
    }

    private void stopJoinTimeoutCount() {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
            linkJoinTimer = null;
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    //????????????????????????????????????????????????????????????????????????rtc?????????onLeaveChannel()???????????????????????????????????????????????????
    void leaveChannel() {
        if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
            dispose(getLinkMicListTimer);
            cleanLinkMicListData();
            muteCacheList.clear();
            if (linkMicView != null) {
                linkMicView.onLeaveRtcChannel();
            }
        }
    }

    private void cleanLinkMicListData() {
        PLVCommonLog.d(TAG, "cleanLinkMicListData() called \n" + Log.getStackTraceString(new Throwable()));
        linkMicList.clear();
    }

    /**
     * ???????????????????????????
     */
    private void loadLinkMicConnectMode(String mode) {
        if (TextUtils.isEmpty(mode)) {
            //???????????????????????????????????????????????????
            muteVideo(true);
            muteAudio(false);
            return;
        }
        if ("audio".equals(mode)) {
            muteAudio(true);
            //?????????????????????
            muteVideo(true);
        } else if ("video".equals(mode)) {
            muteVideo(true);
            //?????????????????????
            muteAudio(false);
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    /**
     * ?????????linkMicManager??????????????????????????????????????????
     * ??????linkMicManager???????????????????????????????????????
     *
     * @param action ????????????????????????
     */
    void pendingActionInCaseLinkMicEngineInitializing(final Runnable action) {
        switch (linkMicInitState) {
            case LINK_MIC_UNINITIATED:
                if (actionAfterLinkMicEngineCreated != null) {
                    actionAfterLinkMicEngineCreated.add(action);
                } else {
                    action.run();
                }
                break;
            case LINK_MIC_INITIATED:
                actionAfterLinkMicEngineCreated = null;
                action.run();
                break;
            default:
                break;
        }
    }

    @Nullable
    IPLVLinkMicContract.IPLVLinkMicView getLinkMicView() {
        return linkMicView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - rtc???????????????">
    private class PolyvLinkMicEventListenerImpl extends PolyvLinkMicEventListener {
        @Override
        public void onJoinChannelSuccess(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onJoinChannelSuccess, uid=" + uid);
            stopJoinTimeoutCount();
            //?????????????????????????????????
            loadLinkMicConnectMode(avConnectMode);
        }

        @Override
        public void onLeaveChannel() {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onLeaveChannel");
            leaveChannel();
        }

        @Override
        public void onUserJoined(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserJoined, uid=" + uid);
            dispose(getLinkMicListDelay);
            getLinkMicListDelay = PLVRxTimer.delay(DELAY_TO_GET_LINK_MIC_LIST, new Consumer<Object>() {
                @Override
                public void accept(Object o) throws Exception {
                    requestLinkMicListFromServer();
                }
            });
        }

        @Override
        public void onUserOffline(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserOffline, uid=" + uid);
            Iterator<PLVLinkMicItemDataBean> it = linkMicList.iterator();//??????
            while (it.hasNext()) {
                PLVLinkMicItemDataBean dataBean = it.next();
                if (dataBean.getLinkMicId().equals(uid)) {
                    if (linkMicView != null) {
                        linkMicView.onUsersLeave(Collections.singletonList(uid));
                    }
                    it.remove();
                    break;
                }
            }
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserMuteAudio,uid=" + uid + " mute=" + mute);
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (uid.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                    plvLinkMicItemDataBean.setMuteAudio(mute);
                    if (linkMicView != null) {
                        linkMicView.onUserMuteAudio(uid, mute, i);
                    }
                    break;
                }
            }
            muteCacheList.addOrUpdateAudioMuteCacheList(uid, mute);
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserMuteVideo uid=" + uid);
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (uid.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                    plvLinkMicItemDataBean.setMuteVideo(mute);
                    if (linkMicView != null) {
                        linkMicView.onUserMuteVideo(uid, mute, i);
                    }
                    break;
                }
            }
            muteCacheList.addOrUpdateVideoMuteCacheList(uid, mute);
        }

        @Override
        public void onLocalAudioVolumeIndication(PLVAudioVolumeInfo speaker) {
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                if (plvLinkMicItemDataBean.getLinkMicId().equals(speaker.getUid())) {
                    plvLinkMicItemDataBean.setCurVolume(speaker.getVolume());
                    break;
                }
            }
            if (linkMicView != null) {
                linkMicView.onLocalUserMicVolumeChanged();
            }
        }

        @Override
        public void onRemoteAudioVolumeIndication(PLVAudioVolumeInfo[] speakers) {
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                //???????????????????????????[onLocalAudioVolumeIndication]????????????
                if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                    continue;
                }

                boolean hitInVolumeInfoList = false;
                for (PLVAudioVolumeInfo speaker : speakers) {
                    if (plvLinkMicItemDataBean.getLinkMicId().equals(String.valueOf(speaker.getUid()))) {
                        hitInVolumeInfoList = true;
                        //?????????????????????0?????????????????????????????????PLVLinkMicItemDataBean.MAX_VOLUME???????????????
                        plvLinkMicItemDataBean.setCurVolume(speaker.getVolume());
                        break;
                    }
                }
                if (!hitInVolumeInfoList) {
                    plvLinkMicItemDataBean.setCurVolume(0);
                }
            }

            if (linkMicView != null) {
                linkMicView.onRemoteUserVolumeChanged(linkMicList);
            }
        }

        @Override
        public void onNetworkQuality(int quality) {
            super.onNetworkQuality(quality);
            if (linkMicView != null) {
                linkMicView.onNetQuality(quality);
            }
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - socket???????????????">
    private class PolyvLinkMicSocketEventListener implements PLVLinkMicMsgHandler.OnLinkMicDataListener {
        @Override
        public void onTeacherReceiveJoinRequest() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherReceiveJoinRequest");
        }

        @Override
        public void onTeacherAllowMeToJoin() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherAllowMeToJoin");
            handleTeacherAllowJoin(isAudioLinkMic);
        }

        @Override
        public void onTeacherHangupMe() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherHangupMe");
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setLeaveLinkMic();
            }
        }

        @Override
        public void onTeacherOpenLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherOpenLinkMic");
            isTeacherOpenLinkMic = true;
            if (linkMicView != null) {
                linkMicView.onTeacherOpenLinkMic();
            }
            /***
             * ///?????????????????????
             * if (liveRoomDataManager.getConfig().isParticipant() && !isJoinLinkMic) {
             *     linkMicManager.enableLocalVideo(!isAudioLinkMic);
             *     //???????????????????????????????????????????????????
             *     linkMicManager.switchRoleToAudience();
             *     handleTeacherAllowJoin(isAudioLinkMic);
             * }
             */
        }

        @Override
        public void onTeacherCloseLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherCloseLinkMic");
            handleTeacherCloseLinkMic();
            if (rtcInvokeStrategy != null) {
                //????????????????????????????????????????????????joinLeave??????
                if(isJoinLinkMic()){
                    rtcInvokeStrategy.setLeaveLinkMic();
                }
            }
        }

        @Override
        public void onTeacherMuteMedia(boolean isMute, boolean isAudio) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherMuteMedia");

            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
                return;
            }
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                    if (isAudio) {
                        plvLinkMicItemDataBean.setMuteAudio(isMute);
                        linkMicManager.muteLocalAudio(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteAudio(myLinkMicId, isMute, i);
                        }
                    } else {
                        plvLinkMicItemDataBean.setMuteVideo(isMute);
                        linkMicManager.muteLocalVideo(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteVideo(myLinkMicId, isMute, i);
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onUserJoinSuccess(PLVLinkMicItemDataBean dataBean) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onUserJoinSuccess");
            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
                return;
            }
            boolean userExistInList = false;
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(dataBean.getLinkMicId())) {
                    userExistInList = true;
                    break;
                }
            }
            if (!userExistInList) {
                muteCacheList.updateUserMuteCacheWhenJoinList(dataBean);
                if (dataBean.isTeacher()) {
                    // ????????????
                    linkMicList.add(0, dataBean);
                } else if (dataBean.getLinkMicId().equals(myLinkMicId)) {
                    // ????????????
                    PLVCommonLog.d(TAG, "onUserJoinSuccess-> ???????????????joinSuccess??????");
                } else {
                    // ????????????
                    linkMicList.add(dataBean);
                }
                if (linkMicView != null) {
                    linkMicView.onUsersJoin(Collections.singletonList(dataBean.getLinkMicId()));
                }
            }
        }

        @Override
        public void onTeacherSendCup(String linkMicId, int cupNum) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherSendCup");
            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
                return;
            }
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(linkMicId)) {
                    itemDataBean.setCupNum(cupNum);
                    break;
                }
            }
        }

        @Override
        public void onUpdateLinkMicType(boolean isAudio) {
            socketRefreshOpenStatusData = System.currentTimeMillis();
            isAudioLinkMic = isAudio;//??????socket????????????????????????
        }

        @Override
        public void onSwitchFirstScreen(final String linkMicId) {
            final IPLVLinkMicContract.IPLVLinkMicView view = linkMicView;
            if (view == null || TextUtils.isEmpty(linkMicId)) {
                return;
            }
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setFirstScreenLinkMicId(linkMicId, isMuteAllVideo);
            }
            //???[linkMicId]????????????????????????????????????
            if (linkMicList.isEmpty()) {
                return;
            }
            final int oldFirstScreenPos = 0;
            //????????????target
            PLVLinkMicItemDataBean itemToBeFirst = linkMicList.get(oldFirstScreenPos);
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(linkMicId)) {
                    itemToBeFirst = itemDataBean;
                }
            }
            final int indexOfTarget = linkMicList.indexOf(itemToBeFirst);

            if (liveRoomDataManager.getConfig().isAloneChannelType()) {
                //?????????????????????????????????????????????media???????????????????????????????????????????????????????????????
                if (indexOfTarget == view.getMediaViewIndexInLinkMicList()) {
                    return;
                }
                //????????????????????????????????????
                if (mainTeacherLinkMicId != null && mainTeacherLinkMicId.equals(linkMicId)) {
                    //1. ??????Media????????????????????????media????????????????????????
                    if (view.isMediaShowInLinkMicList()) {
                        view.performClickInLinkMicListItem(view.getMediaViewIndexInLinkMicList());
                    }
                } else {
                    //1. ????????????????????????????????????????????????????????????????????????????????????
                    view.performClickInLinkMicListItem(indexOfTarget);
                }
            } else {
                //??????target??????????????????????????????????????????
                if (indexOfTarget == oldFirstScreenPos) {
                    return;
                }

                //1. ??????PPT???????????????????????????ppt??????????????????
                boolean pptNeedToGoBackToLinkMicList = false;
                int pptIndexInLinkMicList = -1;
                if (view.isMediaShowInLinkMicList()) {
                    pptIndexInLinkMicList = view.getMediaViewIndexInLinkMicList();
                    pptNeedToGoBackToLinkMicList = true;
                    view.onSwitchPPTViewLocation(true);
                }

                //2. ??????????????????????????????????????????????????????????????????
                PLVLinkMicItemDataBean oldFirst = linkMicList.get(oldFirstScreenPos);
                linkMicList.remove(oldFirst);
                linkMicList.remove(itemToBeFirst);
                linkMicList.add(0, itemToBeFirst);
                linkMicList.add(indexOfTarget, oldFirst);
                view.onSwitchFirstScreen(linkMicId);

                //3. ???????????????????????????PPT????????????????????????
                if (pptNeedToGoBackToLinkMicList) {
                    view.performClickInLinkMicListItem(pptIndexInLinkMicList);
                }
            }

            view.updateFirstScreenChanged(linkMicId, oldFirstScreenPos, indexOfTarget);
        }

        @Override
        public void onSwitchPPTViewLocation(boolean toMainScreen) {
            if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
                //?????????????????????????????????????????????
                if (linkMicView != null) {
                    linkMicView.onSwitchPPTViewLocation(toMainScreen);
                }
            }
        }

        @Override
        public void onFinishClass() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onFinishClass");
            handleTeacherCloseLinkMic();
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setLiveEnd();
            }
        }

        @Override
        public void onLinkMicConnectMode(String avConnectMode) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onLinkMicConnectMode " + avConnectMode);
            //socket????????????????????????????????????????????????????????????????????????
            PLVLinkMicPresenter.this.avConnectMode = avConnectMode;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    //????????????????????????????????????????????????????????????????????????
    private void requestPermissionAndJoin() {

        Activity activity = ActivityUtils.getTopActivity();
        ArrayList<String> permissions = new ArrayList<>(Arrays.asList(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        ));
        PLVFastPermission.getInstance()
                .start(activity, permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
                            @Override
                            public void run() {
                                linkMicManager.sendJoinRequestMsg();
                            }
                        });
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        IPLVLinkMicTraceLogSender iplvLinkMicTraceLogSender = new PLVLinkMicTraceLogSender();
                        iplvLinkMicTraceLogSender.setLogModuleClass(PLVLinkMicELog.class);
                        iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.PERMISSION_DENIED," deniedPermissions: "+deniedPermissions+" deniedForeverP: "+deniedForeverP);
                        if (deniedForeverP == null) {
                            linkMicView.onLeaveLinkMic();
                        } else {
                            showRequestPermissionDialog();
                        }
                    }
                });
    }

    private void showRequestPermissionDialog() {
        new AlertDialog.Builder(ActivityUtils.getTopActivity()).setTitle("??????")
                .setMessage("???????????????????????????????????????????????????????????????????????????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(ActivityUtils.getTopActivity());
                        linkMicView.onLeaveLinkMic();
                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ActivityUtils.getTopActivity(), "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                        linkMicView.onLeaveLinkMic();
                    }
                }).setCancelable(false).show();
    }
    // </editor-fold>
}
