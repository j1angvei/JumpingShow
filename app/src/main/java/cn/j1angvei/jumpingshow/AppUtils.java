package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author j1angvei
 * @since 2018/2/3
 */

public class AppUtils {
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toConfigs(Context context) {
        Intent intent = new Intent(context, ConfigActivity.class);
        context.startActivity(intent);
    }

    public static void toAccessibility(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }


    public static void requestScreenCapturePermission(Context context) {
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(PermissionActivity.EXTRA_PERMISSION, PermissionActivity.VALUE_SCREEN_CAPTURE);
        context.startActivity(intent);
    }

    public static int[] getScreenParams(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(metrics);
        Point realSize = new Point();
        wm.getDefaultDisplay().getRealSize(realSize);
        return new int[]{realSize.x, realSize.y, metrics.densityDpi};
    }

    /**
     * todo 待续
     *
     * @param context
     * @return
     */
    public static JumpParams getJumpParams(Context context) {
        return new JumpParams(
                PrefsUtils.getScalingRatio(context),
                PrefsUtils.getBottomCenterPercentage(context),
                PrefsUtils.getTriangleVerticalRatio(context),
                PrefsUtils.getTriangleBevelRatio(context),
                PrefsUtils.getCannyLowerThreshold(context),
                PrefsUtils.getCannyUpperThreshold(context),
                PrefsUtils.getJumpFactor(context),
                PrefsUtils.isStoreMat(context)
        );
    }
}
