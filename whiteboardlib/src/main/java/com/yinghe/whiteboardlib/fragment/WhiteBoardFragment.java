package com.yinghe.whiteboardlib.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.protruly.fileselect.FileSelectActivity;
import com.protruly.permissions.AfterPermissionGranted;
import com.protruly.permissions.EasyPermissions;
import com.protruly.permissions.PermissionConsts;
import com.yinghe.whiteboardlib.R;
import com.yinghe.whiteboardlib.adapter.SketchDataGridAdapter;
import com.yinghe.whiteboardlib.bean.FileInfo;
import com.yinghe.whiteboardlib.bean.RespFileUpdate;
import com.yinghe.whiteboardlib.bean.SketchData;
import com.yinghe.whiteboardlib.bean.StrokeRecord;
import com.yinghe.whiteboardlib.callback.HttpCallBack;
import com.yinghe.whiteboardlib.helper.MultiImageSelector;
import com.yinghe.whiteboardlib.helper.SaveFileHelper;
import com.yinghe.whiteboardlib.helper.ShareFileHelper;
import com.yinghe.whiteboardlib.listener.OnDrawChangedListener;
import com.yinghe.whiteboardlib.ui.NumPopupView;
import com.yinghe.whiteboardlib.ui.SketchView;
import com.yinghe.whiteboardlib.ui.SlideButton;
import com.yinghe.whiteboardlib.utils.ACache;
import com.yinghe.whiteboardlib.utils.AppUtils;
import com.yinghe.whiteboardlib.utils.BitmapUtils;
import com.yinghe.whiteboardlib.utils.CommConsts;
import com.yinghe.whiteboardlib.utils.DrawConsts;
import com.yinghe.whiteboardlib.utils.EncodingHandler;
import com.yinghe.whiteboardlib.utils.FileUtils;
import com.yinghe.whiteboardlib.utils.NetUtil;
import com.yinghe.whiteboardlib.utils.ScreenUtils;
import com.yinghe.whiteboardlib.utils.TimeUtils;
import com.yinghe.whiteboardlib.utils.ViewUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.yinghe.whiteboardlib.utils.DrawConsts.DEFAULT_ERASER_SIZE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.DEFAULT_STROKE_ALPHA;
import static com.yinghe.whiteboardlib.utils.DrawConsts.DEFAULT_STROKE_SIZE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_MOVE_RECT;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_PHOTO;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_STROKE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.FILE_TYPE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_CIRCLE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_DRAW;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_ERASER;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_LINE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_RECTANGLE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_TEXT;

/**
 * 白板
 * @author wang
 * @time 2017/3/1
 */
