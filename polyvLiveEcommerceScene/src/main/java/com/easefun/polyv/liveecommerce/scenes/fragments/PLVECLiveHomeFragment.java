package com.easefun.polyv.liveecommerce.scenes.fragments;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.IPLVPointRewardEventProducer;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectQueue;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectWidget;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVRewardSVGAHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatImgScanPopupView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatInputWindow;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECGreetingView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECLikeIconView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityAdapter;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityDetailActivity;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPopupView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPushLayout;
import com.easefun.polyv.liveecommerce.modules.player.widget.PLVECNetworkTipsView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ??????????????????????????????????????????????????????????????????????????????
 */
public class PLVECLiveHomeFragment extends PLVECCommonHomeFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="??????">
    //??????????????????
    private PLVECWatchInfoView watchInfoLy;
    //????????????
    private PLVECBulletinView bulletinLy;
    //?????????
    private PLVECGreetingView greetLy;
    //????????????
    private PLVMessageRecyclerView chatMsgRv;
    private PLVECChatMessageAdapter chatMessageAdapter;
    private TextView sendMsgTv;
    private PLVECChatImgScanPopupView chatImgScanPopupView;
    //??????????????????view
    private TextView unreadMsgTv;
    //??????????????????????????????
    private SwipeRefreshLayout swipeLoadView;
    //????????????
    private PLVECLikeIconView likeBt;
    private TextView likeCountTv;
    //??????
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;
    private int currentLinesPos;
    private int currentDefinitionPos;
    private Rect videoViewRect;
    //??????
    private ImageView commodityIv;
    private PLVECCommodityPopupView commodityPopupView;
    private boolean isOpenCommodityMenu;
    private PLVECCommodityPushLayout commodityPushLayout;
    private String lastJumpBuyCommodityLink;
    //??????
    private ImageView rewardIv;

    //???????????????????????????
    private boolean isOpenPointReward = false;
    //????????????????????????
    private IPLVPointRewardEventProducer pointRewardEventProducer;
    //??????????????????item
    private PLVPointRewardEffectWidget polyvPointRewardEffectWidget;
    //????????????svg??????
    private SVGAImageView rewardSvgImage;
    private SVGAParser svgaParser;
    private PLVRewardSVGAHelper svgaHelper;

    // ??????????????????
    private PLVECNetworkTipsView networkTipsView;
    //?????????
    private OnViewActionListener onViewActionListener;

    //?????????????????????
    private boolean isCloseRoomStatus;
    //??????????????????
    private boolean isFocusModeStatus;
    //??????????????????
    private PLVECChatInputWindow chatInputWindow;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_live_page_home_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
        calculateLiveVideoViewRect();
        startLikeAnimationTask(5000);

        //??????????????????????????????
        observeRewardData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isOpenPointReward) {
            destroyPointRewardEffectQueue();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        bulletinLy = findViewById(R.id.bulletin_ly);
        greetLy = findViewById(R.id.greet_ly);
        chatMsgRv = findViewById(R.id.chat_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setOnViewActionListener(onChatMsgViewActionListener);
        //????????????view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);
        chatMsgRv.addUnreadView(unreadMsgTv);
        //????????????
        swipeLoadView = findViewById(R.id.swipe_load_view);
        swipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatroomPresenter.requestChatHistory(0);
            }
        });
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        likeBt = findViewById(R.id.like_bt);
        likeBt.setOnButtonClickListener(this);
        likeCountTv = findViewById(R.id.like_count_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);
        commodityIv = findViewById(R.id.commodity_iv);
        commodityIv.setOnClickListener(this);
        commodityPushLayout = findViewById(R.id.commodity_push_ly);
        rewardIv = findViewById(R.id.reward_iv);
        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
        rewardIv.setOnClickListener(this);
        morePopupView = new PLVECMorePopupView();
        commodityPopupView = new PLVECCommodityPopupView();
        chatImgScanPopupView = new PLVECChatImgScanPopupView();

        //??????????????????
        polyvPointRewardEffectWidget = findViewById(R.id.plvec_point_reward_effect);
        rewardSvgImage = findViewById(R.id.plvec_reward_svg);
        svgaParser = new SVGAParser(getContext());
        svgaHelper = new PLVRewardSVGAHelper();
        svgaHelper.init(rewardSvgImage, svgaParser);

        networkTipsView = findViewById(R.id.plvec_live_network_tips_layout);

        //????????????
        cardPushManager.registerView((ImageView) findViewById(R.id.card_enter_view), (TextView) findViewById(R.id.card_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.card_enter_tips_view));

        initNetworkTipsLayout();
    }

    private void initNetworkTipsLayout() {
        networkTipsView.setOnViewActionListener(new PLVECNetworkTipsView.OnViewActionListener() {
            @Override
            public void onClickChangeNormalLatency() {
                if (onViewActionListener != null) {
                    onViewActionListener.switchLowLatencyMode(false);
                }
            }

            @Override
            public boolean isCurrentLowLatency() {
                if (onViewActionListener != null) {
                    return onViewActionListener.isCurrentLowLatencyMode();
                }
                return false;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    @Override
    protected void registerChatroomView() {
        chatroomPresenter.registerView(chatroomView);
    }

    @Override
    protected void updateWatchInfo(String coverImage, String publisher) {
        watchInfoLy.updateWatchInfo(coverImage, publisher);
        watchInfoLy.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateWatchCount(long watchCount) {
        watchInfoLy.updateWatchCount(watchCount);
    }

    @Override
    protected void updateLikesInfo(String likesString) {
        likeCountTv.setText(likesString);
    }

    @Override
    protected void acceptOpenCommodity() {
        isOpenCommodityMenu = true;
        commodityIv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void acceptCommodityVO(PLVCommodityVO2 commodityVO, boolean isAddOrSet) {
        if (isAddOrSet) {
            commodityPopupView.addCommodityVO(commodityVO);
        } else {
            commodityPopupView.setCommodityVO(commodityVO);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    //??????????????????
    @Override
    public void setPlayerState(PLVPlayerState state) {
        if (state == PLVPlayerState.PREPARED) {
            morePopupView.updatePlayStateView(View.VISIBLE);

            if (onViewActionListener != null) {
                currentLinesPos = onViewActionListener.onGetLinesPosAction();
                currentDefinitionPos = onViewActionListener.onGetDefinitionAction();
                morePopupView.updatePlayMode(onViewActionListener.onGetMediaPlayModeAction());
            }
            morePopupView.updateLinesView(new int[]{onViewActionListener == null ? 1 : onViewActionListener.onGetLinesCountAction(), currentLinesPos});
            morePopupView.updateDefinitionView(onViewActionListener == null ?
                    new Pair<List<PolyvDefinitionVO>, Integer>(null, 0) :
                    onViewActionListener.onShowDefinitionClick(view));
        } else if (state == PLVPlayerState.NO_LIVE || state == PLVPlayerState.LIVE_END) {
            morePopupView.hideAll();
            morePopupView.updatePlayStateView(View.GONE);
        }
    }

    @Override
    public void jumpBuyCommodity() {
        if (TextUtils.isEmpty(lastJumpBuyCommodityLink)) {
            return;
        }
        commodityPushLayout.hide();
        commodityPopupView.hide();
        //??????????????????????????????webView?????????????????????????????????????????????????????????????????????????????????????????????????????????
        PLVECCommodityDetailActivity.start(getContext(), lastJumpBuyCommodityLink);
    }

    @Override
    public void setOnViewActionListener(PLVECCommonHomeFragment.OnViewActionListener listener) {
        this.onViewActionListener = (OnViewActionListener) listener;
    }

    @Override
    public void acceptOnLowLatencyChange(boolean isLowLatency) {
        morePopupView.updateLatencyMode(isLowLatency);
    }

    @Override
    public void acceptNetworkQuality(int quality) {
        networkTipsView.acceptNetworkQuality(quality);
    }

    @Override
    public void showMorePopupWindow() {
        if (moreIv != null) {
            moreIv.performClick();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????????????????">
    /**
     * ?????????????????????????????????item
     */
    private void initPointRewardEffectQueue(){
        if(pointRewardEventProducer == null) {
            pointRewardEventProducer = new PLVPointRewardEffectQueue();
            polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);
        }
    }

    /**
     * ??????????????????????????????
     */
    private void destroyPointRewardEffectQueue(){
        if (pointRewardEventProducer != null) {
            pointRewardEventProducer.destroy();
        }
        svgaHelper.clear();
    }

    private void acceptPointRewardMessage(PLVRewardEvent rewardEvent){
        if (pointRewardEventProducer != null) {
            //?????????????????????????????????
            if (ScreenUtils.isPortrait()) {
                //?????????????????????????????????????????????
//                pointRewardEventProducer.addEvent(rewardEvent);
                //?????????svga
//                svgaHelper.addEvent(rewardEvent);
            }
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void acceptBulletinMessage(final PolyvBulletinVO bulletinVO) {
        bulletinLy.acceptBulletinMessage(bulletinVO);
    }

    private void removeBulletin() {
        bulletinLy.removeBulletin();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ???????????????">
    private void acceptLoginMessage(PLVLoginEvent loginEvent) {
        //???????????????
        greetLy.acceptGreetingMessage(loginEvent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean result = chatMessageAdapter.addDataListChanged(chatMessageDataList);
                if (result) {
                    if (isScrollEnd) {
                        chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        chatMsgRv.scrollToBottomOrShowMore(chatMessageDataList.size());
                    }
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean result = chatMessageAdapter.addDataListChangedAtFirst(chatMessageDataList);
                if (result) {
                    if (isScrollEnd) {
                        chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        chatMsgRv.scrollToPosition(0);
                    }
                }
            }
        });
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRemoveAll) {
                    chatMessageAdapter.removeAllDataChanged();
                } else {
                    chatMessageAdapter.removeDataChanged(id);
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void acceptFocusModeEvent(final PLVFocusModeEvent focusModeEvent) {
        isFocusModeStatus = focusModeEvent.isOpen();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                updateViewByRoomStatusChanged(isFocusModeStatus);
                if (isFocusModeStatus) {
                    chatMessageAdapter.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FOCUS_MODE);
                } else {
                    chatMessageAdapter.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FULL);
                }
                ToastUtils.showLong(isFocusModeStatus ? R.string.plv_chat_toast_focus_mode_open : R.string.plv_chat_toast_focus_mode_close);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">
    private void acceptCloseRoomEvent(PLVCloseRoomEvent closeRoomEvent) {
        isCloseRoomStatus = closeRoomEvent.getValue().isClosed();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                updateViewByRoomStatusChanged(isCloseRoomStatus);
                ToastUtils.showLong(isCloseRoomStatus ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
            }
        });
    }

    private void updateViewByRoomStatusChanged(boolean isDisabled) {
        boolean isEnabled = !isCloseRoomStatus && !isFocusModeStatus;
        if (isDisabled) {
            if (chatInputWindow != null) {
                chatInputWindow.requestClose();
            }
        }
        sendMsgTv.setText(isCloseRoomStatus ? "??????????????????" : (isFocusModeStatus ? "?????????????????????.." : "????????????????????????~"));
        sendMsgTv.setOnClickListener(isEnabled ? this : null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - MVP?????????view?????????">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent) {
            super.onSpeakEvent(speakEvent);
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);//??????????????????????????????textSize
        }

        @Override
        public void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent) {
            super.onImgEvent(chatImgEvent);
        }

        @Override
        public void onLikesEvent(@NonNull PLVLikesEvent likesEvent) {
            super.onLikesEvent(likesEvent);
            acceptLikesMessage(likesEvent.getCount());
        }

        @Override
        public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {
            super.onLoginEvent(loginEvent);
            acceptLoginMessage(loginEvent);
        }

        @Override
        public void onLoginError(@Nullable PLVLoginEvent loginEvent, final String msg, final int errorCode) {
            super.onLoginError(loginEvent, msg, errorCode);
            postToMainThread(new Runnable() {
                @Override
                public void run() {
                    PLVToast.Builder.create()
                            .setText(format("{}({})", msg, errorCode))
                            .show();
                    final Activity activity = firstNotNull(getActivity(), ActivityUtils.getTopActivity());
                    if (activity != null) {
                        activity.finish();
                    }
                }
            });
        }

        @Override
        public void onLogoutEvent(@NonNull PLVLogoutEvent logoutEvent) {
            super.onLogoutEvent(logoutEvent);
        }

        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
            acceptBulletinMessage(bulletinVO);
        }

        @Override
        public void onRewardEvent(@NonNull PLVRewardEvent rewardEvent) {
            super.onRewardEvent(rewardEvent);
            acceptPointRewardMessage(rewardEvent);
        }

        @Override
        public void onRemoveBulletinEvent() {
            super.onRemoveBulletinEvent();
            removeBulletin();
        }

        @Override
        public void onProductControlEvent(@NonNull PLVProductControlEvent productControlEvent) {
            super.onProductControlEvent(productControlEvent);
            acceptProductControlEvent(productControlEvent);
        }

        @Override
        public void onProductRemoveEvent(@NonNull PLVProductRemoveEvent productRemoveEvent) {
            super.onProductRemoveEvent(productRemoveEvent);
            acceptProductRemoveEvent(productRemoveEvent);
        }

        @Override
        public void onProductMoveEvent(@NonNull PLVProductMoveEvent productMoveEvent) {
            super.onProductMoveEvent(productMoveEvent);
            acceptProductMoveEvent(productMoveEvent);
        }

        @Override
        public void onProductMenuSwitchEvent(@NonNull PLVProductMenuSwitchEvent productMenuSwitchEvent) {
            super.onProductMenuSwitchEvent(productMenuSwitchEvent);

            /** ///???????????????????????????????????????
             *   if (productMenuSwitchEvent.getContent() != null) {
             *  boolean isEnabled = productMenuSwitchEvent.getContent().isEnabled();
             *   }
             */
        }

        @Override
        public void onCloseRoomEvent(@NonNull final PLVCloseRoomEvent closeRoomEvent) {
            super.onCloseRoomEvent(closeRoomEvent);
            acceptCloseRoomEvent(closeRoomEvent);
        }

        @Override
        public void onFocusModeEvent(@NonNull PLVFocusModeEvent focusModeEvent) {
            super.onFocusModeEvent(focusModeEvent);
            acceptFocusModeEvent(focusModeEvent);
        }

        @Override
        public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            removeChatMessageToList(id, isRemoveAll);
        }

        @Override
        public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {
            //?????????????????????????????????????????????onRewardEvent????????????
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            if (localMessage != null) {
                //?????????????????????
                List<PLVBaseViewData> dataList = new ArrayList<>();
                dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
                addChatMessageToList(dataList, true);
            }
        }

        @Override
        public void onSpeakImgDataList(List<PLVBaseViewData> chatMessageDataList) {
            super.onSpeakImgDataList(chatMessageDataList);
            //?????????????????????
            addChatMessageToList(chatMessageDataList, false);
        }

        @Override
        public void onHistoryDataList(List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory, int viewIndex) {
            super.onHistoryDataList(chatMessageDataList, requestSuccessTime, isNoMoreHistory, viewIndex);
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
                swipeLoadView.setEnabled(true);
            }
            if (!chatMessageDataList.isEmpty()) {
                addChatHistoryToList(chatMessageDataList, requestSuccessTime == 1);
            }
            if (isNoMoreHistory) {
                ToastUtils.showShort(R.string.plv_chat_toast_history_all_loaded);
                if (swipeLoadView != null) {
                    swipeLoadView.setEnabled(false);
                }
            }
        }

        @Override
        public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
                swipeLoadView.setEnabled(true);
            }
            ToastUtils.showShort(getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
        }

        @Override
        public void onLoadEmotionMessage(@Nullable PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            if (emotionEvent != null) {
                //?????????????????????
                List<PLVBaseViewData> dataList = new ArrayList<>();
                dataList.add(new PLVBaseViewData<>(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
                addChatMessageToList(dataList, emotionEvent.isLocal());
            }
        }

        @Override
        public void onNewsPushStartMessage(@NonNull PLVNewsPushStartEvent newsPushStartEvent) {
            super.onNewsPushStartMessage(newsPushStartEvent);
            cardPushManager.acceptNewsPushStartMessage(chatroomPresenter, newsPushStartEvent);
        }

        @Override
        public void onNewsPushCancelMessage() {
            super.onNewsPushCancelMessage();
            cardPushManager.acceptNewsPushCancelMessage();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????????????????????????????">
    PLVECChatMessageAdapter.OnViewActionListener onChatMsgViewActionListener = new PLVECChatMessageAdapter.OnViewActionListener() {
        @Override
        public void onChatImgClick(View view, String imgUrl) {
            chatImgScanPopupView.showImgScanLayout(view, imgUrl);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????????????????">
    private void showInputWindow() {
        PLVECChatInputWindow.show(getActivity(), PLVECChatInputWindow.class, new PLVECChatInputWindow.MessageSendListener() {
            @Override
            public void onInputContext(PLVECChatInputWindow inputWindow) {
                chatInputWindow = inputWindow;
            }

            @Override
            public boolean onSendMsg(String message) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                Pair<Boolean, Integer> sendResult = chatroomPresenter.sendChatMessage(localMessage);
                if (!sendResult.first) {
                    //????????????
                    ToastUtils.showShort(getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second);
                    return false;
                }
                return true;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????????????????????????????">
    private void acceptLikesMessage(final int likesCount) {
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                startAddLoveIconTask(200, Math.min(5, likesCount));
            }
        });
    }

    private void startLikeAnimationTask(long ts) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int randomLikeCount = new Random().nextInt(5) + 1;
                startAddLoveIconTask(200, randomLikeCount);
                startLikeAnimationTask((new Random().nextInt(6) + 5) * 1000L);
            }
        }, ts);
    }

    private void startAddLoveIconTask(final long ts, final int count) {
        if (count >= 1) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    likeBt.addLoveIcon(1);
                    startAddLoveIconTask(ts, count - 1);
                }
            }, ts);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????? - ??????????????????????????????????????????????????????????????????">
    private void showCommodityLayout(View v) {
        //???????????????
        commodityPopupView.setCommodityVO(null);
        //???????????????????????????????????????????????????
        liveRoomDataManager.requestProductList();
        commodityPopupView.showCommodityLayout(v, new PLVECCommodityAdapter.OnViewActionListener() {
            @Override
            public void onBuyCommodityClick(View view, PLVProductContentBean contentsBean) {
                acceptBuyCommodityClick(contentsBean);
            }

            @Override
            public void onLoadMoreData(int rank) {
                liveRoomDataManager.requestProductList(rank);
            }
        });
    }

    private void acceptProductControlEvent(final PLVProductControlEvent productControlEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                final PLVProductContentBean contentBean = productControlEvent.getContent();
                if (productControlEvent.getContent() == null) {
                    return;
                }
                if (productControlEvent.isPush()) {//????????????
                    commodityPushLayout.setViewActionListener(new PLVECCommodityPushLayout.ViewActionListener() {
                        @Override
                        public void onEnterClick() {
                            acceptBuyCommodityClick(contentBean);
                        }
                    });
                    commodityPushLayout.updateView(contentBean);
                    commodityPushLayout.show();
                } else if (productControlEvent.isNewly()) {//??????
                    commodityPopupView.add(contentBean, true);
                } else if (productControlEvent.isRedact()) {//??????
                    commodityPopupView.update(contentBean);
                } else if (productControlEvent.isPutOnShelves()) {//??????
                    commodityPopupView.add(contentBean, false);
                }
            }
        });
    }

    private void acceptProductRemoveEvent(final PLVProductRemoveEvent productRemoveEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (productRemoveEvent.getContent() != null) {
                    commodityPopupView.delete(productRemoveEvent.getContent().getProductId());//??????/??????
                    if (commodityPushLayout.isShown() && commodityPushLayout.getProductId() == productRemoveEvent.getContent().getProductId()) {
                        commodityPushLayout.hide();
                    }
                }
            }
        });
    }

    private void acceptProductMoveEvent(final PLVProductMoveEvent productMoveEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                commodityPopupView.move(productMoveEvent);//??????
            }
        });
    }

    private void acceptBuyCommodityClick(PLVProductContentBean contentBean) {
        final String link = contentBean.getLinkByType();
        if (TextUtils.isEmpty(link)) {
            ToastUtils.showShort(R.string.plv_commodity_toast_empty_link);
            return;
        }
        lastJumpBuyCommodityLink = link;
        jumpBuyCommodity();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ???????????????????????????">
    private void showMorePopupWindow(View v) {
        boolean isCurrentVideoMode = onViewActionListener == null || onViewActionListener.onGetMediaPlayModeAction() == PolyvMediaPlayMode.MODE_VIDEO;
        morePopupView.showLiveMoreLayout(v, isCurrentVideoMode, new PLVECMorePopupView.OnLiveMoreClickListener() {
            @Override
            public boolean onPlayModeClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeMediaPlayModeClick(view, view.isSelected() ? PolyvMediaPlayMode.MODE_VIDEO : PolyvMediaPlayMode.MODE_AUDIO);
                    return true;
                }
                return false;
            }

            @Override
            public int[] onShowLinesClick(View view) {
                return new int[]{onViewActionListener == null ? 1 : onViewActionListener.onGetLinesCountAction(), currentLinesPos};
            }

            @Override
            public void onLinesChangeClick(View view, int linesPos) {
                if (currentLinesPos != linesPos) {
                    currentLinesPos = linesPos;
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeLinesClick(view, linesPos);
                    }
                }
            }

            @Override
            public Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view) {
                return onViewActionListener == null ? new Pair<List<PolyvDefinitionVO>, Integer>(null, 0)
                        : onViewActionListener.onShowDefinitionClick(view);
            }

            @Override
            public void onDefinitionChangeClick(View view, int definitionPos) {
                if (currentDefinitionPos != definitionPos) {
                    currentDefinitionPos = definitionPos;
                    if (onViewActionListener != null) {
                        onViewActionListener.onDefinitionChangeClick(view, definitionPos);
                    }
                }
            }

            @Override
            public boolean isCurrentLowLatencyMode() {
                if (onViewActionListener != null) {
                    return onViewActionListener.isCurrentLowLatencyMode();
                }
                return false;
            }

            @Override
            public void switchLowLatencyMode(boolean isLowLatency) {
                if (onViewActionListener != null) {
                    onViewActionListener.switchLowLatencyMode(isLowLatency);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????????????????????????????????????????????????????????????????">
    private void calculateLiveVideoViewRect() {
        watchInfoLy.post(new Runnable() {
            @Override
            public void run() {
                acceptVideoViewRectParams(watchInfoLy.getBottom(), 0);
            }
        });
        greetLy.post(new Runnable() {
            @Override
            public void run() {
                acceptVideoViewRectParams(0, greetLy.getTop());
            }
        });
    }

    private void acceptVideoViewRectParams(int top, int bottom) {
        if (videoViewRect == null) {
            videoViewRect = new Rect(0, top, 0, bottom);
        } else {
            videoViewRect = new Rect(0, Math.max(videoViewRect.top, top), 0, Math.max(videoViewRect.bottom, bottom));
            if (onViewActionListener != null) {
                onViewActionListener.onSetVideoViewRectAction(videoViewRect);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ????????????????????????">
    private void observeRewardData(){
        liveRoomDataManager.getPointRewardEnableData().observe(this, new Observer<PLVStatefulData<Boolean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<Boolean> booleanPLVStatefulData) {
                liveRoomDataManager.getPointRewardEnableData().removeObserver(this);
                if( booleanPLVStatefulData != null && booleanPLVStatefulData.getData() != null){
                    isOpenPointReward = booleanPLVStatefulData.getData();
                    if(isOpenPointReward) {
                        initPointRewardEffectQueue();
                    }
                    if (rewardIv != null) {
                        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
                    }
                }
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send_msg_tv) {
            showInputWindow();
        } else if (id == R.id.more_iv) {
            showMorePopupWindow(v);
        } else if (id == R.id.like_bt) {
            chatroomPresenter.sendLikeMessage();
            acceptLikesMessage(1);
        } else if (id == R.id.commodity_iv) {
            showCommodityLayout(v);
        } else if (id == R.id.reward_iv) {
            if(isOpenPointReward){
                //??????????????????????????????
                if(onViewActionListener != null){
                    onViewActionListener.onShowRewardAction();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - view?????????????????????">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //??????????????????
        void onChangeMediaPlayModeClick(View view, int mediaPlayMode);

        //????????????
        void onChangeLinesClick(View view, int linesPos);

        //[?????????????????????????????????]
        Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view);

        //???????????????
        void onDefinitionChangeClick(View view, int definitionPos);

        //??????????????????
        int onGetMediaPlayModeAction();

        //???????????????
        int onGetLinesCountAction();

        //??????????????????
        int onGetLinesPosAction();

        //?????????????????????
        int onGetDefinitionAction();

        //????????????????????????
        void onSetVideoViewRectAction(Rect videoViewRect);

        //????????????????????????
        void onShowRewardAction();

        /**
         * ???????????????????????????
         *
         * @return ?????????????????????
         */
        boolean isCurrentLowLatencyMode();

        /**
         * ?????????????????????
         *
         * @param isLowLatency ?????????????????????
         */
        void switchLowLatencyMode(boolean isLowLatency);
    }
    // </editor-fold>
}
