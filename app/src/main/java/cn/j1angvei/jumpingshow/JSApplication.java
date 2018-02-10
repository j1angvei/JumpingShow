package cn.j1angvei.jumpingshow;

import android.app.Application;
import android.media.projection.MediaProjection;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class JSApplication extends Application {

    private static JSApplication INSTANCE;

    private MediaProjection mMediaProjection;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static JSApplication getInstance() {
        return INSTANCE;
    }

    public void setProjectionData(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }

    public MediaProjection getMediaProjection() {
        return mMediaProjection;
    }


}
