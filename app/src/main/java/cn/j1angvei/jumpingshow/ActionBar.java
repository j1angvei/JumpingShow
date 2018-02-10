package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class ActionBar extends LinearLayout {
    private static final String TAG = ActionBar.class.getSimpleName();

    private ToggleButton tbAuto;
    private ImageButton ibJump;
    private ImageButton ibConfig;
    private ImageButton ibExit;

    private OnActionListener mActionListener;
    private MediaProjection mMediaProjection;
    private int mWidth, mHeight, mDpi;
    private ImageReader mImageReader;

    /**
     * true表示有一张截图正在处理
     */
    private boolean mInProcess;

    /**
     * true表示已经得到一张截图
     */
    private boolean mScreenshotTaken;
    private VirtualDisplay mVirtualDisplay;
    private Mat mJumper = new Mat();

    private JumpParams mJumpParams;
    private JumpListener mJumpListener;

    public ActionBar(Context context) {
        this(context, null);
    }

    public ActionBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            init();
        } catch (Exception e) {
            Log.e(TAG, "ActionBar: init", e);
        }
    }

    private void init() {
        //初始化参数，成员变量等
        initArgs();
        //初始化控件元素
        initUI();
        //初始化点击、触摸等事件
        initListeners();
    }

    private void initArgs() {
        mMediaProjection = JSApplication.getInstance().getMediaProjection();
        int[] screenParams = AppUtils.getScreenParams(getContext());
        mWidth = screenParams[0];
        mHeight = screenParams[1];
        mDpi = screenParams[2];

        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mImageReader.setOnImageAvailableListener(reader -> {

            try {
                Image image = reader.acquireLatestImage();
                if (image == null) {
                    Log.d(TAG, "initArgs: acquired image is null");
                    return;
                }

                if (!mInProcess || mScreenshotTaken) {
                    image.close();
                    Log.d(TAG, String.format("image available: inProcess, %s; screenshotTaken, %s.", mInProcess, mScreenshotTaken));

                } else {
                    Log.d(TAG, "initArgs: prepare to jump");
                    mScreenshotTaken = true;
//                    Image image = reader.acquireLatestImage();
                    new JumpTask(mJumper, mJumpParams, mJumpListener).execute(image);
                }
            } catch (Exception e) {
                Log.e(TAG, "initArgs: image available listener", e);
            }


        }, null);

        //初始化OpenCV的数据
        Mat jumperMat = ImageUtils.assetToMat(getContext(), "i_white.png");
        float scaleRatio = PrefsUtils.getScalingRatio(getContext());
        Size originalSize = jumperMat.size();
        Size scaledSize = new Size(originalSize.width * scaleRatio, originalSize.height * scaleRatio);
        Imgproc.resize(jumperMat, mJumper, scaledSize);

        mJumpParams = AppUtils.getJumpParams(getContext());
        mJumpListener = new JumpListener() {
            @Override
            public void onInit() {
                Log.d(TAG, "onInit: ");
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                    mVirtualDisplay = null;
                }
            }

            @Override
            public void onReady(Point pressPosition, int pressDuration) {
                Log.d(TAG, String.format("onReady: press position, %s; press duration, %s.",
                        pressPosition.toString(), pressDuration));
                setEnabled(false);
                mActionListener.onJump(pressPosition, pressDuration);
                //跳跃完成后，重置flag
                postDelayed(() -> {
                    setEnabled(true);
                    mInProcess = false;
                    mScreenshotTaken = false;
                    ibJump.setEnabled(true);
                }, pressDuration);

                //小人儿跳到下一块石头上，并且挺好，开始下一次跳跃
                if (tbAuto.isChecked()) {
                    postDelayed(() -> {
                        ibJump.performClick();
                        ibJump.setEnabled(false);
                    }, PrefsUtils.getStayTime(getContext()) + pressDuration * 2);
                }
            }
        };
    }


    private void initUI() {
        inflate(getContext(), R.layout.action_bar, this);

        tbAuto = findViewById(R.id.tb_auto);
        ibJump = findViewById(R.id.ib_jump);
        ibConfig = findViewById(R.id.ib_to_config);
        ibExit = findViewById(R.id.ib_exit);

        //添加动画，透明度从0到1
        ViewCompat.setAlpha(tbAuto, 0);
        ViewCompat.setAlpha(ibJump, 0);
        ViewCompat.setAlpha(ibConfig, 0);
        ViewCompat.setAlpha(ibExit, 0);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                ViewCompat.animate(tbAuto).alpha(1).setDuration(500);
                ViewCompat.animate(ibJump).alpha(1).setStartDelay(120).setDuration(500);
                ViewCompat.animate(ibConfig).alpha(1).setStartDelay(240).setDuration(500);
                ViewCompat.animate(ibExit).alpha(1).setStartDelay(360).setDuration(500);
                return false;
            }
        });

        //设置内边距，方便拖曳
        setPadding(20, 20, 20, 20);
        setOrientation(HORIZONTAL);
        //按钮展示完动画之后，再显示动作栏背景
        postDelayed(() -> setBackgroundResource(R.drawable.bg_action_bar), 860);
    }

    private void initListeners() {
        //跳转到设置
        ibConfig.setOnClickListener(v -> AppUtils.toConfigs(getContext()));
        //点移除动作栏
        ibExit.setOnClickListener(v -> {
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }
            mVirtualDisplay = null;
            if (mImageReader != null) {
                mImageReader.close();
            }
            mImageReader = null;
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
            JSApplication.getInstance().setProjectionData(null);
            mMediaProjection = null;

            mActionListener.onRemoveBar();
        });
        //开始辅助跳跃
        ibJump.setOnClickListener(v -> {
            //录屏权限需要重新申请
            try {
                if (mMediaProjection == null) {
                    AppUtils.toast(getContext(), "录屏权限失效，重新申请");
                    AppUtils.requestScreenCapturePermission(getContext());
                    postDelayed(() -> mMediaProjection = JSApplication.getInstance().getMediaProjection(), 2000);

                } else {
                    //录屏权限满足，可以截图
                    Log.d(TAG, "initListeners: pressed jump button ");
                    mInProcess = true;
                    if (mVirtualDisplay == null) {
                        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-capture",
                                mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                mImageReader.getSurface(), null, null
                        );
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "initListeners: ", e);
            }
        });

        //实现动作栏拖曳
        setOnTouchListener(new OnTouchListener() {
            //记录拖曳开始的上次坐标
            private float lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        v.setPressed(true);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float currentX = event.getRawX();
                        float currentY = event.getRawY();
                        mActionListener.onDragBar(currentX - lastX, currentY - lastY);
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                        v.setPressed(false);
                        return true;
                }
                return false;
            }
        });
    }

    public void setActionListener(OnActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface OnActionListener {
        void onDragBar(float dx, float dy);

        void onRemoveBar();

        void onJump(Point pressPosition, int pressDuration);

    }

    /**
     * 显示辅助动作栏的方式，
     * 手动打开，点击通知打开，不显示通知
     */
    public enum ShowMode {
        MANUALLY(R.string.action_bar_show_manually),
        AUTOMATICALLY(R.string.action_bar_show_automatically),
        NEVER(R.string.action_bar_show_never);

        private final int valueId;

        ShowMode(int valueId) {
            this.valueId = valueId;
        }

        public int getValueId() {
            return valueId;
        }
    }


}
