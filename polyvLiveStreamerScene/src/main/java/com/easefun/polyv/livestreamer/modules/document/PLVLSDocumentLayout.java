package com.easefun.polyv.livestreamer.modules.document;

import static com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter.AUTO_ID_WHITE_BOARD;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotNull;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.document.contract.IPLVDocumentContract;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMode;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVPlaceHolderView;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebProcessor;
import com.easefun.polyv.livescenes.document.PLVSDocumentWebView;
import com.easefun.polyv.livescenes.document.model.PLVSPPTPaintStatus;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentControllerExpandMenu;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentControllerLayout;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentInputWidget;
import com.easefun.polyv.livestreamer.modules.streamer.position.PLVLSStreamerViewPositionManager;
import com.easefun.polyv.livestreamer.modules.streamer.position.vo.PLVLSStreamerViewPositionUiState;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ????????????
 */
public class PLVLSDocumentLayout extends FrameLayout implements IPLVLSDocumentLayout {

    // <editor-fold defaultstate="collapsed" desc="??????">

    // ???View
    private View rootView;
    private PLVSwitchViewAnchorLayout documentSwitchAnchorLayout;
    private PLVSDocumentWebView plvlsDocumentWebView;
    private PLVLSDocumentControllerLayout plvlsDocumentControllerLayout;
    private FrameLayout plvlsDocumentNoSelectPptLayout;
    private PLVPlaceHolderView plvlsPlaceHolderView;
    private PLVRoundRectGradientTextView documentZoomValueHintTv;

    // ?????????????????????????????? ????????????
    private PLVLSDocumentInputWidget plvlsDocumentInputWidget;
    // ????????????????????????
    private PLVConfirmDialog plvClearMarkConfirmWindow;

    private final PLVLSStreamerViewPositionManager streamerViewPositionManager = PLVDependManager.getInstance().get(PLVLSStreamerViewPositionManager.class);

    /**
     * MVP - View
     * ???????????????????????????????????????gc???????????????????????????Presenter??????
     */
    private PLVAbsDocumentView documentMvpView;

    // MVP - Presenter
    private IPLVDocumentContract.Presenter documentPresenter;

    // ?????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // ??????????????????????????????????????????
    private OnSwitchFullScreenListener onSwitchFullScreenListener;

    @Nullable
    private PLVUserAbilityManager.OnUserAbilityChangedListener onUserAbilityChangeCallback;

    // ??????PPT??????ID
    private int autoId;
    // ????????????????????????????????????ID
    private int lastOpenNotWhiteBoardAutoId;

