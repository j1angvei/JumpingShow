package cn.j1angvei.jumpingshow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
                Snackbar.make(getView(), "开启无障碍服务才能使用辅助程序", Snackbar.LENGTH_LONG)
                        .setAction("开启", v -> AppUtils.toAccessibility(getContext())).show();
            }
            return backstageReady;
        });

        findPreference("save_screenshot").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //用户关闭开关
                boolean newBoolValue = (boolean) newValue;

                //关闭开关
                if (!newBoolValue) {
                    return true;
                }

                //打开开关，先检查权限
                boolean granted = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (!granted) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 148);
                }
                return granted;
            }
        });

    }

}
