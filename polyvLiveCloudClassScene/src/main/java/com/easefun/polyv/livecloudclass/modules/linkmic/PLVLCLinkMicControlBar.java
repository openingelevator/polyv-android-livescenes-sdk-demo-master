package com.easefun.polyv.livecloudclass.modules.linkmic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.linkmic.widget.PLVLCLinkMicRingButton;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindow;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.PLVDialogFactory;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVNoConsumeTouchEventButton;
import com.easefun.polyv.livecommon.ui.widget.PLVTouchFloatingView;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.log.IPLVLinkMicTraceLogSender;
import com.plv.linkmic.log.PLVLinkMicTraceLogSender;
import com.plv.livescenes.log.linkmic.PLVLinkMicELog;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * date: 2020/8/14
 * author: HWilliamgo
 * description: ?????????????????????
 */
public class PLVLCLinkMicControlBar extends FrameLayout implements IPLVLCLinkMicControlBar {

    // <editor-fold defaultstate="collapsed" desc="??????">

    private static final String TAG = PLVLCLinkMicControlBar.class.getSimpleName();
    //????????????????????????????????????
    private static final int DURATION_MS_LINK_MIC_OPEN_OFF = 300;
    //?????????????????????
    private static final int DELAY_AUTO_HIDE_WHEN_NOT_JOINED = 5000;
    private static final int DELAY_AUTO_HIDE_WHEN_JOINED = 3000;
    //????????????????????????y??????
    private static final int DP_ORIGIN_MARGIN_TOP_PORTRAIT = 466;

    //View
    //--??????
    private PLVTouchFloatingView floatingViewPortraitRoot;
    private PLVLCLinkMicRingButton btnRingActionPortrait;
    private TextView tvRequestTip;
    private PLVNoConsumeTouchEventButton btnCameraOpenPortrait;
    private PLVNoConsumeTouchEventButton btnCameraFrontBackPortrait;
    private PLVNoConsumeTouchEventButton btnMicrophoneOpenPortrait;
    private PLVNoConsumeTouchEventButton btnCollapsePortrait;
    private LinearLayout ll4BtnParent;
    //--??????
    private LinearLayout llLandscapeRoot;
    private Button btnMicrophoneOpenLandscape;
    private Button btnCameraOpenLandscape;
    private Button btnCameraFrontBackLandscape;
    private LinearLayout llFunctionBtnParentLand;
    private Button btnRingActionLandscape;
    private TextView tvRequestTipLandscape;

    //3??????????????????
    //??????????????????????????????????????????????????????????????????????????????
    private int smallStateWidthPortrait;
    //???????????????????????????????????????????????????????????????????????????
    private int middleStateWidthPortrait;
    //?????????????????????????????????????????????????????????
    private int biggestStateWidthPortrait;

    //State
    private PLVLCLinkMicControllerState state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE;

    private boolean isCameraOpen = false;
    private boolean isCameraFront = true;
    private boolean isMicrophoneOpen = true;
    private boolean isPortrait;

    //Listener
    private OnPLCLinkMicControlBarListener onPLCLinkMicControlBarListener;

    //Disposable
    private Disposable autoHideDisposable;

    //???????????????????????????
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            if (PLVNetworkUtils.isConnected(context)) {
                PLVCommonLog.d(TAG, "net work connected");
            } else {
                PLVCommonLog.d(TAG, "net work disconnected");
                onNetworkDisconnected();
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    public PLVLCLinkMicControlBar(Context context) {
        this(context, null);
    }

    public PLVLCLinkMicControlBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLinkMicControlBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initBroadcastReceiver(context);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_linkmic_controller_layout, this, true);

