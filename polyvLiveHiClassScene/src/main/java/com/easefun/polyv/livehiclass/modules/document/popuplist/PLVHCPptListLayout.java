package com.easefun.polyv.livehiclass.modules.document.popuplist;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livecommon.module.modules.document.model.vo.PLVPptUploadLocalCacheVO;
import com.easefun.polyv.livecommon.module.modules.document.presenter.PLVDocumentPresenter;
import com.easefun.polyv.livecommon.module.modules.document.view.PLVAbsDocumentView;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.document.popuplist.adapter.PLVHCPptListAdapter;
import com.easefun.polyv.livehiclass.modules.document.popuplist.holder.PLVHCPptListViewHolder;
import com.easefun.polyv.livehiclass.modules.document.popuplist.vo.PLVHCPptVO;
import com.easefun.polyv.livehiclass.modules.document.popuplist.widget.PLVHCDocumentDeleteArrow;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.easefun.polyv.livescenes.document.model.PLVSPPTInfo;
import com.easefun.polyv.livescenes.upload.OnPLVSDocumentUploadListener;
import com.plv.livescenes.document.model.PLVPPTInfo;
import com.plv.livescenes.upload.PLVDocumentUploadConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PPT??????????????????????????????
 *
 * @author suhongtao
 */
public class PLVHCPptListLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="??????">

    private static final String TAG = PLVHCPptListLayout.class.getSimpleName();

    // ???View
    private View rootView;
    private LinearLayout documentTitleLl;
    private TextView documentNameTv;
    private TextView documentPageTv;
    private RecyclerView documentPptRv;

    // ??????????????????
    private PLVOutsideTouchableLayout container;

    // PPT???????????? ?????????????????? ?????????
    private PLVConfirmDialog pptConvertSelectDialog;

    // PPT???????????? ??????????????????
    private PLVHCDocumentDeleteArrow documentDeleteArrow;
    // PPT?????? ??????????????????
    private PLVConfirmDialog documentDeleteConfirmDialog;
    // PPT?????? ???????????????????????? ??????????????????
    private PLVConfirmDialog documentUploadAgainConfirmDialog;
    // PPT?????? ???????????? ??????????????????
    private PLVConfirmDialog documentConvertFailConfirmDialog;

    /**
     * MVP - View
     * ???????????????????????????????????????gc???????????????????????????Presenter??????
     */
    private PLVAbsDocumentView mvpView;

    // ???????????????
    private PLVHCPptListAdapter pptListAdapter;
    // ??????????????????????????????
    private static final int PPT_ITEMS_EACH_ROW = 4;

    /**
     * ppt??????id??????ppt????????????
     * Key: autoId
     * Value: ppt?????????
     */
    private SparseArray<String> pptAutoIdMapToFullName = new SparseArray<>();

    // PPT???????????? ????????????
    private List<PLVHCPptVO> lastPptCoverVOList = null;
    // PPT???????????? ????????????????????????
    private List<PLVHCPptVO> uploadPptCoverVOList = new ArrayList<>();
    // PPT???????????? ??????????????????fileId??????
    private Set<String> pptConvertAnimateLossFileIdSet = new HashSet<>();

    // PPT??????????????????????????????
    private OnPLVSDocumentUploadListener documentUploadListener;

    private OnViewActionListener onViewActionListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????????">

    public PLVHCPptListLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCPptListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="???????????????">

    private void initView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.plvhc_document_ppt_list_layout, this);
        findView();
        initRecyclerView();
        initPptConvertSelectDialog();
        initPptDeleteConfirmDialog();
        initPptUploadAgainConfirmDialog();
        initPptConvertFailConfirmDialog();
        initDocumentUploadListener();

        initMvpView();
    }

    private void findView() {
        documentTitleLl = (LinearLayout) rootView.findViewById(R.id.plvhc_document_title_ll);
        documentNameTv = (TextView) rootView.findViewById(R.id.plvhc_document_name_tv);
        documentPageTv = (TextView) rootView.findViewById(R.id.plvhc_document_page_tv);
        documentPptRv = (RecyclerView) rootView.findViewById(R.id.plvhc_document_ppt_rv);

        documentDeleteArrow = new PLVHCDocumentDeleteArrow(getContext());
    }

    /**
     * ???????????????
     */
    private void initRecyclerView() {
        initRecyclerViewAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), PPT_ITEMS_EACH_ROW);
        documentPptRv.setLayoutManager(gridLayoutManager);
        documentPptRv.setAdapter(pptListAdapter);
    }

    /**
     * ????????????????????????
     */
    private void initRecyclerViewAdapter() {
        // ?????????Adapter?????????????????????PPT????????????
        pptListAdapter = new PLVHCPptListAdapter(null);
        // ???????????????????????????
        pptListAdapter.setOnPptItemClickListener(new PLVHCPptListViewHolder.OnPptItemClickListener() {
            @Override
            public void onClick(int id) {
                PLVDocumentPresenter.getInstance().requestOpenPptView(id, pptAutoIdMapToFullName.get(id, ""));
                hide();
            }
        });
        // ?????????????????????????????????
        pptListAdapter.setOnPptItemLongClickListener(new PLVHCPptListViewHolder.OnPptItemLongClickListener() {
            @Override
            public void onLongClick(View view, final int id, final String fileId) {
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
        pptConvertSelectDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(getResources().getString(R.string.plvhc_document_upload_choose_convert_type))
                .setContent(getResources().getString(R.string.plvhc_document_upload_choose_convert_type_hint));
    }

    /**
     * ???????????????PPT???????????????
     */
    private void initPptDeleteConfirmDialog() {
        documentDeleteConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_delete_confirm_title)
                .setContent(R.string.plvhc_document_delete_confirm_content)
                .setLeftButtonText(R.string.plvhc_document_delete_confirm_cancel)
                .setLeftBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        documentDeleteConfirmDialog.hide();
                    }
                })
                .setRightButtonText(R.string.plvhc_document_delete_confirm);
    }

    /**
     * ?????????????????????PPT???????????????
     */
    private void initPptUploadAgainConfirmDialog() {
        documentUploadAgainConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_upload_retry_title)
                .setContent(R.string.plvhc_document_upload_retry_content);
    }

    /**
     * ?????????????????????????????????
     */
    private void initPptConvertFailConfirmDialog() {
        documentConvertFailConfirmDialog = new PLVHCConfirmDialog(getContext())
                .setTitle(R.string.plvhc_document_upload_convert_fail_title)
                .setContent(R.string.plvhc_document_upload_convert_fail_content);
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
            public boolean requestSelectUploadFileConvertType(final Uri fileUri) {
                if (fileUri == null) {
                    Log.w(TAG, "file uri is null.");
                    return false;
                }

                String filePath = null;
                if (fileUri.toString().startsWith("content")) {
                    filePath = PLVUriPathHelper.getPath(getContext(), fileUri);
                } else if (fileUri.getPath() != null) {
                    filePath = fileUri.getPath().substring(fileUri.getPath().indexOf("/") + 1);
                }
                if (TextUtils.isEmpty(filePath)) {
                    Log.w(TAG, "file path is empty.");
                    return false;
                }

                final File uploadFile = new File(filePath);
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                String fileMimeType = null;
                if (!TextUtils.isEmpty(fileExtension)) {
                    fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                }
                // ??????????????????????????????toast????????????
                if (TextUtils.isEmpty(fileMimeType)) {
                    PLVHCToast.Builder.context(getContext())
                            .setText(R.string.plvhc_document_upload_not_support_file_type_hint)
                            .build()
                            .show();
                    return false;
                } else if (!PLVFileChooseUtils.isSupportMimeType(fileMimeType)) {
                    PLVHCToast.Builder.context(getContext())
                            .setText(R.string.plvhc_document_upload_not_support_file_type_hint)
                            .build()
                            .show();
                    return false;
                }

                // ??????????????????????????????
                pptConvertSelectDialog
                        .setLeftButtonText(R.string.plvhc_document_upload_convert_type_quick)
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVDocumentUploadConstant.PPTConvertType.COMMON, documentUploadListener);
                                pptConvertSelectDialog.hide();
                            }
                        })
                        .setRightButtonText(R.string.plvhc_document_upload_convert_type_animate)
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().uploadFile(getContext(),
                                        uploadFile, PLVDocumentUploadConstant.PPTConvertType.ANIMATE, documentUploadListener);
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
                        .setLeftButtonText(R.string.plvhc_document_upload_retry_cancel)
                        .setLeftBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVDocumentPresenter.getInstance().removeUploadCache(cacheVOS);
                                documentUploadAgainConfirmDialog.hide();
                            }
                        })
                        .setRightButtonText(R.string.plvhc_document_upload_retry_confirm)
                        .setRightBtnListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                updatePptCoverViewContent();
                return false;
            }

            @Override
            public void onPptDelete(boolean success, @Nullable PLVSPPTInfo.DataBean.ContentsBean deletedPptBean) {
                if (!success) {
                    return;
                }
                if (deletedPptBean != null) {
                    // ????????????????????????PPT???????????????????????????????????????
                    PLVHCPptVO uploadDeletedPptVO = null;
                    for (PLVHCPptVO pptVO : uploadPptCoverVOList) {
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

                PLVHCToast.Builder.context(getContext())
                        .setText("???????????????")
                        .setDrawable(R.drawable.plvhc_document_ppt_deleted_icon)
                        .build().show();
            }
        };

        PLVDocumentPresenter.getInstance().registerView(mvpView);
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
        pptListAdapter.setOnUploadViewButtonClickListener(new PLVHCPptListViewHolder.OnUploadViewButtonClickListener() {
            @Override
            public void onClick(final PLVHCPptVO pptVO) {
                if (pptVO.getUploadStatus() == null) {
                    updatePptCoverViewContent();
                    return;
                }
                if (pptVO.getUploadStatus() == PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS) {
                    // ?????????????????????????????????????????????
                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                    // ????????????????????????????????????
                    for (PLVHCPptVO uploadListPptVO : mergePptCoverList()) {
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
                    documentConvertFailConfirmDialog
                            .setLeftButtonText(R.string.plvhc_document_upload_convert_fail_cancel)
                            .setLeftBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // ???????????????????????????????????????????????????????????????
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    documentConvertFailConfirmDialog.hide();
                                }
                            })
                            .setRightButtonText(R.string.plvhc_document_upload_convert_fail_retry)
                            .setRightBtnListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PLVDocumentPresenter.getInstance().removeUploadCache(pptVO.getFileId());
                                    // ????????????????????????
                                    if (getContext() instanceof Activity) {
                                        PLVFileChooseUtils.chooseFile((Activity) getContext(), PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT);
                                    }
                                    documentConvertFailConfirmDialog.hide();
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
                PLVHCPptVO pptVO = new PLVHCPptVO(documentBean.getPreviewImage(), documentBean.getFileName(), documentBean.getFileType(), documentBean.getAutoId());
                pptVO.setFileId(documentBean.getFileId());
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_PREPARED);
                uploadPptCoverVOList.add(pptVO);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadProgress(PLVSPPTInfo.DataBean.ContentsBean documentBean, int progress) {
                Log.i(TAG, "document upload onUploadProgress, progress:" + progress);
                // ?????????????????? ??????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOADING);
                pptVO.setUploadProgress(progress);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onUploadSuccess");
                // ?????????????????? ??????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS);
                updatePptCoverViewContent();
            }

            @Override
            public void onUploadFailed(@Nullable PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onUploadFailed");
                // ?????????????????? ??????????????????
                if (documentBean == null) {
                    return;
                }
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_UPLOAD_FAILED);
                updatePptCoverViewContent();
            }

            @Override
            public void onConvertSuccess(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onConvertSuccess");
                // ?????????????????? ?????????????????????????????? ????????????????????????PPT???????????? ??????????????????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
            }

            @Override
            public void onConvertFailed(PLVSPPTInfo.DataBean.ContentsBean documentBean, int errorCode, String msg, Throwable throwable) {
                Log.i(TAG, "document upload onConvertFailed");
                // ?????????????????? ??????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_FAILED);
                updatePptCoverViewContent();
            }

            @Override
            public void onDocumentExist(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentExist");
                // ????????????????????? ?????????????????????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO != null) {
                    uploadPptCoverVOList.remove(pptVO);
                }
                PLVHCToast.Builder.context(getContext())
                        .setText("???????????????")
                        .build().show();
            }

            @Override
            public void onDocumentConverting(PLVSPPTInfo.DataBean.ContentsBean documentBean) {
                Log.i(TAG, "document upload onDocumentConverting");
                // ??????????????? ??????????????????
                PLVHCPptVO pptVO = getPptVOFromUploadCache(documentBean.getFileId());
                if (pptVO == null) {
                    return;
                }
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERTING);
                updatePptCoverViewContent();
            }
        };
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void show(int viewWidth, int viewHeight, int[] viewLocation) {
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });
        }

        final int screenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        final int screenHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        int height = screenHeight - viewLocation[1] - ConvertUtils.dp2px(16);
        int width = (int) (screenWidth * (656F / 812F));

        FrameLayout.LayoutParams lp = new LayoutParams(width, height);
        lp.rightMargin = ConvertUtils.dp2px(66);
        lp.bottomMargin = ConvertUtils.dp2px(8);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        setLayoutParams(lp);

        container.removeAllViews();
        container.addView(this);

        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(true);
        }

        initViewByShowType();
    }

    public void hide() {
        if (container != null) {
            container.removeAllViews();
        }
        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(false);
        }
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    /**
     * ????????????
     */
    public void destroy() {
        PLVDocumentPresenter.getInstance().destroy();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - UI??????">

    /**
     * ????????????????????????????????????????????????
     */
    private void initViewByShowType() {
        updatePptCoverViewContent();
        requestUpdateData();
        // ?????????????????????????????????PPT???????????????????????????????????????????????????
        PLVDocumentPresenter.getInstance().checkUploadFileStatus();
    }

    /**
     * ??????PPT????????????????????????
     */
    private void updatePptCoverViewContent() {
        List<PLVHCPptVO> mergedCoverList = mergePptCoverList();
        checkAnimateLossStatus(mergedCoverList);
        documentNameTv.setText("????????????");
        documentPageTv.setText("???" + mergedCoverList.size() + "???");
        pptListAdapter.updatePptList(mergedCoverList);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="?????????????????? - ????????????">

    /**
     * ????????????????????????Presenter????????????????????????
     */
    private void requestUpdateData() {
        //???????????????????????????????????????????????????????????????
        PLVDocumentPresenter.getInstance().requestGetPptCoverList(true);
    }

    /**
     * ???????????????PPT??????????????????
     *
     * @param pptInfo
     */
    private void processPptCoverList(PLVPPTInfo pptInfo) {
        if (pptInfo == null) {
            return;
        }
        List<PLVHCPptVO> pptVOList = new ArrayList<>();
        for (PLVPPTInfo.DataBean.ContentsBean contentsBean : pptInfo.getData().getContents()) {
            String imageUrl = contentsBean.getPreviewImage();
            String type = contentsBean.getFileType();
            if (type == null) {
                type = "";
            }
            String name = contentsBean.getFileName();

            PLVHCPptVO pptVO = new PLVHCPptVO(imageUrl, name, type, contentsBean.getAutoId());
            pptVO.setFileId(contentsBean.getFileId());
            pptVO.setUploadStatus(mapServerUploadStatus(contentsBean.getStatus()));
            pptVOList.add(pptVO);

            pptAutoIdMapToFullName.put(contentsBean.getAutoId(), name + type);
        }
        lastPptCoverVOList = pptVOList;
        updatePptCoverViewContent();
    }

    /**
     * ????????????PPT??????????????????????????????
     *
     * @param beanServerStatus ?????????PPT??????
     * @return ?????? {@link PLVPptUploadStatus}
     */
    private static Integer mapServerUploadStatus(String beanServerStatus) {
        switch (beanServerStatus) {
            case PLVDocumentUploadConstant.ConvertStatus.NORMAL:
                return PLVPptUploadStatus.STATUS_CONVERT_SUCCESS;
            case PLVDocumentUploadConstant.ConvertStatus.WAIT_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOADING;
            case PLVDocumentUploadConstant.ConvertStatus.FAIL_UPLOAD:
                return PLVPptUploadStatus.STATUS_UPLOAD_FAILED;
            case PLVDocumentUploadConstant.ConvertStatus.WAIT_CONVERT:
                return PLVPptUploadStatus.STATUS_UPLOAD_SUCCESS;
            case PLVDocumentUploadConstant.ConvertStatus.FAIL_CONVERT:
                return PLVPptUploadStatus.STATUS_CONVERT_FAILED;
            default:
                return null;
        }
    }

    /**
     * ??????fileId???????????????PPT?????????????????????????????????VO
     *
     * @param fileId
     * @return
     */
    @Nullable
    private PLVHCPptVO getPptVOFromUploadCache(@NonNull String fileId) {
        if (fileId == null) {
            return null;
        }
        for (PLVHCPptVO pptVO : uploadPptCoverVOList) {
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
    private List<PLVHCPptVO> mergePptCoverList() {
        Set<String> fileIdSet = new HashSet<>();
        List<PLVHCPptVO> resultList = new ArrayList<>();
        for (PLVHCPptVO uploadPptVO : uploadPptCoverVOList) {
            if (fileIdSet.add(uploadPptVO.getFileId().toLowerCase())) {
                resultList.add(uploadPptVO);
            }
        }
        if (lastPptCoverVOList == null) {
            return resultList;
        }
        for (PLVHCPptVO serverPptVO : lastPptCoverVOList) {
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
    private void checkAnimateLossStatus(List<PLVHCPptVO> pptVOList) {
        if (pptVOList == null) {
            return;
        }
        for (PLVHCPptVO pptVO : pptVOList) {
            if (pptConvertAnimateLossFileIdSet.contains(pptVO.getFileId())) {
                pptVO.setUploadStatus(PLVPptUploadStatus.STATUS_CONVERT_ANIMATE_LOSS);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="????????? - view?????????????????????">

    public interface OnViewActionListener {

        void onVisibilityChanged(boolean isVisible);

    }

    // </editor-fold>

}