    // ?????????????????????????????????????????????
    private ConstraintLayout.LayoutParams smallScreenLp = null;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    public PLVLSDocumentLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSDocumentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSDocumentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????">
    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_layout, this);
        findView();
        initLayoutSize();
        initMvpView();
        initOnUserAbilityChangeListener();

        streamerViewPositionManager.updateDocumentAnchorLayout(documentSwitchAnchorLayout);
        observeDocumentPosition();
        observeSwitchPositionToUpdateViewSize();
    }

    private void findView() {
        documentSwitchAnchorLayout = findViewById(R.id.plvls_document_switch_anchor_layout);
        plvlsDocumentWebView = (PLVSDocumentWebView) rootView.findViewById(R.id.plvls_document_web_view);
        plvlsDocumentControllerLayout = (PLVLSDocumentControllerLayout) rootView.findViewById(R.id.plvls_document_controller_layout);
        plvlsDocumentNoSelectPptLayout = (FrameLayout) rootView.findViewById(R.id.plvls_document_no_select_ppt_layout);
        plvlsPlaceHolderView = rootView.findViewById(R.id.plvls_document_placeholder_view);
        documentZoomValueHintTv = findViewById(R.id.plvls_document_zoom_value_hint_tv);
    }

    /**
     * ??????????????????????????????????????????
     */
    private void initLayoutSize() {
        post(new Runnable() {
            @Override
            public void run() {
                int landscapeScreenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                int landscapeScreenHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                //?????????????????????????????????
                int documentLayoutHeight = landscapeScreenHeight - ConvertUtils.dp2px(54);
                //?????????????????????????????????
                int documentLayoutWidth = documentLayoutHeight * 16 / 9;
                //???????????????????????????????????????
                int minLinkMicLayoutWidth = ConvertUtils.dp2px(138);
                //????????????
                int layoutPadding = ConvertUtils.dp2px(16 + 16 + 8);
                if (documentLayoutWidth + minLinkMicLayoutWidth + layoutPadding > landscapeScreenWidth) {
                    documentLayoutWidth = landscapeScreenWidth - minLinkMicLayoutWidth - layoutPadding;
                }
                //???????????????????????????
                ViewGroup.LayoutParams vlp = getLayoutParams();
                vlp.width = documentLayoutWidth;
                setLayoutParams(vlp);
            }
        });
    }

    /**
     * ????????? MVP??????
     */
    private void initMvpView() {
        documentMvpView = new PLVAbsDocumentView() {

            @Override
            public void onSwitchShowMode(PLVDocumentMode showMode) {
                if (showMode == PLVDocumentMode.WHITEBOARD) {
                    plvlsDocumentNoSelectPptLayout.setVisibility(GONE);
                    PLVDocumentPresenter.getInstance().enableMarkTool(true);
                } else {
                    if (PLVLSDocumentLayout.this.autoId == 0 && lastOpenNotWhiteBoardAutoId == 0) {
                        // ????????????????????????????????????????????????PPT????????????????????????
                        plvlsDocumentNoSelectPptLayout.setVisibility(VISIBLE);
                        PLVDocumentPresenter.getInstance().enableMarkTool(false);
                    }
                }
            }

            @Override
            public void onPptPageChange(int autoId, int pageId) {
                PLVLSDocumentLayout.this.autoId = autoId;
                if (autoId != 0) {
                    plvlsDocumentNoSelectPptLayout.setVisibility(GONE);
                    PLVDocumentPresenter.getInstance().enableMarkTool(true);
                    lastOpenNotWhiteBoardAutoId = autoId;
                }
            }

            @Override
            public void onPptPaintStatus(@Nullable PLVSPPTPaintStatus pptPaintStatus) {
                //???????????????
                ViewGroup parent = (ViewGroup) rootView;
                if (plvlsDocumentInputWidget == null) {
                    plvlsDocumentInputWidget = new PLVLSDocumentInputWidget(getContext());
                }

                ViewGroup.LayoutParams layoutParams =
                        new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                parent.addView(plvlsDocumentInputWidget, layoutParams);
                plvlsDocumentInputWidget.setText(pptPaintStatus);
            }

            @Override
            public void onZoomValueChanged(String zoomValue) {
                documentZoomValueHintTv.setText(zoomValue + "%");
                PLVViewUtil.showViewForDuration(documentZoomValueHintTv, TimeUnit.SECONDS.toMillis(3));
            }
        };

        PLVDocumentPresenter.getInstance().registerView(documentMvpView);
    }

    /**
     * ???????????????????????????????????????
     */
    private void initOnUserAbilityChangeListener() {
        this.onUserAbilityChangeCallback = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????
                    if (PLVLSDocumentLayout.this.autoId == AUTO_ID_WHITE_BOARD) {
                        PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.WHITEBOARD);
                    }
                }
                updateDocumentConsumeTouchEvent();
            }

            private void updateDocumentConsumeTouchEvent() {
                if (plvlsDocumentWebView != null) {
                    plvlsDocumentWebView.setNeedGestureAction(PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_USE_PAINT));
                }
            }
        };

        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(onUserAbilityChangeCallback));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????????????????????">

    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        initDocumentWebView();
        initPresenter();
        initDocumentController();

        //????????????????????????
        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
            plvlsPlaceHolderView.setPlaceHolderText(getContext().getString(R.string.document_no_live_please_wait));
            plvlsPlaceHolderView.enableRespondLocationSensor(false);
            plvlsPlaceHolderView.setVisibility(VISIBLE);
        }

        // ??????????????????????????????
        PLVDocumentPresenter.getInstance().switchShowMode(PLVDocumentMode.WHITEBOARD);
    }

    /**
     * ???????????????Webview
     */
    private void initDocumentWebView() {
        // ????????????????????????????????????
        plvlsDocumentWebView.setNeedDarkBackground(true);
        plvlsDocumentWebView.loadWeb();
    }

    /**
     * ????????? MVP - Presenter
     */
    private void initPresenter() {
        documentPresenter = PLVDocumentPresenter.getInstance();
        documentPresenter.init((LifecycleOwner) getContext(), liveRoomDataManager, new PLVSDocumentWebProcessor(plvlsDocumentWebView));
    }

    /**
     * ??????????????????
     */
    private void initDocumentController() {
        // ????????????????????????????????????????????????
        plvlsDocumentControllerLayout.initMarkToolAndColor();
        // ???????????????????????????
        plvlsDocumentControllerLayout.show();

        plvlsDocumentControllerLayout.setOnChangeColorListener(new PLVLSDocumentControllerLayout.OnChangeColorListener() {
            @Override
            public void onChangeColor(String colorString) {
                documentPresenter.changeColor(colorString);
            }
        });

        plvlsDocumentControllerLayout.setOnChangeMarkToolListener(new PLVLSDocumentControllerLayout.OnChangeMarkToolListener() {
            @Override
            public void onChangeMarkTool(@PLVDocumentMarkToolType.Range final String markToolType) {
                if (PLVDocumentMarkToolType.CLEAR.equals(markToolType)) {
                    if (plvClearMarkConfirmWindow == null) {
                        plvClearMarkConfirmWindow = PLVLSConfirmDialog.Builder.context(getContext())
                                .setTitleVisibility(GONE)
                                .setContent("????????????????????????????????????????????????")
                                .setLeftButtonText("?????????")
                                .setRightButtonText("??????")
                                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        plvClearMarkConfirmWindow.hide();
                                    }
                                })
                                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        documentPresenter.changeMarkToolType(markToolType);
                                        plvClearMarkConfirmWindow.hide();
                                    }
                                })
                                .build();
                    }
                    plvClearMarkConfirmWindow.show();
                } else {
                    documentPresenter.changeMarkToolType(markToolType);
                }
            }
        });

        plvlsDocumentControllerLayout.setOnChangePptPageListener(new PLVLSDocumentControllerLayout.OnChangePptPageListener() {
            @Override
            public void onChangePage(int pageId) {
                if (autoId == AUTO_ID_WHITE_BOARD) {
                    documentPresenter.changeWhiteBoardPage(pageId);
                } else {
                    documentPresenter.changePptPage(autoId, pageId);
                }
            }
        });

        plvlsDocumentControllerLayout.setSwitchFullScreenListener(new PLVLSDocumentControllerLayout.SwitchFullScreenListener() {
            @Override
            public boolean switchFullScreen() {
                return PLVLSDocumentLayout.this.switchScreen();
            }
        });
    }

    private void observeDocumentPosition() {
        streamerViewPositionManager.getDocumentInMainScreenLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVLSStreamerViewPositionUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVLSStreamerViewPositionUiState viewPositionUiState) {
                        if (viewPositionUiState == null) {
                            return;
                        }
                        final float placeHolderTextSize = viewPositionUiState.isDocumentInMainScreen() ? 16 : 10;
                        plvlsPlaceHolderView.setPlaceHolderTextSize(placeHolderTextSize);
                    }
                });
    }

    private void observeSwitchPositionToUpdateViewSize() {
        documentSwitchAnchorLayout.setOnSwitchListener(new PLVSwitchViewAnchorLayout.IPLVSwitchViewAnchorLayoutListener() {
            @Override
            protected void onSwitchElsewhereAfter() {
                updateViewSize();
            }

            @Override
            protected void onSwitchBackAfter() {
                updateViewSize();
            }

            private void updateViewSize() {
                final View child = firstNotNull(
                        documentSwitchAnchorLayout.findViewById(R.id.plvls_document_layout_container),
                        documentSwitchAnchorLayout.findViewById(R.id.plvls_streamer_round_rect_ly)
                );
                if (child == null) {
                    return;
                }
                final ViewGroup.LayoutParams lp = child.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                child.setLayoutParams(lp);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    @Override
    public boolean isFullScreen() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        return lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    public void onSelectUploadDocument(Intent intent) {
        if (intent == null) {
            return;
        }
        PLVDocumentPresenter.getInstance().onSelectUploadFile(intent.getData());
    }

    @Override
    public void setStreamerStatus(boolean isStartedStatus) {
        PLVDocumentPresenter.getInstance().notifyStreamStatus(isStartedStatus);
    }

    @Override
    public boolean onBackPressed() {
        if (isFullScreen()) {
            switchScreen();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        PLVDocumentPresenter.getInstance().destroy();
        onSwitchFullScreenListener = null;
        onUserAbilityChangeCallback = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">

    /**
     * ?????? ??????/?????? ????????????
     *
     * @return true??????????????????false?????????????????????
     */
    private boolean switchScreen() {
        boolean toFullScreen = !isFullScreen();
        if (toFullScreen) {
            switchToFullScreen();
        } else {
            switchToSmallScreen();
        }

        if (onSwitchFullScreenListener != null) {
            onSwitchFullScreenListener.onSwitchFullScreen(toFullScreen);
        }
        if (plvlsDocumentControllerLayout != null) {
            plvlsDocumentControllerLayout.notifyDocumentLayoutSizeChange(toFullScreen);
        }

        return toFullScreen;
    }

    private void switchToFullScreen() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) getLayoutParams();
        // ??????????????????????????????????????????
        smallScreenLp = new ConstraintLayout.LayoutParams(lp);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        lp.topMargin = 0;
        lp.leftMargin = 0;
        lp.bottomMargin = 0;
        setLayoutParams(lp);
    }

    private void switchToSmallScreen() {
        if (smallScreenLp != null) {
            setLayoutParams(smallScreenLp);
        }
        // ??????????????????????????????????????????????????????????????????UI??????
        plvlsDocumentControllerLayout.closeMarkToolMenu();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????????">

    @Override
    public void setMarkToolOnFoldExpandListener(PLVLSDocumentControllerExpandMenu.OnFoldExpandListener onFoldExpandListener) {
        plvlsDocumentControllerLayout.setMarkToolMenuOnFoldExpandListener(onFoldExpandListener);
    }

    @Override
    public void setOnSwitchFullScreenListener(OnSwitchFullScreenListener onSwitchFullScreenListener) {
        this.onSwitchFullScreenListener = onSwitchFullScreenListener;
    }

    @Override
    public IPLVStreamerContract.IStreamerView getDocumentLayoutStreamerView() {
        return new PLVAbsStreamerView() {
            @Override
            public void onStreamLiveStatusChanged(boolean isLive) {
                String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
                if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                    plvlsPlaceHolderView.setVisibility(isLive ? GONE: VISIBLE);
                }
            }
        };
    }

    /**
     * ??????????????????
     */
    public interface OnSwitchFullScreenListener {

        /**
         * ???????????????/????????????
         *
         * @param toFullScreen true??????????????????false?????????????????????
         */
        void onSwitchFullScreen(boolean toFullScreen);

    }

    // </editor-fold>
}
