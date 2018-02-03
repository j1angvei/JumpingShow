package cn.j1angvei.jumpingshow;

import android.app.Application;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class JSApplication extends Application {
    private static JSApplication INSTANCE;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static JSApplication getInstance() {
        return INSTANCE;
    }

}
