package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.livecommon.module.modules.previous.presenter.PLVPreviousPlaybackPresenter;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatImgScanPopupView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECGreetingView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityAdapter;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityDetailActivity;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPopupView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPushLayout;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.IPLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.PLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.previous.PLVECPreviousAdapter;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackGetDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.playback.chat.PLVChatPlaybackManager;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ??????????????????????????????????????????????????????????????????
 */
public class PLVECPalybackHomeFragment extends PLVECCommonHomeFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="??????">
    //??????????????????
    private PLVECWatchInfoView watchInfoLy;
    //????????????
    private PLVECBulletinView bulletinLy;
    //????????????
    private ImageView playControlIv;
    private TextView playTimeTv;
    private SeekBar playProgressSb;
    private TextView totalTimeTv;
    private boolean isPlaySbDragging;
    //??????
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;

    //?????????
    private PLVECGreetingView greetLy;
    //????????????
    private PLVMessageRecyclerView chatMsgRv;
    private PLVECChatMessageAdapter chatMessageAdapter;
    //??????????????????view
    private TextView unreadMsgTv;
    //??????????????????????????????
    private SwipeRefreshLayout swipeLoadView;
    private PLVECChatImgScanPopupView chatImgScanPopupView;
    //????????????tips
    private TextView chatPlaybackTipsTv;

    //??????
    private ImageView commodityIv;
    private PLVECCommodityPopupView commodityPopupView;
    private boolean isOpenCommodityMenu;
    private PLVECCommodityPushLayout commodityPushLayout;
    private String lastJumpBuyCommodityLink;

    //??????????????????
    private ImageView moreVideoListIv;
    //?????????
    private OnViewActionListener onViewActionListener;
    //???????????????????????????
    private IPLVECPreviousDialogFragment previousPopupView;
    //??????????????????
    private List<PLVPlaybackListVO.DataBean.ContentsBean> dataList;
    //?????????????????????vid
    private String currentVid;

    //?????????????????????presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;
    private PLVPreviousView plvPreviousView;

    //?????????????????????
    private IPLVChatPlaybackManager chatPlaybackManager;
    private Runnable playbackTipsRunnable;

    private Boolean hasPreviousPage = null;
    private boolean hasInitPreviousView = false;
    private Rect videoViewRect;

    //????????????????????????
    protected boolean isChatPlaybackEnabled;
    //??????????????????????????????????????????
    private List<Pair<String, String>> needAddedChatPlaybackTask = new ArrayList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_playback_page_home_fragment, container, false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatPlaybackManager != null) {
            chatPlaybackManager.destroy();
        }
        if (chatPlaybackTipsTv != null) {
            chatPlaybackTipsTv.removeCallbacks(playbackTipsRunnable);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        bulletinLy = findViewById(R.id.bulletin_ly);
        playControlIv = findViewById(R.id.play_control_iv);
        playControlIv.setOnClickListener(this);
        playTimeTv = findViewById(R.id.play_time_tv);
        playProgressSb = findViewById(R.id.play_progress_sb);
        playProgressSb.setOnSeekBarChangeListener(playProgressChangeListener);
        totalTimeTv = findViewById(R.id.total_time_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);
        //??????
        commodityIv = findViewById(R.id.playback_commodity_iv);
        commodityIv.setOnClickListener(this);
        commodityPushLayout = findViewById(R.id.playback_commodity_push_ly);
        commodityPopupView = new PLVECCommodityPopupView();
        moreVideoListIv = findViewById(R.id.more_video_list_iv);
        moreVideoListIv.setVisibility(View.GONE);
        morePopupView = new PLVECMorePopupView();

        previousPresenter = new PLVPreviousPlaybackPresenter(liveRoomDataManager);
        previousPopupView = new PLVECPreviousDialogFragment();
        dataList = new ArrayList<>();

        chatImgScanPopupView = new PLVECChatImgScanPopupView();
        greetLy = findViewById(R.id.greet_ly);
        chatMsgRv = findViewById(R.id.chat_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setOnViewActionListener(new PLVECChatMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(View view, String imgUrl) {
                chatImgScanPopupView.showImgScanLayout(view, imgUrl);
            }
        });
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
                if (isChatPlaybackEnabled) {
                    if (chatPlaybackManager != null) {
                        chatPlaybackManager.loadPrevious();
                    }
                }
            }
        });
        //????????????tips
        chatPlaybackTipsTv = findViewById(R.id.plvlc_chat_playback_tips_tv);
        //????????????
        cardPushManager.registerView((ImageView) findViewById(R.id.card_enter_view), (TextView) findViewById(R.id.card_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.card_enter_tips_view));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    @Override
    protected boolean isPlaybackFragment() {
        return true;
    }

    @Override
    protected void updateWatchInfo(String coverImage, String publisher) {
        watchInfoLy.updateWatchInfo(coverImage, publisher);
        watchInfoLy.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateWatchCount(long times) {
        watchInfoLy.updateWatchCount(times);
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

    @Override
    protected void acceptChatPlaybackEnable(boolean isChatPlaybackEnable) {
        this.isChatPlaybackEnabled = isChatPlaybackEnable;
        if (isChatPlaybackEnable) {
            if (chatPlaybackManager == null) {
                chatPlaybackManager = new PLVChatPlaybackManager();
                chatPlaybackManager.setOnGetDataListener(new IPLVChatPlaybackGetDataListener() {
                    @Override
                    public int currentTime() {
                        int currentTime = 0;
                        if (onViewActionListener != null) {
                            currentTime = onViewActionListener.getVideoCurrentPosition();
                        }
                        return currentTime;
                    }

                    @Override
                    public boolean canScrollBottom() {
                        return chatMsgRv.canScrollVertically(1);
                    }

                    @Override
                    public Object[] getParsedEmoObjects(String content) {
                        return new CharSequence[]{PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(content), ConvertUtils.dp2px(12), Utils.getApp())};
                    }
                });
                chatPlaybackManager.addOnCallDataListener(chatPlaybackCallDataListener);
            }
            if (chatPlaybackTipsTv != null) {
                chatPlaybackTipsTv.setVisibility(View.VISIBLE);
                chatPlaybackTipsTv.postDelayed(playbackTipsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        chatPlaybackTipsTv.setVisibility(View.GONE);
                    }
                }, 5000);
            }
            //??????????????????????????????????????????????????????????????????????????????????????????onActivityCreated???????????????
            calculateLiveVideoViewRect();
        }
        chatroomPresenter.registerView(chatroomView);
        //????????????????????????????????????????????????????????????????????????????????????
        checkStartChatPlayback();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    //??????????????????
    @Override
    public void setPlayerState(PLVPlayerState state) {
        if (state == PLVPlayerState.PREPARED) {
            if (onViewActionListener != null) {
                totalTimeTv.setText(PLVTimeUtils.generateTime(onViewActionListener.onGetDurationAction(), true));
            }
        }
    }

    //??????????????????
    @Override
    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
        if (playInfoVO == null) {
            return;
        }
        int position = playInfoVO.getPosition();
        int totalTime = playInfoVO.getTotalTime();
        int bufPercent = playInfoVO.getBufPercent();
        boolean isPlaying = playInfoVO.isPlaying();
        boolean isSubViewPlaying = playInfoVO.isSubVideoViewPlaying();
        if (isSubViewPlaying) {
            playControlIv.setSelected(false);
            playProgressSb.setProgress(0);
            moreIv.setClickable(false);
            morePopupView.hideAll();
        } else {
            playControlIv.setClickable(true);
            playProgressSb.setClickable(true);
            moreIv.setClickable(true);
            //?????????????????????????????????????????????
            if (!isPlaySbDragging) {
                playTimeTv.setText(PLVTimeUtils.generateTime(position, true));
                if (totalTime > 0) {
                    playProgressSb.setProgress((int) ((long) playProgressSb.getMax() * position / totalTime));
                } else {
                    playProgressSb.setProgress(0);
                }
            }
            playProgressSb.setSecondaryProgress(playProgressSb.getMax() * bufPercent / 100);
            playControlIv.setSelected(isPlaying);
        }

        //?????????????????????????????????????????????
        if (position >= totalTime && totalTime > 0) {
            if (previousPresenter != null) {
                previousPresenter.onPlayComplete();
            }
        }
    }

    @Override
    public void onHasPreviousPage(boolean hasPreviousPage) {
        if (this.hasPreviousPage != null && this.hasPreviousPage == hasPreviousPage) {
            return;
        }
        this.hasPreviousPage = hasPreviousPage;
        if (hasPreviousPage) {
            moreVideoListIv.setOnClickListener(this);
            moreVideoListIv.setVisibility(View.VISIBLE);
            initPreviousView();
        } else {
            moreVideoListIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void setOnViewActionListener(PLVECCommonHomeFragment.OnViewActionListener listener) {
        this.onViewActionListener = (OnViewActionListener) listener;
    }

    //???????????????????????????
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
    public void onPlaybackVideoPrepared(String sessionId, String channelId) {
        if (needAddedChatPlaybackTask != null) {
            needAddedChatPlaybackTask.add(new Pair<>(sessionId, channelId));
        }
        if (isChatPlaybackEnabled && chatPlaybackManager != null) {
            chatPlaybackManager.start(sessionId, channelId);
        }
    }

    @Override
    public void onPlaybackVideoSeekComplete(int time) {
        if (isChatPlaybackEnabled && chatPlaybackManager != null) {
            chatPlaybackManager.seek(time);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    private void checkStartChatPlayback() {
        if (needAddedChatPlaybackTask != null && needAddedChatPlaybackTask.size() > 0) {
            Pair<String, String> data = needAddedChatPlaybackTask.get(needAddedChatPlaybackTask.size() - 1);
            chatPlaybackManager.start(data.first, data.second);
        }
        needAddedChatPlaybackTask = null;
    }

    IPLVChatPlaybackCallDataListener chatPlaybackCallDataListener = new PLVChatPlaybackCallDataExListener() {
        @Override
        public void onLoadPreviousEnabled(boolean enabled, boolean isByClearData) {
            if (swipeLoadView != null) {
                swipeLoadView.setEnabled(enabled);
            }
            if (!enabled && !isByClearData) {
                ToastUtils.showShort(R.string.plv_chat_toast_history_all_loaded);
            }
        }

        @Override
        public void onHasNotAddedData() {
            if (unreadMsgTv != null) {
                if (unreadMsgTv.getVisibility() != View.VISIBLE) {
                    unreadMsgTv.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onLoadPreviousFinish() {
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
            }
        }

        @Override
        public void onDataInserted(int startPosition, int count, List<PLVChatPlaybackData> insertDataList, boolean inHead, int time) {
            List<PLVBaseViewData> dataList = new ArrayList<>();
            for (PLVChatPlaybackData chatPlaybackData : insertDataList) {
                boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(chatPlaybackData.getUserType())
                        || PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatPlaybackData.getUserId());
                int itemType = chatPlaybackData.isImgMsg() ? PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG : PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                // ?????????userType???????????????????????????
                dataList.add(new PLVBaseViewData<>(chatPlaybackData, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(chatPlaybackData.getUserId()) : null));
            }
            if (inHead) {
                addChatMessageToListHead(dataList);
            } else {
                addChatMessageToList(dataList, chatMessageAdapter.getItemCount() == 0);
            }
        }

        @Override
        public void onDataRemoved(int startPosition, int count, List<PLVChatPlaybackData> removeDataList, boolean inHead) {
            removeChatMessageToList(startPosition, count);
        }

        @Override
        public void onDataCleared() {
            removeChatMessageToList(null, true);
        }

        @Override
        public void onData(List<PLVChatPlaybackData> dataList) {
        }

        @Override
        public void onManager(IPLVChatPlaybackManager chatPlaybackManager) {
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
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
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
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
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private void addChatMessageToListHead(final List<PLVBaseViewData> chatMessageDataList) {
        if (chatMessageAdapter != null) {
            chatMessageAdapter.addDataListChangedAtHead(chatMessageDataList);
            chatMsgRv.scrollToPosition(0);
        }
    }

    private void removeChatMessageToList(int startPosition, int count) {
        if (chatMessageAdapter != null) {
            chatMessageAdapter.removeDataChanged(startPosition, count);
            if (!chatMsgRv.canScrollVertically(1)) {
                chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            }
        }
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isRemoveAll) {
                    chatMessageAdapter.removeAllDataChanged();
                } else {
                    chatMessageAdapter.removeDataChanged(id);
                }
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
    // </editor-fold>

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
        //????????????????????????????????????????????????????????????????????????
        if (!isChatPlaybackEnabled) {
            return;
        }
        //???????????????
        greetLy.acceptGreetingMessage(loginEvent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - MVP?????????view?????????">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);//??????????????????????????????textSize
        }

        @Override
        public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {
            super.onLoginEvent(loginEvent);
            acceptLoginMessage(loginEvent);
        }

        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
            acceptBulletinMessage(bulletinVO);
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

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - MVP??????view????????????">

    /**
     * ??????PreviousView?????????
     */
    private void initPreviousView() {
        if (hasInitPreviousView) {
            return;
        }
        hasInitPreviousView = true;

        PLVPreviousView.Builder builder = new PLVPreviousView.Builder(getContext());
        //??????PLVPreviousView
        plvPreviousView = builder.create();
        PLVECPreviousAdapter plvecPreviousAdapter = new PLVECPreviousAdapter();
        plvecPreviousAdapter.setOnViewActionListener(new PLVPreviousAdapter.OnViewActionListener() {
            @Override
            public void changeVideoVidClick(String vid) {
                plvPreviousView.changePlaybackVideoVid(vid);
            }
        });
        builder.setAdapter(plvecPreviousAdapter)
                .setRecyclerViewLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false))
                .setRecyclerViewItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.left = ConvertUtils.dp2px(8);
                        outRect.bottom = ConvertUtils.dp2px(20);
                    }
                }).setThemeColor("#FFFFA611")
                .setOnPrepareChangeVidListener(new PLVPreviousView.PLVPreviousViewInterface.OnPrepareChangeVideoVidListener() {
                    @Override
                    public void onPrepareChangeVideoVid(String vid) {
                        if (onViewActionListener != null) {
                            onViewActionListener.onChangePlaybackVidAndPlay(vid);
                        }
                    }
                });
        plvPreviousView.setParams(builder);
        //??????presenter
        if (previousPresenter != null) {
            previousPresenter.registerView(plvPreviousView.getPreviousView());
        }
        //?????????????????????vid????????????????????????????????????
        if (liveRoomDataManager.getConfig().getVid() == null || liveRoomDataManager.getConfig().getVid().isEmpty()) {
            //??????????????????????????????vid?????????????????????????????????????????????
            plvPreviousView.requestPreviousList();
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
        String link = contentBean.isNormalLink() ? contentBean.getLink() : contentBean.getMobileAppLink();
        if (TextUtils.isEmpty(link)) {
            ToastUtils.showShort(R.string.plv_commodity_toast_empty_link);
            return;
        }
        lastJumpBuyCommodityLink = link;
        jumpBuyCommodity();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ???????????????????????????">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            isPlaySbDragging = true;
            if (onViewActionListener != null) {
                int seekPosition = (int) ((long) onViewActionListener.onGetDurationAction() * progress / seekBar.getMax());
                playTimeTv.setText(PLVTimeUtils.generateTime(seekPosition, true));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPlaySbDragging = false;
            if (onViewActionListener != null) {
                onViewActionListener.onSeekToAction(seekBar.getProgress(), seekBar.getMax());
            }
        }
    };
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

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_control_iv) {
            if (onViewActionListener != null) {
                v.setSelected(onViewActionListener.onPauseOrResumeClick(v));
            }
        } else if (id == R.id.more_iv) {
            float currentSpeed = onViewActionListener == null ? 1 : onViewActionListener.onGetSpeedAction();
            morePopupView.showPlaybackMoreLayout(v, currentSpeed, new PLVECMorePopupView.OnPlaybackMoreClickListener() {
                @Override
                public void onChangeSpeedClick(View view, float speed) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeSpeedClick(view, speed);
                    }
                }
            });
        } else if (id == R.id.more_video_list_iv) {
            //???????????????????????????popview
            if (previousPopupView == null) {
                previousPopupView = new PLVECPreviousDialogFragment();
            }
            if (plvPreviousView != null) {
                previousPopupView.setPrviousView(plvPreviousView);
            }
            previousPopupView.showPlaybackMoreVideoDialog(dataList, currentVid, this);
            //??????DialogFragment????????????????????????
            previousPopupView.setDismissListener(new IPLVECPreviousDialogFragment.DismissListener() {
                @Override
                public void onDismissListener() {
                    //?????????????????????previousPopupView???????????????previousPopupView?????????????????????PLVECPlaybackFragment
                    //??????previousPopupView????????????????????????????????????????????????
                    previousPopupView = null;
                }
            });
        } else if (id == R.id.playback_commodity_iv) {
            showCommodityLayout(v);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - view?????????????????????">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //??????/?????????true: doResume, false: doPause
        boolean onPauseOrResumeClick(View view);

        //????????????
        void onChangeSpeedClick(View view, float speed);

        //????????????
        void onSeekToAction(int progress, int max);

        //?????????????????????
        int onGetDurationAction();

        /**
         * ??????????????????????????????
         *
         * @return ????????????????????????
         */
        int getVideoCurrentPosition();

        //????????????????????????
        void onSetVideoViewRectAction(Rect videoViewRect);

        //????????????
        float onGetSpeedAction();

        /**
         * ?????????????????????vid?????????????????????
         *
         * @param vid
         */
        void onChangePlaybackVidAndPlay(String vid);
    }
    // </editor-fold>
}
