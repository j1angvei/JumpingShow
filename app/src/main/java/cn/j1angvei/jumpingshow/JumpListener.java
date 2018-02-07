package cn.j1angvei.jumpingshow;

import android.graphics.Point;

/**
 * @author j1angvei
 * @since 2018/2/7
 */

public interface JumpListener {
    /**
     *
     */
    void onInit();

    /**
     *
     * @param pressDuration
     */
    void onReady(Point pressPosition, int pressDuration);
}
