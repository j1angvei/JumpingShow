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

}

