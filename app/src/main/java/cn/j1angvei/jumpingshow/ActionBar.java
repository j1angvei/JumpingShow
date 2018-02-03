package cn.j1angvei.jumpingshow;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

public class ActionBar extends LinearLayout implements View.OnTouchListener {
    private static final String TAG = ActionBar.class.getSimpleName();
    //checked为true时，自动进行截屏、跳跃操作，否则为手动
    private ToggleButton tbAuto;
    //计算按压时长并点击屏幕
    private ImageButton ibJump;
    //前往设置界面
    private ImageButton ibConfig;
    //关闭操作栏并退出
    private ImageButton ibExit;

    private OnActionListener mActionListener;

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
        inflate(getContext(), R.layout.action_bar, this);

        setPadding(20, 20, 20, 20);
        setOrientation(HORIZONTAL);
//        setBackgroundColor(getResources().getColor(R.color.bg_action_bar));
        setBackgroundResource(R.drawable.bg_action_bar);

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
                ViewCompat.animate(ibJump).alpha(1).setStartDelay(100).setDuration(500);
                ViewCompat.animate(ibConfig).alpha(1).setStartDelay(200).setDuration(500);
                ViewCompat.animate(ibExit).alpha(1).setStartDelay(300).setDuration(500);
                return false;
            }
        });

        //点击事件，跳转到设置
        ibConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ConfigActivity.class);
                getContext().startActivity(intent);
            }
        });

        //点击事件，移除动作栏
        ibExit.setOnClickListener(v -> mActionListener.onRemove());

        //实现拖曳事件
        setOnTouchListener(this);

    }


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

    public void setActionListener(OnActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface OnActionListener {
        void onDrag(float dx, float dy);

        void onRemove();

    }
}
