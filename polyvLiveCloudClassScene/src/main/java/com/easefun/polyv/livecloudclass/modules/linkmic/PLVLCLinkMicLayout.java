package com.easefun.polyv.livecloudclass.modules.linkmic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.linkmic.adapter.PLVLinkMicListAdapter;
import com.easefun.polyv.livecloudclass.modules.linkmic.widget.PLVLinkMicRvLandscapeItemDecoration;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindow;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVLinkMicPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.module.utils.PLVNotchUtils;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * date: 2020/7/16
 * author: hwj
 * description: ??????????????????
 */
public class PLVLCLinkMicLayout extends FrameLayout implements IPLVLinkMicContract.IPLVLinkMicView, IPLVLCLinkMicLayout {

    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = PLVLCLinkMicLayout.class.getSimpleName();

    // ?????????????????????????????????
    private static final int ERROR_PERMISSION_DENIED = 1060501;

    //??????
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_LEFT = 8;
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT = 34;
    private static final int DP_LAND_SPEAKING_USER_VIEW_MARGIN_RIGHT_TO_LINK_MIC_LIST = 24;

    //??????????????????????????????????????????????????????????????????
    private static final int TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK = 0;
    //???????????????????????????
    private static final int TRY_SCROLL_VIEW_STATE_VISIBLE = 1;
    //??????????????????????????????????????????????????????
    private static final int TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED = 2;

    //Presenter
    private IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter;

    //View
    private IPLVLCLinkMicControlBar linkMicControlBar;
    private FrameLayout flMediaLinkMicRoot;
    private RecyclerView rvLinkMicList;
    private LinearLayout llTryScrollTip;
    private LinearLayout llSpeakingUsers;
    private TextView tvSpeakingUsersText;
    //?????????????????????
    private PLVLinkMicListAdapter linkMicListAdapter;
    private PLVLinkMicRvLandscapeItemDecoration landscapeItemDecoration = new PLVLinkMicRvLandscapeItemDecoration();

    //???????????????????????????????????????????????????
    private PLVViewSwitcher teacherLocationViewSwitcher;

    private final List<Runnable> onUserJoinPendingTask = new LinkedList<>();

    //Listener
    private OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener;
    private RecyclerView.OnScrollListener onScrollTryScrollTipListener;

    //????????????
    //media??????????????????????????????
    private boolean isMediaShowInLinkMicList = false;
    //media????????????????????????item?????????id
    @Nullable
    private String mediaInLinkMicListLinkMicId;
    //????????????????????????
    @TryScrollViewStateType
    private int curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;
    private PLVLiveChannelType liveChannelType;
    private boolean isShowLandscapeLayout = false;

