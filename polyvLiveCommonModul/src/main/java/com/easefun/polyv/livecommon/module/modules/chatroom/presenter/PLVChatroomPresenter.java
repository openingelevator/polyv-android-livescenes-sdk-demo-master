package com.easefun.polyv.livecommon.module.modules.chatroom.presenter;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.arch.lifecycle.Observer;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataRequester;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.data.PLVChatroomData;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketMessage;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVFaceManager;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.gif.RelativeImageSpan;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.IPolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.IPolyvOnlineCountListener;
import com.easefun.polyv.livescenes.chatroom.IPolyvProhibitedWordListener;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.chatroom.event.PolyvEventHelper;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.log.chat.PolyvChatroomELog;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.net.PolyvApiManager;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.log.elog.logcode.chat.PLVErrorCodeChatroomStatus;
import com.plv.foundationsdk.net.PLVResponseApiBean2;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.chatroom.send.custom.PLVCustomEvent;
import com.plv.livescenes.model.PLVEmotionImageVO2;
import com.plv.livescenes.model.PLVKickUsersVO;
import com.plv.livescenes.model.interact.PLVCardPushVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRemoveContentEvent;
import com.plv.socket.event.chat.PLVRemoveHistoryEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVFileShareHistoryEvent;
import com.plv.socket.event.history.PLVHistoryConstant;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVPptShareFileVO;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.socketio.PLVSocketIOClient;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import okhttp3.ResponseBody;

/**
 * mvp-?????????presenter?????????????????? IPLVChatroomContract.IChatroomPresenter ??????
 */
public class PLVChatroomPresenter implements IPLVChatroomContract.IChatroomPresenter {
    // <editor-fold defaultstate="collapsed" desc="??????">
    private static final String TAG = "PLVChatroomPresenter";
    //?????????????????????????????????
    public static final int GET_CHAT_HISTORY_COUNT = 10;
    //????????????????????????
    private static final int CHAT_MESSAGE_TIMESPAN = 500;
    //????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //???????????????
    private PLVChatroomData chatroomData;
    //?????????mvp?????????view
    private List<IPLVChatroomContract.IChatroomView> iChatroomViews;
    //?????????????????????disposable
    private Disposable messageDisposable;

    //?????????
    private long likesCount;
    //???????????????
    private long viewerCount;
    //????????????
    private int onlineCount;
    //????????????
    private int kickCount;
    //????????????????????????????????????
    private boolean isFocusMode;

    //?????????????????????????????????
    private int getChatHistoryCount = GET_CHAT_HISTORY_COUNT;
    //?????????????????????????????????
    private int getChatHistoryTime;
    //??????????????????????????????
    private boolean isNoMoreChatHistory;
    //????????????????????????????????????
    private boolean hasRequestHistoryEvent;
    //?????????????????????viewIndex
    private int requestHistoryViewIndex;
    //?????????????????????disposable
    private Disposable chatHistoryDisposable;
    //????????????????????????????????????
    private boolean isHistoryContainRewardEvent;

    //??????Id
    private String groupId;

    //?????????????????????disposable
    private Disposable chatEmotionImagesDisposable;

    //??????????????????????????????disposable
    private Disposable kickUsersDisposable;

