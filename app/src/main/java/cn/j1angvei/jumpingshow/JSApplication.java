package cn.j1angvei.jumpingshow;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class JSApplication extends Application {

    private static JSApplication INSTANCE;

    private MediaProjection mMediaProjection;
    private int mResultCode;
    private Intent mData;
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static JSApplication getInstance() {
        return INSTANCE;
    }

    public void setProjectionData(int resultCode, Intent data) {
        mResultCode = resultCode;
        mData = data;
    }

    private void initMediaProjection() {
        MediaProjectionManager mpm = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE));
        if (mpm != null) {
            mMediaProjection = mpm.getMediaProjection(mResultCode, mData);
        }
    }

    public MediaProjection getMediaProjection() {
        if (mMediaProjection == null && mResultCode != Activity.RESULT_CANCELED && mData != null) {
            initMediaProjection();
        }
        return mMediaProjection;
    }


}