        //????????????
        floatingViewPortraitRoot = findViewById(R.id.plvlc_linkmic_controller_floating_view_portrait_root);
        btnRingActionPortrait = findViewById(R.id.plvlc_linkmic_controlBar_btn_ring_action);
        tvRequestTip = findViewById(R.id.plvlc_linkmic_controlBar_tv_request_tip);
        btnCameraOpenPortrait = findViewById(R.id.plvlc_linkmic_controlBar_btn_camera_open);
        btnCameraFrontBackPortrait = findViewById(R.id.plvlc_linkmic_controlBar_btn_camera_front_back);
        btnMicrophoneOpenPortrait = findViewById(R.id.plvlc_linkmic_controlBar_btn_microphone_open);
        btnCollapsePortrait = findViewById(R.id.plvlc_linkmic_controlBar_btn_collapse);
        ll4BtnParent = findViewById(R.id.plvlc_linkmic_controlBar_ll_4_btn_parent);
        //??????
        llFunctionBtnParentLand = findViewById(R.id.plvlc_linkmic_controlBar_ll_function_btn_parent);
        btnMicrophoneOpenLandscape = findViewById(R.id.plvlc_linkmic_controlBar_btn_microphone_open_landscape);
        btnCameraOpenLandscape = findViewById(R.id.plvlc_linkmic_controlBar_btn_camera_open_landscape);
        btnCameraFrontBackLandscape = findViewById(R.id.plvlc_linkmic_controlBar_btn_camera_front_back_landscape);
        btnRingActionLandscape = findViewById(R.id.plvlc_linkmic_controlBar_btn_setting_landscape);
        llLandscapeRoot = findViewById(R.id.plvlc_linkmic_controlBar_ll_landscape_root);
        tvRequestTipLandscape = findViewById(R.id.plvlc_linkmic_controlBar_tv_request_tip_landscape);

        //???????????????????????????
        floatingViewPortraitRoot.setIsInterceptTouchEvent(false);
        floatingViewPortraitRoot.enableHorizontalDrag(false);

        //??????3?????????
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams btnSettingLP = (MarginLayoutParams) btnRingActionPortrait.getLayoutParams();
                smallStateWidthPortrait = btnSettingLP.leftMargin + btnRingActionPortrait.getWidth() + btnSettingLP.rightMargin;
                MarginLayoutParams tvRequestTipLp = (MarginLayoutParams) tvRequestTip.getLayoutParams();
                middleStateWidthPortrait = btnSettingLP.leftMargin + btnRingActionPortrait.getWidth() +
                        tvRequestTipLp.leftMargin + tvRequestTip.getWidth() + tvRequestTipLp.rightMargin;
                biggestStateWidthPortrait = floatingViewPortraitRoot.getWidth();