    //????????????????????????????????????
    private Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>> functionSwitchObserver;
    //???????????????????????????
    private Observer<PLVStatefulData<PolyvLiveClassDetailVO>> classDetailVOObserver;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    public static String convertSpecialString(String input) {
        String output;
        output = input.replace("&lt;", "<");
        output = output.replace("&lt", "<");
        output = output.replace("&gt;", ">");
        output = output.replace("&gt", ">");
        output = output.replace("&yen;", "??");
        output = output.replace("&yen", "??");
        return output;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVChatroomPresenter(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        chatroomData = new PLVChatroomData();
        subscribeChatroomMessage();
        observeLiveRoomData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ??????IPLVChatroomContract.IChatroomPresenter???????????????">
    @Override
    public void registerView(@NonNull IPLVChatroomContract.IChatroomView v) {
        if (iChatroomViews == null) {
            iChatroomViews = new ArrayList<>();
        }
        if (!iChatroomViews.contains(v)) {
            iChatroomViews.add(v);
        }
        v.setPresenter(this);
    }

    @Override
    public void unregisterView(IPLVChatroomContract.IChatroomView v) {
        if (iChatroomViews != null) {
            iChatroomViews.remove(v);
        }
    }

    @Override
    public int getViewIndex(IPLVChatroomContract.IChatroomView v) {
        return iChatroomViews == null ? -1 : iChatroomViews.indexOf(v);
    }

    @Override
    public void init() {
        //??????????????????
        PolyvChatroomManager.getInstance().init();
        //????????????????????????
        PolyvChatroomManager.getInstance().setProhibitedWordListener(new IPolyvProhibitedWordListener() {
            @Override
            public void onSendProhibitedWord(@NonNull final String prohibitedMessage, @NonNull final String hintMsg, @NonNull final String status) {
                PLVCommonLog.d(TAG, "chatroom onSendProhibitedWord: ??????????????????????????????");
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onSendProhibitedWord(prohibitedMessage, hintMsg, status);
                        }
                    });
                }
            }
        });
        //?????????????????????????????????
        PolyvChatroomManager.getInstance().addOnRoomStatusListener(new IPolyvChatroomManager.RoomStatusListener() {
            @Override
            public void onStatus(final boolean isClose) {
                PLVCommonLog.d(TAG, "chatroom onRoomStatus: " + isClose);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onCloseRoomStatusChanged(isClose);
                        }
                    });
                }
            }
        });
        //???????????????????????????
        PolyvChatroomManager.getInstance().setOnlineCountListener(new IPolyvOnlineCountListener() {
            @Override
            public void onCall(int onlineCount) {
                PLVChatroomPresenter.this.onlineCount = onlineCount;
                chatroomData.postOnlineCountData(onlineCount);
            }
        });
        //??????socket???????????????
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                PLVCommonLog.d(TAG, "chatroom receiveMessage: " + message + ", event: " + event + ", listenEvent: " + listenEvent);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    PLVRxBus.get().post(new PLVSocketMessage(listenEvent, message, event));
                }
            }
        });
        //??????socket?????????????????????
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                if (status.getStatus() == PLVSocketStatus.STATUS_LOGINSUCCESS) {
                    if (hasRequestHistoryEvent) {//???????????????????????????????????????id??????????????????????????????????????????????????????????????????
                        requestChatHistory(requestHistoryViewIndex);
                    }
                }
            }
        });
    }

    @Override
    public Pair<Boolean, Integer> sendChatMessage(final PolyvLocalMessage textMessage) {
        int sendValue = PolyvChatroomManager.getInstance().sendChatMessage(textMessage, liveRoomDataManager.getSessionId(), true, new Ack() {
            @Override
            public void call(Object... args) {
                PLVCommonLog.d(TAG, "chatroom sendTextMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                /**
                 * ///??????????????????????????????????????????????????????????????????????????????????????????????????????
                 *if ("".equals(args[0])) {
                 *    // ?????????????????????args[0]???""
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: ??????????????????????????????");
                 *    return;
                 *}
                 */

                acceptLocalChatMessage(textMessage, String.valueOf(args[0]));
            }
        });
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//???????????????????????????????????????????????????????????????
            acceptLocalChatMessage(textMessage, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendTextMessage: " + textMessage.getSpeakMessage() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }


    @Override
    public Pair<Boolean, Integer> sendQuoteMessage(final PolyvLocalMessage textMessage, String quoteId) {
        int sendValue = PolyvChatroomManager.getInstance().sendQuoteMessage(textMessage, liveRoomDataManager.getSessionId(), true, new Ack() {
            @Override
            public void call(Object... args) {
                PLVCommonLog.d(TAG, "chatroom sendQuoteMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                /**
                 * ///??????????????????????????????????????????????????????????????????????????????????????????????????????
                 *if ("".equals(args[0])) {
                 *    // ?????????????????????args[0]???""
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: ??????????????????????????????");
                 *    return;
                 *}
                 */

                acceptLocalChatMessage(textMessage, String.valueOf(args[0]));
            }
        }, quoteId);
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//???????????????????????????????????????????????????????????????
            acceptLocalChatMessage(textMessage, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendQuoteMessage: " + textMessage.getSpeakMessage() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }

    @Override
    public int sendQuestionMessage(final PolyvQuestionMessage questionMessage) {
        int sendValue = PolyvChatroomManager.getInstance().sendQuestionMessage(questionMessage);
        if (sendValue > 0) {
            //???????????????????????????????????????
            questionMessage.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(questionMessage.getQuestionMessage(), getQuizEmojiSizes(), Utils.getApp()));
            callbackToView(new ViewRunnable() {
                @Override
                public void run(IPLVChatroomContract.IChatroomView view) {
                    view.onLocalQuestionMessage(questionMessage);
                }
            });
        }
        PLVCommonLog.d(TAG, "chatroom sendQuestionMessage: " + questionMessage.getQuestionMessage() + ", sendValue: " + sendValue);
        return sendValue;
    }

    @Override
    public void sendLikeMessage() {
        PLVCommonLog.d(TAG, "chatroom sendLikeMessage: " + liveRoomDataManager.getSessionId());
        PolyvChatroomManager.getInstance().sendLikes(liveRoomDataManager.getSessionId());
        likesCount++;
        chatroomData.postLikesCountData(likesCount);
    }

    @Override
    public Pair<Boolean, Integer> sendChatEmotionImage(final PLVChatEmotionEvent emotionEvent) {
        PLVCommonLog.d(TAG, "chatroom sendChatEmotionImage: " + liveRoomDataManager.getSessionId());
        int sendValue = PolyvChatroomManager.getInstance().sendEmotionImage(emotionEvent, new Ack() {
            @Override
            public void call(Object... args) {
                PLVCommonLog.d(TAG, "chatroom sendTextMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                /**
                 * ///??????????????????????????????????????????????????????????????????????????????????????????????????????
                 *if ("".equals(args[0])) {
                 *    // ?????????????????????args[0]???""
                 *    PLVCommonLog.d(TAG, "chatroom sendTextMessage: ??????????????????????????????");
                 *    return;
                 *}
                 */

                acceptEmotionMessage(emotionEvent, String.valueOf(args[0]));
            }
        });
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//???????????????????????????????????????????????????????????????
            acceptEmotionMessage(emotionEvent, "");
        }
        PLVCommonLog.d(TAG, "chatroom sendChatEmotionImage: " + emotionEvent.getId() + ", sendValue: " + sendValue);
        return new Pair<>(sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP, sendValue);
    }

    @Override
    public void sendChatImage(final PolyvSendLocalImgEvent localImgEvent) {
        PLVCommonLog.d(TAG, "chatroom sendChatImage: " + localImgEvent.getImageFilePath() + ", sessionId: " + liveRoomDataManager.getSessionId());
        PolyvChatroomManager.getInstance().sendChatImage(localImgEvent, liveRoomDataManager.getSessionId());
        localImgEvent.setTime(System.currentTimeMillis());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                view.onLocalImageMessage(localImgEvent);
            }
        });
    }

    @Override
    public <DataBean> void sendCustomMsg(PolyvBaseCustomEvent<DataBean> baseCustomEvent) {
        PLVCommonLog.d(TAG, "chatroom sendCustomMsg: " + baseCustomEvent);
        PolyvChatroomManager.getInstance().sendCustomMsg(baseCustomEvent);
    }

    @Override
    public PolyvCustomEvent<PLVCustomGiftBean> sendCustomGiftMessage(PLVCustomGiftBean customGiftBean, String tip) {
        PolyvCustomEvent<PLVCustomGiftBean> customEvent = new PolyvCustomEvent<>(PLVCustomGiftBean.EVENT/*????????????????????????*/, customGiftBean);
        customEvent.setTip(tip);
        customEvent.setEmitMode(PolyvBaseCustomEvent.EMITMODE_ALL);//?????????????????????EMITMODE_ALL??????????????????????????????????????????EMITMODE_OTHERS??????????????????????????????????????????
        customEvent.setVersion(PolyvCustomEvent.VERSION_1);//??????????????????????????????????????????????????????????????????
        /**
         * ???????????????????????????????????????????????????????????????????????????
         * PLVCustomEvent.JOIN_HISTORY_TRUE?????????
         * PLVCustomEvent.JOIN_HISTORY_FALSE????????????
         * */
        customEvent.setJoinHistory(PLVCustomEvent.JOIN_HISTORY_TRUE);
        customEvent.setTime(System.currentTimeMillis());
        PLVCommonLog.d(TAG, "chatroom sendCustomGiftMessage: " + customEvent);
        PolyvChatroomManager.getInstance().sendCustomMsg(customEvent);
        return customEvent;
    }

    @Override
    public void setGetChatHistoryCount(int getChatHistoryCount) {
        this.getChatHistoryCount = getChatHistoryCount;
    }

    @Override
    public void requestChatHistory(final int viewIndex) {
        if (PolyvSocketWrapper.getInstance().isAllowChildRoom()) {
            if (!PolyvSocketWrapper.getInstance().canGetChildRoomIdStatus()) {
                //??????????????????????????????????????????????????????id????????????????????????????????????????????????id??????????????????
                hasRequestHistoryEvent = true;
                requestHistoryViewIndex = viewIndex;
                return;
            }
        }
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        int start = getChatHistoryTime * getChatHistoryCount;
        int end = (getChatHistoryTime + 1) * getChatHistoryCount - 1;
        chatHistoryDisposable = PolyvApiManager.getPolyvApichatApi().getChatHistory(getRoomIdCombineDiscuss(), start, end, 1, 1)
                .map(new Function<ResponseBody, JSONArray>() {
                    @Override
                    public JSONArray apply(ResponseBody responseBody) throws Exception {
                        return new JSONArray(responseBody.string());
                    }
                })
                .compose(new PLVRxBaseTransformer<JSONArray, JSONArray>())
                .map(new Function<JSONArray, JSONArray>() {
                    @Override
                    public JSONArray apply(JSONArray jsonArray) throws Exception {
                        if (jsonArray.length() < getChatHistoryCount) {
                            isNoMoreChatHistory = true;
                        }
                        return jsonArray;
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<JSONArray, List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public List<PLVBaseViewData<PLVBaseEvent>> apply(JSONArray jsonArray) throws Exception {
                        return acceptChatHistory(jsonArray, getSpeakEmojiSizes());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVBaseViewData<PLVBaseEvent>>>() {
                    @Override
                    public void accept(final List<PLVBaseViewData<PLVBaseEvent>> dataList) throws Exception {
                        getChatHistoryTime++;
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onHistoryDataList(dataList, getChatHistoryTime, isNoMoreChatHistory, viewIndex);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(final Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //???????????????????????????????????????
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.LOAD_HISTORY_FAIL, throwable);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onHistoryRequestFailed(PLVLiveRoomDataRequester.getErrorMessage(throwable), throwable, viewIndex);
                            }
                        });
                    }
                });
    }

    @Override
    public int[] getSpeakEmojiSizes() {
        return getEmojiSizes(1);
    }

    @Override
    public void requestKickUsers() {
        if (kickUsersDisposable != null) {
            kickUsersDisposable.dispose();
        }
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//?????????????????????????????????????????????id???????????????????????????
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();//socket??????????????????????????????
        }
        kickUsersDisposable = PLVChatApiRequestHelper.getKickUsers(loginRoomId)
                .subscribe(new Consumer<PLVKickUsersVO>() {
                    @Override
                    public void accept(final PLVKickUsersVO plvsKickUsersVO) throws Exception {
                        if (plvsKickUsersVO.getCode() == 200) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    kickCount = plvsKickUsersVO.getData().size();
                                    chatroomData.postKickCountData(kickCount);
                                    view.onKickUsersList(plvsKickUsersVO.getData());
                                }
                            });
                        } else {
                            PLVCommonLog.exception(new Throwable(plvsKickUsersVO.toString()));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //???????????????????????????????????????
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.GET_KICKUSERS_FAIL, throwable);
                    }
                });
    }

    @Override
    public int getChatHistoryTime() {
        return getChatHistoryTime;
    }

    @Override
    public void setHistoryContainRewardEvent(boolean historyContainRewardEvent) {
        this.isHistoryContainRewardEvent = historyContainRewardEvent;
    }

    @Override
    public void getChatEmotionImages() {
        //???????????????????????????????????????????????????50????????????????????????????????????????????????
        int size = 100;
        int page = 1;
        String channel = PLVSocketIOClient.getInstance().getChannelId();
        String accountId = PLVSocketIOClient.getInstance().getAccountUserId();
        if (chatEmotionImagesDisposable != null){
            chatEmotionImagesDisposable.dispose();
        }
        chatEmotionImagesDisposable = PolyvApiManager.getPolyvApichatApi().getEmotionImages(channel, accountId, page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVResponseApiBean2<com.easefun.polyv.livescenes.model.PLVEmotionImageVO>>() {
                    @Override
                    public void accept(PLVResponseApiBean2<com.easefun.polyv.livescenes.model.PLVEmotionImageVO> polyvEmotionImageVO) throws Exception {
                        if (polyvEmotionImageVO != null && polyvEmotionImageVO.getData() != null && polyvEmotionImageVO.getData().getList() != null) {
                            List<PLVEmotionImageVO.EmotionImage> emotionImages = polyvEmotionImageVO.getData().getList();
                            chatroomData.postEmotionImages(emotionImages);
                            //?????????PLVFaceManager
                            PLVFaceManager.getInstance().initEmotionList(emotionImages);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    @Override
    public Observable<PLVCardPushVO> getCardPushInfo(String cardId) {
        return PLVChatApiRequestHelper.requestCardPushInfo(getConfig().getChannelId(), cardId);
    }

    @Override
    public boolean isCloseRoom() {
        return PolyvChatroomManager.getInstance().isCloseRoom();
    }

    @Override
    public void toggleRoom(boolean isClose, IPolyvChatroomManager.RequestApiListener<String> listener) {
        PolyvChatroomManager.getInstance().toggleRoom(isClose, listener);
    }

    @Override
    public void onJoinDiscuss(String groupId) {
        this.groupId = groupId;
        clearHistoryInfo();
    }

    @Override
    public void onLeaveDiscuss() {
        groupId = null;
        clearHistoryInfo();
    }

    @NonNull
    @Override
    public PLVChatroomData getData() {
        return chatroomData;
    }

    @Override
    public void destroy() {
        clearHistoryInfo();
        if (iChatroomViews != null) {
            iChatroomViews.clear();
        }
        if (messageDisposable != null) {
            messageDisposable.dispose();
        }
        if (chatEmotionImagesDisposable != null){
            chatEmotionImagesDisposable.dispose();
        }
        if (kickUsersDisposable != null) {
            kickUsersDisposable.dispose();
        }
        liveRoomDataManager.getFunctionSwitchVO().removeObserver(functionSwitchObserver);
        liveRoomDataManager.getClassDetailVO().removeObserver(classDetailVOObserver);
        PolyvChatroomManager.getInstance().destroy();//?????????????????????????????????????????????
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ??????????????????">
    private List<PLVBaseViewData<PLVBaseEvent>> acceptChatHistory(JSONArray jsonArray, int[] speakEmojiSizes) {
        if (speakEmojiSizes == null) {
            speakEmojiSizes = new int[1];
            speakEmojiSizes[0] = ConvertUtils.dp2px(12);
        }
        List<PLVBaseViewData<PLVBaseEvent>> tempChatItems = new ArrayList<>();
        for (int i = 0; i < (jsonArray.length() <= getChatHistoryCount ? jsonArray.length() : jsonArray.length() - 1); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String msgType = jsonObject.optString("msgType");
                if (!TextUtils.isEmpty(msgType)) {
                    if (PLVHistoryConstant.MSGTYPE_CUSTOMMESSAGE.equals(msgType)) {
                        //custom message
                    }
                    continue;
                }
                String messageSource = jsonObject.optString("msgSource");
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                JSONObject jsonObject_content = jsonObject.optJSONObject("content");
                if (!TextUtils.isEmpty(messageSource)) {
                    //???/?????????/????????????/???????????????????????????????????????
                    if (PLVHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG;
                        PLVChatImgHistoryEvent chatImgHistory = PLVGsonUtil.fromJson(PLVChatImgHistoryEvent.class, jsonObject.toString());
                        //??????????????????????????????????????????????????????
                        if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatImgHistory.getUser().getUserId())) {
                            chatImgHistory.getUser().setNick(PolyvSocketWrapper.getInstance().getLoginVO().getNickName());
                            itemType = PLVChatMessageItemType.ITEMTYPE_SEND_IMG;
                        }
                        boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(chatImgHistory.getUser().getUserType())
                                || PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatImgHistory.getUser().getUserId());
                        PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(chatImgHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(chatImgHistory.getUser().getUserId()) : null);
                        tempChatItems.add(0, itemData);
                    } else if (PLVHistoryConstant.MSGSOURCE_REWARD.equals(messageSource)) {
                        //????????????
                        PLVRewardEvent.ContentBean rewardContentsBean = PolyvEventHelper.gson.fromJson(jsonObject_content.toString(), PLVRewardEvent.ContentBean.class);
                        PLVRewardEvent historyRewardEvent = new PLVRewardEvent();
                        if (rewardContentsBean != null) {
                            historyRewardEvent.setContent(rewardContentsBean);
                            historyRewardEvent.setRoomId(jsonObject_user.optInt("roomId"));

                            PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(historyRewardEvent);
                            PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT);
                            if (isHistoryContainRewardEvent) {
                                tempChatItems.add(0, itemData);
                            }
                        }
                        //todo ????????????????????????????????????????????????????????????event???????????????????????????????????????
                        PLVRewardEvent rewardEvent = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PLVRewardEvent.class);
                        if(rewardEvent != null){
                            if (jsonObject_user != null) {
                                rewardEvent.setRoomId(jsonObject_user.optInt("roomId"));
                                PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(rewardEvent);
                                PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT);
                                if (isHistoryContainRewardEvent) {
                                    tempChatItems.add(0, itemData);
                                    continue;
                                }
                                String goodImage = rewardEvent.getContent().getGimg();
                                String nickName = rewardEvent.getContent().getUnick();
                                int goodNum = rewardEvent.getContent().getGoodNum();
                                Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                                if (rewardSpan != null) {
                                    rewardEvent.setObjects(rewardSpan);
                                    int itemType = PLVChatMessageItemType.ITEMTYPE_REWARD;
                                    PLVBaseViewData chatTypeItem = new PLVBaseViewData<>(rewardEvent, itemType, false);
                                    tempChatItems.add(0, chatTypeItem);
                                }
                            }
                        }
                    } else if (PLVHistoryConstant.MSGSOURCE_FILE.equals(messageSource)) {
                        int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                        PLVFileShareHistoryEvent fileShareEvent = PLVGsonUtil.fromJson(PLVFileShareHistoryEvent.class, jsonObject.toString());
                        if (fileShareEvent != null) {
                            parseFileShareEventFileData(fileShareEvent);
                            //??????????????????????????????????????????????????????
                            if (PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(fileShareEvent.getUser().getUserId())) {
                                fileShareEvent.getUser().setNick(PLVSocketWrapper.getInstance().getLoginVO().getNickName());
                                itemType = PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK;
                            }
                            //???????????????????????????????????????
                            fileShareEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(fileShareEvent.getContent()), speakEmojiSizes, Utils.getApp()));
                            PLVChatQuoteVO chatQuoteVO = fileShareEvent.getQuote();
                            if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                                chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), speakEmojiSizes, Utils.getApp()));
                            }
                            boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(fileShareEvent.getUser().getUserType())
                                    || PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(fileShareEvent.getUser().getUserId());
                            PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(fileShareEvent, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(fileShareEvent.getUser().getUserId()) : null);
                            tempChatItems.add(0, itemData);
                        }
                    }
                    continue;
                }
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    if (PLVHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //?????????????????????????????????
                        continue;
                    }
                    if (jsonObject_content != null) {
                        //content??????????????????????????????????????????
                        continue;
                    }
                    int itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                    PLVSpeakHistoryEvent speakHistory = PLVGsonUtil.fromJson(PLVSpeakHistoryEvent.class, jsonObject.toString());
                    //??????????????????????????????????????????????????????
                    if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakHistory.getUser().getUserId())) {
                        speakHistory.getUser().setNick(PolyvSocketWrapper.getInstance().getLoginVO().getNickName());
                        itemType = PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK;
                    }
                    //???????????????????????????????????????
                    speakHistory.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(speakHistory.getContent()), speakEmojiSizes, Utils.getApp()));
                    PLVChatQuoteVO chatQuoteVO = speakHistory.getQuote();
                    if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                        chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), speakEmojiSizes, Utils.getApp()));
                    }
                    boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(speakHistory.getUser().getUserType())
                            || PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakHistory.getUser().getUserId());
                    PLVBaseViewData<PLVBaseEvent> itemData = new PLVBaseViewData<PLVBaseEvent>(speakHistory, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(speakHistory.getUser().getUserId()) : null);
                    tempChatItems.add(0, itemData);
                }
            }
        }
        return tempChatItems;
    }

    private void clearHistoryInfo() {
        getChatHistoryTime = 0;
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????">
    private void subscribeChatroomMessage() {
        messageDisposable = PLVRxBus.get().toObservable(PLVSocketMessage.class)
                .buffer(CHAT_MESSAGE_TIMESPAN, TimeUnit.MILLISECONDS)//500ms??????????????????????????????????????????????????????
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<PLVSocketMessage>, List<PLVSocketMessage>>() {
                    @Override
                    public List<PLVSocketMessage> apply(List<PLVSocketMessage> chatroomMessages) throws Exception {
                        //???????????????PolyvSocketWrapper.getInstance()
                        if (PolyvSocketWrapper.getInstance().getLoginVO() == null) {
                            return new ArrayList<>();
                        }
                        return getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId()) ? chatroomMessages : null;
                    }
                })
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<List<PLVSocketMessage>>() {
                    @Override
                    public void accept(List<PLVSocketMessage> chatroomMessages) throws Exception {
                        acceptChatroomMessage(chatroomMessages);//????????????????????????
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void acceptChatroomMessage(List<PLVSocketMessage> socketMessages) {
        if (socketMessages == null || socketMessages.isEmpty()) {
            return;
        }

        final List<PLVBaseViewData> chatMessageDataList = new ArrayList<>();
        for (PLVSocketMessage chatroomMessage : socketMessages) {
            String message = chatroomMessage.getMessage();
            String event = chatroomMessage.getEvent();
            String listenEvent = chatroomMessage.getListenEvent();
            Object chatMessage = null;
            int itemType = PLVChatMessageItemType.ITEMTYPE_UNDEFINED;
            boolean isSpecialType = false;
            String specialTypeUserId = null;
            if (PLVEventConstant.SE_CUSTOMMESSAGE.equals(listenEvent)) {
                //???????????????
                switch (event) {
                    //?????????????????????????????????
                    case PLVCustomGiftBean.EVENT:
                        Type giftType = new TypeToken<PolyvCustomEvent<PLVCustomGiftBean>>() {
                        }.getType();
                        final PolyvCustomEvent<PLVCustomGiftBean> customGiftEvent = PolyvEventHelper.gson.fromJson(message, giftType);
                        if (customGiftEvent != null && PolyvCustomEvent.VERSION_1 == customGiftEvent.getVersion()
                                && customGiftEvent.getData() != null && customGiftEvent.getUser() != null) {
                            if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(customGiftEvent.getUser().getUserId())) {
                                //?????????????????????
                            } else {
                                //???????????????????????????
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(IPLVChatroomContract.IChatroomView view) {
                                        view.onCustomGiftEvent(customGiftEvent.getUser(), customGiftEvent.getData());
                                    }
                                });
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else if (PLVEventConstant.MESSAGE_EVENT.equals(listenEvent)) {
                //??????????????????
                switch (event) {
                    //??????????????????
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_SPEAK:
                        final PLVSpeakEvent speakEvent = PLVEventHelper.toMessageEventModel(message, PLVSpeakEvent.class);
                        if (speakEvent != null && speakEvent.getUser() != null) {
                            parseSpeakEventFileData(speakEvent);
                            if (!PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(speakEvent.getUser().getUserId())) {
                                //???????????????????????????????????????
                                speakEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(speakEvent.getValues().get(0)), getSpeakEmojiSizes(), Utils.getApp()));
                                //??????????????????
                                PLVChatQuoteVO chatQuoteVO = speakEvent.getQuote();
                                if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                                    chatQuoteVO.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(convertSpecialString(chatQuoteVO.getContent()), getSpeakEmojiSizes(), Utils.getApp()));
                                }
                                chatMessage = speakEvent;
                                itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                                isSpecialType = PLVEventHelper.isSpecialType(speakEvent.getUser().getUserType());
                                specialTypeUserId = speakEvent.getUser().getUserId();
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(IPLVChatroomContract.IChatroomView view) {
                                        view.onSpeakEvent(speakEvent);
                                    }
                                });
                                if (!speakEvent.isFileShareEvent()) {
                                    chatroomData.postSpeakMessageData((CharSequence) speakEvent.getObjects()[0], isSpecialType);
                                }
                            }
                        }
                        break;
                    //??????????????????
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_CHAT_IMG:
                        final PLVChatImgEvent chatImgEvent = PLVEventHelper.toMessageEventModel(message, PLVChatImgEvent.class);
                        if (chatImgEvent != null &&
                                !PolyvSocketWrapper.getInstance().getLoginVO().getUserId().
                                        equals(chatImgEvent.getUser().getUserId())) {
                            chatMessage = chatImgEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG;
                            isSpecialType = PLVEventHelper.isSpecialType(chatImgEvent.getUser().getUserType());
                            specialTypeUserId = chatImgEvent.getUser().getUserId();
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onImgEvent(chatImgEvent);
                                }
                            });
                        }
                        break;
                    //????????????
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_LIKES:
                        final PLVLikesEvent likesEvent = PLVEventHelper.toMessageEventModel(message, PLVLikesEvent.class);
                        if (likesEvent != null &&
                                !PolyvSocketWrapper.getInstance().getLoginVO().getUserId().
                                        equals(likesEvent.getUserId())) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLikesEvent(likesEvent);
                                }
                            });
                            likesCount = likesCount + likesEvent.getCount();
                            chatroomData.postLikesCountData(likesCount);
                        }
                        break;
                    //??????????????????
                    case PLVEventConstant.MESSAGE_EVENT_LOGIN:
                        final PLVLoginEvent loginEvent = PLVEventHelper.toMessageEventModel(message, PLVLoginEvent.class);
                        if (loginEvent != null) {
                            final boolean isMyLoginEvent = PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(loginEvent.getUser().getUserId());
                            final long restrictMaxViewer = getOrDefault(PLVChannelFeatureManager.onChannel(getConfig().getChannelId()).get(PLVChannelFeature.LIVE_CHATROOM_RESTRICT_MAX_VIEWER), 0L);
                            final boolean restrictedByMaxViewer = restrictMaxViewer > 0 && loginEvent.getOnlineUserNumber() > restrictMaxViewer;
                            if (isMyLoginEvent && restrictedByMaxViewer) {
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onLoginError(
                                                loginEvent,
                                                getString(R.string.plv_chat_restrict_max_viewer_hint),
                                                PLVErrorCodeChatroomStatus.getCode(PLVErrorCodeChatroomStatus.SecondCode.CHATROOM_RESTRICT_MAX_VIEWER)
                                        );
                                    }
                                });
                                break;
                            }

                            chatroomData.postLoginEventData(loginEvent);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLoginEvent(loginEvent);
                                }
                            });
                            //?????????????????????socket??????????????????????????????+1
                            if (!isMyLoginEvent) {
                                viewerCount++;
                                chatroomData.postViewerCountData(viewerCount);
                            }

                        }
                        break;
                    //??????????????????
                    case PLVEventConstant.MESSAGE_EVENT_LOGOUT:
                        final PLVLogoutEvent logoutEvent = PLVEventHelper.toMessageEventModel(message, PLVLogoutEvent.class);
                        if (logoutEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onLogoutEvent(logoutEvent);
                                }
                            });
                        }
                        break;
                    //??????????????????
                    case PLVEventConstant.Interact.BULLETIN_SHOW:
                        final PolyvBulletinVO bulletinVO = PLVGsonUtil.fromJson(PolyvBulletinVO.class, message);
                        if (bulletinVO != null) {
                            chatroomData.postBulletinVO(bulletinVO);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onBulletinEvent(bulletinVO);
                                }
                            });
                        }
                        break;
                    //??????????????????
                    case PLVEventConstant.Interact.BULLETIN_REMOVE:
                        chatroomData.postBulletinVO(null);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(IPLVChatroomContract.IChatroomView view) {
                                view.onRemoveBulletinEvent();
                            }
                        });
                        break;
                    //??????????????????
                    case PLVEventConstant.Chatroom.EVENT_PRODUCT_MESSAGE:
                        PLVProductEvent productEvent = PLVGsonUtil.fromJson(PLVProductEvent.class, message);
                        if (productEvent != null) {
                            if (productEvent.isProductControlEvent()) { //????????????/??????/??????/????????????
                                final PLVProductControlEvent productControlEvent = PLVEventHelper.toMessageEventModel(message, PLVProductControlEvent.class);
                                if (productControlEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductControlEvent(productControlEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductRemoveEvent()) { //????????????/????????????
                                final PLVProductRemoveEvent productRemoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductRemoveEvent.class);
                                if (productRemoveEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductRemoveEvent(productRemoveEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductMoveEvent()) { //????????????/????????????
                                final PLVProductMoveEvent productMoveEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMoveEvent.class);
                                if (productMoveEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductMoveEvent(productMoveEvent);
                                        }
                                    });
                                }
                            } else if (productEvent.isProductMenuSwitchEvent()) { //?????????????????????
                                final PLVProductMenuSwitchEvent productMenuSwitchEvent = PLVEventHelper.toMessageEventModel(message, PLVProductMenuSwitchEvent.class);
                                if (productMenuSwitchEvent != null) {
                                    callbackToView(new ViewRunnable() {
                                        @Override
                                        public void run(IPLVChatroomContract.IChatroomView view) {
                                            view.onProductMenuSwitchEvent(productMenuSwitchEvent);
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    //?????????????????????/????????????
                    case PLVEventConstant.Chatroom.MESSAGE_EVENT_CLOSEROOM:
                        final PLVCloseRoomEvent closeRoomEvent = PLVEventHelper.toMessageEventModel(message, PLVCloseRoomEvent.class);
                        if (closeRoomEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onCloseRoomEvent(closeRoomEvent);
                                }
                            });
                        }
                        break;
                    //????????????
                    case PLVEventConstant.Chatroom.EVENT_T_ANSWER:
                        final PLVTAnswerEvent tAnswerEvent = PLVEventHelper.toMessageEventModel(message, PLVTAnswerEvent.class);
                        //??????????????????????????????
                        if (tAnswerEvent != null &&
                                PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(tAnswerEvent.getS_userId())) {
                            //???????????????????????????????????????
                            tAnswerEvent.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(tAnswerEvent.getContent(), getQuizEmojiSizes(), Utils.getApp()));
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(IPLVChatroomContract.IChatroomView view) {
                                    view.onAnswerEvent(tAnswerEvent);
                                }
                            });
                        }
                        break;
                    //????????????
                    case PLVEventConstant.Chatroom.EVENT_REWARD:
                        final PLVRewardEvent rewardEvent = PLVEventHelper.toMessageEventModel(message, PLVRewardEvent.class);
                        if(rewardEvent != null){
                            if (rewardEvent.getContent() != null) {
                                String goodImage = rewardEvent.getContent().getGimg();
                                String nickName = rewardEvent.getContent().getUnick();
                                int goodNum = rewardEvent.getContent().getGoodNum();
                                Spannable rewardSpan = generateRewardSpan(nickName, goodImage, goodNum);
                                if (rewardSpan != null) {
                                    rewardEvent.setObjects(rewardSpan);
                                }
                            }
                            itemType = PLVChatMessageItemType.ITEMTYPE_REWARD;
                            chatMessage = rewardEvent;
                            chatroomData.postRewardEvent(rewardEvent);
                            liveRoomDataManager.getRewardEventData().postValue(rewardEvent);
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRewardEvent(rewardEvent);
                                }
                            });
                        }
                        break;
                    //???????????????????????????????????????
                    case PLVRemoveContentEvent.EVENT:
                        final PLVRemoveContentEvent removeContentEvent = PLVEventHelper.toMessageEventModel(message, PLVRemoveContentEvent.class);
                        if (removeContentEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRemoveMessageEvent(removeContentEvent.getId(), false);
                                }
                            });
                        }
                        break;
                    //???????????????????????????????????????
                    case PLVRemoveHistoryEvent.EVENT:
                        PLVRemoveHistoryEvent removeHistoryEvent = PLVEventHelper.toMessageEventModel(message, PLVRemoveHistoryEvent.class);
                        if (removeHistoryEvent != null) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                    view.onRemoveMessageEvent(null, true);
                                }
                            });
                        }
                        break;
                    //onSliceId??????
                    case PLVEventConstant.Ppt.ON_SLICE_ID_EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toMessageEventModel(message, PLVOnSliceIDEvent.class);
                        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
                            if (isFocusMode != onSliceIDEvent.getData().isFocusMode()) {
                                isFocusMode = onSliceIDEvent.getData().isFocusMode();
                                final PLVFocusModeEvent focusModeEvent = new PLVFocusModeEvent(isFocusMode);
                                callbackToView(new ViewRunnable() {
                                    @Override
                                    public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                        view.onFocusModeEvent(focusModeEvent);
                                    }
                                });
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (chatMessage != null) {
                    chatMessageDataList.add(new PLVBaseViewData<>(chatMessage, itemType, isSpecialType ? new PLVSpecialTypeTag(specialTypeUserId) : null));
                }
            } else if (PLVEventConstant.EMOTION_EVENT.equals(listenEvent)) {
                final PLVChatEmotionEvent emotionEvent = PLVGsonUtil.fromJson(PLVChatEmotionEvent.class, message);
                if (emotionEvent != null) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onLoadEmotionMessage(emotionEvent);
                        }
                    });
                }
            } else if (PLVEventConstant.Interact.NEWS_PUSH.equals(listenEvent)) {
                //??????????????????
                if (PLVEventConstant.Interact.NEWS_PUSH_START.equals(event)) {
                    final PLVNewsPushStartEvent newsPushStartEvent = PLVGsonUtil.fromJson(PLVNewsPushStartEvent.class, message);
                    if (newsPushStartEvent != null) {
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onNewsPushStartMessage(newsPushStartEvent);
                            }
                        });
                    }
                } else if (PLVEventConstant.Interact.NEWS_PUSH_CANCEL.equals(event)) {
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                            view.onNewsPushCancelMessage();
                        }
                    });
                }
            } else if (PLVEventConstant.Chatroom.SE_FOCUS.equals(listenEvent)) {
                //??????????????????????????????
                if (PLVEventConstant.Chatroom.FOCUS_EVENT_FOCUS_SPECIAL_SPEAK.equals(event)) {
                    final PLVFocusModeEvent focusModeEvent = PLVGsonUtil.fromJson(PLVFocusModeEvent.class, message);
                    if (focusModeEvent != null && isFocusMode != focusModeEvent.isOpen()) {
                        isFocusMode = focusModeEvent.isOpen();
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVChatroomContract.IChatroomView view) {
                                view.onFocusModeEvent(focusModeEvent);
                            }
                        });
                    }
                }
            }
        }
        if (!chatMessageDataList.isEmpty()) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(IPLVChatroomContract.IChatroomView view) {
                    view.onSpeakImgDataList(chatMessageDataList);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private PLVLiveChannelConfig getConfig() {
        return liveRoomDataManager.getConfig();
    }

    private String getRoomIdCombineDiscuss() {
        if (!TextUtils.isEmpty(groupId)) {
            return groupId;
        }
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//?????????????????????????????????????????????id???????????????????????????
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = getConfig().getChannelId();//socket??????????????????????????????
        }
        return loginRoomId;
    }

    private int[] getQuizEmojiSizes() {
        return getEmojiSizes(2);
    }

    private int[] getEmojiSizes(int textSizeType) {//1????????????2?????????
        List<Integer> textSizes = new ArrayList<>();
        if (iChatroomViews != null) {
            for (IPLVChatroomContract.IChatroomView view : iChatroomViews) {
                int textSize;
                switch (textSizeType) {
                    case 1:
                        textSize = view.getSpeakEmojiSize();
                        break;
                    case 2:
                        textSize = view.getQuizEmojiSize();
                        break;
                    default:
                        textSize = 0;
                        break;
                }
                if (view == null || textSize <= 0) {
                    textSizes.add(ConvertUtils.dp2px(12));
                } else {
                    textSizes.add(textSize);
                }
            }
        }
        Integer[] array = textSizes.toArray(new Integer[0]);
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private void acceptLocalChatMessage(final PolyvLocalMessage textMessage, String messageId) {
        //????????????????????????????????????id
        textMessage.setId(messageId);
        //???????????????????????????????????????
        textMessage.setObjects((Object[]) PLVTextFaceLoader.messageToSpan(textMessage.getSpeakMessage(), getSpeakEmojiSizes(), Utils.getApp()));
        //???????????????????????????
        textMessage.setTime(System.currentTimeMillis());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NotNull IPLVChatroomContract.IChatroomView view) {
                view.onLocalSpeakMessage(textMessage);
            }
        });
        chatroomData.postSpeakMessageData((CharSequence) textMessage.getObjects()[0], true);
    }

    private Spannable generateRewardSpan(String nickName, String goodImageUrl, int goodNum) {
        if(goodImageUrl.startsWith("//")){
            goodImageUrl = "https:"+goodImageUrl;
        }
        SpannableStringBuilder span = new SpannableStringBuilder(nickName + " ?????? p");
        int drawableSpanStart = span.length() - 1;
        int drawableSpanEnd = span.length();
        if (goodNum != 1) {
            span.append(" x" + goodNum);
        }
        Drawable drawable = PLVImageLoader.getInstance().getImageAsDrawable(Utils.getApp(), goodImageUrl);
        if (drawable == null) {
            return null;
        }
        int textSize = ConvertUtils.dp2px(12);
        drawable.setBounds(0, 0, textSize * 2, textSize * 2);
        span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), drawableSpanStart, drawableSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private void acceptEmotionMessage(final PLVChatEmotionEvent emotionEvent, String messageId) {
        emotionEvent.setMessageId(messageId);
        emotionEvent.setTime(System.currentTimeMillis());
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull @NotNull IPLVChatroomContract.IChatroomView view) {
                view.onLoadEmotionMessage(emotionEvent);
            }
        });
    }

    private static void parseSpeakEventFileData(@NonNull PLVSpeakEvent speakEvent) {
        if (!speakEvent.isFileShareEvent()) {
            return;
        }
        speakEvent.setFileData(createFileData(speakEvent.getValues().get(0)));
    }

    private static void parseFileShareEventFileData(@NonNull PLVFileShareHistoryEvent fileShareEvent) {
        fileShareEvent.setFileData(createFileData(fileShareEvent.getContent()));
    }

    @Nullable
    private static PLVPptShareFileVO createFileData(@NonNull String jsonContent) {
        final Map<String, String> content = PLVGsonUtil.fromJson(new TypeToken<Map<String, String>>() {}, jsonContent);
        if (content == null) {
            return null;
        }
        final String url = content.get("url");
        final String name = content.get("name");
        return new PLVPptShareFileVO()
                .setUrl(url)
                .setName(name)
                .setSuffix(name == null ? "" : name.substring(name.lastIndexOf(".") + 1));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ????????????????????????">
    private void observeLiveRoomData() {
        //???????????????????????????????????????
        functionSwitchObserver = new Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvChatFunctionSwitchVO> chatFunctionSwitchStateData) {
                liveRoomDataManager.getFunctionSwitchVO().removeObserver(this);
                if (chatFunctionSwitchStateData == null || !chatFunctionSwitchStateData.isSuccess()) {
                    return;
                }
                PolyvChatFunctionSwitchVO functionSwitchVO = chatFunctionSwitchStateData.getData();
                if (functionSwitchVO == null || functionSwitchVO.getData() == null) {
                    return;
                }
                List<PolyvChatFunctionSwitchVO.DataBean> dataBeanList = functionSwitchVO.getData();
                if (dataBeanList == null) {
                    return;
                }
                chatroomData.postFunctionSwitchData(dataBeanList);
            }
        };
        liveRoomDataManager.getFunctionSwitchVO().observeForever(functionSwitchObserver);
        //????????????????????????????????????
        classDetailVOObserver = new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> classDetailVOStateData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (classDetailVOStateData == null || !classDetailVOStateData.isSuccess()) {
                    return;
                }
                PolyvLiveClassDetailVO classDetailVO = classDetailVOStateData.getData();
                if (classDetailVO == null || classDetailVO.getData() == null) {
                    return;
                }
                likesCount = classDetailVO.getData().getLikes();
                viewerCount = classDetailVO.getData().getPageView();
                chatroomData.postLikesCountData(likesCount);
                chatroomData.postViewerCountData(viewerCount);
            }
        };
        liveRoomDataManager.getClassDetailVO().observeForever(classDetailVOObserver);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - view??????">
    private void callbackToView(ViewRunnable runnable) {
        if (iChatroomViews != null) {
            for (IPLVChatroomContract.IChatroomView view : iChatroomViews) {
                if (view != null && runnable != null) {
                    runnable.run(view);
                }
            }
        }
    }

    private interface ViewRunnable {
        void run(@NonNull IPLVChatroomContract.IChatroomView view);
    }
    // </editor-fold>
}
