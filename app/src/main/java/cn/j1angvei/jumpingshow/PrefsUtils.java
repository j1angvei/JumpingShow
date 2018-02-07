package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static cn.j1angvei.jumpingshow.ActionBar.ShowMode;

/**
 * @author j1angvei
 * @since 2018/2/2
 */

public class PrefsUtils {
    /**
     * 读取首选项
     *
     * @param context 上下文
     * @return 默认的SharedPreferences
     */
    private static SharedPreferences read(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 存储首选项
     *
     * @param context 上下文
     * @return 默认的SharedPreferences编辑器
     */
    private static SharedPreferences.Editor write(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit();
    }

    public static void registerListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        read(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        read(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static final String KEY_ACTION_BAR_SHOW_MODE = "action_bar_show_mode";

    public static ShowMode getActionBarShowMode(Context context) {
        ShowMode showMode = ShowMode.MANUALLY;

        String defaultValue = context.getString(showMode.getValueId());
        String value = read(context).getString(KEY_ACTION_BAR_SHOW_MODE, defaultValue);

        for (ShowMode mode : ShowMode.values()) {
            if (context.getString(mode.getValueId()).equals(value)) {
                showMode = mode;
                break;
            }
        }

        return showMode;
    }

    public static final String KEY_MAIN_SWITCH = "main_switch";

    public static boolean isMainSwitchOn(Context context) {
        return read(context).getBoolean(KEY_MAIN_SWITCH, false);
    }

    public static final String KEY_BACKSTAGE_READY = "backstage_ready";

    public static boolean isBackstageReady(Context context) {
        return read(context).getBoolean(KEY_BACKSTAGE_READY, false);
    }

    public static void setBackstageReady(Context context, boolean isReady) {
        write(context).putBoolean(KEY_BACKSTAGE_READY, isReady).apply();
    }

    public static final String KEY_SCALE_RATIO = "bottle_scale_ratio";

    public static float getScalingRatio(Context context) {
        String ratio= read(context).getString("scaling_ratio", "1.1818");
        return Float.valueOf(ratio);
    }

    public static final String KEY_BOTTOM_CENTER_3D = "bottom_center_3d";

    public static  float getBottomCenterPercentage(Context context) {
        String percentage= read(context).getString(KEY_BOTTOM_CENTER_3D, "0.910112");
        return Float.parseFloat(percentage);
    }
    public static  float getTriangleVerticalRatio(Context context){
        String ratio= read(context).getString("triangle_vertical_ratio","0.58");
        return Float.parseFloat(ratio);
    }
    public static  float getTriangleBevelRatio(Context context){
        String ratio= read(context).getString("triangle_bevel_ratio","1.156");
        return Float.parseFloat(ratio);
    }

    public static double getCannyLowerThreshold(Context context){
        String threshold= read(context).getString("canny_lower_threshold","40");
        return Double.parseDouble(threshold);
    }

    public static double getCannyUpperThreshold(Context context){
        String threshold= read(context).getString("canny_upper_threshold","80");
        return Double.parseDouble(threshold);
    }

    public static float getJumpFactor(Context context){
        String factor= read(context).getString("jump_factor","1.392");
        return Float.parseFloat(factor);
    }

}

