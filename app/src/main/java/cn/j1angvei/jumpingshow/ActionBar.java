package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * @author j1angvei
 * @since 2018/2/1
 */

public class ActionBar extends LinearLayout {
    //拖曳操作栏
    private ImageButton ibDrag;
    //checked为true时，自动进行截屏、跳跃操作，否则为手动
    private ToggleButton tbAuto;
    //checked为true时，替代用户点击，否则只进行计时
    private ToggleButton tbJump;
    //关闭操作栏并退出
    private ImageButton ibExit;

    public ActionBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.action_bar, this);
        ibDrag = findViewById(R.id.ib_hand);
        tbAuto = findViewById(R.id.tb_auto);
        tbJump = findViewById(R.id.tb_jump);
        ibExit = findViewById(R.id.ib_exit);
    }
}
