package cn.j1angvei.jumpingshow;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class ActionBar extends LinearLayout {

    private static final Object LOCK = new Object();

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

    public ActionBar(Context context) {
        this(context, null);
    }

    public ActionBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ActionBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        //初始化参数，成员变量等
        initArgs();
        //初始化控件元素
        initWidgets();
        //初始化点击、触摸等事件
        initListeners();
        //初始化View的数据显示等
        initData();
    }

    private void initArgs() {
        mMediaProjection = JSApplication.getInstance().getMediaProjection();
        int[] screenParams = AppUtils.getScreenParams(getContext());
        mWidth = screenParams[0];
        mHeight = screenParams[1];
        mDpi = screenParams[2];
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //不在处理图片过程，不做任何处理
                if (!mInProcess) {
                    return;
                }
                //已经得到一张截图，不再继续获取截图
                if (mScreenshotTaken) {
                    return;
                }
                reader.acquireLatestImage();

            }
        }, null);

    }

    private void initWidgets() {
        inflate(getContext(), R.layout.action_bar, this);

        tbAuto = findViewById(R.id.tb_auto);
        ibJump = findViewById(R.id.ib_jump);
        ibConfig = findViewById(R.id.ib_to_config);
        ibExit = findViewById(R.id.ib_exit);
    }

    private void initListeners() {
        //跳转到设置
        ibConfig.setOnClickListener(v -> AppUtils.toConfigs(getContext()));
        //点移除动作栏
        ibExit.setOnClickListener(v -> mActionListener.onRemove());
        //开始辅助跳跃
        ibJump.setOnClickListener(v -> {
            //录屏权限需要重新申请
            if (mMediaProjection == null) {
                AppUtils.toast(getContext(), "录屏权限失效，重新申请");
                AppUtils.requestScreenCapturePermission(getContext());
            } else {
                //录屏权限满足，可以截图
                mInProcess = true;
                ibJump.setEnabled(false);
                mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-capture",
                        mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mImageReader.getSurface(), null, null
                );
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
                        mActionListener.onDrag(currentX - lastX, currentY - lastY);
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

    private void initData() {
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
        postDelayed(() -> setBackgroundResource(R.drawable.bg_action_bar), 860);
    }

    /**
     * 在后台线程操作
     */
    private void jumpOnce() throws RuntimeException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Run jump operation in background");
        }


    }

    public void setActionListener(OnActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface OnActionListener {
        void onDrag(float dx, float dy);

        void onRemove();

    }

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

    public static class JumpTask extends AsyncTask<Image, Void, Float> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Float doInBackground(Image... images) {
            return null;
        }

        @Override
        protected void onPostExecute(Float aFloat) {
            super.onPostExecute(aFloat);
        }
    }
}
