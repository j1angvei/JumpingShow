package cn.j1angvei.jumpingshow;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.opencv.android.OpenCVLoader;

import java.util.List;
import java.util.Random;

/**
 * @author j1angvei
 * @since 2018/2/2
 */

public class BackstageService extends AccessibilityService {
    private static final String TAG = BackstageService.class.getSimpleName();
    private static final String GAME_ENTRY_TEXT = "跳一跳";
    private static final String GAME_ENTRY_WIDGET = "android.widget.TextView";

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

    private WindowManager mWindowManager;
    private ActionBar mActionBar;
    private ActionBar.ShowMode mShowMode;
    private ActionBar.OnActionListener mActionListener;
    private WindowManager.LayoutParams mLayoutParams;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (mShowMode == ActionBar.ShowMode.AUTOMATICALLY && gameEntryVisible(eventType, rootNode)) {
            try {
                addActionBar();
            } catch (Exception e) {
                Log.e(TAG, "onAccessibilityEvent: ", e);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AppUtils.toast(this, "onServiceConnected");
        try {
            OpenCVLoader.initDebug();
            Log.d(TAG, "onServiceConnected: init OpenCV success!");
        } catch (Exception e) {
            Log.e(TAG, "onServiceConnected: fail to load openCV libs", e);
        }
        PrefsUtils.setBackstageReady(this, true);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mShowMode = PrefsUtils.getActionBarShowMode(this);
        mPreferenceChangeListener = initPreferenceChangeListener();
        PrefsUtils.registerListener(this, mPreferenceChangeListener);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AppUtils.toast(this, "onUnbind");
        PrefsUtils.setBackstageReady(this, false);
        PrefsUtils.unregisterListener(this, mPreferenceChangeListener);
        mPreferenceChangeListener = null;
        try {
            removeActionBar();
        } catch (Exception e) {
            Log.e(TAG, "onUnbind: ", e);
        }
        return super.onUnbind(intent);
    }

    private boolean gameEntryVisible(int eventType, AccessibilityNodeInfo rootNode) {
        //只监测微信主界面下拉出现的“跳一跳”入口(非前面4个小程序需要继续左滑)
        if (eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            return false;
        }
        //root节点为空，没有监测到入口
        if (rootNode == null) {
            return false;
        }
        //遍历寻找root子节点中文本为“跳一跳”的子节点
        List<AccessibilityNodeInfo> textNodes = rootNode.findAccessibilityNodeInfosByText(GAME_ENTRY_TEXT);
        if (textNodes.isEmpty()) {
            return false;
        }
        //如果为TextView,则视为游戏入口
        for (AccessibilityNodeInfo nodeInfo : textNodes) {
            if (nodeInfo.getClassName().equals(GAME_ENTRY_WIDGET)) {
                return true;
            }
        }
        //排除聊天界面出现的“跳一跳”文字，不是游戏入口
        return false;
    }

    /**
     * 显示辅助动作栏
     */
    private void addActionBar() {
        if (mActionBar != null) {
            Log.d(TAG, "ActionBar already added");
            return;
        }

        mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        mLayoutParams.verticalMargin = 0.15f;

        mActionBar = new ActionBar(this);
        mWindowManager.addView(mActionBar, mLayoutParams);

        //回调函数，包括点击跳跃，拖曳窗口
        mActionListener = new ActionBar.OnActionListener() {
            @Override
            public void onDragBar(float dx, float dy) {
                mLayoutParams.x += dx;
                mLayoutParams.y += dy;
                mWindowManager.updateViewLayout(mActionBar, mLayoutParams);
            }

            @Override
            public void onRemoveBar() {
                removeActionBar();
            }

            @Override
            public void onJump(Point pressPosition, int pressDuration) {
                float x = pressPosition.x;
                float y = pressPosition.y;
                Path pressPath = new Path();
                pressPath.moveTo(x, y);
                pressPath.lineTo(x + new Random().nextInt(10), y + new Random().nextInt(10));
                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(pressPath, 100, pressDuration));
                Log.d(TAG, "onJump: Start jump");
                try {

                    dispatchGesture(builder.build(), new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            Log.d(TAG, "onCompleted: gesture");
                            super.onCompleted(gestureDescription);
                        }
                    }, null);

                } catch (Exception e) {
                    Log.e(TAG, "onJump: gesture  ", e);
                }
            }


        };
        mActionBar.setActionListener(mActionListener);

    }

    /**
     * 移除辅助动作栏
     */
    private void removeActionBar() {
        if (mActionBar == null) {
            Log.d(TAG, "ActionBar already removed");
            return;
        }
        mWindowManager.removeView(mActionBar);
        mActionBar = null;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener initPreferenceChangeListener() {
        return (sharedPreferences, key) -> {
            switch (key) {
                //主开关关闭时，移除动作栏
                case PrefsUtils.KEY_MAIN_SWITCH:
                    if (!PrefsUtils.isMainSwitchOn(BackstageService.this)) {
                        removeActionBar();
                        break;
                    }
                    //主开关打开时，根据显示模式设置动作栏
                case PrefsUtils.KEY_ACTION_BAR_SHOW_MODE:
                    mShowMode = PrefsUtils.getActionBarShowMode(BackstageService.this);
                    if (mShowMode == ActionBar.ShowMode.MANUALLY) {
                        addActionBar();
                    } else {
                        removeActionBar();
                    }
                    break;

                default:
                    break;
            }
        };
    }
}