public class WhiteBoardFragment extends BaseFragment implements OnDrawChangedListener, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    public static UIHandler mHandler;

    // 选择文件的请求类型
    public static final int REQUEST_IMAGE = 2;
    public static final int REQUEST_BACKGROUND = 3;
    public static final int REQUEST_SELECT_VIDEO = 4;
    public static final int REQUEST_SELECT_DOC = 5;

    int keyboardHeight;
    int textOffX;
    int textOffY;

    SketchView mSketchView;//画板区域
    View controlLayout;//控制布局

    View btnMenu;// 菜单按钮

    View rlUndo;//撤销的父布局
    ImageView btnUndo;//撤销
    View rlRedo;//取消撤销的父布局
    ImageView btnRedo;//取消撤销

    View rlBtnStroke;//画笔父布局
    ImageView btnStroke;//画笔
    ImageView btnEraser;//橡皮擦
    TextView eraserText;// 橡皮擦文字
    View rlBtnEraser;//橡皮擦

    View rlBtnDrag;//拖拽

    View btnPagePrev;//切换到上一个画板页面
    View btnPageShow;//显示所有的画板页面
    TextView pageNum;// 显示页数
    View btnPageNext;//切换到下一个画板页面

    ImageView btnAdd;//添加画板
    ImageView btnDelete;//删除画板

    RadioGroup strokeTypeRG, strokeColorRG;

    // 橡皮擦弹框中
    View popupEraserBtn;
    View popupEraserCircleBtn;

    View eraserSizeChangeRL;
    TextView eraserSizeTip;

    Activity activity;//上下文

    int strokeType = STROKE_TYPE_DRAW;//模式
    int lastEraserType = DrawConsts.STROKE_TYPE_ERASER_CIRCLE;//上一次橡皮擦的模式

    private ProgressBar mProgressBar;

    // 画布显示列表
    GridView sketchGV;
    SketchDataGridAdapter sketchGVAdapter;

    int pupWindowsDPWidth = 300;//弹窗宽度，单位DP
    int strokePupWindowsDPHeight = 260;//画笔弹窗高度，单位DP
    int eraserPupWindowsDPHeight = 150;//橡皮擦弹窗高度，单位DP
    int menuPupWindowsDPHeight = 200;//画笔弹窗高度，单位DP

    PopupWindow strokePopupWindow, eraserPopupWindow, textPopupWindow, menuPopupWindow;//画笔、橡皮擦参数设置弹窗实例
    private View popupStrokeLayout, popupEraserLayout, popupTextLayout, popupMenuLayout;//画笔、橡皮擦弹窗布局

    private NumPopupView mNumPopupView;// 数字键弹框

    private View menuDefaultLayout;// 默认菜单布局
    private View menuScanShareLayout;// 扫码二维码的布局

    private View menuShowSwitchRL;// 显示右边切换按钮的布局
    private SlideButton mSlideButton;// 滑动按钮
    private TextView encryptTip;// 加密提示
    private View showEncryptRL;// 显示密码布局
    private TextView mEncryptText;// 显示密码文字
    private ImageView mEncryptEdit;// 编辑密码按钮

    private String encrypt;// 加密的密码

    private View menuShowEditEncryptRL;// 编辑密码的布局
    private TextView editTextEncrypt;// 编辑密码
    private String encryptTextStr;
    private ImageView mScanImage;// 扫码的二维码
    private TextView mShowScanTv;// 扫码二维码提示文字

    private Bitmap qrcodeBitmap;// 生成的二维码图片

    private ProgressBar mScanCreateProgressBar;// 二维码生成过程中的进度条

    private boolean isUploadToServer = false;// 是否上传到服务器：true 上传到服务器，false 不上传到服务器

    private SeekBar strokeSeekBar, strokeAlphaSeekBar, eraserSeekBar;
    private ImageView strokeImageView, strokeAlphaImage, eraserImageView;//画笔宽度，画笔不透明度，橡皮擦宽度IV
    private EditText strokeET;//绘制文字的内容
    private int size;
    private ArrayList<String> mSelectPath;

    private int curPageIndex = 0;// 当前画板页面索引
    private List<SketchData> sketchDataList = new ArrayList<>();

    // 尺寸
    public static int sketchViewHeight;
    public static int sketchViewWidth;
    public static int sketchViewRight;
    public static int sketchViewBottom;
    public static int decorHeight;
    public static int decorWidth;

    private RelativeLayout rootView;// 主界面根节点

    private static String savePathName;// 保存文件的路径
    private static int selectRequest;// 选择文件或者选择背景的的请求

    int selectedBG = R.drawable.btn_click_shape_bg;// 被选中状态的背景
    BitmapDrawable defaultBG = new BitmapDrawable();// 空背景

    // 对话框UI
    Dialog dialog;
    TextView mTitle;
    TextView mMessage;
    EditText mEditText;
    Button mConfirm; //确定按钮
    Button mCancel; //取消按钮

    private ACache aCache;
    private ShareFileHelper mShareFileHelper;
    private SaveFileHelper mSaveFileHelper;

    /**
     * show 默认新建一个画板
     * @author TangentLu
     * create at 16/6/17 上午9:59
     */
    public static WhiteBoardFragment newInstance() {
        return new WhiteBoardFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.d("初始化Fragment");

        activity = getActivity();//初始化上下文
        mHandler = new UIHandler(this);// 初始化UIHandler

        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_white_board, container, false);
        findView(rootView);//载入所有的按钮实例

        // 延迟加载弹框布局和画板预览界面
        mHandler.postDelayed(() -> {
            // 初始化所有的弹框
            initPopup(inflater);
            initPopupWindows();//初始化弹框
            initDialogView();// 初始化对话框UI
            initSketchGV();// 初始化画板适配数据
        }, 100);

        initDrawParams();//初始化绘画参数
        initData();// 初始化画板数据

        return rootView;
    }

    @Override
    public void onDestroy() {
        releaseData();
        super.onDestroy();
    }

    /**
     * 释放资源
     */
    private void releaseData(){
        mSketchView.releaseData();

        if (mSelectPath != null){
            mSelectPath.clear();
            mSelectPath = null;
        }

        if (sketchDataList != null){
            sketchDataList.clear();
            sketchDataList = null;
        }

        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 添加空的画板数据
        SketchData newSketchData = new SketchData();
        sketchDataList.add(newSketchData);
        newSketchData.editMode = EDIT_STROKE;
        newSketchData.strokeType = STROKE_TYPE_DRAW;
        mSketchView.setSketchData(newSketchData);
        updatePageShowNum(curPageIndex);

        aCache = ACache.get(activity);
        aCache.put(CommConsts.KEY_MD5_FILE_NAME, "");
        mShareFileHelper = new ShareFileHelper();
        mSaveFileHelper = new SaveFileHelper();
    }

    /**
     * 初始化图片Adapter
     */
    private void initSketchGV() {
        sketchGVAdapter = new SketchDataGridAdapter(activity, sketchDataList, new SketchDataGridAdapter.OnActionCallback() {
            @Override
            public void onDeleteCallback(int position) {
                LogUtils.d("onDeleteCallback,position->%s", position);
                // 需要动态更新curPageIndex
                curPageIndex = (curPageIndex < position) ? curPageIndex: (curPageIndex = position - 1);
                curPageIndex = (curPageIndex < 0) ? 0: curPageIndex;
                SketchData sketchData = sketchDataList.get(curPageIndex);

                // 更新画笔数据
                mSketchView.updateSketchData(sketchData);
                showSketchView(false);

                updatePageShowNum(curPageIndex); // 更新页面索引值
                updateBtnAdd();// 判断白板数量是否达到最大值

                // 刷新界面
                sketchDataList.remove(position);
                sketchGVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAddCallback(int position) {
                LogUtils.d("position->%s", position);
                curPageIndex = position;

                // 添加一个新的白板
                SketchData newSketchData = new SketchData();
                sketchDataList.add(newSketchData);
                mSketchView.updateSketchData(newSketchData);
                mSketchView.setEditMode(EDIT_STROKE);//切换笔画编辑模式
                mSketchView.setStrokeType(STROKE_TYPE_DRAW);
                showSketchView(true);

                // 改变画笔按钮状态
                setStrokeBtn(rlBtnStroke);
                updatePageShowNum(curPageIndex);
                updateBtnAdd();
            }

            @Override
            public void onSelectCallback(int position) {
                // 更新画板数据
                SketchData sketchData = sketchDataList.get(position);
                mSketchView.updateSketchData(sketchData);

                //显示画板
                showSketchView(true);

                // 更新画板索引
                curPageIndex = position;
                updatePageShowNum(curPageIndex);
            }
        });
        sketchGV.setAdapter(sketchGVAdapter);
    }

    /**
     * 显示或者隐藏画布
     *
     * @param b
     */
    private void showSketchView(boolean b) {
        mSketchView.setVisibility(b ? View.VISIBLE : View.GONE);
        sketchGV.setVisibility(!b ? View.VISIBLE : View.GONE);
    }

    /**
     * 初始化对话框UI
     */
    private void initDialogView(){
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_layout, null);
        mTitle = (TextView) dialogView.findViewById(R.id.title);
        mEditText = (EditText) dialogView.findViewById(R.id.message_edit);
        mMessage = (TextView) dialogView.findViewById(R.id.message);
        mConfirm = (Button) dialogView.findViewById(R.id.positiveButton);
        mCancel = (Button) dialogView.findViewById(R.id.negativeButton);

        dialog = new Dialog(activity);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 设置对话框的大小
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = ScreenUtils.dip2px(activity, 400); // 宽度
        lp.height = ScreenUtils.dip2px(activity, 240); // 高度

        dialogWindow.setAttributes(lp);
    }

    /**
     * 初始化绘制数据
     */
    private void initDrawParams() {
        //画笔宽度缩放基准参数
        Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        size = circleDrawable.getIntrinsicWidth();
    }

    /**
     * 初始化弹框
     */
    private void initPopupWindows() {
        initMenuPop();
        initStrokePop();
        initEraserPop();
        initTextPop();
    }

    /**
     * 文字画笔弹框
     */
    private void initTextPop() {
        textPopupWindow = new PopupWindow(activity);
        textPopupWindow.setContentView(popupTextLayout);
        textPopupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);//宽度200dp
        textPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        textPopupWindow.setFocusable(true);
        textPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        textPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        textPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!strokeET.getText().toString().equals("")) {
                    StrokeRecord record = new StrokeRecord(strokeType);
                    record.text = strokeET.getText().toString();
                }
            }
        });
    }

    /**
     * 橡皮擦弹框
     */
    private void initEraserPop() {
        //橡皮擦弹窗
        eraserPopupWindow = new PopupWindow(activity);
        eraserPopupWindow.setContentView(popupEraserLayout);//设置主体布局
        eraserPopupWindow.setWidth(ScreenUtils.dip2px(activity, pupWindowsDPWidth));//宽度200dp
//        eraserPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        eraserPopupWindow.setHeight(ScreenUtils.dip2px(activity, eraserPupWindowsDPHeight));//高度自适应
        eraserPopupWindow.setFocusable(true);
        eraserPopupWindow.setBackgroundDrawable(new BitmapDrawable());//设置空白背景
        eraserPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画
    }

    /**
     * 菜单弹框
     */
    private void initMenuPop(){
        //菜单弹窗
        menuPopupWindow = new PopupWindow(activity);
        menuPopupWindow.setContentView(popupMenuLayout);//设置主体布局

        // 设置弹窗大小
        menuPopupWindow.setWidth(ScreenUtils.dip2px(activity, pupWindowsDPWidth));//宽度
        menuPopupWindow.setHeight(ScreenUtils.dip2px(activity, menuPupWindowsDPHeight));//高度
        menuPopupWindow.setFocusable(true);

        menuPopupWindow.setBackgroundDrawable(new BitmapDrawable());//设置空白背景
        menuPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画
    }

    /**
     * 画笔擦弹框
     */
    private void initStrokePop() {
        //画笔弹窗
        strokePopupWindow = new PopupWindow(activity);
        strokePopupWindow.setContentView(popupStrokeLayout);//设置主体布局
        strokePopupWindow.setWidth(ScreenUtils.dip2px(activity, pupWindowsDPWidth));//宽度
//        strokePopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        strokePopupWindow.setHeight(ScreenUtils.dip2px(activity, strokePupWindowsDPHeight));//高度
        strokePopupWindow.setFocusable(true);
        strokePopupWindow.setBackgroundDrawable(new BitmapDrawable());//设置空白背景
        strokePopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画

        // 选择画笔类型
        strokeTypeRG.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.stroke_type_rbtn_draw) {
                strokeType = STROKE_TYPE_DRAW;
            } else if (checkedId == R.id.stroke_type_rbtn_line) {
                strokeType = STROKE_TYPE_LINE;
            } else if (checkedId == R.id.stroke_type_rbtn_circle) {
                strokeType = STROKE_TYPE_CIRCLE;
            } else if (checkedId == R.id.stroke_type_rbtn_rectangle) {
                strokeType = STROKE_TYPE_RECTANGLE;
            } else if (checkedId == R.id.stroke_type_rbtn_text) {
                strokeType = STROKE_TYPE_TEXT;
            }

            mSketchView.setStrokeType(strokeType);// 设置画笔类型
        });

        // 选择画笔颜色
        strokeColorRG.setOnCheckedChangeListener((group, checkedId) -> {
            int color = DrawConsts.COLOR_BLACK;
            if (checkedId == R.id.stroke_color_black) {
                color = DrawConsts.COLOR_BLACK;
            } else if (checkedId == R.id.stroke_color_red) {
                color = DrawConsts.COLOR_RED;
            } else if (checkedId == R.id.stroke_color_green) {
                color = DrawConsts.COLOR_GREEN;
            } else if (checkedId == R.id.stroke_color_orange) {
                color = DrawConsts.COLOR_ORANGE;
            } else if (checkedId == R.id.stroke_color_blue) {
                color = DrawConsts.COLOR_BLUE;
            }

            mSketchView.setStrokeColor(color);
        });

        //画笔宽度拖动条
        strokeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setSeekBarProgress(progress, STROKE_TYPE_DRAW);
            }
        });
        strokeSeekBar.setProgress(DEFAULT_STROKE_SIZE);

        //画笔不透明度拖动条
        strokeAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                int alpha = (progress * 255) / 100;//百分比转换成256级透明度
                mSketchView.setStrokeAlpha(alpha);
                strokeAlphaImage.setAlpha(alpha);
            }
        });
        strokeAlphaSeekBar.setProgress(DEFAULT_STROKE_ALPHA);
    }

    /**
     * 初始化界面
     *
     * @param view
     */
    private void findView(View view) {
        // 软键盘弹框问题
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //下面的代码主要是为了解决软键盘弹出后遮挡住文字录入PopWindow的问题
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);//获取rootView的可视区域
            int screenHeight = rootView.getHeight();//获取rootView的高度
            keyboardHeight = screenHeight - (r.bottom - r.top);//用rootView的高度减去rootView的可视区域高度得到软键盘高度
            if (textOffY > (sketchViewHeight - keyboardHeight)) {//如果输入焦点出现在软键盘显示的范围内则进行布局上移操作
                rootView.setTop(-keyboardHeight);//rootView整体上移软键盘高度
                //更新PopupWindow的位置
                int x = textOffX;
                int y = textOffY - mSketchView.getHeight();
                textPopupWindow.update(mSketchView, x, y,
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

        sketchGV = (GridView) view.findViewById(R.id.sketch_data_gv);

        //画板整体布局
        mSketchView = (SketchView) view.findViewById(R.id.sketch_view);
        controlLayout = view.findViewById(R.id.controlLayout);

        // 底部菜单按钮
        btnMenu = view.findViewById(R.id.rl_menu);

        // 底部画笔操作按钮
        rlBtnStroke = view.findViewById(R.id.rl_stroke);
        rlBtnStroke.setBackgroundResource(R.drawable.btn_click_shape_bg);
        btnStroke = (ImageView) view.findViewById(R.id.btn_stroke);
        rlBtnEraser = view.findViewById(R.id.rl_eraser);
        btnEraser = (ImageView) view.findViewById(R.id.btn_eraser);
        eraserText = (TextView) view.findViewById(R.id.tv_eraser);

        rlUndo = view.findViewById(R.id.rl_undo);
        btnUndo = (ImageView) view.findViewById(R.id.btn_undo);

        rlRedo = view.findViewById(R.id.rl_redo);
        btnRedo = (ImageView) view.findViewById(R.id.btn_redo);

        rlBtnDrag = view.findViewById(R.id.rl_drag);

        // 底部右边按钮
        btnPagePrev = view.findViewById(R.id.rl_page_prev);
        btnPageShow = view.findViewById(R.id.rl_pages_show);
        pageNum = (TextView) view.findViewById(R.id.tv_page_num);
        btnPageNext = view.findViewById(R.id.rl_page_next);

        // 最右边菜单栏
        btnAdd = (ImageView) view.findViewById(R.id.right_menu_btn_add);
        btnDelete = (ImageView) view.findViewById(R.id.right_menu_btn_delete);

        // 加载进度条
        mProgressBar = (ProgressBar) view.findViewById(R.id.large_progressbar);

        //设置点击监听
        btnMenu.setOnClickListener(this);

        rlBtnStroke.setOnClickListener(this);
        rlBtnEraser.setOnClickListener(this);
        rlUndo.setOnClickListener(this);
        rlRedo.setOnClickListener(this);

        rlBtnDrag.setOnClickListener(this);
        btnPagePrev.setOnClickListener(this);
        btnPageShow.setOnClickListener(this);
        btnPageNext.setOnClickListener(this);

        btnAdd.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

//        mSketchView.setOnPhotoRecordChangeListener(mOnPhotoRecordChangeListener);//设置图片改变的监听器
        mSketchView.setOnDrawChangedListener(this);//设置撤销动作监听器
        mSketchView.setTextWindowCallback((anchor, record) -> {// 文字书写监听
            textOffX = record.textOffX;
            textOffY = record.textOffY;
            showTextPopupWindow(anchor, record);
        });
    }

    /**
     * 初始化所有的弹框
     */
    private void initPopup(LayoutInflater inflater){
        // popupWindow布局

        // 菜单弹框布局
        initMenuPopupLayout(inflater);

        // 数字键弹框布局
        initNumPopupLayout();

        //画笔弹窗布局
        initStrokePopupLayout(inflater);

        //橡皮擦弹窗布局
        initEraserPopupLayout(inflater);

        //文本录入弹窗布局
        initTextPopupLayout(inflater);

        getSketchSize();//计算选择图片弹窗的高宽
    }

    /**
     * 数字键弹框布局
     */
    private void initNumPopupLayout(){
        mNumPopupView = new NumPopupView(activity, editTextEncrypt);
        // 设置数字键完成的监听
        mNumPopupView.setConfirmCallback(() -> {
            LogUtils.d("密码编辑完成");
            // 获得加密文字
            encrypt = editTextEncrypt.getText().toString().trim();

            // 加密的密码不能为空
            if (TextUtils.isEmpty(encrypt)){
                Toast.makeText(getContext(), "密码不能为空！", Toast.LENGTH_LONG).show();
                return;
            }

            // 显示密码UI
            menuShowSwitchRL.setVisibility(View.VISIBLE);
            encryptTip.setVisibility(View.INVISIBLE);
            showEncryptRL.setVisibility(View.VISIBLE);

            String text = String.format(encryptTextStr, encrypt);
            mEncryptText.setText(text);

            // 编辑密码UI隐藏
            menuShowEditEncryptRL.setVisibility(View.GONE);

            // 修改文件密码
            String md5filename = aCache.getAsString(CommConsts.KEY_MD5_FILE_NAME);
            mShareFileHelper.changeFilePwd(md5filename, encrypt);
        });

        mNumPopupView.setCancelCallback(() -> {
            menuShowSwitchRL.setVisibility(View.VISIBLE);
            encryptTip.setVisibility(View.INVISIBLE);
            showEncryptRL.setVisibility(View.VISIBLE);

            menuShowEditEncryptRL.setVisibility(View.INVISIBLE);

            String text = "";
            encrypt = editTextEncrypt.getText().toString().trim();
            if (TextUtils.isEmpty(encrypt)){
                text = "请设置密码!";
            } else {
                text = String.format(encryptTextStr, encrypt);
            }

            mEncryptText.setText(text);
            encryptTip.setVisibility(View.GONE);
        });
    }

    /**
     * 菜单弹框布局
     * @param inflater
     */
    private void initMenuPopupLayout(LayoutInflater inflater){
        // 菜单弹框布局
        popupMenuLayout = inflater.inflate(R.layout.popup_sketch_menu, null);
        menuDefaultLayout = popupMenuLayout.findViewById(R.id.menu_default);
        menuDefaultLayout.setOnClickListener(menuOnClickListener);
        menuScanShareLayout = popupMenuLayout.findViewById(R.id.menu_sub_scan_share);
        menuScanShareLayout.setOnClickListener(menuOnClickListener);
        View back = popupMenuLayout.findViewById(R.id.rl_menu_back);
        back.setOnClickListener(menuOnClickListener);

        // 扫码分享的界面
        // 右边UI:显示switch按钮
        menuShowSwitchRL = popupMenuLayout.findViewById(R.id.rl_show_switch_ui);
        menuShowSwitchRL.setVisibility(View.VISIBLE);
        mSlideButton = (SlideButton) popupMenuLayout.findViewById(R.id.switch_button);
        mSlideButton.setOnSlideButtonClickListener(onSlideButtonClickListener);
        encryptTip = (TextView) popupMenuLayout.findViewById(R.id.tv_encrypt_tip);

        // 显示密码
        showEncryptRL = popupMenuLayout.findViewById(R.id.rl_show_encrypt);
        mEncryptText = (TextView) popupMenuLayout.findViewById(R.id.doc_encrypt_text);
        encryptTextStr = activity.getResources().getString(R.string.doc_encrypt_text);
        mEncryptEdit = (ImageView) popupMenuLayout.findViewById(R.id.btn_edit_encrypt);
        mEncryptEdit.setOnClickListener(menuOnClickListener);

        // 右边UI:显示密码编辑
        menuShowEditEncryptRL = popupMenuLayout.findViewById(R.id.rl_show_edit_encrypt_ui);
        editTextEncrypt = (TextView) popupMenuLayout.findViewById(R.id.edit_text_encrypt);

        // 生成二维码过程中的加载进度条
        mScanCreateProgressBar = (ProgressBar) popupMenuLayout.findViewById(R.id.scan_create_progressbar);
        // 显示二维码
        mScanImage = (ImageView) popupMenuLayout.findViewById(R.id.scan_image);
        // 显示二维码提示文字
        mShowScanTv = (TextView) popupMenuLayout.findViewById(R.id.show_scan_image_tv);

        View btnPhoto = popupMenuLayout.findViewById(R.id.menu_btn_select_photo);//选择图片
        btnPhoto.setOnClickListener(menuOnClickListener);
        View btnDoc = popupMenuLayout.findViewById(R.id.menu_btn_select_file);//选择文档
        btnDoc.setOnClickListener(menuOnClickListener);
        View btnVideo = popupMenuLayout.findViewById(R.id.menu_btn_select_video);// 选择视频
        btnVideo.setOnClickListener(menuOnClickListener);
        View btnBg= popupMenuLayout.findViewById(R.id.menu_btn_select_bg);// 更换背景
        btnBg.setOnClickListener(menuOnClickListener);

        View btnScan= popupMenuLayout.findViewById(R.id.menu_btn_scan_share);// 扫码分享
        btnScan.setOnClickListener(menuOnClickListener);

        View btnSave= popupMenuLayout.findViewById(R.id.menu_btn_save);// 保存
        btnSave.setOnClickListener(menuOnClickListener);

        View btnExist= popupMenuLayout.findViewById(R.id.menu_btn_exist);// 退出
        btnExist.setOnClickListener(menuOnClickListener);
    }

    /**
     * 画笔弹窗布局
     * @param inflater
     */
    private void initStrokePopupLayout(LayoutInflater inflater){
        //画笔弹窗布局
        popupStrokeLayout = inflater.inflate(R.layout.popup_sketch_stroke, null);
        strokeImageView = (ImageView) popupStrokeLayout.findViewById(R.id.stroke_circle);
        strokeAlphaImage = (ImageView) popupStrokeLayout.findViewById(R.id.stroke_alpha_circle);
        strokeSeekBar = (SeekBar) (popupStrokeLayout.findViewById(R.id.stroke_seekbar));
        strokeAlphaSeekBar = (SeekBar) (popupStrokeLayout.findViewById(R.id.stroke_alpha_seekbar));

        //画笔颜色
        strokeTypeRG = (RadioGroup) popupStrokeLayout.findViewById(R.id.stroke_type_radio_group);
        strokeColorRG = (RadioGroup) popupStrokeLayout.findViewById(R.id.stroke_color_radio_group);
    }

    /**
     * 橡皮擦弹窗布局
     * @param inflater
     */
    private void initEraserPopupLayout(LayoutInflater inflater){
        // 橡皮擦弹窗布局
        popupEraserLayout = inflater.inflate(R.layout.popup_sketch_eraser, null);
        popupEraserCircleBtn = popupEraserLayout.findViewById(R.id.rl_btn_eraser_circle);
        popupEraserBtn = popupEraserLayout.findViewById(R.id.rl_btn_eraser);

        eraserSizeTip = (TextView) popupEraserLayout.findViewById(R.id.eraser_size_tip);
        eraserSizeChangeRL = popupEraserLayout.findViewById(R.id.rl_erase_size_change);
        eraserImageView = (ImageView) popupEraserLayout.findViewById(R.id.stroke_circle);
        eraserSeekBar = (SeekBar) (popupEraserLayout.findViewById(R.id.stroke_seekbar));

        // 监听事件
        popupEraserCircleBtn.setOnClickListener(eraserPopupClickListener);
        popupEraserBtn.setOnClickListener(eraserPopupClickListener);
        //橡皮擦宽度拖动条
        eraserSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eraserPopupWindow.dismiss();//改变橡皮擦大小接收后隐藏弹框
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setSeekBarProgress(progress, STROKE_TYPE_ERASER);
            }
        });

        eraserSeekBar.setProgress(DEFAULT_ERASER_SIZE);
    }

    /**
     * 橡皮擦中弹框点击事件
     */
    private View.OnClickListener eraserPopupClickListener = v -> {
        // 防止快速重复点击
        if (ViewUtils.isFastDoubleClick()){
            Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
            return;
        }

        int id = v.getId();
        if (id == R.id.rl_btn_eraser_circle){// 点击圈选
            popupEraserCircleBtn.setBackgroundResource(selectedBG);
            popupEraserBtn.setBackground(defaultBG);

            eraserSizeTip.setVisibility(View.INVISIBLE);
            eraserSizeChangeRL.setVisibility(View.INVISIBLE);

            // 切换为圈选擦除模式
            btnEraser.setImageResource(R.mipmap.btn_eraser_circle);
            lastEraserType = DrawConsts.STROKE_TYPE_ERASER_CIRCLE;
            mSketchView.setStrokeType(DrawConsts.STROKE_TYPE_ERASER_CIRCLE);
            mSketchView.setEditMode(DrawConsts.EDIT_STROKE);

            eraserPopupWindow.dismiss();//改变橡皮擦大小接收后隐藏弹框

            eraserText.setText(getResources().getString(R.string.tab_eraser_circle));
        } else if (id == R.id.rl_btn_eraser){
            popupEraserCircleBtn.setBackground(defaultBG);
            popupEraserBtn.setBackgroundResource(selectedBG);

            eraserSizeTip.setVisibility(View.VISIBLE);
            eraserSizeChangeRL.setVisibility(View.VISIBLE);

            // 切换为点迹擦除模式
            btnEraser.setImageResource(R.mipmap.btn_eraser);
            lastEraserType = STROKE_TYPE_ERASER;
            mSketchView.setStrokeType(STROKE_TYPE_ERASER);
            mSketchView.setEditMode(DrawConsts.EDIT_STROKE);

            eraserText.setText(getResources().getString(R.string.tab_eraser_point));
        }
    };

    /**
     * 文本录入弹窗布局
     * @param inflater
     */
    private void initTextPopupLayout(LayoutInflater inflater){
        //文本录入弹窗布局
        popupTextLayout = inflater.inflate(R.layout.popup_sketch_text, null);
        strokeET = (EditText) popupTextLayout.findViewById(R.id.text_pupwindow_et);
    }

    /**
     * 滑块事件
     */
    private SlideButton.OnSlideButtonClickListener onSlideButtonClickListener = isChecked -> {
        if (isChecked){// 打开状态
            LogUtils.d("打开滑块");
            menuShowSwitchRL.setVisibility(View.GONE);
            menuShowEditEncryptRL.setVisibility(View.VISIBLE);
            mEncryptText.setVisibility(View.VISIBLE);
            encryptTip.setVisibility(View.INVISIBLE);

            mNumPopupView.showNumPopupWindow(btnMenu);
        } else {// 关闭状态
            menuShowSwitchRL.setVisibility(View.VISIBLE);
            encryptTip.setVisibility(View.VISIBLE);
            showEncryptRL.setVisibility(View.GONE);

            editTextEncrypt.setText("");
            menuShowEditEncryptRL.setVisibility(View.GONE);

            // 上传文件，生成二维码
            encrypt = "";

            LogUtils.d("关闭滑块");
            String md5filename = aCache.getAsString(CommConsts.KEY_MD5_FILE_NAME);
            mShareFileHelper.changeFilePwd(md5filename, "");
        }
    };

    /**
     * 展开菜单按钮后的点击事件
     */
    public View.OnClickListener menuOnClickListener = v -> {
        // 防止快速重复点击
        if (ViewUtils.isFastDoubleClick()){
            Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
            return;
        }

        selectRequest = -1;
        int id = v.getId();
        if (id == R.id.menu_btn_select_photo){// 选择图片
            selectRequest = REQUEST_IMAGE;
        } else if (id == R.id.menu_btn_select_file){// 选择文件
            selectRequest = REQUEST_SELECT_DOC;
            LogUtils.d("选择文件");
        } else if (id == R.id.menu_btn_select_video){// 选择视频
            selectRequest = REQUEST_SELECT_VIDEO;
            LogUtils.d("选择视频");
        } else if (id == R.id.menu_btn_select_bg){// 更换背景
            selectRequest = REQUEST_BACKGROUND;
            LogUtils.d("更换背景");
        } else if (id == R.id.menu_btn_save){// 保存
            LogUtils.d("保存画板记录");

            if (mSketchView.getRecordCount(null) == 0) {
                Toast.makeText(activity, getResources().getString(R.string.tip_is_empty_sketch), Toast.LENGTH_SHORT).show();
            } else {
                // 保存图片记录到本地
                btnSaveDialog();
            }

            menuPopupWindow.dismiss();
        } else if (id == R.id.menu_btn_scan_share){// 扫码分享
            // 生成图片，上传图片，生成二维码
            if (NetUtil.NETWORK_NONE == NetUtil.getNetWorkState(activity.getApplicationContext())){
                LogUtils.i("网络连接异常，请检查网络。");
                Toast.makeText(activity, getResources().getString(R.string.network_error_tip), Toast.LENGTH_LONG).show();
                return;
            }

            menuShowSwitchRL.setVisibility(View.GONE);
            showEncryptRL.setVisibility(View.GONE);
            menuShowEditEncryptRL.setVisibility(View.GONE);

            LogUtils.d("扫码分享");
            // 只有一个界面，并且没有画笔记录时
            if (sketchDataList != null && !sketchDataList.isEmpty()){
                // 判断是否存在画笔记录
                boolean flagExist = false;
                for (SketchData sketchData:sketchDataList){
                    flagExist = mSketchView.getRecordCount(sketchData) != 0;
                    if (flagExist){
                        break;
                    }
                }

                // 存在画笔记录
                if (flagExist){
                    menuScanShareLayout.setVisibility(View.VISIBLE);
                    menuDefaultLayout.setVisibility(View.GONE);

                    // 保存图片记录到本地
                    isUploadToServer = true;
                    String fileName = TimeUtils.getNowTimeString();
                    saveFileToLocal(fileName);
                } else {// 不存在画笔记录
                    Toast.makeText(activity, getResources().getString(R.string.tip_is_empty_sketch), Toast.LENGTH_SHORT).show();
                }
            }
        }  else if (id == R.id.rl_menu_back){// 返回
            // 生成图片，上传图片，生成二维码
            LogUtils.d("从扫码分享返回到菜单的默认界面");
            menuShowSwitchRL.setVisibility(View.GONE);
            menuScanShareLayout.setVisibility(View.GONE);
            menuDefaultLayout.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_edit_encrypt){// 点击编辑密码
            LogUtils.d("点击编辑密码");
            menuShowSwitchRL.setVisibility(View.GONE);
            menuShowEditEncryptRL.setVisibility(View.VISIBLE);

            mNumPopupView.showNumPopupWindow(btnMenu);
        } else if (id == R.id.menu_btn_exist) {// 退出应用
            exist();
        }

        // 有打开文件的请求
        if (selectRequest > 0){
            int photoCount = mSketchView.getPhotoRecordCount();
            if (selectRequest == REQUEST_IMAGE
                    && photoCount >= DrawConsts.SELECT_IMAGES_MAX){
                Toast.makeText(activity, getResources().getString(R.string.tip_is_image_max, DrawConsts.SELECT_IMAGES_MAX + ""), Toast.LENGTH_LONG).show();
            } else {
                selectFileFromSDTask();// 打开文件夹
            }
            menuPopupWindow.dismiss();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getSketchSize();
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 获得画板界面尺寸
     */
    private void getSketchSize() {
        ViewTreeObserver vto = mSketchView.getViewTreeObserver();
        vto.addOnPreDrawListener(() -> {
            if (sketchViewHeight == 0 && sketchViewWidth == 0) {
                int height = mSketchView.getMeasuredHeight();
                int width = mSketchView.getMeasuredWidth();
                sketchViewHeight = height;
                sketchViewWidth = width;
                sketchViewRight = mSketchView.getRight();
                sketchViewBottom = mSketchView.getBottom();
                Log.i("onPreDraw", sketchViewHeight + "  " + sketchViewWidth);
                decorHeight = activity.getWindow().getDecorView().getMeasuredHeight();
                decorWidth = activity.getWindow().getDecorView().getMeasuredWidth();
                Log.i("onPreDraw", "decor height:" + decorHeight + "   width:" + decorHeight);
                int height3 = controlLayout.getMeasuredHeight();
                int width3 = controlLayout.getMeasuredWidth();
                Log.i("onPreDraw", "controlLayout  height:" + height3 + "   width:" + width3);
            }
            return true;
        });
        Log.i("getSketchSize", sketchViewHeight + "  " + sketchViewWidth);
    }

    /**
     * 设置橡皮擦进度条
     *
     * @param progress
     * @param drawMode
     */
    protected void setSeekBarProgress(int progress, int drawMode) {
        int calcProgress = progress > 1 ? progress : 1;
        int newSize = Math.round((size / 100f) * calcProgress);
        int offset = Math.round((size - newSize) / 2);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(newSize, newSize);
        lp.setMargins(offset, offset, offset, offset);
        if (drawMode == STROKE_TYPE_DRAW) {
            strokeImageView.setLayoutParams(lp);
        } else {
            eraserImageView.setLayoutParams(lp);
        }
        mSketchView.setSize(newSize, drawMode);
    }

    @Override
    public void onDrawChanged() {
        mHandler.postDelayed(() -> {
            // 撤销Undo
            float undoAlpha = DrawConsts.BTN_ALL_SHOW;
            boolean undoClickable = true;
            if (mSketchView.getStrokeRecordCount() <= 0){
                undoAlpha = DrawConsts.BTN_ALPHA;
                undoClickable = false;
            }
            rlUndo.setAlpha(undoAlpha);
            rlUndo.setClickable(undoClickable);

            // 恢复Redo
            float redoAlpha = DrawConsts.BTN_ALL_SHOW;
            boolean redoClickable = true;
            if (mSketchView.getRedoCount() <= 0){
                redoAlpha = DrawConsts.BTN_ALPHA;
                redoClickable = false;
            }
            rlRedo.setAlpha(redoAlpha);
            rlRedo.setClickable(redoClickable);

        }, 10);
    }

    /**
     * 更新图片Adapter
     */
    private void updateGV() {
        sketchGVAdapter.notifyDataSetChanged();
    }

    /**
     * 更新页面索引值
     *
     * @param curPageIndex
     */
    private void updatePageShowNum(int curPageIndex){
        String pageNumStr = (curPageIndex + 1) + "";
        pageNum.setText(pageNumStr);

        // 判断btnPagePrev是否可用点击
        boolean prevClickable = true;
        float preAlpha = DrawConsts.BTN_ALL_SHOW;
        if (curPageIndex == 0){
            prevClickable = false;
            preAlpha = DrawConsts.BTN_ALPHA;
        }
        btnPagePrev.setAlpha(preAlpha);
        btnPagePrev.setClickable(prevClickable);

        // 判断btnPageNext是否可用点击
        int curPageNums = sketchDataList.size();
        boolean nextClickable = true;
        float nextAlpha = DrawConsts.BTN_ALL_SHOW;
        if ((curPageIndex + 1) == curPageNums){
            nextClickable = false;
            nextAlpha = DrawConsts.BTN_ALPHA;
        }
        btnPageNext.setAlpha(nextAlpha);
        btnPageNext.setClickable(nextClickable);
    }

    /**
     * 更新添加白板按钮
     */
    private void updateBtnAdd(){
        // 判断白板数量是否达到最大值
        if (sketchDataList.size() == CommConsts.PAGES_MAX){
            btnAdd.setAlpha(DrawConsts.BTN_ALPHA);
            btnAdd.setClickable(false);
        } else {
            btnAdd.setAlpha(DrawConsts.BTN_ALL_SHOW);
            btnAdd.setClickable(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (mSketchView.isTouch){// 绘制过程中，点击其他按钮无效
//            Toast.makeText(activity, getResources().getString(R.string.drawing_not_click_other_tip), Toast.LENGTH_SHORT).show();
            return;
        }

        // 防止快速重复点击
        if (ViewUtils.isFastDoubleClick()){
//            Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
            return;
        }

        int id = v.getId();
        if (id == R.id.rl_menu){// 菜单
            menuScanShareLayout.setVisibility(View.GONE);
            menuDefaultLayout.setVisibility(View.VISIBLE);

            showMenuPopupWindow(v);
        } else if (id == R.id.right_menu_btn_add) {// 添加新的画布
            // 保留当前数据
            saveCurSketchData(curPageIndex);
            SketchData newSketchData = new SketchData();
            sketchDataList.add(newSketchData);
            mSketchView.updateSketchData(newSketchData);
            showSketchView(true);
            sketchGVAdapter.notifyDataSetChanged();

            // 切换画笔状态
            mSketchView.setEditMode(EDIT_STROKE);//切换笔画编辑模式
            mSketchView.setStrokeType(STROKE_TYPE_DRAW);

            // 更新页面索引值
            curPageIndex = sketchDataList.size() - 1;
            updatePageShowNum(curPageIndex);

            // 更新添加白板按钮
            setStrokeBtn(rlBtnStroke);
            updateBtnAdd();
        } else if (id == R.id.rl_page_prev) {// 切换到前一个画布
            if (curPageIndex != 0){// 当前页面不是第一个页面
                // 保留当前数据
                saveCurSketchData(curPageIndex);

                // 更新画笔数据
                curPageIndex -= 1;
                SketchData sketchData = sketchDataList.get(curPageIndex);
                mSketchView.updateSketchData(sketchData);

                showSketchView(true);
                updatePageShowNum(curPageIndex);
            }
        }  else if (id == R.id.rl_pages_show) {// 显示所有的画布
            if (mSketchView.getVisibility() == View.VISIBLE) {
                int size = sketchDataList.size();
                if (curPageIndex < size ){
                    // 保留当前数据
                    saveCurSketchData(curPageIndex);
                }

                showSketchView(false);
            } else {
                showSketchView(true);
            }

            updateGV();
        }  else if (id == R.id.rl_page_next) {// 切换到下一个画布
            int size = sketchDataList.size();
            if ((curPageIndex + 1) != size ){// 当前页面不是最后一个页面
                // 保留当前数据
                saveCurSketchData(curPageIndex);

                // 添加新的画板页面
                curPageIndex += 1;
                SketchData sketchData = sketchDataList.get(curPageIndex);
                mSketchView.updateSketchData(sketchData);

                showSketchView(true);
                updatePageShowNum(curPageIndex);
            }
        } else if (id == R.id.rl_stroke) {// 画笔
            setStrokeBtn(v);

            if ((mSketchView.getEditMode() == DrawConsts.EDIT_STROKE)
                    && (mSketchView.getStrokeType() != STROKE_TYPE_ERASER)
                    && (mSketchView.getStrokeType() != DrawConsts.STROKE_TYPE_ERASER_CIRCLE)
                    && (mSketchView.getStrokeType() != DrawConsts.STROKE_TYPE_ERASER_RECT)) {
                showParamsPopupWindow(v, STROKE_TYPE_DRAW);
            } else {
                int checkedId = strokeTypeRG.getCheckedRadioButtonId();

                if (checkedId == R.id.stroke_type_rbtn_draw) {
                    strokeType = STROKE_TYPE_DRAW;
                } else if (checkedId == R.id.stroke_type_rbtn_line) {
                    strokeType = STROKE_TYPE_LINE;
                } else if (checkedId == R.id.stroke_type_rbtn_circle) {
                    strokeType = STROKE_TYPE_CIRCLE;
                } else if (checkedId == R.id.stroke_type_rbtn_rectangle) {
                    strokeType = STROKE_TYPE_RECTANGLE;
                } else if (checkedId == R.id.stroke_type_rbtn_text) {
                    strokeType = STROKE_TYPE_TEXT;
                }

                mSketchView.setStrokeType(strokeType);
            }

            mSketchView.setEditMode(DrawConsts.EDIT_STROKE);
        } else if (id == R.id.rl_eraser) {// 橡皮擦
            setStrokeBtn(v);
            if (mSketchView.getEditMode() == DrawConsts.EDIT_STROKE
                    && (mSketchView.getStrokeType() == STROKE_TYPE_ERASER
                    || mSketchView.getStrokeType() == DrawConsts.STROKE_TYPE_ERASER_CIRCLE)) {
//                showParamsPopupWindow(v, DrawConsts.STROKE_TYPE_ERASER);
            } else {
                mSketchView.setStrokeType(lastEraserType);
            }
            mSketchView.setEditMode(DrawConsts.EDIT_STROKE);
        } else if (id == R.id.rl_undo) {// 撤销
            mSketchView.undo();
        } else if (id == R.id.rl_redo) {// 恢复
            mSketchView.redo();
        } else if (id == R.id.rl_drag) {// 手势拖拽
            setStrokeBtn(v);
            mSketchView.setEditMode(EDIT_MOVE_RECT);
        } else if (id == R.id.right_menu_btn_delete){// 删除
            if (mSketchView.getRecordCount(null) == 0) {// 当前没有绘图，不必弹出删除对话框
                Toast.makeText(activity, getResources().getString(R.string.tip_is_empty_sketch), Toast.LENGTH_SHORT).show();
            } else {
                askForClearAll();
            }
        }
    }

    /**
     * 设置按钮的状态
     *
     * @param iv
     */
    private void setStrokeBtn(View iv) {
        int id = iv.getId();
        if (id == R.id.rl_stroke){// 画笔
            rlBtnStroke.setBackgroundResource(selectedBG);
            rlBtnEraser.setBackground(defaultBG);
            rlBtnDrag.setBackground(defaultBG);
        } else if (id == R.id.rl_eraser){// 橡皮擦
            rlBtnStroke.setBackground(defaultBG);
            rlBtnEraser.setBackgroundResource(selectedBG);
            rlBtnDrag.setBackground(defaultBG);
        } else if (id == R.id.rl_drag){// 手势拖拽
            rlBtnStroke.setBackground(defaultBG);
            rlBtnEraser.setBackground(defaultBG);
            rlBtnDrag.setBackgroundResource(selectedBG);
        }
    }

    /**
     * 保存当前数据
     * @param pageIndex
     */
    private void saveCurSketchData(int pageIndex) {
        SketchData curSketchData = sketchDataList.get(pageIndex);
        curSketchData.thumbnailBM = mSketchView.getThumbnailResultBitmap();
    }

    /**
     * 请求清空画板
     */
    private void askForClearAll() {
        mTitle.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mMessage.setText(R.string.dialog_ask_clear_all_title);
        mMessage.setVisibility(View.VISIBLE);

        LogUtils.d("擦除绘制记录");
        mConfirm.setOnClickListener(v -> {
            // 防止快速重复点击
            if (ViewUtils.isFastDoubleClick()){
                Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
                return;
            }

            LogUtils.d("点击确认");
            sketchGVAdapter.notifyDataSetChanged();
            mSketchView.clearAll();

            // 设置为画笔模式
            mSketchView.setEditMode(EDIT_STROKE);//切换笔画编辑模式
            mSketchView.setStrokeType(strokeType);
            setStrokeBtn(rlBtnStroke);
            dialog.dismiss();
        });

        mCancel.setOnClickListener(v -> {
            // 防止快速重复点击
            if (ViewUtils.isFastDoubleClick()){
                Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
                return;
            }

            LogUtils.i("取消清空画板..");
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * 点击了保存对话框
     */
    private void btnSaveDialog() {
        LogUtils.d("保存画板内容");
        mTitle.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        mEditText.setVisibility(View.VISIBLE);
        if (isUploadToServer){
            mEditText.setText(TimeUtils.getNowTimeString());
        } else {
            // 获得文件夹名
            String relativeDir = mSaveFileHelper.createMeetingDirName(activity);
            mEditText.setText(relativeDir);
            mEditText.setSelection(relativeDir.length());//将光标移至文字末尾
        }

        mConfirm.setOnClickListener(v -> {
            // 防止快速重复点击
            if (ViewUtils.isFastDoubleClick()){
                Toast.makeText(activity, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
                return;
            }

            LogUtils.d("点击确认");
            ScreenUtils.hideInput(dialog.getCurrentFocus());
            String input = mEditText.getText().toString().trim();
            if (TextUtils.isEmpty(input)){
                Toast.makeText(activity, getResources().getString(R.string.tip_save_file_not_null), Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存到本地
            isUploadToServer = false;
            saveFileToLocal(input);
            dialog.dismiss();
        });

        mCancel.setOnClickListener(v -> {
            LogUtils.i("点击取消。");
            ScreenUtils.hideInput(dialog.getCurrentFocus());
            dialog.dismiss();
        });
        dialog.show();
        ScreenUtils.showInput(mSketchView);
    }

    /**
     * 打开系统文件管理器
     */
    private void openFileExplore(int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(FILE_TYPE);// VIDEO_UNSPECIFIED//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(intent, requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, getResources().getString(R.string.tip_install_file_manager), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        LogUtils.d("requestCode：" + requestCode);
        mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
        LogUtils.d("返回文件路径：" + mSelectPath);
        String path = "";
        if (mSelectPath != null || !mSelectPath.isEmpty()) {
            path = mSelectPath.get(0);
            if (TextUtils.isEmpty(path)){
                Toast.makeText(activity, getResources().getString(R.string.tip_select_path_error), Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(activity, getResources().getString(R.string.tip_select_path_error), Toast.LENGTH_LONG).show();
            return;
        }

        // 判断返回的请求类型
        if (requestCode == REQUEST_IMAGE) { // 选择图片
            if (!FileUtils.isImageFile(path)){
                Toast.makeText(activity, getResources().getString(R.string.tip_image_select), Toast.LENGTH_LONG).show();
                return;
            }

            LogUtils.d("插入图片");
            //加载图片
            mSketchView.addPhotoRecord(path);
            mSketchView.setEditMode(EDIT_PHOTO);
        } else if (requestCode == REQUEST_BACKGROUND) {//设置背景
            if (!FileUtils.isImageFile(path)){
                Toast.makeText(activity, getResources().getString(R.string.tip_image_select), Toast.LENGTH_LONG).show();
                return;
            }

            //加载图片设置画板背景
            // 获得背景图片
            mSketchView.setBackgroundByPath(path);
        } else if(requestCode == REQUEST_SELECT_VIDEO){// 选择视频文件
            // 判断是否为视频文件格式
            if (!FileUtils.isVideoFile(path)){
                Toast.makeText(activity, getResources().getString(R.string.tip_video_select), Toast.LENGTH_LONG).show();
                return;
            }
        }  else if(requestCode == REQUEST_SELECT_DOC){// 选择文档
            final String realPathFromURI = path;
            // 判断是否为文档文件格式
            if (!FileUtils.isDOCFile(path)){
                Toast.makeText(activity, getResources().getString(R.string.tip_document_select), Toast.LENGTH_LONG).show();
                return;
            }

            // 打开WPS
            mHandler.post(() -> AppUtils.openDocFileByWPS(activity, realPathFromURI));
        }
    }

    /**
     * 退出应用
     */
    private void exist(){
        if (activity != null && !activity.isFinishing()){
            activity.finish();
        }
    }

    /**
     * 显示菜单弹框
     *
     * @param anchor
     */
    private void showMenuPopupWindow(View anchor) {
        menuPopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF));
    }

    /**
     * 显示画笔参数弹框
     *
     * @param anchor
     * @param drawMode
     */
    private void showParamsPopupWindow(View anchor, int drawMode) {
        if (BitmapUtils.isLandScreen(activity)) {
            if (drawMode == STROKE_TYPE_DRAW) {
                strokePopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF));
            } else {
                eraserPopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF));
            }
        } else {
            if (drawMode == STROKE_TYPE_DRAW) {
                strokePopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF));
            } else {
                eraserPopupWindow.showAsDropDown(anchor, 0, ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF));
            }
        }
    }

    /**
     * 显示文字弹框
     *
     * @param anchor
     * @param record
     */
    private void showTextPopupWindow(View anchor, final StrokeRecord record) {
        strokeET.requestFocus();
        strokeET.setTextColor(mSketchView.getStrokeColor());
        textPopupWindow.showAsDropDown(anchor, record.textOffX, record.textOffY - mSketchView.getHeight());
        textPopupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        textPopupWindow.setOnDismissListener(() -> {
            if (!strokeET.getText().toString().equals("")) {
                record.text = strokeET.getText().toString();
                record.textPaint.setTextSize(strokeET.getTextSize());
                record.textWidth = strokeET.getMaxWidth();
                mSketchView.addTextStrokeRecord(record);
            }
        });
    }

    /**
     * 将文件保存到本地
     * @param pathName
     */
    private void saveFileToLocal(String pathName){
        savePathName = pathName;
        saveFileToSDTask();
    }

    /**
     * 保存文件到sdcard
     */
    @AfterPermissionGranted(PermissionConsts.REQUEST_STORAGE)
    private void saveFileToSDTask(){
        String[] permissins =  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, permissins)) {
            boolean isEncrypt = mSlideButton.isChecked();
            new SaveToFileTask(isEncrypt).execute(savePathName);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    PermissionConsts.REQUEST_STORAGE, permissins);
        }
    }

    /**
     * 打开文件选择对话框
     * @param requestType
     */
    private void openFileSelect(int requestType){
        Intent intent = new Intent(activity, FileSelectActivity.class);
        intent.putExtra(FileSelectActivity.EXTRA_REQUEST_TYPE, requestType);
        startActivityForResult(intent, requestType);
        LogUtils.d("启动文件浏览器对话框");
    }

    /**
     * 从SD卡中选择图片
     */
    @AfterPermissionGranted(PermissionConsts.REQUEST_STORAGE)
    private void selectFileFromSDTask(){
        String[] permissins =  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(activity, permissins)) {
            openFileSelect(selectRequest);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    PermissionConsts.REQUEST_STORAGE, permissins);
        }
    }

    /**
     * 保存单个图片到本地文件，耗时操作
     * @param saveFile 相对路径
     */
    public File saveInOI(String saveFile) {
        if (!saveFile.toLowerCase().contains(DrawConsts.IMAGE_SAVE_SUFFIX)){
            saveFile += DrawConsts.IMAGE_SAVE_SUFFIX;
        }

        // 创建保存的文件目录
        SketchData tmpSketchData = sketchDataList.get(curPageIndex);
        if (tmpSketchData != null
                && mSketchView.getRecordCount(null) > 0){
            Bitmap newBM = mSketchView.getResultBitmap();
            if (newBM == null){
                return null;
            }

            // 保存图片到sdcard中
            BitmapUtils.saveBitmapToSdcard(saveFile, newBM, 80);
        }

        return new File(saveFile);
    }

    /**
     * 保存多个图片到本地文件，耗时操作
     * @param saveDir 相对路径
     */
    public File saveAllInOI(String saveDir) {
        // 创建保存的文件目录
        File fileDir = new File(saveDir);
        if (fileDir.exists() && fileDir.isDirectory()){
            boolean delFlag = FileUtils.deleteFile(fileDir);
            LogUtils.d("fileDir.delete, delFlag->%s", delFlag);
        }

        fileDir.mkdirs();

        // 遍历保存画板记录
        if (!sketchDataList.isEmpty()){
            Bitmap newBM = null;
            String filePath = "";
            int len = sketchDataList.size();
            SketchData tmpSketchData = null;
            for (int i=0; i< len; i++){
                tmpSketchData = sketchDataList.get(i);
                if (tmpSketchData != null
                        && mSketchView.getRecordCount(tmpSketchData) > 0){
                    filePath = saveDir + File.separator + (i + 1) + DrawConsts.IMAGE_SAVE_SUFFIX;
                    newBM = mSketchView.getResultBitmap();

                    if (newBM == null){
                        continue;
                    }
                    // 保存图片到sdcard中
                    BitmapUtils.saveBitmapToSdcard(filePath, newBM, 80);
                }
            }
        }

        return fileDir;
    }

    /**
     * 保存文件异步任务
     */
    class SaveToFileTask extends AsyncTask<String, Void, File> {
        private boolean isEncrypt;

        public SaveToFileTask(boolean isEncrypt) {
            this.isEncrypt = isEncrypt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            LogUtils.tag(TAG).d("onPreExecute");

            if (!isUploadToServer){// 直接保存本地，不上传到服务器
                // 显示加载条
                if (mProgressBar.getVisibility() != View.VISIBLE){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                // 显示加载条
                showScanProgressBar();
            }
        }

        @Override
        protected File doInBackground(String... photoName) {
//            LogUtils.tag(TAG).d("doInBackground");
            File saveFile = null;

            // 不上传，只是本地保存
            if (!isUploadToServer){
                String saveDir = CommConsts.FILE_PATH_LOCAL_DIR + photoName[0];
                saveFile = saveAllInOI(saveDir); // 保存多个文件

                // 保存会议记录的名称信息
                mSaveFileHelper.saveNextMeetingDirName(activity, photoName[0]);
            } else { // 上传，只保存单个文件
                String savePath = CommConsts.FILE_PATH_SHARE_FILES_DIR + photoName[0] + DrawConsts.IMAGE_SAVE_SUFFIX;
                saveFile = saveInOI(savePath); // 保存单个文件
                // 生成PDF文件
                mShareFileHelper.uploadFileToServer(saveFile.getAbsolutePath(),
                        encrypt, isEncrypt, uploadFileHttpCallBack);
            }

            return saveFile;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
//            LogUtils.tag(TAG).d("onPostExecute");

            // 保存图片之后的提示
            String tips = "";
            if ((file != null)
                    && file.exists()){
                String dirInfo = "sdcard/WhiteBoard/" + file.getName();
                tips = getResources().getString(R.string.tip_save_successfully, dirInfo);// file.getAbsolutePath()
                // 通知图库
                AppUtils.noticeMediaScan(activity, file.getAbsolutePath());
            } else{
                tips = getResources().getString(R.string.tip_save_error);
            }

            // 隐藏加载条
            if (!isUploadToServer){// 直接保存本地，不上传到服务器
                // toast显示
                Toast.makeText(activity, tips, Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 显示身材二维码的进度条
     */
    private void showScanProgressBar(){
        // 显示加载条
        if (mScanCreateProgressBar.getVisibility() != View.VISIBLE){
            mScanCreateProgressBar.setVisibility(View.VISIBLE);
            mScanImage.setVisibility(View.GONE);
            mShowScanTv.setVisibility(View.GONE);
        }
    }

    /**
     * 显示二维码
     */
    private void showScanImage(){
        // 隐藏进度条
        mScanCreateProgressBar.setVisibility(View.GONE);

        // 显示滑块
        // 隐藏编辑密码
        menuShowEditEncryptRL.setVisibility(View.GONE);

        // 显示二维码
        menuShowSwitchRL.setVisibility(View.VISIBLE);
        boolean isEncrypt = mSlideButton.isChecked();
        if (isEncrypt){
            showEncryptRL.setVisibility(View.VISIBLE);
            mEncryptText.setVisibility(View.VISIBLE);

            String text = "";
            if (TextUtils.isEmpty(encrypt)){
                text = "请设置密码!";
            } else {
                text = String.format(encryptTextStr, encrypt);
            }

            mEncryptText.setText(text);
            encryptTip.setVisibility(View.GONE);
        } else {
            showEncryptRL.setVisibility(View.GONE);
            encryptTip.setVisibility(View.VISIBLE);
        }

        mScanImage.setVisibility(View.VISIBLE);
        mScanImage.setImageBitmap(qrcodeBitmap);

        mShowScanTv.setVisibility(View.VISIBLE);
        mShowScanTv.setText(R.string.menu_scan_share_tip);
    }

    /**
     * 显示二维码
     */
    private void showScanImageError(int type){
        // 隐藏进度条
        mScanCreateProgressBar.setVisibility(View.GONE);

        // 显示二维码
        showEncryptRL.setVisibility(View.GONE);
        menuShowSwitchRL.setVisibility(View.GONE);
        mScanImage.setVisibility(View.GONE);

        mShowScanTv.setVisibility(View.VISIBLE);
        int textID = R.string.menu_create_error_tip;
        if (type == 1){// 上传失败
            textID = R.string.menu_upload_error_tip;
        }
        mShowScanTv.setText(textID);
    }

    /**
     * UI异步处理
     */
    public static final class UIHandler extends Handler {
        WeakReference<WhiteBoardFragment> weakReference;

        public UIHandler(WhiteBoardFragment mFragment) {
            super();
            this.weakReference = new WeakReference<WhiteBoardFragment>(mFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WhiteBoardFragment mFragment = weakReference.get();

            if (mFragment == null || !mFragment.getUserVisibleHint()) {
                return;
            }
            // 开始处理更新UI
        }
    }

    /**
     * 上传文件的回调
     */
    private HttpCallBack<RespFileUpdate> uploadFileHttpCallBack = new HttpCallBack<RespFileUpdate>() {
        @Override
        public void onError() {
            // 生成二维码失败
            mHandler.postDelayed(() -> {
                showScanImageError(1);
            },10);
        }

        @Override
        public void onResponse(RespFileUpdate response) {
            List<FileInfo> data = response.getData();
            if (data != null && !data.isEmpty()){
                String md5filename = data.get(0).getMd5filename();
                String shareUrl = CommConsts.SHARE_URL + "/" + md5filename;
                LogUtils.d("shareUrl-->" + shareUrl);

                aCache.put(CommConsts.KEY_MD5_FILE_NAME, md5filename);

                // 生成二维码
                try{
                    int size = ScreenUtils.dip2px(activity, 100);
                    qrcodeBitmap = EncodingHandler.createQRCode(shareUrl, size);
                } catch (Exception e){
                    e.printStackTrace();
                    LogUtils.e("生成二维码失败!" + e.getMessage());
                    // 生成二维码失败
                    mHandler.postDelayed(() -> {
                        showScanImageError(0);
                    },10);
                    return;
                }

                // 显示二维码
                mHandler.postDelayed(() -> {
                    showScanImage();
                },10);
            } else {
                // 生成二维码失败
                mHandler.postDelayed(() -> {
                    showScanImageError(0);
                },10);
            }
        }
    };

}
