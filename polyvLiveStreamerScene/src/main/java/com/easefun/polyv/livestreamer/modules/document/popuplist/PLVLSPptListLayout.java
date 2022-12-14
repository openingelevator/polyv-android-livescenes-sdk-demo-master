package com.easefun.polyv.livestreamer.modules.document.popuplist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.document.model.PLVSPPTDetail;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.document.model.PLVSPPTJsModel;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.document.popuplist.adapter.PLVLSPptListAdapter;
import com.easefun.polyv.livestreamer.modules.document.popuplist.enums.PLVLSPptViewType;
import com.easefun.polyv.livestreamer.modules.document.popuplist.holder.PLVLSPptListViewHolder;
import com.easefun.polyv.livestreamer.modules.document.popuplist.vo.PLVLSPptVO;
import com.easefun.polyv.livestreamer.modules.document.popuplist.widget.PLVLSDocumentDeleteArrow;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * PPT?????????PPT??????????????????????????????
 * ?????????????????????????????????????????????????????????{@link #open(boolean)}??????????????????
 *
 * @author suhongtao
 */
public class PLVLSPptListLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="??????">

    private static final String TAG = PLVLSPptListLayout.class.getSimpleName();

    /**
     * SharePreference Key
     * ?????????????????????PPT??????????????????????????????
     * ?????????????????????????????????????????????PPT???????????????????????????
     */
    private static final String SP_KEY_HAS_SHOW_INDICATOR = "key_has_show_ppt_page_list_indicator";

    // ???View
    private View rootView;
    private LinearLayout plvlsDocumentTitleLl;
    private ImageView plvlsDocumentListBackIv;
    private TextView plvlsDocumentNameTv;
    private TextView plvlsDocumentPageTv;
    private TextView plvlsDocumentRefreshTv;
    private View plvlsDocumentSeparatorView;
    private RecyclerView plvlsDocumentPptRv;
    private PLVTriangleIndicateTextView plvlsDocumentBackIndicator;

    // ????????????
    private PLVBlurView plvlsBlurView;
    // ????????????????????????
    private Disposable updateBlurViewDisposable;

    // ????????????
    private PLVMenuDrawer menuDrawer;

    // PPT???????????? ?????????????????? ?????????
    private PLVConfirmDialog pptConvertSelectDialog;

    // PPT???????????? ??????????????????
    private PLVLSDocumentDeleteArrow documentDeleteArrow;
    // PPT?????? ??????????????????
    private PLVConfirmDialog documentDeleteConfirmDialog;
    // PPT?????? ???????????????????????? ??????????????????
    private PLVConfirmDialog documentUploadAgainConfirmDialog;

    /**
     * MVP - View
     * ???????????????????????????????????????gc???????????????????????????Presenter??????
     */
    private PLVAbsDocumentView mvpView;

    // ???????????????
    private PLVLSPptListAdapter pptListAdapter;
    // ??????????????????????????????
    private static final int PPT_ITEMS_EACH_ROW = 4;
    // ??????????????????
    @PLVLSPptViewType.Range
    private int showViewType = PLVLSPptViewType.COVER;

    /**
     * ppt??????id??????ppt????????????
     * Key: autoId
     * Value: ppt?????????
     */
    private SparseArray<String> pptAutoIdMapToFullName = new SparseArray<>();
    // ??????ppt??????id
    private int currentAutoId = 0;
    // ??????ppt??????id
    private int currentPageId = 0;

    //?????????????????????tag
    private boolean recoverTag = false;

    // PPT???????????? ????????????
    private String lastPptName = null;
    private int lastPptPageCount = 0;
    private List<PLVLSPptVO> lastPptPageVOList = null;

    // PPT???????????? ????????????
    private List<PLVLSPptVO> lastPptCoverVOList = null;
    // PPT???????????? ????????????????????????
    private List<PLVLSPptVO> uploadPptCoverVOList = new ArrayList<>();
    // PPT???????????? ??????????????????fileId??????
    private Set<String> pptConvertAnimateLossFileIdSet = new HashSet<>();

    // PPT??????????????????????????????
    private OnPLVSDocumentUploadListener documentUploadListener;

    @Nullable
    private PLVUserAbilityManager.OnUserAbilityChangedListener onUserAbilityChangeCallback;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    public PLVLSPptListLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvls_document_ppt_list_layout, this);
        findView();
        initRecyclerView();
        initPptConvertSelectDialog();
        initPptDeleteConfirmDialog();
        initPptUploadAgainConfirmDialog();
        initDocumentUploadListener();
        initOnClickBackListener();
        initOnClickRefreshListener();
        PLVBlurUtils.initBlurView(plvlsBlurView);

        initMvpView();
        initOnUserAbilityChangeListener();
    }

    private void findView() {
        plvlsDocumentTitleLl = (LinearLayout) rootView.findViewById(R.id.plvls_document_title_ll);
        plvlsDocumentListBackIv = (ImageView) rootView.findViewById(R.id.plvls_document_list_back_iv);
        plvlsDocumentNameTv = (TextView) rootView.findViewById(R.id.plvls_document_name_tv);
        plvlsDocumentPageTv = (TextView) rootView.findViewById(R.id.plvls_document_page_tv);
        plvlsDocumentRefreshTv = (TextView) rootView.findViewById(R.id.plvls_document_refresh_tv);
        plvlsDocumentSeparatorView = (View) rootView.findViewById(R.id.plvls_document_separator_view);
        plvlsDocumentPptRv = (RecyclerView) rootView.findViewById(R.id.plvls_document_ppt_rv);
        plvlsDocumentBackIndicator = (PLVTriangleIndicateTextView) rootView.findViewById(R.id.plvls_document_back_indicator);

        plvlsBlurView = (PLVBlurView) rootView.findViewById(R.id.blur_ly);

        documentDeleteArrow = new PLVLSDocumentDeleteArrow();
    }

    /**
     * ???????????????
     */
    private void initRecyclerView() {
        initRecyclerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), PPT_ITEMS_EACH_ROW);
        plvlsDocumentPptRv.setLayoutManager(gridLayoutManager);
        plvlsDocumentPptRv.setAdapter(pptListAdapter);
    }

    /**
     * ????????????????????????
     */
    private void initRecyclerViewAdapter() {
        // ?????????Adapter?????????????????????PPT????????????
        pptListAdapter = new PLVLSPptListAdapter(null, PLVLSPptViewType.COVER);
        // ???????????????????????????
        pptListAdapter.setOnPptItemClickListener(new PLVLSPptListViewHolder.OnPptItemClickListener() {
            @Override
            public void onClick(int id) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
                    return;
                }
                if (pptListAdapter.getRealViewType() == PLVLSPptViewType.COVER) {
                    // ?????????PPT????????????
                    if (currentAutoId == id) {
                        // ??????????????????????????????????????????PPT????????????
                        updatePptPageViewContent();
                    } else {
                        // ???????????????????????????????????????PPT????????????????????????
                        PLVDocumentPresenter.getInstance().changePpt(id);
                        currentAutoId = id;
                        showViewType = PLVLSPptViewType.PAGE;
                        close();
                    }
                } else if (pptListAdapter.getRealViewType() == PLVLSPptViewType.PAGE) {
                    // ?????????PPT??????????????????????????????????????????PPT??????
                    PLVDocumentPresenter.getInstance().changePptPage(currentAutoId, id);
                    currentPageId = id;
                }
            }
        });
        // ?????????????????????????????????
        pptListAdapter.setOnPptItemLongClickListener(new PLVLSPptListViewHolder.OnPptItemLongClickListener() {
            @Override
            public void onLongClick(View view, final int id, final String fileId) {
                if (pptListAdapter.getRealViewType() != PLVLSPptViewType.COVER) {
                    return;
                }
                if (documentDeleteArrow != null) {
                    // ????????????????????????
                    documentDeleteArrow.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // ????????????????????? ????????????????????????
                            documentDeleteConfirmDialog
                                    .setRightBtnListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_DELETE_PPT)) {
                                                return;
                                            }
                                            PLVDocumentPresenter.getInstance().deleteDocument(fileId);
                                            documentDeleteConfirmDialog.hide();
                                        }
                                    })
                                    .show();
                        }
                    });
                    documentDeleteArrow.showAtLocation(view);
                }
            }
        });
        // ??????????????????????????????????????????????????????
        initOnUploadLayoutButtonClickListener();
    }

    /**
     * ?????????PPT??????????????????
     */
    private void initPptConvertSelectDialog() {
        pptConvertSelectDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitle(getResources().getString(R.string.plvls_document_upload_choose_convert_type))
                .setContent(getResources().getString(R.string.plvls_document_upload_choose_convert_type_hint))
                .build();
    }

    /**
     * ???????????????PPT???????????????
     */
    private void initPptDeleteConfirmDialog() {
        documentDeleteConfirmDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitleVisibility(View.GONE)
                .setContent("??????????????????????????????")
                .setLeftButtonText("?????????")
                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        documentDeleteConfirmDialog.hide();
                    }
                })
                .setRightButtonText("??????")
                .build();
    }

    /**
     * ?????????????????????PPT???????????????
     */
    private void initPptUploadAgainConfirmDialog() {
        documentUploadAgainConfirmDialog = PLVLSConfirmDialog.Builder.context(getContext())
                .setTitleVisibility(View.GONE)
                .build();
    }

    /**
     * ?????????????????????????????????
     */
    private void initOnClickBackListener() {
        plvlsDocumentListBackIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ????????????PPT??????????????????????????????????????????????????????????????????PPT????????????
                showViewType = PLVLSPptViewType.COVER;
                updatePptCoverViewContent();
                requestUpdateData();
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    private void initOnClickRefreshListener() {
        plvlsDocumentRefreshTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_PULL_PPT)) {
                    return;
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }
        });
    }

    /**
     * ????????? MVP - View
     */
    private void initMvpView() {
        mvpView = new PLVAbsDocumentView() {
            @Override
            public void onPptCoverList(@Nullable PLVSPPTInfo pptInfo) {
                processPptCoverList(pptInfo);
            }

            @Override
            public void onPptPageList(@Nullable PLVSPPTJsModel plvspptJsModel) {

                if(!recoverTag && PLVLiveChannelConfigFiller.generateNewChannelConfig().isLiveStreamingWhenLogin()){
                    //????????????????????????????????????????????????
                    if(plvspptJsModel != null && !TextUtils.isEmpty(plvspptJsModel.getFileName())){
                        lastPptName = plvspptJsModel.getFileName();
                        currentAutoId = plvspptJsModel.getAutoId();
                        showViewType = PLVLSPptViewType.PAGE;
                        refreshPptPageStatus(plvspptJsModel);
                        recoverTag = true;
                        return;
                    }
                }
                processPptPageList(plvspptJsModel);
            }

            @Override
            public void onAssistantChangePptPage(int pageId) {
                PLVDocumentPresenter.getInstance().changePptPage(currentAutoId, pageId);
            }

            @Override
            public void onPptPageChange(int autoId, int pageId) {
                if (showViewType == PLVLSPptViewType.COVER) {
                    pptListAdapter.setCurrentSelectedId(autoId);
                }
                if (showViewType == PLVLSPptViewType.PAGE) {
                    pptListAdapter.setCurrentSelectedId(pageId);
                }
                pptListAdapter.notifyDataSetChanged();

                currentAutoId = autoId;
                currentPageId = pageId;
            }

            @Override
            public boolean requestSelectUploadFileConvertType(final Uri fileUri) {
                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_UPLOAD_PPT)) {
                    return true;
                }
                if (fileUri == null) {
                    Log.w(TAG, "file uri is null.");
                    PLVToast.Builder.context(getContext())
                            .setText("??????????????????????????????")
                            .build().show();
                    return false;
                }

                String filePath = null;
                PLVUriPathHelper.copyFile(getContext(), fileUri,
                        new File(getContext().getExternalFilesDir(""),
                                PLVUriPathHelper.getRealFileName(getContext(), fileUri)));
                File file = new File(getContext().getExternalFilesDir(""),
                        PLVUriPathHelper.getRealFileName(getContext(), fileUri));
                if (fileUri.toString().startsWith("content")) {
                    filePath = PLVUriPathHelper.getPath(getContext(), fileUri);
                } else if (fileUri.getPath() != null) {
                    filePath = fileUri.getPath().substring(fileUri.getPath().indexOf("/") + 1);
                }
                if (TextUtils.isEmpty(filePath)) {
                    Log.w(TAG, "file path is empty.");
                    PLVToast.Builder.context(getContext())
                            .setText("??????????????????????????????")
                            .build().show();
                    return false;
                }

                final File uploadFile = file;

                // ??????????????????????????????
                pptConvertSelectDialog
                        .setLeftButtonText("????????????")
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVSDocumentUploadConstant.PPTConvertType.COMMON, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .setRightButtonText("????????????????????????")
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVSDocumentUploadConstant.PPTConvertType.ANIMATE, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .show();

                return true;
            }

            @Override
            public boolean notifyFileUploadNotSuccess(@NonNull final List<PLVPptUploadLocalCacheVO> cacheVOS) {
                if (cacheVOS.size() == 0) {
                    return true;
                }
                // ????????????????????????????????????
                documentUploadAgainConfirmDialog
                        .setContent("??????????????????????????????????????????????????????")
                        .setLeftButtonText("??????")
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .setRightButtonText("??????")
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_UPLOAD_PPT)) {
                                    // ?????????????????????????????????
                                    PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                    documentUploadAgainConfirmDialog.hide();
                                    return;
                                }

                                for (PLVPptUploadLocalCacheVO localCacheVO : cacheVOS) {
                                    File file = new File(localCacheVO.getFilePath());
                                    if (!file.exists()) {
                                        continue;
                                    }
                                    PLVDocumentPresenter.getInstance().uploadFile(getContext(), file, localCacheVO.getConvertType(), documentUploadListener);
                                }
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .show();
                return true;
            }

            @Override
            public boolean notifyFileConvertAnimateLoss(@NonNull List<PLVPptUploadLocalCacheVO> cacheVOS) {
                for (PLVPptUploadLocalCacheVO localCacheVO : cacheVOS) {
                    pptConvertAnimateLossFileIdSet.add(localCacheVO.getFileId());
                }
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
                return false;
            }

            @Override
            public void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean) {
                if (!success) {
                    return;
                }
                if (deletedPptBean != null) {
                    // ????????????????????????PPT???????????????????????????????????????
                    PLVLSPptVO uploadDeletedPptVO = null;
                    for (PLVLSPptVO pptVO : uploadPptCoverVOList) {
                        if (pptVO.getFileId() != null && pptVO.getFileId().equalsIgnoreCase(deletedPptBean.getFileId())) {
                            uploadDeletedPptVO = pptVO;
                            break;
                        }
                    }
                    if (uploadDeletedPptVO != null) {
                        uploadPptCoverVOList.remove(uploadDeletedPptVO);
                    }
                }
                // ???????????????????????????PPT???????????????????????????????????????????????????
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }
        };

        PLVDocumentPresenter.getInstance().registerView(mvpView);
    }

    /**
     * ???????????????????????????????????????
     */
    private void initOnUserAbilityChangeListener() {
        this.onUserAbilityChangeCallback = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
                    close();
                }
            }
        };

        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(onUserAbilityChangeCallback));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="??????????????? - ????????????????????????????????????">

    /**
     * ??????????????????????????????????????????????????????
     * <p>
     * ?????????????????????PPT???????????????????????????PPT??????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????
     * ?????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void initOnUploadLayoutButtonClickListener() {
        pptListAdapter.setOnUploadViewButtonClickListener(new PLVLSPptListViewHolder.OnUploadViewButtonClickListener() {
            @Override
            public void onClick(final PLVLSPptVO pptVO) {
                if (showViewType != PLVLSPptViewType.COVER) {
                    return;
                }
                if (pptVO.getUploadStatus() == null) {
                    updatePptCoverViewContent();
                    return;
                }
                if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS) {
                    // ?????????????????????????????????????????????
                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                    // ????????????????????????????????????
                    for (PLVLSPptVO uploadListPptVO : mergePptCoverList()) {
                        if (uploadListPptVO.getFileId().equalsIgnoreCase(pptVO.getFileId())) {
                            uploadListPptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_SUCCESS);
                            break;
                        }
                    }
                    pptConvertAnimateLossFileIdSet.remove(pptVO.getFileId());
                    // ????????????
                    updatePptCoverViewContent();
                } else if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_FAILED) {
                    // ???????????? ????????????
                    documentUploadAgainConfirmDialog
                            .setContent("??????????????????????????????????????????????????? ??? ??????PDF?????? ??????????????????????????????????????????")
                            .setLeftButtonText("??????")
                            .setLeftBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // ???????????????????????????????????????????????????????????????
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    documentUploadAgainConfirmDialog.hide();
                                }
                            })
                            .setRightButtonText("????????????")
                            .setRightBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    // ????????????????????????
                                    if (getContext() instanceof Activity) {
                                        PLVFileChooseUtils.chooseFile((Activity) getContext(), PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT);
                                    }
                                    documentUploadAgainConfirmDialog.hide();
                                }
                            })
                            .show();
                } else if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_UPLOAD_FAILED) {
                    // ???????????? ?????????????????????
                    uploadPptCoverVOList.remove(pptVO);
                    PLVDocumentPresenter.getInstance().restartUploadFromCache(getContext(), pptVO.getFileId(), documentUploadListener);
                } else {
                    updatePptCoverViewContent();
                }
            }
        });
    }

    /**
     * ?????????????????????????????????
     * <p>
     * ??????????????????????????????????????????{@link PLVPptUploadStatus}
     */
    private void initDocumentUploadListener() {
        documentUploadListener = new OnPLVSDocumentUploadListener() {
            @Override
            public void onPrepared(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onPrepared");
                // onPrepared ??????????????????PPT???????????????
                PLVLSPptVO pptVO = new PLVLSPptVO(documentBean.getPreviewImage(), documentBean.getFileName(), documentBean.getFileType(), documentBean.getAutoId());
                pptVO.setFileId(documentBean.getFileId());
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_PREPARED);
                uploadPptCoverVOList.add(pptVO);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadProgress(PLVSPPTInfo.DataBean.ContentsBean documentBean, int progress) {
                Log.i(TAG, "document upload onUploadProgress, progress:" + progress);
                // ?????????????????? ??????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOADING);
                pptVO.setUploadProgress(progress);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onUploadSuccess");
                // ?????????????????? ??????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onUploadFailed(@Nullable PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onUploadFailed");
                String message = msg;
                if (TextUtils.isEmpty(message)) {
                    message = throwable.getMessage();
                }
                PLVToast.Builder.context(getContext())
                        .setText(errorCode + "-" + message)
                        .build().show();

                // ?????????????????? ??????????????????
                if (documentBean == null) {
                    return;
                }
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_FAILED);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onConvertSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onConvertSuccess");
                // ?????????????????? ?????????????????????????????? ????????????????????????PPT???????????? ??????????????????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }

            @Override
            public void onConvertFailed(PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onConvertFailed");
                // ?????????????????? ??????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_FAILED);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }

            @Override
            public void onDocumentExist(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentExist");
                // ????????????????????? ?????????????????????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                // TODO ?????????????????????????????????
            }

            @Override
            public void onDocumentConverting(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentConverting");
                // ??????????????? ??????????????????
                PLVLSPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERTING);
                if (showViewType == PLVLSPptViewType.COVER) {
                    updatePptCoverViewContent();
                }
            }
        };
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * ????????????
     */
    public void open(boolean refresh) {
        if (!checkHasDocumentPermissionOrToast(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_OPEN_PPT)) {
            return;
        }
        if (refresh) {
            PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
        }
        final int landscapeHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (menuDrawer == null) {
            // ???????????????
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_FULLSCREEN);
            menuDrawer.setMenuSize((int) (landscapeHeight * 0.75));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                }
            });
            plvlsDocumentPptRv.post(new Runnable() {
                @Override
                public void run() {
                    menuDrawer.setDragAreaMenuBottom((int) (plvlsDocumentPptRv.getTop() + landscapeHeight * 0.25));
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
        initViewByShowType();
    }

    /**
     * ????????????
     */
    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    /**
     * ??????????????????
     *
     * @return consume
     */
    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    /**
     * ????????????
     */
    public void destroy() {
        onUserAbilityChangeCallback = null;
        stopUpdateBlurViewTimer();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - UI??????">

    /**
     * ????????????????????????????????????????????????
     */
    private void initViewByShowType() {
        if (currentAutoId == 0) {
            showViewType = PLVLSPptViewType.COVER;
        } else {
            showViewType = PLVLSPptViewType.PAGE;
        }

        if (showViewType == PLVLSPptViewType.COVER) {
            updatePptCoverViewContent();
        } else {
            updatePptPageViewContent();
        }
        requestUpdateData();
        // ?????????????????????????????????PPT???????????????????????????????????????????????????
        PLVDocumentPresenter.getInstance().checkUploadFileStatus();
    }

    /**
     * ??????PPT????????????????????????
     */
    private void updatePptCoverViewContent() {
        List<PLVLSPptVO> mergedCoverList = mergePptCoverList();
        checkAnimateLossStatus(mergedCoverList);
        showViewType = PLVLSPptViewType.COVER;
        plvlsDocumentListBackIv.setVisibility(GONE);
        plvlsDocumentBackIndicator.setVisibility(GONE);
        plvlsDocumentPageTv.setVisibility(GONE);
        plvlsDocumentRefreshTv.setVisibility(VISIBLE);

        String name = String.format("???????????? ???%s???", mergedCoverList.size());
        SpannableString spannableString = new SpannableString(name);
        spannableString.setSpan(new AbsoluteSizeSpan(ConvertUtils.sp2px(12f)),
                name.lastIndexOf("???"), name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        plvlsDocumentNameTv.setText(spannableString);
        pptListAdapter.setCurrentSelectedId(currentAutoId);
        pptListAdapter.updatePptList(mergedCoverList, PLVLSPptViewType.COVER);
        plvlsDocumentPptRv.scrollToPosition(currentAutoId);
    }

    /**
     * ??????PPT????????????????????????
     */
    private void updatePptPageViewContent() {
        showViewType = PLVLSPptViewType.PAGE;
        plvlsDocumentListBackIv.setVisibility(VISIBLE);
        plvlsDocumentPageTv.setVisibility(VISIBLE);
        plvlsDocumentRefreshTv.setVisibility(GONE);

        if (lastPptName == null) {
            plvlsDocumentNameTv.setText("");
        } else {
            int suffixDotIndex = lastPptName.lastIndexOf('.');
            if (suffixDotIndex <= 22) {
                // ??????????????????22????????????????????????
                plvlsDocumentNameTv.setText(lastPptName);
            } else {
                String suffix = lastPptName.substring(suffixDotIndex);
                String truncatedPptName = lastPptName.substring(0, 22);
                plvlsDocumentNameTv.setText(truncatedPptName + ".." + suffix);
            }
        }
        plvlsDocumentPageTv.setText("???" + lastPptPageCount + "???");
        pptListAdapter.setCurrentSelectedId(currentPageId);
        pptListAdapter.updatePptList(lastPptPageVOList, PLVLSPptViewType.PAGE);
        plvlsDocumentPptRv.scrollToPosition(currentPageId);

        // ???????????? ???????????????
        if (!hasShowBackIndicatorBefore()) {
            // ???????????? ??????????????????????????? ??????????????????
            setHasShowIndicator();

            plvlsDocumentBackIndicator.setVisibility(VISIBLE);
            // 3??????????????????
            plvlsDocumentBackIndicator.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (plvlsDocumentBackIndicator != null) {
                        plvlsDocumentBackIndicator.setVisibility(GONE);
                    }
                }
            }, 3000);
        }
    }

    private boolean checkHasDocumentPermissionOrToast(PLVUserAbility documentAbility) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(documentAbility)) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvls_document_usage_not_permeitted)
                    .show();
            return false;
        }
        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">

    /**
     * ????????????????????????Presenter????????????????????????
     */
    private void requestUpdateData() {
        if (showViewType == PLVLSPptViewType.COVER) {
            PLVDocumentPresenter.getInstance().requestGetPptCoverList();
        } else if (showViewType == PLVLSPptViewType.PAGE) {
            PLVDocumentPresenter.getInstance().requestGetPptPageList(currentAutoId);
        }
    }

    /**
     * ???????????????PPT??????????????????
     *
     * @param pptInfo
     */
    private void processPptCoverList(PLVSPPTInfo pptInfo) {
        if (pptInfo == null) {
            return;
        }
        List<PLVLSPptVO> pptVOList = new ArrayList<>();
        for (PLVSPPTInfo.DataBean.ContentsBean contentsBean : pptInfo.getData().getContents()) {
            String imageUrl = contentsBean.getPreviewImage();
            String type = contentsBean.getFileType();
            if (type == null) {
                type = "";
            }
            String name = contentsBean.getFileName();

            PLVLSPptVO pptVO = new PLVLSPptVO(imageUrl, name, type, contentsBean.getAutoId());
            pptVO.setFileId(contentsBean.getFileId());
            pptVO.setUploadStatus(mapServerUploadStatus(contentsBean.getStatus()));
            pptVOList.add(pptVO);

            pptAutoIdMapToFullName.put(contentsBean.getAutoId(), name + type);
        }
        lastPptCoverVOList = pptVOList;
        if (showViewType == PLVLSPptViewType.COVER) {
            updatePptCoverViewContent();
        }
    }

    /**
     * ????????????PPT??????????????????????????????
     *
     * @param beanServerStatus ?????????PPT??????
     * @return ?????? {@link PLVPptUploadStatus}
     */
    private static Integer mapServerUploadStatus(String beanServerStatus) {
        switch (beanServerStatus) {
            case PLVSDocumentUploadConstant.ConvertStatus.NORMAL:
                return PLVPptUploadStatus.STATUS_CONVERT_SUCCESS;
            case PLVSDocumentUploadConstant.ConvertStatus.WAIT_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOADING;
            case PLVSDocumentUploadConstant.ConvertStatus.FAIL_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOAD_FAILED;
            case PLVSDocumentUploadConstant.ConvertStatus.WAIT_CONVERT:
                return PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS;
            case PLVSDocumentUploadConstant.ConvertStatus.FAIL_CONVERT:
                return PLVPptUploadStatus.STATUS_CONVERT_FAILED;
            default:
                return null;
        }
    }

    /**
     * ???????????????PPT??????????????????
     *
     * @param jsModel
     */
    private void processPptPageList(PLVSPPTJsModel jsModel) {
        if (jsModel == null || currentAutoId != jsModel.getAutoId()) {
            return;
        }
        refreshPptPageStatus(jsModel);
    }

    /**
     * ??????ppt??????
     */
    private void refreshPptPageStatus(PLVSPPTJsModel jsModel){
        List<PLVLSPptVO> pptVOList = new ArrayList<>();
        for (PLVSPPTDetail pptDetail : jsModel.getPPTImages()) {
            String imageUrl = pptDetail.getImageUrl();
            int pptPageId = pptDetail.getPos();
            PLVLSPptVO pptVO = new PLVLSPptVO(imageUrl, pptPageId);
            pptVOList.add(pptVO);
        }
        lastPptPageCount = pptVOList.size();
        lastPptPageVOList = pptVOList;
        lastPptName = pptAutoIdMapToFullName.get(jsModel.getAutoId());
        if (showViewType == PLVLSPptViewType.PAGE) {
            updatePptPageViewContent();
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    private boolean hasShowBackIndicatorBefore() {
        return SPUtils.getInstance().getBoolean(SP_KEY_HAS_SHOW_INDICATOR, false);
    }

    /**
     * ?????????????????????????????????
     */
    private void setHasShowIndicator() {
        SPUtils.getInstance().put(SP_KEY_HAS_SHOW_INDICATOR, true);
    }

    /**
     * ??????fileId???????????????PPT?????????????????????????????????VO
     *
     * @param fileId
     * @return
     */
    @Nullable
    private PLVLSPptVO getPptVOFromUploadCache(@NonNull String fileId) {
        if (fileId == null) {
            return null;
        }
        for (PLVLSPptVO pptVO : uploadPptCoverVOList) {
            if (fileId.equalsIgnoreCase(pptVO.getFileId())) {
                return pptVO;
            }
        }
        return null;
    }

    /**
     * ??????2???PPT???????????????????????????PPT?????? ??? ??????????????????PPT??????
     *
     * @return
     */
    private List<PLVLSPptVO> mergePptCoverList() {
        Set<String> fileIdSet = new HashSet<>();
        List<PLVLSPptVO> resultList = new ArrayList<>();
        for (PLVLSPptVO uploadPptVO : uploadPptCoverVOList) {
            if (fileIdSet.add(uploadPptVO.getFileId().toLowerCase())) {
                resultList.add(uploadPptVO);
            }
        }
        if (lastPptCoverVOList == null) {
            return resultList;
        }
        for (PLVLSPptVO serverPptVO : lastPptCoverVOList) {
            if (fileIdSet.add(serverPptVO.getFileId().toLowerCase())) {
                resultList.add(serverPptVO);
            }
        }
        return resultList;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param pptVOList
     */
    private void checkAnimateLossStatus(List<PLVLSPptVO> pptVOList) {
        if (pptVOList == null) {
            return;
        }
        for (PLVLSPptVO pptVO : pptVOList) {
            if (pptConvertAnimateLossFileIdSet.contains(pptVO.getFileId())) {
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????????????????">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        plvlsBlurView.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
            updateBlurViewDisposable = null;
        }
    }
    // </editor-fold>

}
