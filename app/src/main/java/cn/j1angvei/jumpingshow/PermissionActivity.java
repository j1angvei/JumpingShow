package cn.j1angvei.jumpingshow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * 申请权限（录制屏幕，存储权限，开启无障碍服务等）
 *
 * @author j1angvei
 * @since 2018/2/1
 */

public class PermissionActivity extends AppCompatActivity {

    public static final String EXTRA_REQUESTED_PERMISSION = "PermissionActivity.extra_requested_permission";

    private static String[] STORAGE_PERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int REQUEST_STORAGE = 134;
    private static final int REQUEST_SCREEN_CAPTURE = 145;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_REQUESTED_PERMISSION)) {
            Permission permission = (Permission) intent.getSerializableExtra(EXTRA_REQUESTED_PERMISSION);
            requestPermission(permission);
        } else {
            AppUtils.toast(this, "未指定需要申请的权限");
            onBackPressed();
        }
    }

    private void requestPermission(Permission permission) {
        switch (permission) {
            case WRITE_STORAGE:
                //检查是否已经授予存储权限
                boolean alreadyGranted = PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(this, STORAGE_PERMISSION[0]);
                if (!alreadyGranted) {
                    ActivityCompat.requestPermissions(this, STORAGE_PERMISSION, REQUEST_STORAGE);
                } else {
                    AppUtils.toast(this, "已经授予存储权限");
                    setResult(RESULT_OK);
                    onBackPressed();
                }
                break;
            case SCREEN_CAPTURE:
                boolean granted = JSApplication.getInstance().getMediaProjection() != null;
                if (granted) {
                    AppUtils.toast(this, "已经授予录屏权限");
                    onBackPressed();
                } else {
                    MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                    if (mpm != null) {
                        startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_SCREEN_CAPTURE);
                    }
                }
                break;
            case ACCESSIBILITY_SERVICE:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SCREEN_CAPTURE) {
            JSApplication.getInstance().setProjectionData(resultCode,data);
            boolean granted = resultCode == RESULT_OK;
            String hint = granted ? "已经授予录屏权限" : "授权失败，无法进行屏幕截图";
            int result = granted ? RESULT_OK : RESULT_CANCELED;
            AppUtils.toast(this, hint);
            setResult(result);
            onBackPressed();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            String hint = granted ? "授予存储权限" : "保存出错的处理结果需要存储权限";
            int result = granted ? RESULT_OK : RESULT_CANCELED;
            AppUtils.toast(this, hint);
            setResult(result);
            onBackPressed();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public enum Permission {
        WRITE_STORAGE,
        SCREEN_CAPTURE,
        ACCESSIBILITY_SERVICE
    }
}
