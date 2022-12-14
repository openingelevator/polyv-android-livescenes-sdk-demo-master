package com.easefun.polyv.livestreamer.scenes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.di.PLVBeautyModule;
import com.easefun.polyv.livecommon.module.modules.beauty.helper.PLVBeautyInitHelper;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.beauty.IPLVLSBeautyLayout;
import com.easefun.polyv.livestreamer.modules.beauty.PLVLSBeautyLayout;
import com.easefun.polyv.livestreamer.modules.chatroom.IPLVLSChatroomLayout;
import com.easefun.polyv.livestreamer.modules.document.IPLVLSDocumentLayout;
import com.easefun.polyv.livestreamer.modules.document.PLVLSDocumentLayout;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentControllerExpandMenu;
import com.easefun.polyv.livestreamer.modules.statusbar.IPLVLSStatusBarLayout;
import com.easefun.polyv.livestreamer.modules.streamer.IPLVLSStreamerLayout;
import com.easefun.polyv.livestreamer.modules.streamer.di.PLVLSStreamerModule;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * ????????????????????????????????????
 * ?????????????????????????????????????????????????????????
 */
public class PLVLSLiveStreamerActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="??????">

    private static final String TAG = PLVLSLiveStreamerActivity.class.getSimpleName();

    // ?????? - ??????????????????????????????
    private static final String EXTRA_CHANNEL_ID = "channelId";             // ?????????
    private static final String EXTRA_VIEWER_ID = "viewerId";               // ?????????Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";           // ???????????????
    private static final String EXTRA_AVATAR_URL = "avatarUrl";             // ???????????????url
    private static final String EXTRA_ACTOR = "actor";                      // ???????????????
    private static final String EXTRA_USERTYPE = "usertype";                // ???????????????
    private static final String EXTRA_COLIN_MIC_TYPE = "colinMicType";      // ??????????????????
    private static final String EXTRA_IS_OPEN_MIC = "isOpenMic";            // ???????????????
    private static final String EXTRA_IS_OPEN_CAMERA = "isOpenCamera";      // ???????????????
    private static final String EXTRA_IS_FRONT_CAMERA = "isFrontCamera";    // ???????????????

    // ???????????????????????????????????????????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // ???????????????
    private IPLVLSStatusBarLayout plvlsStatusBarLy;
    // ????????????
    private IPLVLSDocumentLayout plvlsDocumentLy;
    // ?????????????????????
    private IPLVLSStreamerLayout plvlsStreamerLy;
    // ???????????????
    private IPLVLSChatroomLayout plvlsChatroomLy;
    // ????????????
    private IPLVLSBeautyLayout beautyLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????Activity?????????">

    /**
     * ??????????????????????????????
     *
     * @param activity      ?????????Activity
     * @param channelId     ?????????
     * @param viewerId      ?????????ID
     * @param viewerName    ???????????????
     * @param avatarUrl     ???????????????url
     * @param actor         ???????????????
     * @param isOpenMic     ?????????????????????
     * @param isOpenCamera  ??????????????????
     * @param isFrontCamera ???????????????????????????
     * @return PLVLaunchResult.isSuccess=true?????????????????????PLVLaunchResult.isSuccess=false??????????????????
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchStreamer(@NonNull Activity activity,
                                                 @NonNull String channelId,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String avatarUrl,
                                                 @NonNull String actor,
                                                 @NonNull String usertype,
                                                 @NonNull String colinMicType,
                                                 boolean isOpenMic,
                                                 boolean isOpenCamera,
                                                 boolean isFrontCamera) {
        if (activity == null) {
            return PLVLaunchResult.error("activity ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(avatarUrl)) {
            return PLVLaunchResult.error("avatarUrl ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(actor)) {
            return PLVLaunchResult.error("actor ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(usertype)) {
            return PLVLaunchResult.error("usertype ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(colinMicType)) {
            return PLVLaunchResult.error("colinMicType ????????????????????????????????????????????????");
        }

        Intent intent = new Intent(activity, PLVLSLiveStreamerActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_ACTOR, actor);
        intent.putExtra(EXTRA_USERTYPE, usertype);
        intent.putExtra(EXTRA_COLIN_MIC_TYPE, colinMicType);
        intent.putExtra(EXTRA_IS_OPEN_MIC, isOpenMic);
        intent.putExtra(EXTRA_IS_OPEN_CAMERA, isOpenCamera);
        intent.putExtra(EXTRA_IS_FRONT_CAMERA, isFrontCamera);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependency();
        setContentView(R.layout.plvls_live_streamer_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initBeautyModule();

        checkStreamRecover();

        observeStatusBarLayout();
        observeStreamerLayout();
        observeChatroomLayout();
        observeDocumentLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVBeautyInitHelper.getInstance().destroy();
        if (plvlsStatusBarLy != null) {
            plvlsStatusBarLy.destroy();
        }
        if (plvlsStreamerLy != null) {
            plvlsStreamerLy.destroy();
        }
        if (plvlsChatroomLy != null) {
            plvlsChatroomLy.destroy();
        }
        if (plvlsDocumentLy != null) {
            plvlsDocumentLy.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && requestCode == PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT
                && data != null) {
            // ????????????PPT??????
            if (plvlsDocumentLy != null) {
                plvlsDocumentLy.onSelectUploadDocument(data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (plvlsStatusBarLy != null && plvlsStatusBarLy.onBackPressed()) {
            return;
        } else if (plvlsChatroomLy != null && plvlsChatroomLy.onBackPressed()) {
            return;
        } else if (plvlsDocumentLy != null && plvlsDocumentLy.onBackPressed()) {
            return;
        } else if (beautyLayout != null && beautyLayout.onBackPressed()) {
            return;
        }

        // ?????????????????????????????????
        PLVLSConfirmDialog.Builder.context(this)
                .setTitleVisibility(View.GONE)
                .setContent(R.string.plv_live_room_dialog_steamer_exit_confirm_ask)
                .setRightButtonText(R.string.plv_common_dialog_confirm)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        PLVLSLiveStreamerActivity.super.onBackPressed();
                    }
                })
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVBeautyModule.instance)
                .addModule(PLVLSStreamerModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void initParams() {
        // ??????????????????
        Intent intent = getIntent();
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        String avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL);
        String actor = intent.getStringExtra(EXTRA_ACTOR);
        String role = intent.getStringExtra(EXTRA_USERTYPE);
        String colinMicType = intent.getStringExtra(EXTRA_COLIN_MIC_TYPE);

        // ??????Config??????
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, avatarUrl, role, actor);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);
        PLVLiveChannelConfigFiller.setColinMicType(colinMicType);

        PLVLiveLocalActionHelper.getInstance().enterChannel(channelId);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????????????????">
    private void initLiveRoomManager() {
        // ??????PLVLiveChannelConfigFiller?????????????????????????????????????????????????????????????????????
        liveRoomDataManager = new PLVLiveRoomDataManager(PLVLiveChannelConfigFiller.generateNewChannelConfig());

        // ?????????????????????????????????????????????
        liveRoomDataManager.requestChannelDetail();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????UI">
    private void initView() {
        plvlsStatusBarLy = findViewById(R.id.plvls_status_bar_ly);
        plvlsDocumentLy = findViewById(R.id.plvls_document_ly);
        plvlsStreamerLy = findViewById(R.id.plvls_streamer_ly);
        plvlsChatroomLy = findViewById(R.id.plvls_chatroom_ly);

        // ??????????????????????????????
        plvlsStreamerLy.init(liveRoomDataManager);
        // ??????????????????????????????
        boolean isOpenMic = getIntent().getBooleanExtra(EXTRA_IS_OPEN_MIC, true);
        boolean isOpenCamera = getIntent().getBooleanExtra(EXTRA_IS_OPEN_CAMERA, true);
        boolean isFrontCamera = getIntent().getBooleanExtra(EXTRA_IS_FRONT_CAMERA, true);
        plvlsStreamerLy.enableRecordingAudioVolume(isOpenMic);
        plvlsStreamerLy.enableLocalVideo(isOpenCamera);
        plvlsStreamerLy.setCameraDirection(isFrontCamera);

        if(PLVSStreamerInnerDataTransfer.getInstance().isOnlyAudio()){
            //??????????????????????????????????????????????????????????????????????????????isOpenCamera
            plvlsStreamerLy.enableLocalVideo(false);
        }

        // ????????????????????????
        plvlsStatusBarLy.init(liveRoomDataManager);

        // ????????????????????????streamerView??????????????????????????????
        plvlsStreamerLy.getStreamerPresenter().registerView(plvlsStatusBarLy.getMemberLayoutStreamerView());
        plvlsStreamerLy.getStreamerPresenter().requestMemberList();

        // ????????????????????????
        plvlsChatroomLy.init(liveRoomDataManager);

        // ?????????????????????
        plvlsDocumentLy.init(liveRoomDataManager);
        plvlsStreamerLy.getStreamerPresenter().registerView(plvlsDocumentLy.getDocumentLayoutStreamerView());

        // ??????????????????
        PLVScreenUtils.enterLandscape(this);

        // ?????????????????????
        beautyLayout = new PLVLSBeautyLayout(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">

    private void initBeautyModule() {
        if (!PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_BEAUTY_ENABLE)) {
            return;
        }
        PLVBeautyInitHelper.getInstance().init(this, new PLVSugarUtil.Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) {
                PLVCommonLog.i(TAG, "initBeauty success: " + success);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????">
    private void observeStatusBarLayout() {
        //????????????????????????UI????????????
        plvlsStatusBarLy.setOnViewActionListener(new IPLVLSStatusBarLayout.OnViewActionListener() {
            @Override
            public void onClassControl(boolean isStart) {
                if (isStart) {
                    plvlsStreamerLy.startClass();
                } else {
                    plvlsStreamerLy.stopClass();
                }
            }

            @Override
            public int getCurrentNetworkQuality() {
                return plvlsStreamerLy.getNetworkQuality();
            }

            @Override
            public boolean isStreamerStartSuccess() {
                return plvlsStreamerLy.isStreamerStartSuccess();
            }

            @Override
            public void updateLinkMicMediaType(boolean isVideoLinkMicType) {
            }

            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return plvlsStreamerLy.getBitrateInfo();
            }

            @Override
            public void onBitrateClick(int bitrate) {
                plvlsStreamerLy.setBitrate(bitrate);
            }

            @Override
            public boolean isCurrentLocalVideoEnable() {
                final Boolean res = plvlsStreamerLy.getStreamerPresenter().getData().getEnableVideo().getValue();
                return res != null && res;
            }

            @Override
            public void onMicControl(int position, boolean isMute) {
                if (position == 0) {
                    plvlsStreamerLy.enableRecordingAudioVolume(!isMute);
                } else {
                    plvlsStreamerLy.muteUserMedia(position, false, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (position == 0) {
                    plvlsStreamerLy.enableLocalVideo(!isMute);
                } else {
                    plvlsStreamerLy.muteUserMedia(position, true, isMute);
                }
            }

            @Override
            public void onFrontCameraControl(int position, boolean isFront) {
                if (position == 0) {
                    plvlsStreamerLy.setCameraDirection(isFront);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, boolean isAllowJoin) {
                plvlsStreamerLy.controlUserLinkMic(position, isAllowJoin);
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, final boolean isGrant) {
                plvlsStreamerLy.getStreamerPresenter().setUserPermissionSpeaker(userId, isGrant, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                    @Override
                    public void onCall(Object... args) {
                        final boolean isGuestTransferPermission = !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
                        final String text;
                        if (!isGrant) {
                            text = "?????????????????????";
                        } else if (isGuestTransferPermission) {
                            text = "?????????????????????";
                        } else {
                            text = "?????????????????????";
                        }
                        PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                                .setText(text)
                                .show();
                    }
                });
            }

            @Override
            public void closeAllUserLinkMic() {
                plvlsStreamerLy.closeAllUserLinkMic();
            }

            @Override
            public void muteAllUserAudio(boolean isMute) {
                plvlsStreamerLy.muteAllUserAudio(isMute);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ???????????????">
    private void observeStreamerLayout() {
        //????????????????????????
        plvlsStreamerLy.addOnStreamerStatusListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isStartedStatus) {
                if (isStartedStatus == null) {
                    return;
                }
                if (plvlsDocumentLy != null) {
                    plvlsDocumentLy.setStreamerStatus(isStartedStatus);
                }
                plvlsStatusBarLy.setStreamerStatus(isStartedStatus);
            }
        });
        //????????????????????????
        plvlsStreamerLy.addOnNetworkQualityListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvlsStatusBarLy.updateNetworkQuality(integer);
            }
        });
        //???????????????????????????
        plvlsStreamerLy.addOnStreamerTimeListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvlsStatusBarLy.updateStreamerTime(integer);
            }
        });
        //?????????????????????20s???????????????
        plvlsStreamerLy.addOnShowNetBrokenListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsStatusBarLy.showAlertDialogNoNetwork();
            }
        });
        //?????????????????????????????????
        plvlsStreamerLy.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) {
                    return;
                }
                plvlsStatusBarLy.updateUserRequestStatus(s);
            }
        });
        //????????????????????????????????????
        plvlsStreamerLy.addOnEnableAudioListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setOpenMicViewStatus(aBoolean);
                PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                        .setText("???" + (aBoolean ? "??????" : "??????") + "?????????")
                        .build()
                        .show();
            }
        });
        plvlsStreamerLy.addOnEnableVideoListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setOpenCameraViewStatus(aBoolean);
                PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                        .setText("???" + (aBoolean ? "??????" : "??????") + "?????????")
                        .build()
                        .show();
            }
        });
        plvlsStreamerLy.addOnIsFrontCameraListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setFrontCameraViewStatus(aBoolean);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????">
    private void observeChatroomLayout() {
        //????????????????????????UI????????????
        plvlsChatroomLy.setOnViewActionListener(new IPLVLSChatroomLayout.OnViewActionListener() {
            @Override
            public boolean onMicControl(boolean isMute) {
                return plvlsStreamerLy.enableRecordingAudioVolume(!isMute);
            }

            @Override
            public boolean onCameraControl(boolean isMute) {
                PLVLiveLocalActionHelper.getInstance().updateCameraEnable(!isMute);
                return plvlsStreamerLy.enableLocalVideo(!isMute);
            }

            @Override
            public boolean onFrontCameraControl(boolean isFront) {
                PLVLiveLocalActionHelper.getInstance().updateCameraDirection(isFront);
                return plvlsStreamerLy.setCameraDirection(isFront);
            }
        });
        //????????????????????????????????????
        plvlsChatroomLy.addOnOnlineCountListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvlsStatusBarLy.setOnlineCount(integer);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ??????">
    private void observeDocumentLayout() {
        // ??????????????????????????????
        plvlsDocumentLy.setMarkToolOnFoldExpandListener(new PLVLSDocumentControllerExpandMenu.OnFoldExpandListener() {
            @Override
            public void onFoldExpand(boolean isExpand) {
                plvlsChatroomLy.notifyDocumentMarkToolExpand(isExpand);
            }
        });

        // ??????????????????????????????
        plvlsDocumentLy.setOnSwitchFullScreenListener(new PLVLSDocumentLayout.OnSwitchFullScreenListener() {
            @Override
            public void onSwitchFullScreen(boolean toFullScreen) {
                plvlsChatroomLy.notifyDocumentLayoutFullScreen(toFullScreen);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">

    private void checkStreamRecover() {
        boolean isTeacher = PLVSocketUserConstant.USERTYPE_TEACHER.equals(PLVLiveChannelConfigFiller.generateNewChannelConfig().getUser().getViewerType());

        if(liveRoomDataManager.isNeedStreamRecover() && isTeacher){
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("???????????????????????????\n?????????????????????")
                    .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            liveRoomDataManager.setNeedStreamRecover(false);
                            plvlsStreamerLy.getStreamerPresenter().setRecoverStream(false);
                            plvlsStreamerLy.stopClass();
                        }
                    })
                    .setNegativeButton("????????????", null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (plvlsStreamerLy.getNetworkQuality() == PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                        plvlsStatusBarLy.showAlertDialogNoNetwork();
                        return;
                    }

                    liveRoomDataManager.setNeedStreamRecover(true);
                    plvlsStreamerLy.getStreamerPresenter().setRecoverStream(true);
                    PLVLiveLocalActionHelper.Action action = PLVLiveLocalActionHelper.getInstance().getChannelAction(liveRoomDataManager.getConfig().getChannelId());
                    plvlsStatusBarLy.switchPptType(action.pptType);
                    plvlsStreamerLy.setCameraDirection(action.isFrontCamera);
                    plvlsStreamerLy.enableLocalVideo(action.isEnableCamera);
                    plvlsStreamerLy.startClass();
                    dialog.dismiss();
                }
            });
        }
    }

    // </editor-fold >
}
