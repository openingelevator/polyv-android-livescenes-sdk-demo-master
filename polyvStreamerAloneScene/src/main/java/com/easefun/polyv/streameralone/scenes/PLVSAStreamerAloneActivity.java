package com.easefun.polyv.streameralone.scenes;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.di.PLVBeautyModule;
import com.easefun.polyv.livecommon.module.modules.beauty.helper.PLVBeautyInitHelper;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchViewPager;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.beauty.IPLVSABeautyLayout;
import com.easefun.polyv.streameralone.modules.beauty.PLVSABeautyLayout;
import com.easefun.polyv.streameralone.modules.liveroom.IPLVSASettingLayout;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSACleanUpLayout;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSALinkMicRequestTipsLayout;
import com.easefun.polyv.streameralone.modules.streamer.IPLVSAStreamerLayout;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerFinishLayout;
import com.easefun.polyv.streameralone.modules.streamer.PLVSAStreamerFullscreenLayout;
import com.easefun.polyv.streameralone.scenes.fragments.PLVSAEmptyFragment;
import com.easefun.polyv.streameralone.scenes.fragments.PLVSAStreamerHomeFragment;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * ?????????????????????????????????
 * ????????????????????????????????????????????????
 */
public class PLVSAStreamerAloneActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="??????">

    private static final String TAG = PLVSAStreamerAloneActivity.class.getSimpleName();

    // ?????? - ??????????????????????????????
    private static final String EXTRA_CHANNEL_ID = "channelId"; // ?????????
    private static final String EXTRA_VIEWER_ID = "viewerId";   // ?????????Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // ???????????????
    private static final String EXTRA_AVATAR_URL = "avatarUrl"; // ???????????????url
    private static final String EXTRA_ACTOR = "actor";  // ???????????????
    private static final String EXTRA_CHANNEL_NAME = "channelName";//???????????????
    private static final String EXTRA_USERTYPE = "usertype";                // ???????????????
    private static final String EXTRA_COLIN_MIC_TYPE = "colinMicType";      // ??????????????????

    // ????????????
    private static final int RES_BACKGROUND_PORT = R.drawable.plvsa_streamer_page_bg;
    private static final int RES_BACKGROUND_LAND = R.drawable.plvsa_streamer_page_bg_land;

    // ???????????????????????????????????????????????????????????????
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // view
    // ?????????
    private ConstraintLayout plvsaRootLayout;
    // ?????????????????????
    private IPLVSAStreamerLayout streamerLayout;
    // ??????????????????
    private IPLVSASettingLayout settingLayout;
    // ??????????????????
    @Nullable
    private PLVSACleanUpLayout cleanUpLayout;
    //????????????
    private PLVSAStreamerFullscreenLayout fullscreenLayout;

    // ??????????????????
    private PLVSAStreamerFinishLayout streamerFinishLayout;
    // ??????????????????viewpager??????
    private PLVNoInterceptTouchViewPager topLayerViewPager;
    // ????????????????????? ?????????????????????
    private PLVSALinkMicRequestTipsLayout linkMicRequestTipsLayout;
    // ????????????
    private IPLVSABeautyLayout beautyLayout;
    // ??????fragment
    private PLVSAStreamerHomeFragment homeFragment;
    // ??????fragment
    private PLVSAEmptyFragment emptyFragment;

    private Group maskGroup;
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
     * @param channelName   ???????????????
     * @param usertype      ???????????????
     * @param colinMicType  ??????????????????
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
                                                 @NonNull String channelName,
                                                 @NonNull String usertype,
                                                 @NonNull String colinMicType) {
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
        if (TextUtils.isEmpty(channelName)) {
            return PLVLaunchResult.error("channelName ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(usertype)) {
            return PLVLaunchResult.error("usertype ????????????????????????????????????????????????");
        }
        if (TextUtils.isEmpty(colinMicType)) {
            return PLVLaunchResult.error("colinMicType ????????????????????????????????????????????????");
        }

        Intent intent = new Intent(activity, PLVSAStreamerAloneActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_ACTOR, actor);
        intent.putExtra(EXTRA_CHANNEL_NAME, channelName);
        intent.putExtra(EXTRA_USERTYPE, usertype);
        intent.putExtra(EXTRA_COLIN_MIC_TYPE, colinMicType);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependency();
        setContentView(R.layout.plvsa_streamer_alone_activity);
        setStatusBarColor();

        initParams();
        initLiveRoomManager();
        initView();
        initBeautyModule();

        checkStreamRecover();

        observeSettingLayout();
        observeViewPagerLayout();
        observeStreamerLayout();
        observeCleanUpLayout();
        observeLinkmicRequestLayout();
        observeFullscreenLayout();
        observeBeautyLayoutStatus();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVBeautyInitHelper.getInstance().destroy();
        if (streamerLayout != null) {
            streamerLayout.destroy();
        }
        if (beautyLayout != null) {
            beautyLayout.destroy();
        }
        //last destroy socket
        if (homeFragment != null) {
            homeFragment.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (topLayerViewPager != null) {
            topLayerViewPager.onSuperTouchEvent(event);
        }
        if (streamerLayout != null) {
            streamerLayout.onRvSuperTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (settingLayout != null && settingLayout.onBackPressed()) {
            return;
        } else if (settingLayout != null && settingLayout.isShown()) {
            super.onBackPressed();
            return;
        } else if (homeFragment != null && homeFragment.onBackPressed()) {
            return;
        } else if (streamerLayout != null && streamerLayout.onBackPressed()) {
            return;
        } else if (beautyLayout != null && beautyLayout.onBackPressed()) {
            return;
        } else if (streamerFinishLayout != null && streamerFinishLayout.isShown()) {
            super.onBackPressed();
            return;
        }

        // ?????????????????????????????????
        final boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        String content =  isGuest ? getString(R.string.plv_live_room_dialog_exit_confirm_ask)
                : getString(R.string.plv_live_room_dialog_steamer_exit_confirm_ask);
        new PLVSAConfirmDialog(this)
                .setTitleVisibility(View.GONE)
                .setContent(content)
                .setRightButtonText(R.string.plv_common_dialog_confirm)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        if(isGuest){
                            finish();
                            return;
                        }
                        if (streamerLayout != null && streamerFinishLayout != null) {
                            streamerLayout.stopLive();
                            streamerFinishLayout.show();
                        } else {
                            PLVSAStreamerAloneActivity.super.onBackPressed();
                        }
                    }
                })
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - ????????????">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVBeautyModule.instance);
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
        String channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME);
        String role = intent.getStringExtra(EXTRA_USERTYPE);
        String colinMicType = intent.getStringExtra(EXTRA_COLIN_MIC_TYPE);

        // ??????Config??????
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, avatarUrl, role, actor);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);
        PLVLiveChannelConfigFiller.setupChannelName(channelName);
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
        plvsaRootLayout = findViewById(R.id.plvsa_root_layout);
        streamerLayout = findViewById(R.id.plvsa_streamer_layout);
        settingLayout = findViewById(R.id.plvsa_setting_layout);
        cleanUpLayout = findViewById(R.id.plvsa_clean_up_layout);
        streamerFinishLayout = findViewById(R.id.plvsa_streamer_finish_layout);
        topLayerViewPager = findViewById(R.id.plvsa_top_layer_view_pager);
        linkMicRequestTipsLayout = findViewById(R.id.plvsa_linkmic_request_layout);
        fullscreenLayout = findViewById(R.id.plvsa_fullscreen_view);
        maskGroup = findViewById(R.id.plvsa_mask_group);

        //??????????????????????????????
        streamerLayout.init(liveRoomDataManager);

        //???????????????????????????
        settingLayout.init(liveRoomDataManager);

        // ????????????????????????fragment
        homeFragment = new PLVSAStreamerHomeFragment();
        emptyFragment = new PLVSAEmptyFragment();
        // ??????fragment???????????????ViewPage???
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                topLayerViewPager,
                1,
                emptyFragment,
                homeFragment
        );

        // ?????????????????????
        beautyLayout = new PLVSABeautyLayout(this);
    }

    private void observeBeautyLayoutStatus() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe(this, new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        final boolean isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        maskGroup.setVisibility(isBeautyLayoutShowing ? View.GONE : View.VISIBLE);
                    }
                });
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

    // <editor-fold defaultstate="collapsed" desc="????????????">

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (PLVScreenUtils.isPortrait(this)) {
            plvsaRootLayout.setBackgroundResource(RES_BACKGROUND_PORT);
        } else {
            plvsaRootLayout.setBackgroundResource(RES_BACKGROUND_LAND);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">
    private void observeSettingLayout() {
        settingLayout.setOnViewActionListener(new IPLVSASettingLayout.OnViewActionListener() {
            @Override
            public void onStartLiveAction() {
                getIntent().putExtra(EXTRA_CHANNEL_NAME, liveRoomDataManager.getConfig().getChannelName());
                homeFragment.updateChannelName();
                homeFragment.chatroomLogin();
                topLayerViewPager.setVisibility(View.VISIBLE);
                streamerLayout.startLive();
                //??????????????????????????????????????????
                streamerLayout.getStreamerPresenter().requestMemberList();
            }

            @Override
            public void onEnterLiveAction() {
                //?????????????????????
                getIntent().putExtra(EXTRA_CHANNEL_NAME, liveRoomDataManager.getConfig().getChannelName());
                homeFragment.updateChannelName();
                homeFragment.chatroomLogin();
                topLayerViewPager.setVisibility(View.VISIBLE);
                streamerLayout.enterLive();
                //??????????????????????????????????????????
                streamerLayout.getStreamerPresenter().requestMemberList();
            }

            @Override
            public int getCurrentNetworkQuality() {
                return streamerLayout.getNetworkQuality();
            }

            @Override
            public void setCameraDirection(boolean front) {
                streamerLayout.setCameraDirection(front);
            }

            @Override
            public void setMirrorMode(boolean isMirror) {
                streamerLayout.setMirrorMode(isMirror);
            }

            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return streamerLayout.getBitrateInfo();
            }

            @Override
            public void onBitrateClick(int bitrate) {
                streamerLayout.setBitrate(bitrate);
            }

            @Override
            public IPLVStreamerContract.IStreamerPresenter getStreamerPresenter() {
                if (streamerLayout == null) {
                    return null;
                }
                return streamerLayout.getStreamerPresenter();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ???????????????">
    private void observeStreamerLayout() {
        //??????view?????????????????????
        streamerLayout.setOnViewActionListener(new IPLVSAStreamerLayout.OnViewActionListener() {
            @Override
            public void onRestartLiveAction() {
                recreate();
            }

            @Override
            public void onFullscreenAction(PLVLinkMicItemDataBean itemDataBean, PLVSwitchViewAnchorLayout switchItemView) {
                //?????????????????????????????????????????????
                if(fullscreenLayout != null && !fullscreenLayout.isFullScreened()) {
                    fullscreenLayout.changeViewToFullscreen(switchItemView, itemDataBean);
                    homeFragment.closeMemberLayout();
                }
            }
        });
        //?????????????????????20s???????????????
        streamerLayout.addOnShowNetBrokenListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.showAlertDialogNoNetwork();
            }
        });
        //????????????????????????????????????
        streamerLayout.addOnIsFrontCameraListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.setFrontCameraStatus(aBoolean);
            }
        });
        streamerLayout.addOnIsFrontMirrorModeListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.setMirrorModeStatus(aBoolean);
            }
        });
        // ????????????????????????
        streamerLayout.addStreamerTimeListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                if (streamerFinishLayout != null) {
                    streamerFinishLayout.updateSecondsSinceStartTiming(integer);
                }
            }
        });
        //????????????????????????
        streamerLayout.addLinkMicCountListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                if(PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())){
                    //???????????????????????????
                    return;
                }
                homeFragment.updateLinkMicLayoutTypeVisibility(integer > 1);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ??????????????????">

    private void observeCleanUpLayout() {
        if (streamerLayout != null && cleanUpLayout != null) {
            streamerLayout.getStreamerPresenter().registerView(cleanUpLayout.getStreamerView());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ??????viewPager?????????">
    private void observeViewPagerLayout() {
        homeFragment.setOnViewActionListener(new PLVSAStreamerHomeFragment.OnViewActionListener() {
            @Override
            public void onViewCreated() {
                homeFragment.init(liveRoomDataManager);
                //??????streamerView
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getMoreLayoutStreamerView());
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getMemberLayoutStreamerView());
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getStatusBarLayoutStreamerView());

                //?????????????????????????????????
                streamerLayout.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (s == null) {
                            return;
                        }
                        homeFragment.updateUserRequestStatus();
                    }
                });
            }

            @Override
            public void onStopLive() {
                updateStopLiveLayout();
            }

            @Override
            public void onClickToOpenMemberLayout() {
                linkMicRequestTipsLayout.cancel();
            }

            @Override
            public boolean showCleanUpLayout() {
                boolean success = false;
                if (cleanUpLayout != null) {
                    success = cleanUpLayout.show();
                    if (success) {
                        cleanUpLayout = null;
                    }
                }
                return success;
            }

            @Override
            public void onChangeLinkMicLayoutType() {
                if(streamerLayout != null){
                    streamerLayout.changeLinkMicLayoutType();
                }
            }
        });

        emptyFragment.setOnViewActionListener(new PLVSAEmptyFragment.OnViewActionListener() {
            @Override
            public void onViewCreated() {
                streamerLayout.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (s == null) {
                            return;
                        }
                        linkMicRequestTipsLayout.show();
                    }
                });
            }

            @Override
            public void onStopLive() {
                updateStopLiveLayout();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ?????????????????????">

    private void observeLinkmicRequestLayout() {
        linkMicRequestTipsLayout.setOnTipsClickListener(new PLVSALinkMicRequestTipsLayout.OnTipsClickListener() {
            @Override
            public void onClickBar() {
                linkMicRequestTipsLayout.cancel();
            }

            @Override
            public void onClickNavBtn() {
                linkMicRequestTipsLayout.cancel();
                // homeFragment index=1
                topLayerViewPager.setCurrentItem(1);
                homeFragment.openMemberLayoutAndHideUserRequestTips();
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">
    private void observeFullscreenLayout() {
        if (streamerLayout != null && fullscreenLayout != null) {
            streamerLayout.getStreamerPresenter().registerView(fullscreenLayout.getStreamerView());
            fullscreenLayout.setOnViewActionListener(new PLVSAStreamerFullscreenLayout.OnViewActionListener() {
                @Override
                public void onScaleStreamerView(PLVLinkMicItemDataBean linkMicItemDataBean, float scaleFactor) {
                    streamerLayout.scaleStreamerView(linkMicItemDataBean, scaleFactor);
                }

                @Override
                public void onExitFullscreen(PLVLinkMicItemDataBean linkmicItem, PLVViewSwitcher fullscreenSwitcher) {
                    streamerLayout.clearFullscreenState(linkmicItem);
                }
            });
        }
    }
    // </editor-fold >

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
                            streamerLayout.getStreamerPresenter().setRecoverStream(false);
                            streamerLayout.getStreamerPresenter().stopLiveStream();
                        }
                    })
                    .setNegativeButton("????????????", null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (streamerLayout.getNetworkQuality() == PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                        settingLayout.showAlertDialogNoNetwork();
                        return;
                    }
                    liveRoomDataManager.setNeedStreamRecover(true);
                    streamerLayout.getStreamerPresenter().setRecoverStream(true);
                    //????????????
                    PLVLiveLocalActionHelper.Action action = PLVLiveLocalActionHelper.getInstance().getChannelAction(liveRoomDataManager.getConfig().getChannelId());
                    if(!action.isPortrait){
                        PLVScreenUtils.enterLandscape(PLVSAStreamerAloneActivity.this);
                        ScreenUtils.setLandscape(PLVSAStreamerAloneActivity.this);
                        streamerLayout.getStreamerPresenter().setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE);
                    }
                    streamerLayout.setBitrate(action.bitrate);
                    streamerLayout.setCameraDirection(action.isFrontCamera);
                    //????????????
                    settingLayout.liveStart();
                    dialog.dismiss();
                }
            });
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="????????????">

    /**
     * ????????????????????????????????????
     */
    private void updateStopLiveLayout(){
        if (streamerLayout != null) {
            if(PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())){
                //???????????????????????????
                finish();
            } else {
                if(streamerFinishLayout != null){
                    streamerLayout.stopLive();
                    streamerFinishLayout.show();
                }
            }
        } else {
            finish();
        }
    }
    // </editor-fold >
}
