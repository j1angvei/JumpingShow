package cn.j1angvei.jumpingshow;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class ConfigFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.configs);
        //主开关检测无障碍服务是否开启
        findPreference(PrefsUtils.KEY_MAIN_SWITCH).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean newBoolValue = (boolean) newValue;
            //用户关闭开关
            if (!newBoolValue) {
                return true;
            }
            //用户打开开关,无障碍服务是否打开
            boolean backstageReady = PrefsUtils.isBackstageReady(getContext());

            if (!backstageReady) {
                Snackbar.make(getView(), "无障碍服务尚未打开", Snackbar.LENGTH_LONG)
                        .setAction("去打开", v -> {
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            getContext().startActivity(intent);
                        }).show();
            }
            return backstageReady;
        });

    }

}
