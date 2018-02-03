package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.widget.Toast;

/**
 * @author j1angvei
 * @since 2018/2/3
 */

public class AppUtils {
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
