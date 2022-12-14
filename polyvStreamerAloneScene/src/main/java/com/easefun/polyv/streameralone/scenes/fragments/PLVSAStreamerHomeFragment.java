package com.easefun.polyv.streameralone.scenes.fragments;

import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.streamer.linkmic.IPLVSLinkMicEventSender;
import com.easefun.polyv.livescenes.streamer.linkmic.PLVSLinkMicEventSender;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.IPLVSAChatroomLayout;
import com.easefun.polyv.streameralone.modules.chatroom.PLVSAChatroomLayout;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSAGreetingView;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSARewardGiftAnimView;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSAMemberLayout;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSAMoreLayout;
import com.easefun.polyv.streameralone.modules.statusbar.PLVSAStatusBarLayout;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ???????????????????????????fragment
 */
public class PLVSAStreamerHomeFragment extends PLVBaseFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="??????">

    // ????????????
    private static final int LAYOUT_VERTICAL_PADDING = ConvertUtils.dp2px(8);
    private static final int LAYOUT_HORIZON_PADDING_PORT = ConvertUtils.dp2px(8);
    private static final int LAYOUT_HORIZON_PADDING_LAND = ConvertUtils.dp2px(16);

    /**
     * ????????????????????????????????????????????????????????????
     */
    private static final int CHAT_MESSAGE_SIZE_TO_SHOW_CLEAN_UP_HINT = 10;

    //????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //????????????
    private PLVSAMoreLayout moreLayout;
    //??????????????????
    private PLVSAMemberLayout memberLayout;
    //???????????????
    private PLVSAStatusBarLayout plvsaStatusBarLayout;
    //????????????
    private PLVSARewardGiftAnimView plvsaChatroomRewardLy;
    //???????????????
    private PLVSAGreetingView plvsaChatroomGreetingLy;
    //???????????????
    private IPLVSAChatroomLayout plvsaChatroomLayout;
    //view
    private ConstraintLayout homeFragmentLayout;
    private TextView plvsaToolBarCallInputTv;
    private ImageView plvsaToolBarMoreIv;
    private ImageView plvsaToolBarMemberIv;
    private ImageView plvsaToolBarLinkmicIv;
    private ImageView plvsaToolBarLinkmicTypeIv;
    private View plvsaToolBarMemberLinkmicRequestTipsView;
    private TextView plvsaToolBarLinkmicTypeTip;

    // ??????????????????????????????
    private RecyclerView.AdapterDataObserver chatMessageDataObserver;

    //listener
    private OnViewActionListener onViewActionListener;

    private boolean isBeautyLayoutShowing = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvsa_streamer_home_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????view">
    private void initView() {
        homeFragmentLayout = findViewById(R.id.plvsa_home_fragment_layout);
        plvsaStatusBarLayout = (PLVSAStatusBarLayout) findViewById(R.id.plvsa_status_bar_layout);
        plvsaChatroomRewardLy = findViewById(R.id.plvsa_chatroom_reward_ly);
        plvsaChatroomGreetingLy = findViewById(R.id.plvsa_chatroom_greet_ly);
        plvsaChatroomLayout = (PLVSAChatroomLayout) findViewById(R.id.plvsa_chatroom_layout);
        plvsaToolBarCallInputTv = (TextView) findViewById(R.id.plvsa_tool_bar_call_input_tv);
        plvsaToolBarMoreIv = (ImageView) findViewById(R.id.plvsa_tool_bar_more_iv);
        plvsaToolBarMemberIv = (ImageView) findViewById(R.id.plvsa_tool_bar_member_iv);
        plvsaToolBarLinkmicIv = (ImageView) findViewById(R.id.plvsa_tool_bar_linkmic_iv);
        plvsaToolBarLinkmicTypeIv = (ImageView) findViewById(R.id.plvsa_tool_bar_linkmic_type_iv);
        plvsaToolBarMemberLinkmicRequestTipsView = findViewById(R.id.plvsa_tool_bar_member_linkmic_request_tips_view);
        plvsaToolBarLinkmicTypeTip = findViewById(R.id.plvsa_tool_bar_linkmic_type_tip);

        plvsaToolBarCallInputTv.setOnClickListener(this);
        plvsaToolBarMoreIv.setOnClickListener(this);
        plvsaToolBarMemberIv.setOnClickListener(this);
        plvsaToolBarLinkmicIv.setOnClickListener(this);
        plvsaToolBarLinkmicTypeIv.setOnClickListener(this);

        initMoreLayout();
        initMemberLayout();

        observeBeautyLayoutStatus();

        updateViewWithOrientation();
    }

    private void initMoreLayout() {
        moreLayout = new PLVSAMoreLayout(view.getContext());
    }

    private void initMemberLayout() {
        memberLayout = new PLVSAMemberLayout(view.getContext());
    }

    private void observeBeautyLayoutStatus() {
        if (getActivity() == null) {
            return;
        }
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe(getActivity(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSAStreamerHomeFragment.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    //after onViewCreated init
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        //????????????????????????
        plvsaStatusBarLayout.init(liveRoomDataManager);
        //????????????????????????
        plvsaChatroomLayout.init(liveRoomDataManager);
        //?????????????????????
        memberLayout.init(liveRoomDataManager);
        //?????????????????????
        moreLayout.init(liveRoomDataManager);

        observeChatroomLayout();
        observeStatusBarLayout();
        updateGuestLayout();
    }

    public void chatroomLogin(){
        plvsaChatroomLayout.loginAndLoadHistory();
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void updateChannelName() {
        if (plvsaStatusBarLayout != null && liveRoomDataManager != null) {
            plvsaStatusBarLayout.updateChannelName(liveRoomDataManager.getConfig().getChannelName());
        }
    }

    public void updateUserRequestStatus() {
        showUserRequestTips();
    }

    public void updateLinkMicLayoutTypeVisibility(boolean isShow) {
        if(isShow && !loadStatus()){
            plvsaToolBarLinkmicTypeTip.setVisibility(View.VISIBLE);
            saveStatus();
        } else {
            plvsaToolBarLinkmicTypeTip.setVisibility(View.INVISIBLE);
        }
        plvsaToolBarLinkmicTypeIv.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void openMemberLayoutAndHideUserRequestTips() {
        memberLayout.open();
        hideUserRequestTips();
    }

    public void closeMemberLayout(){
        memberLayout.closeAndHideWindow();
    }

    public IPLVStreamerContract.IStreamerView getMoreLayoutStreamerView() {
        return moreLayout == null ? null : moreLayout.getStreamerView();
    }

    public IPLVStreamerContract.IStreamerView getMemberLayoutStreamerView() {
        return memberLayout == null ? null : memberLayout.getStreamerView();
    }

    public IPLVStreamerContract.IStreamerView getStatusBarLayoutStreamerView() {
        return plvsaStatusBarLayout == null ? null : plvsaStatusBarLayout.getStreamerView();
    }

    public boolean onBackPressed() {
        return plvsaChatroomLayout.onBackPressed()
                || moreLayout.onBackPressed()
                || memberLayout.onBackPressed()
                || plvsaStatusBarLayout.onBackPressed();
    }

    public void destroy() {
        if (plvsaChatroomLayout != null) {
            plvsaChatroomLayout.destroy();
        }
        if (plvsaStatusBarLayout != null) {
            plvsaStatusBarLayout.destroy();
        }
        if (moreLayout != null) {
            moreLayout.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewWithOrientation();
    }

    private void updateViewWithOrientation() {
        if (getContext() == null) {
            return;
        }
        // padding - left right top bottom
        int pl, pr, pt, pb;
        if (PLVScreenUtils.isPortrait(getContext())) {
            pl = pr = LAYOUT_HORIZON_PADDING_PORT;
        } else {
            pl = pr = LAYOUT_HORIZON_PADDING_LAND;
        }
        pt = pb = LAYOUT_VERTICAL_PADDING;
        view.setPadding(pl, pt, pr, pb);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    /**
     * ?????????????????????
     */
    private void updateGuestLayout() {
        if (isGuest()) {
            moreLayout.updateCloseRoomLayout(true);
            updateLinkMicLayoutTypeVisibility(false);
            plvsaToolBarLinkmicIv.setVisibility(View.GONE);
        }
    }

    private void updateVisibility() {
        // ?????????????????????????????????????????????
        if (isBeautyLayoutShowing) {
            homeFragmentLayout.setVisibility(View.GONE);
            return;
        }
        homeFragmentLayout.setVisibility(View.VISIBLE);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????">

    private void observeChatroomLayout() {
        //????????????????????????????????????
        plvsaChatroomLayout.addOnOnlineCountListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvsaStatusBarLayout.setOnlineCount(integer);
                memberLayout.updateOnlineCount(integer);
            }
        });
        //????????????????????????????????????
        plvsaChatroomLayout.addOnLoginEventListener(new IPLVOnDataChangedListener<PLVLoginEvent>() {
            @Override
            public void onChanged(@Nullable PLVLoginEvent loginEvent) {
                if (loginEvent == null) {
                    return;
                }
                plvsaChatroomGreetingLy.acceptGreetingMessage(loginEvent);
            }
        });
        //???????????????????????????
        plvsaChatroomLayout.addOnRewardEventListener(new IPLVOnDataChangedListener<PLVRewardEvent>() {
            @Override
            public void onChanged(@Nullable PLVRewardEvent rewardEvent) {
                if (rewardEvent == null) {
                    return;
                }
                plvsaChatroomRewardLy.acceptRewardGiftMessage(rewardEvent);
                addRewardEventToChatList(rewardEvent);
            }
        });
        plvsaChatroomLayout.addObserverToChatMessageAdapter(chatMessageDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }
        });
    }

    private void checkIfNeedShowCleanUpLayout() {
        if (plvsaChatroomLayout != null) {
            int messageListSize = plvsaChatroomLayout.getChatMessageListSize();
            if (messageListSize >= CHAT_MESSAGE_SIZE_TO_SHOW_CLEAN_UP_HINT
                    && onViewActionListener != null) {
                boolean success = onViewActionListener.showCleanUpLayout();
                if (success) {
                    plvsaChatroomLayout.removeObserverFromChatMessageAdapter(chatMessageDataObserver);
                }
            }
        }
    }

    private void addRewardEventToChatList(PLVRewardEvent rewardEvent) {
        PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(rewardEvent);
        List<PLVBaseViewData> dataList = new ArrayList<>();
        dataList.add(new PLVBaseViewData<>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT));
        plvsaChatroomLayout.addChatMessageToChatList(dataList, false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????">

    private void observeStatusBarLayout() {
        //?????????????????????
        plvsaStatusBarLayout.setOnStopLiveListener(new PLVSAStatusBarLayout.OnStopLiveListener() {
            @Override
            public void onStopLive() {
                if (onViewActionListener != null) {
                    onViewActionListener.onStopLive();
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????????????????">
    private void showUserRequestTips() {
        if (memberLayout != null && memberLayout.isOpen()) {
            return;
        }
        plvsaToolBarMemberLinkmicRequestTipsView.setVisibility(View.VISIBLE);
    }

    private void hideUserRequestTips() {
        plvsaToolBarMemberLinkmicRequestTipsView.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvsa_tool_bar_call_input_tv) {
            plvsaChatroomLayout.callInputWindow();
        } else if (id == R.id.plvsa_tool_bar_more_iv) {
            moreLayout.open();
        } else if (id == R.id.plvsa_tool_bar_member_iv) {
            openMemberLayoutAndHideUserRequestTips();
            if (onViewActionListener != null) {
                onViewActionListener.onClickToOpenMemberLayout();
            }
        } else if (id == R.id.plvsa_tool_bar_linkmic_iv) {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    boolean isOpenLinkMic = PLVSLinkMicEventSender.getInstance().openLinkMic(true, !v.isSelected(), new IPLVSLinkMicEventSender.PLVSMainCallAck() {
                        @Override
                        public void onCall(Object... args) {
                            v.setSelected(!v.isSelected());
                            PLVToast.Builder.context(v.getContext())
                                    .setText(v.isSelected() ? R.string.plv_linkmic_video_type_open_success_tip : R.string.plv_linkmic_hang_all_off_tip)
                                    .build()
                                    .show();
                        }
                    });
                    if (!isOpenLinkMic) {
                        PLVToast.Builder.context(v.getContext())
                                .setText(R.string.plv_linkmic_error_tip_have_not_opened)
                                .build()
                                .show();
                    }
                }
            };
            if (v.isSelected()) {
                new PLVSAConfirmDialog(v.getContext())
                        .setTitle("??????????????????????????????")
                        .setContent("??????????????????????????????????????????")
                        .setLeftButtonText(R.string.plv_common_dialog_cancel)
                        .setRightButtonText("??????")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                runnable.run();
                            }
                        })
                        .show();
                return;
            }
            runnable.run();
        } else if (id == R.id.plvsa_tool_bar_linkmic_type_iv) {
            plvsaToolBarLinkmicTypeTip.setVisibility(View.INVISIBLE);
            if(PLVDebounceClicker.tryClick(this, 800)) {
                v.setSelected(!v.isSelected());
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeLinkMicLayoutType();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private boolean isGuest() {
        if (liveRoomDataManager != null) {
            if (PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
                return true;
            }
        }
        return false;
    }

    private void saveStatus() {
        SPUtils.getInstance().put("plv_key_linkmic_type_tips_is_showed", true);
    }

    private boolean loadStatus() {
        return SPUtils.getInstance().getBoolean("plv_key_linkmic_type_tips_is_showed", false);
    }
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="????????? - view?????????????????????">
    public interface OnViewActionListener {
        /**
         * onActivityCreated call
         */
        void onViewCreated();

        void onStopLive();

        void onClickToOpenMemberLayout();

        boolean showCleanUpLayout();

        /**
         * ????????????????????????
         */
        void onChangeLinkMicLayoutType();
    }
    // </editor-fold>

}
