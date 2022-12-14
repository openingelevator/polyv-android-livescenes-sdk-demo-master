package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.liveroom.adapter.PLVLSMemberAdapter;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * ??????????????????
 */
public class PLVLSMemberLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="????????????">
    //????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //????????????
    private PLVMenuDrawer menuDrawer;

    //view
    private PLVBlurView blurView;
    private TextView plvlsMemberCountTv;
    private RecyclerView plvlsMemberListRv;
    private TextView plvlsMemberListLinkMicDownAllTv;
    private TextView plvlsMemberListLinkMicMuteAllAudioTv;

    //adapter
    private PLVLSMemberAdapter memberAdapter;

    //disposable
    private Disposable updateBlurViewDisposable;

    //????????????
    private boolean isStartedStatus;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVLSMemberLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        memberAdapter = new PLVLSMemberAdapter(liveRoomDataManager);
        plvlsMemberListRv.setAdapter(memberAdapter);

        memberAdapter.setOnViewActionListener(new PLVLSMemberAdapter.OnViewActionListener() {
            @Override
            public void onMicControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onMicControl(position, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onCameraControl(position, isMute);
                }
            }

            @Override
            public void onFrontCameraControl(int position, boolean isFront) {
                if (onViewActionListener != null) {
                    onViewActionListener.onFrontCameraControl(position, isFront);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, boolean isAllowJoin) {
                if (onViewActionListener != null) {
                    onViewActionListener.onControlUserLinkMic(position, isAllowJoin);
                }
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, boolean isGrant) {
                if(onViewActionListener != null){
                    onViewActionListener.onGrantSpeakerPermission(position, userId, isGrant);
                }
            }
        });

        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
            plvlsMemberListLinkMicDownAllTv.setVisibility(INVISIBLE);
            plvlsMemberListLinkMicMuteAllAudioTv.setVisibility(INVISIBLE);
        }
        updateMemberListLinkMicShowType(liveRoomDataManager.isOnlyAudio());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_member_layout, this);

        plvlsMemberCountTv = findViewById(R.id.plvls_member_count_tv);
        plvlsMemberListRv = findViewById(R.id.plvls_member_list_rv);
        plvlsMemberListLinkMicDownAllTv = findViewById(R.id.plvls_member_list_link_mic_down_all_tv);
        plvlsMemberListLinkMicMuteAllAudioTv = findViewById(R.id.plvls_member_list_link_mic_mute_all_audio_tv);

        plvlsMemberListLinkMicDownAllTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartedStatus) {
                    PLVLSConfirmDialog.Builder.context(getContext())
                            .setTitleVisibility(View.GONE)
                            .setContent(R.string.plv_linkmic_dialog_hang_all_off_confirm_ask)
                            .setRightButtonText(R.string.plv_common_dialog_confirm)
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    if (onViewActionListener != null) {
                                        onViewActionListener.closeAllUserLinkMic();
                                    }
                                    PLVToast.Builder.context(getContext())
                                            .setText("???????????????")
                                            .build()
                                            .show();
                                }
                            })
                            .show();
                } else {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_toast_please_click_class_first)
                            .build()
                            .show();
                }
            }
        });

        plvlsMemberListLinkMicMuteAllAudioTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isStartedStatus) {
                    final boolean currentIsMuteAll = v.isSelected();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (onViewActionListener != null) {
                                onViewActionListener.muteAllUserAudio(!currentIsMuteAll);
                                v.setSelected(!currentIsMuteAll);
                                plvlsMemberListLinkMicMuteAllAudioTv.setText(!currentIsMuteAll ? "??????????????????" : "????????????");
                            }
                        }
                    };
                    if (!currentIsMuteAll) {
                        PLVLSConfirmDialog.Builder.context(getContext())
                                .setTitleVisibility(View.GONE)
                                .setContent(R.string.plv_linkmic_dialog_mute_all_audio_confirm_ask)
                                .setRightButtonText(R.string.plv_common_dialog_confirm)
                                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        dialog.dismiss();
                                        runnable.run();
                                        PLVToast.Builder.context(getContext())
                                                .setText("???????????????")
                                                .build()
                                                .show();
                                    }
                                })
                                .show();
                    } else {
                        runnable.run();
                    }
                } else {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_toast_please_click_class_first)
                            .build()
                            .show();
                }
            }
        });

        plvlsMemberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        plvlsMemberListRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(0, ConvertUtils.dp2px(8)));

        blurView = findViewById(R.id.blur_ly);
        PLVBlurUtils.initBlurView(blurView);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ??????????????????????????????">
    public void open() {
        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            //?????????menuDrawer
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize((int) (landscapeWidth * 0.56));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });

            memberAdapter.setIsFirstOpenMemberLayout();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isOpen() {
        return menuDrawer != null && menuDrawer.isMenuVisible();
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    public void setOnlineCount(int onlineCount) {
        if (memberAdapter.getItemCount() == 0) {
            plvlsMemberCountTv.setText("");//?????????????????????????????????????????????
        } else {
            plvlsMemberCountTv.setText("(" + Math.max(onlineCount, memberAdapter.getItemCount()) + "???)");
        }
    }

    public void setStreamerStatus(boolean isStartedStatus) {
        this.isStartedStatus = isStartedStatus;
        memberAdapter.setStreamerStatus(isStartedStatus);
    }

    public void updateLinkMicMediaType(boolean isVideoLinkMicType) {
        memberAdapter.updateLinkMicMediaType(isVideoLinkMicType);
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - MVP?????????view?????????">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> leaveUsers) {
            // ????????????????????????????????????
            for (PLVLinkMicItemDataBean leaveUser : leaveUsers) {
                if (leaveUser.isHasSpeaker()) {
                    onViewActionListener.onGrantSpeakerPermission(-1, leaveUser.getUserId(), false);
                }
            }
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteVideo(uid, mute, streamerListPos, memberListPos);
            memberAdapter.updateUserMuteVideo(memberListPos);
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteAudio(uid, mute, streamerListPos, memberListPos);
            memberAdapter.updateVolumeChanged();
        }

        @Override
        public void onLocalUserMicVolumeChanged(int volume) {
            memberAdapter.updateVolumeChanged();
        }

        @Override
        public void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList) {
            super.onRemoteUserVolumeChanged(linkMicList);
            memberAdapter.updateVolumeChanged();
        }

        @Override
        public void onUpdateMemberListData(List<PLVMemberItemDataBean> dataBeanList) {
            super.onUpdateMemberListData(dataBeanList);
            memberAdapter.update(dataBeanList);
        }

        @Override
        public void onCameraDirection(boolean front, int pos) {
            super.onCameraDirection(front, pos);
            memberAdapter.updateCameraDirection(pos);
        }

        @Override
        public void onUpdateSocketUserData(int pos) {
            super.onUpdateSocketUserData(pos);
            memberAdapter.updateSocketUserData(pos);
        }

        @Override
        public void onAddMemberListData(int pos) {
            super.onAddMemberListData(pos);
            memberAdapter.insertUserData(pos);
        }

        @Override
        public void onRemoveMemberListData(int pos) {
            super.onRemoveMemberListData(pos);
            memberAdapter.removeUserData(pos);
        }

        @Override
        public void onReachTheInteractNumLimit() {
            super.onReachTheInteractNumLimit();
            PLVLSConfirmDialog.Builder.context(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent(R.string.plv_linkmic_dialog_reach_the_interact_num_limit)
                    .setIsNeedLeftBtn(false)
                    .setRightButtonText(R.string.plv_common_dialog_alright)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        public void onSetPermissionChange(String type, boolean isGranted, boolean isCurrentUser, PLVSocketUserBean user) {
            super.onSetPermissionChange(type, isGranted, isCurrentUser, user);
            if(type.equals(PLVPPTAuthentic.PermissionType.TEACHER)) {
                memberAdapter.updatePermissionChange();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    /**
     * ??????????????????????????????
     * @param isOnlyAudio ?????????????????????
     */
    private void updateMemberListLinkMicShowType(boolean isOnlyAudio){
        if(memberAdapter != null){
            memberAdapter.setOnlyShowAudioUI(isOnlyAudio);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="????????????????????????view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurView.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - view?????????????????????">
    public interface OnViewActionListener extends PLVLSMemberAdapter.OnViewActionListener {
        /**
         * ????????????????????????
         */
        void closeAllUserLinkMic();

        /**
         * ????????????????????????/????????????
         *
         * @param isMute true????????????false?????????
         */
        void muteAllUserAudio(boolean isMute);
    }
    // </editor-fold>
}