                floatingViewPortraitRoot.setTranslationX(biggestStateWidthPortrait);
            }
        });

        //??????????????????View??????????????????????????????
        btnRingActionPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCollapsePortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCameraFrontBackPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnCameraOpenPortrait.setShareTouchEventView(floatingViewPortraitRoot);
        btnMicrophoneOpenPortrait.setShareTouchEventView(floatingViewPortraitRoot);

        //???????????????????????????
        setPortraitClickListener();
        //???????????????????????????
        setLandscapeClickListener();
        //???????????????????????????
        floatingViewPortraitRoot.setInitLocation(0, PLVScreenUtils.dip2px(DP_ORIGIN_MARGIN_TOP_PORTRAIT), 0, 0);

        //??????????????????
        isPortrait = PLVScreenUtils.isPortrait(getContext());
        setOrientation(isPortrait);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????????????????">
    private void initBroadcastReceiver(final Context context) {
        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            activity.getLifecycle().addObserver(new GenericLifecycleObserver() {
                @Override
                public void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
                    if (event.equals(Lifecycle.Event.ON_DESTROY)) {
                        context.unregisterReceiver(receiver);
                    }
                }
            });
        } else {
            PLVCommonLog.e(TAG, "context not instance of AppCompatActivity, in danger of leak broadcast receiver");
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????API - ???????????????????????????">

    //???????????????
    @Override
    public void setOnPLCLinkMicControlBarListener(OnPLCLinkMicControlBarListener onPLCLinkMicControlBarListener) {
        this.onPLCLinkMicControlBarListener = onPLCLinkMicControlBarListener;
    }

    //?????????????????????????????????
    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        if (isTeacherOpenLinkMic) {
            if (state != PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
                return;
            }
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN;

            //????????????UI
            btnRingActionPortrait.setRingUpState();
            tvRequestTip.setVisibility(VISIBLE);
            tvRequestTip.setText(R.string.plv_linkmic_tip_request_link_mic);
            ll4BtnParent.setVisibility(INVISIBLE);

            //????????????UI
            btnRingActionLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_up);
            llFunctionBtnParentLand.setVisibility(GONE);
            tvRequestTipLandscape.setVisibility(VISIBLE);
            tvRequestTipLandscape.setText(R.string.plv_linkmic_tip_request_link_mic);

            //?????????????????????UI
            setOrientation(isPortrait);

            startAutoHideCountDown();
        } else {
            if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
                return;
            }
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE;
            llLandscapeRoot.setVisibility(GONE);
            stopAutoHideCountDown();
        }

        animateLinkMicOpenOrClose(isTeacherOpenLinkMic);
    }

    @Override
    public void setCameraOpenOrClose(boolean toOpen) {
        setBtnCameraOpenState(toOpen);
    }

    @Override
    public void setMicrophoneOpenOrClose(boolean toOpen) {
        setBtnMicrophoneOpenState(toOpen);
    }

    //??????????????????
    @Override
    public void setJoinLinkMicSuccess() {
        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS;
        isCameraOpen = false;
        isCameraFront = true;
        isMicrophoneOpen = true;

        animateMoveToShowBiggestWidth();

        //????????????UI
        tvRequestTip.setVisibility(INVISIBLE);
        ll4BtnParent.setVisibility(VISIBLE);

        //????????????UI
        tvRequestTipLandscape.setVisibility(GONE);
        llFunctionBtnParentLand.setVisibility(VISIBLE);

        startAutoHideCountDown();
    }

    @Override
    public void setIsAudio(boolean isAudio) {
        if (isAudio) {
            btnCameraOpenPortrait.setVisibility(GONE);
            btnCameraOpenLandscape.setVisibility(GONE);
            btnCameraFrontBackPortrait.setVisibility(GONE);
            btnCameraFrontBackLandscape.setVisibility(GONE);
        } else {
            btnCameraOpenPortrait.setVisibility(VISIBLE);
            btnCameraOpenLandscape.setVisibility(VISIBLE);
            btnCameraFrontBackPortrait.setVisibility(VISIBLE);
            btnCameraFrontBackLandscape.setVisibility(VISIBLE);
        }
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        post(new Runnable() {
            @Override
            public void run() {
                biggestStateWidthPortrait = floatingViewPortraitRoot.getWidth();
            }
        });
    }

    //????????????
    @Override
    public void setLeaveLinkMic() {
        isCameraOpen = false;
        isCameraFront = true;
        isMicrophoneOpen = true;

        //????????????UI
        tvRequestTip.setVisibility(VISIBLE);
        tvRequestTip.setText(R.string.plv_linkmic_tip_request_link_mic);
        btnRingActionPortrait.setRingUpState();
        ll4BtnParent.setVisibility(INVISIBLE);

        //????????????UI
        btnRingActionLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_up);
        tvRequestTipLandscape.setVisibility(VISIBLE);
        tvRequestTipLandscape.setText(R.string.plv_linkmic_tip_request_link_mic);
        llFunctionBtnParentLand.setVisibility(GONE);

        if (state != PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
            state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN;
            animateMoveToShowMiddleWidth();
            startAutoHideCountDown();
        }
    }

    @Override
    public void show() {
        PLVCommonLog.d(TAG, "show");
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        PLVCommonLog.d(TAG, "hide");
        setVisibility(INVISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????">
    //???????????????????????????
    private void animateLinkMicOpenOrClose(boolean open) {
        //???????????????????????????
        if (open) {
            animateMoveToShowMiddleWidth();
        } else {
            animateMoveToHide();
        }

        //???????????????????????????
        tipGradientShowOrHide(open, DURATION_MS_LINK_MIC_OPEN_OFF);
    }

    private void animateMoveToShowBiggestWidth() {
        animateMove(0);
    }

    private void animateMoveToShowMiddleWidth() {
        animateMove((float) (biggestStateWidthPortrait - middleStateWidthPortrait));
    }

    private void animateMoveToShowSmallestWidth() {
        animateMove((float) (biggestStateWidthPortrait - smallStateWidthPortrait));
    }

    private void animateMoveToHide() {
        animateMove(biggestStateWidthPortrait);
    }

    private void animateMove(float translationX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingViewPortraitRoot, "translationX",
                floatingViewPortraitRoot.getTranslationX(), translationX);
        animator.setDuration(DURATION_MS_LINK_MIC_OPEN_OFF);
        animator.start();
    }


    //????????????????????????
    private void tipGradientShowOrHide(boolean show, int duration) {
        ObjectAnimator objectAnimator;
        if (show) {
            tvRequestTip.setVisibility(VISIBLE);
            objectAnimator = ObjectAnimator.ofFloat(tvRequestTip, "alpha", 0, 1);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(tvRequestTip, "alpha", 1, 0);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvRequestTip.setVisibility(INVISIBLE);
                    tvRequestTip.setAlpha(1);
                }
            });
        }
        objectAnimator.setDuration(duration);
        objectAnimator.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    //???????????????????????????
    private void startAutoHideCountDown() {
        dispose(autoHideDisposable);
        int delay;
        if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN) {
            delay = DELAY_AUTO_HIDE_WHEN_NOT_JOINED;
        } else if (state == PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS) {
            delay = DELAY_AUTO_HIDE_WHEN_JOINED;
        } else {
            return;
        }
        autoHideDisposable = PLVRxTimer.delay(delay, new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                animateMoveToShowSmallestWidth();
                tipGradientShowOrHide(false, DURATION_MS_LINK_MIC_OPEN_OFF);
                switch (state) {
                    case STATE_TEACHER_LINK_MIC_OPEN:
                        state = PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE;
                        break;
                    case STATE_JOIN_LINK_MIC_SUCCESS:
                        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE;
                        btnRingActionPortrait.setRingSettingState();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //??????????????????
    private void stopAutoHideCountDown() {
        dispose(autoHideDisposable);
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        post(new Runnable() {
            @Override
            public void run() {
                startAutoHideCountDown();
            }
        });
        return super.onInterceptTouchEvent(ev);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setOrientation(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    private void setOrientation(boolean isPortrait) {
        //???????????????????????????????????????????????????????????????
        if (state == PLVLCLinkMicControllerState.STATE_TEACHER_LINK_MIC_CLOSE) {
            return;
        }
        if (isPortrait) {
            floatingViewPortraitRoot.setVisibility(VISIBLE);
            //???????????????????????????????????????????????????????????????????????????
            if (state.ordinal() >= PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS.ordinal()) {
                clickRingSetting();
            }
            llLandscapeRoot.setVisibility(GONE);
        } else {
            floatingViewPortraitRoot.setVisibility(GONE);
            llLandscapeRoot.setVisibility(VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">
    private void setPortraitClickListener() {
        //????????????
        btnRingActionPortrait.setOnLinkMicRingButtonClickListener(new PLVLCLinkMicRingButton.OnPLVLCLinkMicRingButtonClickListener() {
            @Override
            public void onClickRingUp() {
                if (!PLVNetworkUtils.isConnected(getContext())) {
                    PLVCommonLog.w(TAG, "net work not available");
                    return;
                }
                if (toastWhenFloatingPlayerShowing()) {
                    return;
                }
                btnRingActionPortrait.setRingOffState();
                tvRequestTip.setText(R.string.plv_linkmic_tip_requesting_link_mic);

                btnRingActionLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_off);
                tvRequestTipLandscape.setText(R.string.plv_linkmic_tip_requesting_link_mic);
                switch (state) {
                    case STATE_TEACHER_LINK_MIC_OPEN:
                        PLVCommonLog.d(TAG, "btnSetting.onClickRingUp->STATE_TEACHER_LINK_MIC_OPEN");
                        break;
                    case STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE:
                        //???????????????????????????
                        animateMoveToShowMiddleWidth();
                        tipGradientShowOrHide(true, DURATION_MS_LINK_MIC_OPEN_OFF);
                        PLVCommonLog.d(TAG, "btnSetting.onClickRingUp->STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE");
                        break;
                    default:
                        break;
                }
                state = PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC;

                // ??????????????????????????????
                PLVDependManager.getInstance().get(PLVLCFloatingWindow.class).showByUser(false);

                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickRingUpLinkMic();
                }
            }

            @Override
            public void onClickRingOff() {
                PLVDialogFactory.createConfirmDialog(
                        getContext(),
                        getResources().getString(R.string.plv_linkmic_dialog_hang_off_confirm_ask),
                        getResources().getString(R.string.plv_linkmic_dialog_hang_off),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                IPLVLinkMicTraceLogSender iplvLinkMicTraceLogSender = new PLVLinkMicTraceLogSender();
                                iplvLinkMicTraceLogSender.setLogModuleClass(PLVLinkMicELog.class);
                                if (state.equals(PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC)) {
                                    iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.USER_CANCEL_LINK_MIC, "waitingUserDidCancelLinkMic???state???" + state);
                                } else {
                                    iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.USER_CLOSE_LINK_MIC, "joinedUserDidCloseLinkMic???state???" + state);
                                }
                                handleRingOff();
                                dialog.dismiss();
                            }
                        }
                ).show();
            }

            @Override
            public void onClickRingSetting() {
                clickRingSetting();
            }
        });
        //???????????????
        btnCameraOpenPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnCameraOpenState(!isCameraOpen);
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickCameraOpenOrClose(!isCameraOpen);
                }
            }
        });
        //???????????????
        btnCameraFrontBackPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isCameraFront = !isCameraFront;
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickCameraFrontOfBack(isCameraFront);
                }
            }
        });
        //???????????????
        btnMicrophoneOpenPortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnMicrophoneOpenState(!isMicrophoneOpen);
                if (onPLCLinkMicControlBarListener != null) {
                    onPLCLinkMicControlBarListener.onClickMicroPhoneOpenOrClose(!isMicrophoneOpen);
                }
            }
        });
        btnCollapsePortrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateMoveToShowSmallestWidth();
                tipGradientShowOrHide(false, DURATION_MS_LINK_MIC_OPEN_OFF);
                btnRingActionPortrait.setRingSettingState();
                state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE;
                startAutoHideCountDown();
            }
        });
    }

    private void clickRingSetting() {
        btnRingActionPortrait.setRingOffState();
        animateMoveToShowBiggestWidth();
        state = PLVLCLinkMicControllerState.STATE_JOIN_LINK_MIC_SUCCESS;
        startAutoHideCountDown();
    }

    private void setBtnCameraOpenState(boolean toOpen) {
        isCameraOpen = toOpen;
        if (isCameraOpen) {
            btnCameraOpenPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_open);
            btnCameraOpenLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_open);
            btnCameraFrontBackPortrait.setEnabled(true);
            btnCameraFrontBackLandscape.setEnabled(true);
            btnCameraFrontBackPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_front_back_enabled);
            btnCameraFrontBackLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_front_back_enabled);
        } else {
            btnCameraOpenPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_close);
            btnCameraOpenLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_close);
            btnCameraFrontBackPortrait.setEnabled(false);
            btnCameraFrontBackLandscape.setEnabled(false);
            btnCameraFrontBackPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_front_back_disabled);
            btnCameraFrontBackLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_camera_front_back_disabled);
        }
    }

    private void setBtnMicrophoneOpenState(boolean toOpen) {
        isMicrophoneOpen = toOpen;
        if (isMicrophoneOpen) {
            btnMicrophoneOpenPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_microphone_open);
            btnMicrophoneOpenLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_microphone_open);
        } else {
            btnMicrophoneOpenPortrait.setBackgroundResource(R.drawable.plvlc_linkmic_iv_microphone_close);
            btnMicrophoneOpenLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_microphone_close);
        }
    }

    private void setLandscapeClickListener() {
        btnMicrophoneOpenLandscape.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMicrophoneOpenPortrait.performClick();
            }
        });
        btnCameraOpenLandscape.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCameraOpenPortrait.performClick();
            }
        });
        btnCameraFrontBackLandscape.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCameraFrontBackPortrait.performClick();
            }
        });
        btnRingActionLandscape.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRingActionPortrait.performClick();
            }
        });
    }

    //???????????????????????????
    private void handleRingOff() {
        setLeaveLinkMic();
        btnRingActionLandscape.setBackgroundResource(R.drawable.plvlc_linkmic_iv_ring_up);
        if (onPLCLinkMicControlBarListener != null) {
            onPLCLinkMicControlBarListener.onClickRingOffLinkMic();
        }
        startAutoHideCountDown();
    }

    private boolean toastWhenFloatingPlayerShowing() {
        if (PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing()) {
            PLVToast.Builder.context(getContext())
                    .setText("?????????????????????????????????")
                    .show();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    private void onNetworkDisconnected() {
        //????????????????????????????????????????????????????????????
        if (state == PLVLCLinkMicControllerState.STATE_REQUESTING_JOIN_LINK_MIC) {
            handleRingOff();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ?????????????????????">

    /**
     * ?????????????????????
     */
    private enum PLVLCLinkMicControllerState {
        /**
         * ??????????????????
         */
        STATE_TEACHER_LINK_MIC_CLOSE,

        /**
         * ??????????????????
         */
        STATE_TEACHER_LINK_MIC_OPEN,

        /**
         * ?????????????????????????????????
         */
        STATE_TEACHER_LINK_MIC_OPEN_COLLAPSE,

        /**
         * ??????????????????
         */
        STATE_REQUESTING_JOIN_LINK_MIC,

        /**
         * ??????????????????
         */
        STATE_JOIN_LINK_MIC_SUCCESS,

        /**
         * ?????????????????????????????????
         */
        STATE_JOIN_LINK_MIC_SUCCESS_COLLAPSE,
    }

    // </editor-fold>
}
