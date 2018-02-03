package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
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
        intent.putExtra(PermissionActivity.EXTRA_REQUESTED_PERMISSION, PermissionActivity.Permission.SCREEN_CAPTURE);
        context.startActivity(intent);
    }
}
