package com.easefun.polyv.streameralone.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVRadiusBackgroundSpan;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAMessageAdapter;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSAChatMsgTipsWindow;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;


/**
 * ??????????????????????????????viewHolder
 */
public class PLVSAMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="??????">
    public static final String LOADIMG_MOUDLE_TAG = "PLVLCMessageViewHolder";

    //item
    //????????????
    private GifSpanTextView chatMsgTv;
    //?????????????????????
    private TextView chatNickTv;
    //???????????????????????????
    private GifSpanTextView quoteChatMsgTv;
    //?????????????????????????????????
    private TextView quoteChatNickTv;
    //??????????????????????????????
    private ImageView prohibitedWordTipsIv;

    //????????????????????????????????????
    private View failedImageItemLl;
    private ImageView failedImageTipIv;

    //???/??????????????????
    private View imgMessageItem;
    private ImageView imgMessageIv;
    //???/?????????????????????
    private ProgressBar imgLoadingView;
    //???/?????????????????????
    private View quoteSplitView;
    //???/??????????????????????????????
    private ImageView quoteImgMessageIv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVSAMessageViewHolder(View itemView, final PLVSAMessageAdapter adapter) {
        super(itemView, adapter);
        //item
        chatMsgTv = (GifSpanTextView) findViewById(R.id.chat_msg_tv);
        chatNickTv = (TextView) findViewById(R.id.chat_nick_tv);
        quoteChatMsgTv = (GifSpanTextView) findViewById(R.id.quote_chat_msg_tv);
        quoteChatNickTv = (TextView) findViewById(R.id.quote_chat_nick_tv);
        prohibitedWordTipsIv = findViewById(R.id.prohibited_word_tips_iv);
        imgMessageItem = findViewById(R.id.chat_image_item_fl);
        failedImageItemLl = findViewById(R.id.failed_image_ll);
        failedImageTipIv = findViewById(R.id.failed_image_tips_iv);
        //common item
        imgMessageIv = (ImageView) findViewById(R.id.img_message_iv);
        imgLoadingView = (ProgressBar) findViewById(R.id.img_loading_view);
        quoteSplitView = findViewById(R.id.quote_split_view);
        quoteImgMessageIv = (ImageView) findViewById(R.id.quote_img_message_iv);

        initView();
        addOnSendImgListener();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????view">
    private void initView() {
        if (chatMsgTv != null) {
            chatMsgTv.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
                @Override
                public void webLinkOnClick(String url) {
                    PLVWebUtils.openWebLink(url, chatMsgTv.getContext());
                }
            });

            chatMsgTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean onlyShowCopyItem = prohibitedWordVO != null;//??????????????????????????????
                    PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, onlyShowCopyItem, chatMsgTv.getText().toString(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                            chatQuoteVO.setUserId(userId);
                            chatQuoteVO.setNick(nickName);
                            chatQuoteVO.setContent(speakMsg.toString());
                            chatQuoteVO.setObjects(PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(chatQuoteVO.getContent()), ConvertUtils.dp2px(12), Utils.getApp()));
                            adapter.callOnShowAnswerWindow(chatQuoteVO, ((IPLVIdEvent) messageData).getId());
                        }
                    });
                    return true;
                }
            });
        }

        if (prohibitedWordTipsIv != null) {
            prohibitedWordTipsIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (prohibitedWordVO == null) {
                        return;
                    }
                    String msg = prohibitedWordVO.getMessage() + "???" + prohibitedWordVO.getValue();
                    int[] location = new int[2];
                    itemView.getLocationInWindow(location);
                    new PLVSAChatMsgTipsWindow(v).show(v, msg, location[0], location[0] + itemView.getWidth(), location[1] + itemView.getHeight());
                }
            });
        }

        if(failedImageTipIv != null){
            failedImageTipIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_FAIL) {
                        String msg = localImgFailMessage+"";
                        int[] location = new int[2];
                        itemView.getLocationInWindow(location);
                        new PLVSAChatMsgTipsWindow(v).show(v, msg, location[0], location[0] + itemView.getWidth(), location[1] + itemView.getHeight());
                    }
                }
            });
        }

        if (imgMessageIv != null) {
            imgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PLVSAMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, chatImgUrl, false);
                }
            });

            imgMessageIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAndAnswerWithImg();
                    return true;
                }
            });
        }

        if (chatNickTv != null) {
            chatNickTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAndAnswerWithImg();
                    return true;
                }
            });
        }

        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chatQuoteVO != null && chatQuoteVO.getImage() != null) {
                        String imageUrl = chatQuoteVO.getImage().getUrl();
                        ((PLVSAMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, imageUrl, true);
                    }
                }
            });
        }
    }

    private void showAndAnswerWithImg() {
        if (localImgStatus != PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS) {
            return;//?????????????????????????????????
        }
        PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                chatQuoteVO.setUserId(userId);
                chatQuoteVO.setNick(nickName);
                PLVChatQuoteVO.ImageBean imageBean = new PLVChatQuoteVO.ImageBean();
                imageBean.setUrl(chatImgUrl);
                imageBean.setWidth(chatImgWidth);
                imageBean.setHeight(chatImgHeight);
                chatQuoteVO.setImage(imageBean);
                adapter.callOnShowAnswerWindow(chatQuoteVO, ((IPLVIdEvent) messageData).getId());
            }
        });
    }

    private void addOnSendImgListener() {
        PolyvChatroomManager.getInstance().addSendChatImageListener(new PolyvSendChatImageListener() {
            @Override
            public void onUploadFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("??????????????????: " + t.getMessage())
                            .build()
                            .show();
                }
            }

            @Override
            public void onSendFail(PolyvSendLocalImgEvent localImgEvent, int sendValue) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("??????????????????: " + sendValue)
                            .build()
                            .show();
                }
            }

            @Override
            public void onSuccess(PolyvSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onProgress(PolyvSendLocalImgEvent localImgEvent, float progress) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_SENDING);
                localImgEvent.setSendProgress((int) (progress * 100));
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    localImgProgress = localImgEvent.getSendProgress();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.VISIBLE);
                        imgLoadingView.setProgress((int) (progress * 100));
                    }
                }
            }

            @Override
            public void onCheckFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                //???????????????
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    localImgFailMessage = t.getMessage();
                    ((PolyvSendLocalImgEvent) messageData).setObj1(localImgFailMessage);
                    if (imgMessageItem != null) {
                        imgMessageItem.setVisibility(View.GONE);
                    }
                    if(failedImageItemLl != null){
                        failedImageItemLl.setVisibility(View.VISIBLE);
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("??????????????????: " + t.getMessage())
                            .build()
                            .show();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ??????PLVChatMessageBaseViewHolder???????????????">
    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        resetView();
        //???????????????????????????
        boolean isSpecialType = PLVEventHelper.isSpecialType(userType);//???????????????????????????????????????????????????????????????
        //????????????
        SpannableStringBuilder nickSpan = generateNickSpan(nickName);
        if (chatNickTv != null && chatImgUrl != null) {
            chatNickTv.setVisibility(View.VISIBLE);
            chatNickTv.setText(nickSpan);
        }
        //????????????????????????
        if (speakMsg != null) {
            if (chatMsgTv != null) {
                chatMsgTv.setVisibility(View.VISIBLE);
                chatMsgTv.setTextInner(nickSpan.append(speakMsg), isSpecialType);
            }
        }
        //??????????????????
        setImgMessage();

        //?????????????????????????????????
        if (chatQuoteVO != null) {
            String nickName = chatQuoteVO.getNick();
            if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatQuoteVO.getUserId())) {
                nickName = nickName + "(???)";
            }
            if (quoteSplitView != null) {
                quoteSplitView.setVisibility(View.VISIBLE);
            }
            if (chatQuoteVO.isSpeakMessage()) {
                if (quoteChatMsgTv != null) {
                    quoteChatMsgTv.setVisibility(View.VISIBLE);
                    quoteChatMsgTv.setText(new SpannableStringBuilder(nickName).append(": ").append(quoteSpeakMsg));
                }
            } else {
                if (quoteChatNickTv != null) {
                    quoteChatNickTv.setVisibility(View.VISIBLE);
                    quoteChatNickTv.setText(nickName + ": ");
                }
                if (chatQuoteVO.getImage() != null) {
                    if (quoteImgMessageIv != null) {
                        quoteImgMessageIv.setVisibility(View.VISIBLE);
                        fitChatImgWH((int) chatQuoteVO.getImage().getWidth(), (int) chatQuoteVO.getImage().getHeight(), quoteImgMessageIv, 40, 30);//???????????????????????????
                        PLVImageLoader.getInstance().loadImage(chatQuoteVO.getImage().getUrl(), quoteImgMessageIv);
                    }
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API">
    public void processData(PLVBaseViewData data, int position, @NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PLVSAMessageAdapter.PAYLOAD_PROHIBITED_CHANGED:
                    super.processData(data, position);
                    if (chatMsgTv != null) {
                        if (chatMsgTv.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) chatMsgTv.getLayoutParams());
                            layoutParams.rightMargin = prohibitedWordVO == null ? 0 : ConvertUtils.dp2px(20);
                            chatMsgTv.setLayoutParams(layoutParams);
                        }
                    }
                    if (prohibitedWordTipsIv != null) {
                        prohibitedWordTipsIv.setVisibility(prohibitedWordVO == null ? View.GONE : View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI - ??????view">
    private void resetView() {
        if (chatMsgTv != null) {
            chatMsgTv.setVisibility(View.GONE);
            if (chatMsgTv.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) chatMsgTv.getLayoutParams());
                layoutParams.rightMargin = prohibitedWordVO == null ? 0 : ConvertUtils.dp2px(20);
                chatMsgTv.setLayoutParams(layoutParams);
            }
        }
        if (prohibitedWordTipsIv != null) {
            prohibitedWordTipsIv.setVisibility(prohibitedWordVO == null ? View.GONE : View.VISIBLE);
        }
        if (chatNickTv != null) {
            chatNickTv.setVisibility(View.GONE);
        }
        if (imgMessageIv != null) {
            imgMessageIv.setVisibility(View.GONE);
            if (imgMessageIv.getDrawable() != null) {
                imgMessageIv.setImageDrawable(null);
            }
        }
        if (imgLoadingView != null) {
            imgLoadingView.setTag(messageData);
        }
        if (isLocalChatImg) {
            if (imgLoadingView != null) {
                imgLoadingView.setVisibility(localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_SENDING ? View.VISIBLE : View.GONE);
                imgLoadingView.setProgress(localImgProgress);
            }
        } else {
            if (imgLoadingView != null) {
                imgLoadingView.setVisibility(View.GONE);
                imgLoadingView.setProgress(0);
            }
        }
        //????????????????????????view reset
        if (quoteChatMsgTv != null) {
            quoteChatMsgTv.setVisibility(View.GONE);
        }
        if (quoteChatNickTv != null) {
            quoteChatNickTv.setVisibility(View.GONE);
        }
        if (quoteSplitView != null) {
            quoteSplitView.setVisibility(View.GONE);
        }
        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????? - ??????????????????">
    private void setImgMessage() {
        if(isLocalChatImg){
            if(failedImageItemLl != null && imgMessageIv != null){
                if(localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_FAIL){
                    failedImageItemLl.setVisibility(View.VISIBLE);
                    imgMessageIv.setVisibility(View.GONE);
                    return;
                }
            }
        }
        if (chatImgUrl != null) {
            if (imgMessageIv != null) {
                imgMessageIv.setVisibility(View.VISIBLE);
                fitChatImgWH(chatImgWidth, chatImgHeight, imgMessageIv, 80, 60);//???????????????????????????
                if (isLocalChatImg) {
                    PLVImageLoader.getInstance().loadImage(chatImgUrl, imgMessageIv);
                } else {
                    PLVImageLoader.getInstance().loadImage(
                            itemView.getContext(),
                            LOADIMG_MOUDLE_TAG,
                            LOADIMG_MOUDLE_TAG + messageData,
                            R.drawable.plv_icon_image_load_err,
                            createStatusListener(chatImgUrl),
                            imgMessageIv);
                }
            }
        }
    }

    private PLVAbsProgressStatusListener createStatusListener(String imgUrl) {
        return new PLVAbsProgressStatusListener(imgUrl) {
            @Override
            public void onStartStatus(String url) {
                if (imgLoadingView.getTag() != messageData) {//addFistData position can replace
                    return;
                }
                if (imgLoadingView.getProgress() == 0 && imgLoadingView.getVisibility() != View.VISIBLE) {
                    imgLoadingView.setVisibility(View.VISIBLE);
                    imgMessageIv.setImageDrawable(null);
                }
            }

            @Override
            public void onProgressStatus(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                if (isComplete) {
                    imgLoadingView.setProgress(100);
                    imgLoadingView.setVisibility(View.GONE);
                } else {//onFailed??????????????????onProgress
                    if (imgMessageIv.getDrawable() != null) {
                        imgMessageIv.setImageDrawable(null);
                    }
                    imgLoadingView.setVisibility(View.VISIBLE);
                    imgLoadingView.setProgress(percentage);
                }
            }

            @Override
            public void onFailedStatus(@Nullable Exception e, Object model) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                imgLoadingView.setVisibility(View.GONE);
                imgLoadingView.setProgress(0);
                imgMessageIv.setImageResource(R.drawable.plv_icon_image_load_err);//fail can no set
            }

            @Override
            public void onResourceReadyStatus(Drawable drawable) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                imgMessageIv.setImageDrawable(drawable);
            }
        };
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    private SpannableStringBuilder generateNickSpan(String nickName) {
        SpannableStringBuilder nickSpan = new SpannableStringBuilder(nickName);
        if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(userId)) {
            nickSpan.append("(???)");
        }
        nickSpan.append(": ");
        nickSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD16B")), 0, nickSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
            insertActorToNickSpan(nickSpan, Color.parseColor("#F09343"));
        } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
            insertActorToNickSpan(nickSpan, Color.parseColor("#598FE5"));
        } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            insertActorToNickSpan(nickSpan, Color.parseColor("#EB6165"));
        } else if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(userType)) {
            insertActorToNickSpan(nickSpan, Color.parseColor("#33BBC5"));
        }
        return nickSpan;
    }

    private void insertActorToNickSpan(SpannableStringBuilder nickSpan, int bgColor) {
        if (TextUtils.isEmpty(userType) || TextUtils.isEmpty(actor)) {
            return;
        }
        nickSpan.insert(0, userType);
        PLVRadiusBackgroundSpan radiusBackgroundSpan = new PLVRadiusBackgroundSpan(
                itemView.getContext(), bgColor, Color.parseColor("#ffffff"), actor);
        radiusBackgroundSpan.setmBgHeight(ConvertUtils.dp2px(14));
        radiusBackgroundSpan.setmRadius(ConvertUtils.dp2px(7));
        nickSpan.setSpan(radiusBackgroundSpan, 0, userType.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    // </editor-fold>
}
