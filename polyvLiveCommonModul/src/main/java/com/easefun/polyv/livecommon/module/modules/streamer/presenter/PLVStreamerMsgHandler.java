package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.streamer.listener.PLVSStreamerEventListener;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.streamer.transfer.PLVStreamerInnerDataTransfer;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVBanIpEvent;
import com.plv.socket.event.chat.PLVSetNickEvent;
import com.plv.socket.event.chat.PLVUnshieldEvent;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinLeaveSEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

/**
 * ?????????????????????????????????
 */
public class PLVStreamerMsgHandler {
    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = "PLVStreamerMsgHandler";
    private static final String LINK_MIC_TYPE_AUDIO = "audio";

    private final PLVStreamerPresenter streamerPresenter;

    private PLVSocketIOObservable.OnConnectStatusListener onConnectStatusListener;
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;

    private PLVSStreamerEventListener linkMicEventHandler;

    @Nullable
    private String lastFirstScreenUserId;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVStreamerMsgHandler(PLVStreamerPresenter streamerPresenter) {
        this.streamerPresenter = streamerPresenter;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void run() {
        observeSocketData();
    }

    public void destroy() {
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnConnectStatusListener(onConnectStatusListener);
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);

        streamerPresenter.getStreamerManager().removeEventHandler(linkMicEventHandler);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket - ?????????????????????">
    private void observeSocketData() {
        onConnectStatusListener = new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                //?????????????????????????????????????????????????????????????????????????????????
                if (PLVSocketStatus.STATUS_RECONNECTSUCCESS == status.getStatus()) {
                    streamerPresenter.requestMemberList();
                }
            }
        };
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                switch (event) {
                    //????????????
                    case PLVBanIpEvent.EVENT:
                        PLVBanIpEvent banIpEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVBanIpEvent.class);
                        acceptBanIpEvent(banIpEvent);
                        break;
                    //??????????????????
                    case PLVUnshieldEvent.EVENT:
                        PLVUnshieldEvent unshieldEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVUnshieldEvent.class);
                        acceptUnshieldEvent(unshieldEvent);
                        break;
                    //??????????????????
                    case PLVSetNickEvent.EVENT:
                        PLVSetNickEvent setNickEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVSetNickEvent.class);
                        acceptSetNickEvent(setNickEvent);
                        break;
                    //??????????????????
                    case PLVKickEvent.EVENT:
                        PLVKickEvent kickEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVKickEvent.class);
                        acceptKickEvent(kickEvent);
                        break;
                    //??????????????????
                    case PLVLoginEvent.EVENT:
                        PLVLoginEvent loginEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVLoginEvent.class);
                        acceptLoginEvent(loginEvent);
                        break;
                    //??????????????????
                    case PLVLogoutEvent.EVENT:
                        PLVLogoutEvent logoutEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVLogoutEvent.class);
                        acceptLogoutEvent(logoutEvent);
                        break;
                    //sessionId??????
                    case PLVOnSliceIDEvent.EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceIDEvent.class);
                        acceptOnSliceIDEvent(onSliceIDEvent);
                        break;
                    case PLVEventConstant.Ppt.ON_SLICE_START_EVENT:
                        PLVOnSliceStartEvent onSliceStartEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceStartEvent.class);
                        acceptOnSliceStartEvent(onSliceStartEvent);
                        break;
                    //????????????????????????
                    case PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT:
                        PLVJoinRequestSEvent joinRequestSEvent = PLVGsonUtil.fromJson(PLVJoinRequestSEvent.class, message);
                        acceptJoinRequestSEvent(joinRequestSEvent);
                        break;
                    //????????????????????????
                    case PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT:
                        PLVJoinLeaveSEvent joinLeaveSEvent = PLVGsonUtil.fromJson(PLVJoinLeaveSEvent.class, message);
                        acceptJoinLeaveSEvent(joinLeaveSEvent);
                        break;
                    //????????????/??????????????????
                    case PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT:
                        PLVJoinAnswerSEvent joinAnswerSEvent = PLVGsonUtil.fromJson(PLVJoinAnswerSEvent.class, message);
                        acceptJoinAnswerSEvent(joinAnswerSEvent);
                        break;
                    case PLVEventConstant.LinkMic.TEACHER_SET_PERMISSION:
                        PLVPPTAuthentic authentic = PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message);
                        acceptTeacherSetPermissionEvent(authentic);
                        break;
                    //???????????????????????????????????????
                    case PLVEventConstant.LinkMic.EVENT_MUTE_USER_MICRO:
                        PLVLinkMicMedia micMedia = PLVGsonUtil.fromJson(PLVLinkMicMedia.class, message);
                        if (micMedia != null) {
                            boolean isMute = micMedia.isMute();
                            boolean isAudio = LINK_MIC_TYPE_AUDIO.equals(micMedia.getType());
                            streamerPresenter.callUpdateGuestMediaStatus(isMute, isAudio);
                        }
                        break;
                    // ??????????????????
                    case PLVEventConstant.Class.SE_SWITCH_MESSAGE:
                        updateFirstScreen(PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message));
                        break;
                    // PPT??????????????????????????????
                    case PLVEventConstant.Class.SE_SWITCH_PPT_MESSAGE:
                        updateDocumentStreamerViewPosition(PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message));
                        break;
                    default:
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(onConnectStatusListener);
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                Socket.EVENT_MESSAGE);
    }

    private void acceptBanIpEvent(PLVBanIpEvent banIpEvent) {
        if (banIpEvent != null) {
            List<PLVSocketUserBean> shieldUsers = banIpEvent.getUserIds();
            if (shieldUsers == null) {
                return;
            }
            for (PLVSocketUserBean socketUserBean : shieldUsers) {
                final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(true);
                    streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onUpdateSocketUserData(item.first);
                        }
                    });
                }
            }
        }
    }

    private void acceptUnshieldEvent(PLVUnshieldEvent unshieldEvent) {
        if (unshieldEvent != null) {
            List<PLVSocketUserBean> unShieldUsers = unshieldEvent.getUserIds();
            if (unShieldUsers == null) {
                return;
            }
            for (PLVSocketUserBean socketUserBean : unShieldUsers) {
                final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(false);
                    streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onUpdateSocketUserData(item.first);
                        }
                    });
                }
            }
        }
    }

    private void acceptSetNickEvent(PLVSetNickEvent setNickEvent) {
        if (setNickEvent != null && PLVSetNickEvent.STATUS_SUCCESS.equals(setNickEvent.getStatus())) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(setNickEvent.getUserId());
            if (item != null) {
                PLVSocketUserBean socketUserBean = item.second.getSocketUserBean();
                socketUserBean.setNick(setNickEvent.getNick());
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUpdateSocketUserData(item.first);
                    }
                });
            }
        }
    }

    private void acceptKickEvent(PLVKickEvent kickEvent) {
        if (kickEvent != null && kickEvent.getUser() != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(kickEvent.getUser().getUserId());
            if (item != null) {
                streamerPresenter.memberList.remove(item.second);
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoveMemberListData(item.first);
                    }
                });
            }
        }
    }

    private void acceptLoginEvent(PLVLoginEvent loginEvent) {
        if (loginEvent != null && loginEvent.getUser() != null) {
            if (PLVSocketUserConstant.USERSOURCE_CHATROOM.equals(loginEvent.getUser().getUserSource())) {
                return;//??????"userSource":"chatroom"?????????
            }
            Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(loginEvent.getUser().getUserId());
            if (item != null) {
                return;
            }
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            memberItemDataBean.setSocketUserBean(loginEvent.getUser());
            streamerPresenter.memberList.add(memberItemDataBean);
            PLVStreamerPresenter.SortMemberListUtils.sort(streamerPresenter.memberList);
            final Pair<Integer, PLVMemberItemDataBean> newItem = streamerPresenter.getMemberItemWithUserId(loginEvent.getUser().getUserId());
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onAddMemberListData(newItem.first);
                }
            });
        }
    }

    private void acceptLogoutEvent(PLVLogoutEvent logoutEvent) {
        if (logoutEvent != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(logoutEvent.getUserId());
            if (item != null) {
                streamerPresenter.memberList.remove(item.second);
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoveMemberListData(item.first);
                    }
                });
            }
        }
    }

    private void acceptOnSliceIDEvent(PLVOnSliceIDEvent onSliceIDEvent) {
        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
            streamerPresenter.getLiveRoomDataManager().setSessionId(onSliceIDEvent.getData().getSessionId());
            //????????????????????????
            if ("audio".equals(onSliceIDEvent.getData().getAvConnectMode())) {
                streamerPresenter.callUpdateGuestMediaStatus(true, true);
            }

            //??????data?????????????????????
            if(PLVLiveChannelConfigFiller.generateNewChannelConfig().isLiveStreamingWhenLogin()){
                //??????ppt??????
                PLVPPTStatus pptStatus = new PLVPPTStatus();
                PLVOnSliceIDEvent.DataBean data = onSliceIDEvent.getData();
                pptStatus.setAutoId(data.getAutoId());
                pptStatus.setStep(PLVFormatUtils.integerValueOf(data.getStep(), 0));
                pptStatus.setPageId(data.getPageId());

                PLVStreamerInnerDataTransfer.getInstance().setPPTStatusForOnSliceStartEvent(pptStatus);
            }

            // ?????????????????????
            final boolean documentInMainScreen = onSliceIDEvent.getPptAndVedioPosition() == 0;
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onDocumentStreamerViewChange(documentInMainScreen);
                }
            });
        }
    }

    private void acceptOnSliceStartEvent(PLVOnSliceStartEvent onSliceStartEvent) {
        if (onSliceStartEvent != null && onSliceStartEvent.getData() != null) {
            //??????????????????sessionId??????????????????????????????sessionId??????????????????????????????????????????
            if (streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST)) {
                String sessionId = onSliceStartEvent.getSessionId();
                streamerPresenter.getLiveRoomDataManager().setSessionId(sessionId);
            }
        }
    }

    private void acceptJoinRequestSEvent(PLVJoinRequestSEvent joinRequestSEvent) {
        if (joinRequestSEvent != null && joinRequestSEvent.getUser() != null) {
            PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinRequestSEvent.getUser());
            final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinRequestSEvent.getUser());
            boolean hasChanged = streamerPresenter.updateMemberListItemInfo(socketUserBean, linkMicItemDataBean, false, true);
            //????????????????????????
            if (hasChanged) {
                streamerPresenter.callUpdateSortMemberList();
                //??????????????????????????????????????????
                if (!streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST)) {
                    streamerPresenter.getData().postUserRequestData(linkMicItemDataBean.getLinkMicId());
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUserRequest(linkMicItemDataBean.getLinkMicId());
                    }
                });
            }
        }
    }

    private void acceptJoinLeaveSEvent(PLVJoinLeaveSEvent joinLeaveSEvent) {
        if (joinLeaveSEvent != null && joinLeaveSEvent.getUser() != null) {
            String userId = joinLeaveSEvent.getUser().getUserId();
            if (streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerId().equals(userId) &&
                    streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST)) {
                //???????????????????????????????????????joinLeave????????????????????????????????????
                PLVCommonLog.d(TAG, "guest receive joinLeave");
            } else {
                updateMemberListWithLeave(joinLeaveSEvent.getUser().getUserId());
            }

        }
    }

    private void acceptJoinAnswerSEvent(PLVJoinAnswerSEvent joinAnswerSEvent) {
        if (joinAnswerSEvent != null) {
            String linkMicUid = joinAnswerSEvent.getUserId();
            if (joinAnswerSEvent.isRefuse()) {
                updateMemberListWithLeave(linkMicUid);
            }
        }
    }


    private void acceptTeacherSetPermissionEvent(final PLVPPTAuthentic authentic) {
        if(authentic != null){
            //memberlist???streamerlist??????????????????
            Pair<Integer, PLVMemberItemDataBean> memberItem = streamerPresenter.getMemberItemWithLinkMicId(authentic.getUserId());
            Pair<Integer, PLVLinkMicItemDataBean> streamerItem = streamerPresenter.getLinkMicItemWithLinkMicId(authentic.getUserId());

            if (PLVPPTAuthentic.TYPE_SPEAKER.equals(authentic.getType())) {
                if(memberItem != null && memberItem.second != null) {
                    memberItem.second.getLinkMicItemDataBean().setHasSpeaker(!authentic.hasNoAthuentic());
                }
                if(streamerItem != null && streamerItem.second != null) {
                    streamerItem.second.setHasSpeaker(!authentic.hasNoAthuentic());
                }
            } else if (PLVPPTAuthentic.PermissionType.SCREEN_SHARE.equals(authentic.getType())){
                if(memberItem != null && memberItem.second != null) {
                    memberItem.second.getLinkMicItemDataBean().setScreenShare(!authentic.hasNoAthuentic());
                }
                if(streamerItem != null && streamerItem.second != null) {
                    streamerItem.second.setScreenShare(!authentic.hasNoAthuentic());
                }
            }
            final boolean isCurrentUser = authentic.getUserId().equals(streamerPresenter.getStreamerManager().getLinkMicUid());
            final PLVSocketUserBean bean = (memberItem != null && memberItem.second != null) ? memberItem.second.getSocketUserBean() : null;
            streamerPresenter.onCurrentSpeakerChanged(authentic.getType(), !authentic.hasNoAthuentic(), isCurrentUser, bean);
        }


    }

    private void updateFirstScreen(final PLVPPTAuthentic authentic) {
        if (authentic == null || authentic.getUserId() == null) {
            return;
        }
        streamerPresenter.onFirstScreenChange(authentic.getUserId(), !authentic.hasNoAthuentic());
    }

    private void updateDocumentStreamerViewPosition(final PLVPPTAuthentic authentic) {
        if (authentic == null) {
            return;
        }
        final boolean documentInMainScreen = "0".equals(authentic.getStatus());
        streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onDocumentStreamerViewChange(documentInMainScreen);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????? - ?????????????????????">
    void observeLinkMicData() {
        linkMicEventHandler = new PLVSStreamerEventListener() {
            @Override
            public void onJoinChannelSuccess(String uid) {
                super.onJoinChannelSuccess(uid);
                PLVCommonLog.d(TAG, "onJoinChannelSuccess: " + uid);
                if (PLVSocketUserConstant.USERTYPE_GUEST.equals(streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType())) {
                    streamerPresenter.getStreamerManager().switchRoleToBroadcaster();
                    streamerPresenter.callUpdateGuestStatus(true);
                }
            }

            @Override
            public void onLeaveChannel() {
                super.onLeaveChannel();
                PLVCommonLog.d(TAG, "onLeaveChannel");
            }

            @Override
            public void onUserOffline(String uid) {
                super.onUserOffline(uid);
                PLVCommonLog.d(TAG, "onUserOffline: " + uid);
                updateMemberListWithLeave(uid);
            }

            @Override
            public void onUserJoined(String uid) {
                super.onUserJoined(uid);
                PLVCommonLog.d(TAG, "onUserJoined: " + uid);
                updateMemberListWithJoin(uid);
            }

            @Override
            public void onUserMuteVideo(String uid, boolean mute) {
                super.onUserMuteVideo(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteVideo: " + uid + "*" + mute);
                streamerPresenter.callUserMuteVideo(uid, mute);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : streamerPresenter.rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteVideoInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute));
                    }
                }
            }

            @Override
            public void onUserMuteAudio(final String uid, final boolean mute) {
                super.onUserMuteAudio(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteAudio: " + uid + "*" + mute);
                streamerPresenter.callUserMuteAudio(uid, mute);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : streamerPresenter.rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteAudioInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute));
                    }
                }
            }

            @Override
            public void onRemoteAudioVolumeIndication(PLVAudioVolumeInfo[] speakers) {
                super.onRemoteAudioVolumeIndication(speakers);
                for (PLVMemberItemDataBean memberItemDataBean : streamerPresenter.memberList) {
                    @Nullable PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
                    if (linkMicItemDataBean == null) {
                        continue;
                    }
                    String linkMicId = linkMicItemDataBean.getLinkMicId();
                    if (linkMicId == null || linkMicId.equals(streamerPresenter.getStreamerManager().getLinkMicUid())) {
                        continue;
                    }
                    boolean hitInVolumeInfoList = false;
                    for (PLVAudioVolumeInfo audioVolumeInfo : speakers) {
                        if (linkMicId.equals(audioVolumeInfo.getUid())) {
                            hitInVolumeInfoList = true;
                            //?????????????????????0?????????????????????????????????PLVLinkMicItemDataBean.MAX_VOLUME???????????????
                            linkMicItemDataBean.setCurVolume(audioVolumeInfo.getVolume());
                            break;
                        }
                    }
                    if (!hitInVolumeInfoList) {
                        linkMicItemDataBean.setCurVolume(0);
                    }
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoteUserVolumeChanged(streamerPresenter.memberList);
                    }
                });
            }

            @Override
            public void onLocalAudioVolumeIndication(final PLVAudioVolumeInfo speaker) {
                super.onLocalAudioVolumeIndication(speaker);
                Pair<Integer, PLVLinkMicItemDataBean> item = streamerPresenter.getLinkMicItemWithLinkMicId(speaker.getUid());
                if (item != null) {
                    item.second.setCurVolume(speaker.getVolume());
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLocalUserMicVolumeChanged(speaker.getVolume());
                    }
                });
            }
        };
        streamerPresenter.getStreamerManager().addEventHandler(linkMicEventHandler);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private void updateMemberListWithLeave(final String linkMicUid) {
        streamerPresenter.rtcJoinMap.remove(linkMicUid);
        Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithLinkMicId(linkMicUid);
        if (item != null) {
            item.second.getLinkMicItemDataBean().setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
            streamerPresenter.callUpdateSortMemberList();
        }
        final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = streamerPresenter.getLinkMicItemWithLinkMicId(linkMicUid);
        if (linkMicItem != null) {
            streamerPresenter.streamerList.remove(linkMicItem.second);
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    //???????????????????????????
                    view.onUsersLeave(Collections.singletonList(linkMicItem.second));
                }
            });
            streamerPresenter.updateMixLayoutUsers();
            streamerPresenter.updateLinkMicCount();
        }
    }

    private void updateMemberListWithJoin(final String linkMicUid) {
        if (!streamerPresenter.rtcJoinMap.containsKey(linkMicUid)) {
            PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
            linkMicItemDataBean.setLinkMicId(linkMicUid);
            streamerPresenter.rtcJoinMap.put(linkMicUid, linkMicItemDataBean);
        }
        Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithLinkMicId(linkMicUid);
        if (item != null) {
            boolean result = streamerPresenter.updateMemberListLinkMicStatusWithRtcJoinList(item.second, linkMicUid);
            if (result) {
                streamerPresenter.callUpdateSortMemberList();
            }
        }
    }
    // </editor-fold>
}
