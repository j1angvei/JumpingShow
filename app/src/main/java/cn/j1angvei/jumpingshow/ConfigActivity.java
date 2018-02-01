package cn.j1angvei.jumpingshow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * 用户配置所有参数：
 * 小瓶子身高缩放比例、
 * 跳跃系数、
 * 图片边界检测阈值、
 *
 * @author j1angvei
 * @since 2018/2/1
 */

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new ConfigFragment()).commit();

    }
}