    //?????????????????????
    private boolean curIsLandscape = false;
    //??????????????????
    private int landscapeWidth = 0;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVLCLinkMicLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_linkmic_media_layout, this, true);
        flMediaLinkMicRoot = findViewById(R.id.plvlc_linkmic_fl_media_linkmic_root);
        rvLinkMicList = findViewById(R.id.plvlc_link_mic_rv_linkmic_list);
        llTryScrollTip = findViewById(R.id.plvlc_link_mic_ll_try_scroll_tip);
        llSpeakingUsers = findViewById(R.id.plvlc_linkmic_ll_speaking_users);
        tvSpeakingUsersText = findViewById(R.id.plvlc_linkmic_tv_speaking_users_text);

        //init RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        rvLinkMicList.setLayoutManager(linearLayoutManager);
        rvLinkMicList.addItemDecoration(landscapeItemDecoration);
        //??????RecyclerView????????????
        rvLinkMicList.getItemAnimator().setAddDuration(0);
        rvLinkMicList.getItemAnimator().setChangeDuration(0);
        rvLinkMicList.getItemAnimator().setMoveDuration(0);
        rvLinkMicList.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = rvLinkMicList.getItemAnimator();
        if (rvAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
        }

        //init adapter
        linkMicListAdapter = new PLVLinkMicListAdapter(rvLinkMicList, linearLayoutManager, new PLVLinkMicListAdapter.OnPLVLinkMicAdapterCallback() {
            @Override
            public SurfaceView createLinkMicRenderView() {
                return linkMicPresenter.createRenderView(Utils.getApp());
            }

            @Override
            public void setupRenderView(SurfaceView surfaceView, String linkMicId) {
                linkMicPresenter.setupRenderView(surfaceView, linkMicId);
            }

            @Override
            public void releaseRenderView(SurfaceView surfaceView) {
                linkMicPresenter.releaseRenderView(surfaceView);
            }

            @Override
            public void muteAudioVideo(String linkMicId, boolean mute) {
                linkMicPresenter.muteAudio(linkMicId, mute);
                linkMicPresenter.muteVideo(linkMicId, mute);
            }

            @Override
            public void muteAllAudioVideo(boolean mute) {
                linkMicPresenter.muteAllAudio(mute);
                linkMicPresenter.muteAllVideo(mute);
            }

            @Override
            public void onClickItemListener(int pos, @Nullable PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
                if (onPLVLinkMicLayoutListener != null) {
                    if (switchViewHasMedia == null) {
                        //??????????????????media??????????????????????????????
                        onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(switchViewGoMainScreen);
                    } else if (switchViewHasMedia == switchViewGoMainScreen) {
                        //???????????????media??????????????????????????????PPT???item????????????????????????????????????
                        onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(switchViewGoMainScreen);
                    } else {
                        //???????????????media?????????????????????media???item???????????????????????????
                        onPLVLinkMicLayoutListener.onClickSwitchWithMediaTwice(switchViewHasMedia, switchViewGoMainScreen);
                    }
                }
                if (switchViewHasMedia == switchViewGoMainScreen) {
                    //media?????????????????????
                    isMediaShowInLinkMicList = false;
                    setMediaInLinkMicListLinkMicId(null);
                } else {
                    //media????????????
                    isMediaShowInLinkMicList = true;
                    setMediaInLinkMicListLinkMicId(String.valueOf(switchViewGoMainScreen.getTag(R.id.tag_link_mic_id)));
                }
            }
        });

        //init??????
        curIsLandscape = PLVScreenUtils.isLandscape(getContext());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????">
    private void initLinkMicControlBar(IPLVLCLinkMicControlBar linkMicControlBar) {
        if (linkMicControlBar == null) {
            PLVCommonLog.exception(new Throwable("linkMicController == null"));
            return;
        }
        this.linkMicControlBar = linkMicControlBar;
        //??????????????????????????????????????????
        linkMicControlBar.setOnPLCLinkMicControlBarListener(new IPLVLCLinkMicControlBar.OnPLCLinkMicControlBarListener() {
            @Override
            public void onClickRingUpLinkMic() {
                linkMicPresenter.requestJoinLinkMic();
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onRequestJoinLinkMic();
                }
            }

            @Override
            public void onClickRingOffLinkMic() {
                if (linkMicPresenter.isJoinLinkMic()) {
                    linkMicPresenter.leaveLinkMic();
                } else {
                    linkMicPresenter.cancelRequestJoinLinkMic();
                    if (onPLVLinkMicLayoutListener != null) {
                        onPLVLinkMicLayoutListener.onCancelRequestJoinLinkMic();
                    }
                }
            }

            @Override
            public void onClickCameraOpenOrClose(boolean toClose) {
                linkMicPresenter.muteVideo(toClose);
            }

            @Override
            public void onClickCameraFrontOfBack(boolean toFront) {
                linkMicPresenter.switchCamera();
            }

            @Override
            public void onClickMicroPhoneOpenOrClose(boolean toClose) {
                linkMicPresenter.muteAudio(toClose);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ???????????????????????????">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, IPLVLCLinkMicControlBar linkMicControlBar) {
        liveChannelType = liveRoomDataManager.getConfig().getChannelType();
        linkMicPresenter = new PLVLinkMicPresenter(liveRoomDataManager, this);
        initLinkMicControlBar(linkMicControlBar);
        updatePushResolution(curIsLandscape);
        observeOnAudioState(liveRoomDataManager);
    }


    @Override
    public void destroy() {
        linkMicPresenter.destroy();
    }

    @Override
    public void showAll() {
        PLVCommonLog.d(TAG, "show");
        showLinkMicList();
        showControlBar();
    }

    @Override
    public void hideAll() {
        PLVCommonLog.d(TAG, "hide");
        hideLinkMicList();
        linkMicControlBar.hide();
    }

    @Override
    public void hideLinkMicList() {
        PLVCommonLog.d(TAG, "hideOnlyLinkMicList");
        linkMicListAdapter.hideAllRenderView();
        //????????????post?????????????????????????????????View???????????????????????????????????????
        post(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        });
    }

    @Override
    public void showLinkMicList() {
        setVisibility(VISIBLE);
        linkMicListAdapter.showAllRenderView();
        //?????????????????????????????????????????????????????????????????????UI??????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????UI???
        linkMicListAdapter.updateAllItem();
    }

    @Override
    public void hideControlBar() {
        PLVCommonLog.d(TAG, "hide");
        //????????????????????????????????????????????????
        if (linkMicControlBar != null && curIsLandscape) {
            linkMicControlBar.hide();
        }
    }

    @Override
    public void showControlBar() {
        if (linkMicControlBar != null) {
            linkMicControlBar.show();
        }
    }

    @Override
    public void pause() {
        linkMicListAdapter.pauseAllRenderView();
    }

    @Override
    public void resume() {
        linkMicListAdapter.resumeAllRenderView();
    }

    @Override
    public boolean isPausing() {
        return linkMicListAdapter.isPausing();
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        linkMicControlBar.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
        linkMicPresenter.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
    }


    @Override
    public void setIsAudio(boolean isAudioLinkMic) {
        linkMicPresenter.setIsAudioLinkMic(isAudioLinkMic);
    }

    @Override
    public boolean isJoinChannel() {
        return linkMicPresenter.isJoinChannel();
    }

    @Override
    public boolean isMediaShowInLinkMicList() {
        return isMediaShowInLinkMicList;
    }

    @Override
    public void notifySwitchedPptToMainScreenOnJoinChannel() {
        PLVSwitchViewAnchorLayout firstScreenSwitchView = linkMicListAdapter.getFirstScreenSwitchView();
        if (firstScreenSwitchView != null) {
            // ??????????????????PPT???????????????????????????PPT????????????????????????????????????????????????
            onSwitchPPTViewLocation(false);
        } else {
            onUserJoinPendingTask.add(new Runnable() {
                @Override
                public void run() {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onSwitchPPTViewLocation(false);
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getMediaViewIndexInLinkMicList() {
        return linkMicListAdapter.getMediaViewIndexInLinkMicList();
    }

    @Override
    public void performClickInLinkMicListItem(final int index) {
        rvLinkMicList.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder viewHolder = rvLinkMicList.findViewHolderForAdapterPosition(index);
                if (viewHolder != null) {//??????????????????????????????adapter???item???onclick????????????notifyDataSetChanged????????????????????????????????????viewHolder???null
                    viewHolder.itemView.performClick();
                }
            }
        });
    }

    @Override
    public void updateAllLinkMicList() {
        linkMicListAdapter.updateAllItem();
    }

    @Override
    public void setOnPLVLinkMicLayoutListener(OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener) {
        this.onPLVLinkMicLayoutListener = onPLVLinkMicLayoutListener;
    }


    @Override
    public int getLandscapeWidth() {
        return landscapeWidth;
    }

    @Override
    public void setLiveStart() {
        linkMicPresenter.setLiveStart();
    }

    @Override
    public void setLiveEnd() {
        linkMicPresenter.setLiveEnd();
    }

    @Override
    public void setWatchLowLatency(boolean watchLowLatency) {
        if (PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch()) {
            linkMicPresenter.setWatchRtc(watchLowLatency);
        }
    }

    @Override
    public void setLogoView(PLVPlayerLogoView plvPlayerLogoView) {
        linkMicListAdapter.setPlvPlayerLogoView(plvPlayerLogoView);
    }

    @Override
    public void onRTCPrepared() {
        onPLVLinkMicLayoutListener.onRTCPrepared();
    }

    @Override
    public void updateFirstScreenChanged(String firstScreenLinkMicId, int oldPos, int newPos) {
        linkMicListAdapter.setFirstScreenLinkMicId(firstScreenLinkMicId);
        if (oldPos > 0) {
            linkMicListAdapter.updateUserMuteVideo(oldPos);
        }
        if (newPos > 0) {
            linkMicListAdapter.updateUserMuteVideo(newPos);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVLinkMicContract.IPLVLinkMicView??????">

    @Override
    public void onLinkMicError(int errorCode, Throwable throwable) {
        PLVCommonLog.exception(throwable);
        if (errorCode == ERROR_PERMISSION_DENIED) {
            new AlertDialog.Builder(getContext()).setTitle(R.string.plv_common_dialog_tip)
                    .setMessage(R.string.plv_linkmic_error_tip_permission_denied)
                    .setPositiveButton(R.string.plv_common_dialog_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVFastPermission.getInstance().jump2Settings(getContext());
                        }
                    })
                    .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), R.string.plv_linkmic_error_tip_permission_cancel, Toast.LENGTH_SHORT).show();
                        }
                    }).setCancelable(false).show();
        } else {
            ToastUtils.showShort(getResources().getString(R.string.plv_linkmic_toast_error) + errorCode);
        }
    }

    @Override
    public void onTeacherOpenLinkMic() {
        //??????????????????
        linkMicControlBar.setIsTeacherOpenLinkMic(true);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onChannelLinkMicOpenStatusChanged(true);
        }
    }

    @Override
    public void onTeacherCloseLinkMic() {
        //??????????????????
        linkMicControlBar.setIsTeacherOpenLinkMic(false);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onChannelLinkMicOpenStatusChanged(false);
        }
    }

    @Override
    public void onTeacherAllowJoin() {
        PLVCommonLog.d(TAG, "onTeacherAllowJoin");
    }

    @Override
    public void onJoinChannelTimeout() {
        ToastUtils.showShort("??????????????????????????????");
    }

    @Override
    public void onPrepareLinkMicList(String linkMicUid, PLVLinkMicListShowMode linkMicListShowMode, List<PLVLinkMicItemDataBean> linkMicList) {
        PLVCommonLog.d(TAG, "PLVLinkMicLayout.onBeforeJoinChannel");
        //???????????????????????????????????????????????????
        linkMicListAdapter.setDataList(linkMicList);
        linkMicListAdapter.setListShowMode(linkMicListShowMode);
        linkMicListAdapter.setMyLinkMicId(linkMicUid);

        rvLinkMicList.setAdapter(linkMicListAdapter);

        //??????????????????????????????????????????????????????????????????????????????????????????0
        int marginRight = PLVNotchUtils.hasNotchInScreen((Activity) getContext()) ? PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT) : 0;
        landscapeWidth = linkMicListAdapter.getItemWidth() + PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_LEFT) + marginRight;
        //???????????????????????????????????????????????????????????????????????????????????????????????????
        if (PLVScreenUtils.isPortrait(getContext())) {
            onPortrait();
        } else {
            onLandscape();
        }
        //??????????????????View????????????
        MarginLayoutParams lpOfSpeakingUsers = (MarginLayoutParams) llSpeakingUsers.getLayoutParams();
        lpOfSpeakingUsers.rightMargin = landscapeWidth + PLVScreenUtils.dip2px(DP_LAND_SPEAKING_USER_VIEW_MARGIN_RIGHT_TO_LINK_MIC_LIST);
        llSpeakingUsers.setLayoutParams(lpOfSpeakingUsers);

        linkMicControlBar.setIsAudio(linkMicPresenter.getIsAudioLinkMic());

        initShouldShowLandscapeRTCLayout();
    }

    @Override
    public void onJoinRtcChannel() {
        curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;
        //?????????????????????
        flMediaLinkMicRoot.setKeepScreenOn(true);
        flMediaLinkMicRoot.setVisibility(VISIBLE);
        //??????????????????
        linkMicListAdapter.updateAllItem();
        //??????????????????
        Activity activity = (Activity) getContext();
        PLVForegroundService.startForegroundService(activity.getClass(), "?????????", R.drawable.ic_launcher);

        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onJoinRtcChannel();
        }
        isMediaShowInLinkMicList = false;
    }

    @Override
    public void onLeaveRtcChannel() {
        //????????????????????????????????????media????????????????????????????????????
        //??????????????????3????????????
        //1. ??????????????????RTC??????????????????????????????media????????????????????????
        //2. ?????????????????????RTC?????????????????????media???????????????????????????????????????
        //3. ??????????????????????????????media?????????PPT?????????????????????
        if (linkMicPresenter.isAloneChannelTypeSupportRTC()) {
            //??????????????????????????????RTC?????????

            //?????????????????????????????????????????????media(video)????????????
            if (teacherLocationViewSwitcher != null && teacherLocationViewSwitcher.isViewSwitched()) {
                teacherLocationViewSwitcher.switchView();
            } else {
                PLVCommonLog.exception(new Exception("teacherLocationViewSwitcher should not be null"));
            }
        } else {
            //????????????1. ??????????????????2. ?????????????????????RTC?????????

            //??????media?????????????????????????????????media?????????????????????????????????
            if (isMediaShowInLinkMicList && linkMicListAdapter.getSwitchViewHasMedia() != null) {
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(linkMicListAdapter.getSwitchViewHasMedia());
                }
            }
        }

        //??????????????????
        linkMicListAdapter.updateAllItem();
        linkMicListAdapter.releaseView();
        rvLinkMicList.removeAllViews();
        tryShowOrHideLandscapeRTCLayout(false);
        //?????????????????????
        flMediaLinkMicRoot.setVisibility(GONE);
        flMediaLinkMicRoot.setKeepScreenOn(false);
        //????????????????????????
        llTryScrollTip.setVisibility(GONE);
        //??????????????????????????????
        isMediaShowInLinkMicList = false;
        teacherLocationViewSwitcher = null;
        setMediaInLinkMicListLinkMicId(null);
        //??????????????????
        PLVForegroundService.stopForegroundService();
        //??????????????????
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onLeaveRtcChannel();
        }
    }

    @Override
    public void onChangeListShowMode(PLVLinkMicListShowMode linkMicListShowMode) {
        linkMicListAdapter.setListShowMode(linkMicListShowMode);
    }

    @Override
    public void onJoinLinkMic() {
        //????????????????????????
        PLVCommonLog.d(TAG, "onJoinLinkMic");
        ToastUtils.showShort("????????????");
        // ??????????????????????????????
        PLVDependManager.getInstance().get(PLVLCFloatingWindow.class).showByUser(false);
        // ????????????????????????rtc??????
        resume();
        //?????????????????????
        //??????????????????????????????????????????????????????????????????
        linkMicControlBar.setIsAudio(linkMicPresenter.getIsAudioLinkMic());
        linkMicControlBar.setJoinLinkMicSuccess();
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onJoinLinkMic();
        }
        tryShowOrHideLandscapeRTCLayout(true);
    }

    @Override
    public void onLeaveLinkMic() {
        //??????????????????

        //?????????????????????
        linkMicControlBar.setLeaveLinkMic();
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onLeaveLinkMic();
        }
    }

    @Override
    public void onUsersJoin(List<String> uids) {
        linkMicListAdapter.updateAllItem();

        if (curTryScrollViewState == TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK
                && linkMicListAdapter.getItemCount() > PLVLinkMicListAdapter.HORIZONTAL_VISIBLE_COUNT
                && getRvScrolledXOffset() == 0) {

            if (onScrollTryScrollTipListener == null) {
                onScrollTryScrollTipListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dx > 0) {
                            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED;
                            llTryScrollTip.setVisibility(GONE);
                            rvLinkMicList.removeOnScrollListener(this);
                        }
                    }
                };
            }

            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_VISIBLE;
            if (!curIsLandscape) {
                llTryScrollTip.setVisibility(VISIBLE);
            }
            rvLinkMicList.addOnScrollListener(onScrollTryScrollTipListener);
        }

        tryShowOrHideLandscapeRTCLayout(true);

        Iterator<Runnable> pendingTaskIterator = onUserJoinPendingTask.iterator();
        while (pendingTaskIterator.hasNext()) {
            pendingTaskIterator.next().run();
            pendingTaskIterator.remove();
        }
    }

    @Override
    public void onUsersLeave(List<String> uids) {
        //??????media???????????????????????????????????????????????????
        if (isMediaShowInLinkMicList && getMediaViewIndexInLinkMicList() != -1) {
            for (String uid : uids) {
                if (mediaInLinkMicListLinkMicId != null && mediaInLinkMicListLinkMicId.equals(uid)) {
                    //????????????????????????????????????media??????????????????media????????????
                    final int mediaIndex = getMediaViewIndexInLinkMicList();
                    RecyclerView.ViewHolder viewHolder = rvLinkMicList.findViewHolderForAdapterPosition(mediaIndex);
                    if (viewHolder != null) {
                        viewHolder.itemView.performClick();
                    }
                    break;
                }
            }
        }

        // ??????????????? ?????????????????? ????????????????????????????????????????????????
        if (liveChannelType == PLVLiveChannelType.ALONE) {
            final String mainTeacherLinkMicId = linkMicPresenter.getMainTeacherLinkMicId();
            if (mainTeacherLinkMicId != null && uids.contains(mainTeacherLinkMicId)) {
                if (teacherLocationViewSwitcher != null && teacherLocationViewSwitcher.isViewSwitched()) {
                    teacherLocationViewSwitcher.switchView();
                    linkMicListAdapter.setHasNotifyTeacherViewHolderBind(false);
                }
            }
        }

        linkMicListAdapter.updateAllItem();

        if (curTryScrollViewState == TRY_SCROLL_VIEW_STATE_VISIBLE
                && linkMicListAdapter.getItemCount() <= PLVLinkMicListAdapter.HORIZONTAL_VISIBLE_COUNT) {
            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;
            llTryScrollTip.setVisibility(GONE);
            if (onScrollTryScrollTipListener != null) {
                rvLinkMicList.removeOnScrollListener(onScrollTryScrollTipListener);
            }
        }

        tryShowOrHideLandscapeRTCLayout(false);
    }

    @Override
    public void onNotInLinkMicList() {
        ToastUtils.showShort("????????????????????????????????????");
        linkMicPresenter.leaveLinkMic();
    }


    @Override
    public void onUserMuteVideo(String uid, boolean mute, int pos) {
        //??????????????????
        linkMicListAdapter.updateUserMuteVideo(pos);
        if (uid.equals(linkMicPresenter.getLinkMicId())) {
            linkMicControlBar.setCameraOpenOrClose(!mute);
        }
    }

    @Override
    public void onUserMuteAudio(String uid, boolean mute, int pos) {
        //??????????????????
        linkMicListAdapter.updateVolumeChanged();
        if (uid.equals(linkMicPresenter.getLinkMicId())) {
            linkMicControlBar.setMicrophoneOpenOrClose(!mute);
        }
    }

    @Override
    public void onLocalUserMicVolumeChanged() {
        linkMicListAdapter.updateVolumeChanged();
    }

    @Override
    public void onRemoteUserVolumeChanged(List<PLVLinkMicItemDataBean> linkMicList) {
        linkMicListAdapter.updateVolumeChanged();
        //??????????????????????????????
        if (PLVScreenUtils.isLandscape(getContext())) {
            //???????????????????????????
            PLVLinkMicItemDataBean curSpeakingUser = null;
            boolean moreThanOneUserSpeaking = false;
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                if (plvLinkMicItemDataBean.getLinkMicId().equals(linkMicPresenter.getLinkMicId())) {
                    continue;
                }
                if (plvLinkMicItemDataBean.getCurVolume() != 0) {
                    if (curSpeakingUser == null) {
                        curSpeakingUser = plvLinkMicItemDataBean;
                    } else {
                        moreThanOneUserSpeaking = true;
                        break;
                    }
                }
            }
            if (curSpeakingUser != null) {
                //??????????????????????????????????????????????????????"..." ??? "???"
                String userNick = curSpeakingUser.getNick();
                StringBuilder userNickSBuilder = new StringBuilder();
                if (userNick.length() > 8) {
                    userNickSBuilder.append(userNick, 0, 8);
                } else {
                    userNickSBuilder.append(userNick);
                }

                if (moreThanOneUserSpeaking) {
                    userNickSBuilder.append("...???");
                }
                tvSpeakingUsersText.setText(userNickSBuilder.toString());
                llSpeakingUsers.setVisibility(VISIBLE);
            } else {
                //???????????????????????????????????????
                llSpeakingUsers.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onNetQuality(int quality) {
        linkMicListAdapter.updateNetQuality(quality);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onNetworkQuality(quality);
        }
    }

    @Override
    public void onSwitchFirstScreen(String linkMicId) {
        linkMicListAdapter.updateAllItem();
    }

    @Override
    public void onAdjustTeacherLocation(final String linkMicId, final int teacherPos, boolean isNeedSwitchToMain, final Runnable onAdjustFinished) {
        if (isNeedSwitchToMain) {
            linkMicListAdapter.setTeacherViewHolderBindListener(new PLVLinkMicListAdapter.OnTeacherSwitchViewBindListener() {
                @Override
                public void onTeacherSwitchViewBind(PLVSwitchViewAnchorLayout teacherSwitchView) {
                    linkMicListAdapter.setInvisibleItemLinkMicId(linkMicId);
                    if (onPLVLinkMicLayoutListener != null) {
                        teacherLocationViewSwitcher = new PLVViewSwitcher();
                        onPLVLinkMicLayoutListener.onChangeTeacherLocation(teacherLocationViewSwitcher, teacherSwitchView);
                        onAdjustFinished.run();
                    }
                    rvLinkMicList.post(new Runnable() {
                        @Override
                        public void run() {
                            linkMicListAdapter.updateInvisibleItem(linkMicId);
                        }
                    });
                }
            });
        } else {
            linkMicListAdapter.updateInvisibleItem(linkMicId);
            onAdjustFinished.run();
        }
    }

    @Override
    public void onSwitchPPTViewLocation(boolean toMainScreen) {
        PLVSwitchViewAnchorLayout firstScreenSwitchView = linkMicListAdapter.getFirstScreenSwitchView();
        //??????????????????????????????????????????
        if (firstScreenSwitchView == null) {
            return;
        }
        if (isMediaShowInLinkMicList && linkMicListAdapter.getSwitchViewHasMedia() != null) {
            //ppt?????????????????????

            if (linkMicListAdapter.getSwitchViewHasMedia() == firstScreenSwitchView) {
                //???ppt???????????????
                if (!toMainScreen) {
                    return;
                }
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(linkMicListAdapter.getSwitchViewHasMedia());
                    linkMicListAdapter.setSwitchViewHasMedia(null);
                }
            } else {
                //???ppt??????????????????
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithMediaTwice(linkMicListAdapter.getSwitchViewHasMedia(), firstScreenSwitchView);
                    linkMicListAdapter.setSwitchViewHasMedia(firstScreenSwitchView);
                }
            }

        } else {
            //ppt??????????????????
            //??????PPT?????????????????????????????????
            if (toMainScreen) {
                return;
            }
            //???ppt?????????????????????????????????
            if (onPLVLinkMicLayoutListener != null) {
                onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(firstScreenSwitchView);
                linkMicListAdapter.setSwitchViewHasMedia(firstScreenSwitchView);
            }
        }

        isMediaShowInLinkMicList = !toMainScreen;
        setMediaInLinkMicListLinkMicId(isMediaShowInLinkMicList ? String.valueOf(firstScreenSwitchView.getTag(R.id.tag_link_mic_id)) : null);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="set???get">
    private void setMediaInLinkMicListLinkMicId(String linkMicId) {
        this.mediaInLinkMicListLinkMicId = linkMicId;
        if (linkMicListAdapter != null) {
            linkMicListAdapter.setMediaInLinkMicListLinkMicId(linkMicId);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //????????????
            PLVCommonLog.d(TAG, "onConfigurationChanged->landscape");
            if (!curIsLandscape) {
                onLandscape();
            }
            curIsLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //????????????
            PLVCommonLog.d(TAG, "onConfigurationChanged->portrait");

            if (curIsLandscape) {
                onPortrait();
            }
            curIsLandscape = false;
        }
    }

    //????????????
    @SuppressLint("RtlHardcoded")
    private void onLandscape() {
        updatePushResolution(true);
        //root
        ConstraintLayout.LayoutParams lpOfRoot = (ConstraintLayout.LayoutParams) getLayoutParams();
        lpOfRoot.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lpOfRoot.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lpOfRoot.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lpOfRoot.width = LayoutParams.MATCH_PARENT;
        lpOfRoot.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        lpOfRoot.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        setLayoutParams(lpOfRoot);

        //rvRoot
        showOrHideLandscapeLayout(isShowLandscapeLayout);

        //rv
        FrameLayout.LayoutParams lpOfRv = (LayoutParams) rvLinkMicList.getLayoutParams();
        lpOfRv.gravity = Gravity.LEFT;
        lpOfRv.leftMargin = PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_LEFT);
        rvLinkMicList.setLayoutParams(lpOfRv);
        //??????vertical??????
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvLinkMicList.setLayoutManager(llm);
        linkMicListAdapter.setLinearLayoutManager(llm);
        //????????????item??????
        landscapeItemDecoration.setLandscape();
        //??????item????????????
        linkMicListAdapter.setShowRoundRect(true);

        //????????????????????????View
        llTryScrollTip.setVisibility(GONE);

        //????????????
        if (isJoinChannel()) {
            llSpeakingUsers.setVisibility(VISIBLE);
        } else {
            llSpeakingUsers.setVisibility(GONE);
        }
    }

    //????????????
    @SuppressLint("RtlHardcoded")
    private void onPortrait() {
        updatePushResolution(false);
        //root
        ConstraintLayout.LayoutParams lpOfRoot = (ConstraintLayout.LayoutParams) getLayoutParams();
        lpOfRoot.width = LayoutParams.MATCH_PARENT;
        lpOfRoot.height = LayoutParams.WRAP_CONTENT;
        lpOfRoot.topToBottom = R.id.plvlc_video_viewstub;
        lpOfRoot.topToTop = ConstraintLayout.LayoutParams.UNSET;
        lpOfRoot.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        setLayoutParams(lpOfRoot);

        //rvRoot
        LayoutParams lpOfRvRoot = (LayoutParams) flMediaLinkMicRoot.getLayoutParams();
        lpOfRvRoot.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lpOfRvRoot.gravity = Gravity.LEFT;
        flMediaLinkMicRoot.setLayoutParams(lpOfRvRoot);

        //rv
        FrameLayout.LayoutParams lpOfRv = (LayoutParams) rvLinkMicList.getLayoutParams();
        lpOfRv.leftMargin = 0;
        rvLinkMicList.setLayoutParams(lpOfRv);
        //??????horizontal??????
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvLinkMicList.setLayoutManager(llm);
        linkMicListAdapter.setLinearLayoutManager(llm);
        //????????????item??????
        landscapeItemDecoration.setPortrait();
        //??????item????????????
        linkMicListAdapter.setShowRoundRect(false);

        //????????????
        llSpeakingUsers.setVisibility(GONE);

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //??????????????????????????????????????????????????????????????????
        if (linkMicPresenter.isTeacherOpenLinkMic()) {
            linkMicControlBar.show();
        }
    }

    private void updatePushResolution(boolean isLandscape) {
        if (linkMicPresenter != null) {
            linkMicPresenter.setPushPictureResolutionType(isLandscape ?
                    PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE :
                    PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView??????">
    private int getRvScrolledXOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvLinkMicList.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisiableChildView.getWidth();
        return (position) * itemWidth - firstVisiableChildView.getLeft();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????RTC??????">
    //??????RTC?????? - ????????????????????????
    private void initShouldShowLandscapeRTCLayout() {
        switch (liveChannelType) {
            case PPT:
                //????????????????????????PPT???????????????????????????RTC??????
                changeShowOrHideLandscapeLayoutState(true);
                break;
            case ALONE:
                //???????????????????????????RTC?????????????????????????????????RTC??????
                changeShowOrHideLandscapeLayoutState(false);
                break;
        }
    }

    //??????RTC?????? - ???????????????
    private void tryShowOrHideLandscapeRTCLayout(boolean show) {
        if (show) {
            //???????????????????????????????????????????????????????????????RTC??????
            if (liveChannelType == PLVLiveChannelType.ALONE && linkMicPresenter.getRTCListSize() > 1) {
                changeShowOrHideLandscapeLayoutState(true);
            }
        } else {
            //???????????????????????????????????????????????????????????????RTC??????
            if (liveChannelType == PLVLiveChannelType.ALONE && linkMicPresenter.getRTCListSize() <= 1) {
                changeShowOrHideLandscapeLayoutState(false);
            }
        }
    }

    //??????RTC?????? - ??????layout params
    private void showOrHideLandscapeLayout(boolean show) {
        //rvRoot
        LayoutParams lpOfRvRoot = (LayoutParams) flMediaLinkMicRoot.getLayoutParams();
        lpOfRvRoot.width = show ? landscapeWidth : 0;
        lpOfRvRoot.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.gravity = Gravity.RIGHT;
        flMediaLinkMicRoot.setLayoutParams(lpOfRvRoot);
    }

    //??????RTC?????? - ?????????????????????
    private void changeShowOrHideLandscapeLayoutState(boolean show) {
        onPLVLinkMicLayoutListener.onShowLandscapeRTCLayout(show);
        //????????????????????????????????????????????????????????????????????????
        if (curIsLandscape) {
            showOrHideLandscapeLayout(show);
        }
        isShowLandscapeLayout = show;
    }


// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????-TryScrollViewStateType">
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            TRY_SCROLL_VIEW_STATE_VISIBLE,
            TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK,
            TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED
    })
    public @interface TryScrollViewStateType {/**/
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ?????????????????????">

    private void observeOnAudioState(final IPLVLiveRoomDataManager liveRoomDataManager) {
        //?????? ?????????????????????????????????
        liveRoomDataManager.getIsOnlyAudioEnabled().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean onlyAudio) {
                if(onlyAudio == null){
                    onlyAudio = false;
                }
                ArrayList<String> permissions = new ArrayList<>();
                permissions.add(Manifest.permission.RECORD_AUDIO);
                if(!onlyAudio){
                    permissions.add(Manifest.permission.CAMERA);
                }
                linkMicPresenter.resetRequestPermissionList(permissions);
                if(linkMicListAdapter != null){
                    linkMicListAdapter.setOnlyAudio(onlyAudio);
                    linkMicListAdapter.updateTeacherCoverImage();
                }
            }
        });

        //?????????
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (polyvLiveClassDetailVOPLVStatefulData == null || !polyvLiveClassDetailVOPLVStatefulData.isSuccess()) {
                    return;
                }
                PLVLiveClassDetailVO liveClassDetail = polyvLiveClassDetailVOPLVStatefulData.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }

                String coverImage = liveClassDetail.getData().getSplashImg();
                if(linkMicListAdapter != null){
                    linkMicListAdapter.setCoverImage(coverImage);
                    linkMicListAdapter.updateTeacherCoverImage();
                }
            }
        });
    }
    // </editor-fold >
}
