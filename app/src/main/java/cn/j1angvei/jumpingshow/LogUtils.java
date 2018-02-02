package cn.j1angvei.jumpingshow;

import android.util.Log;

/**
 * @author j1angvei
 * @since 2018/1/30
 */

public class LogUtils {
    public static void i(Object msg) {
        String[] tagAndMethod = getTagAndMethod();
        Log.i(tagAndMethod[0], tagAndMethod[1] + " : " + msg.toString());
    }

    public static void d(Object msg) {
        String[] tagAndMethod = getTagAndMethod();
        Log.d(tagAndMethod[0], tagAndMethod[1] + " : " + msg.toString());
    }

    public static void e(Object msg, Throwable e) {
        String[] tagAndMethod = getTagAndMethod();
        Log.e(tagAndMethod[0], tagAndMethod[1] + " : " + msg.toString(), e);
    }

    private static String[] getTagAndMethod() {
        String tag = LogUtils.class.getSimpleName();
        String method = "log";
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("cn.j1angvei.jumpingshow")
                    && !element.getClassName().equals(LogUtils.class.getName())) {
                tag = element.getClass().getSimpleName();
                method = element.getMethodName();
                break;
            }
        }
        if (tag.length() > 23) {
            tag = tag.substring(0, 23);
        }
        return new String[]{tag, method};
    }
}
