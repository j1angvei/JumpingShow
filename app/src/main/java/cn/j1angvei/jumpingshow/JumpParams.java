package cn.j1angvei.jumpingshow;

/**
 * @author j1angvei
 * @since 2018/2/7
 */

public final class JumpParams {
    /**
     * 小人儿的身高比例，原图中的宽高比为66*178
     */
    public final float scalingRatio;
    /**
     * 小人儿在3D世界中，其底部中间的点在2D图片中的位置百分比
     */
    public final float bottomCenterPercentage;

    /**
     * 游戏界面存在的三角形，水平边长度设为1，该值表示竖直边的长度
     */
    public final float triangleVerticalEdge;

    /**
     * 游戏界面存在的三角形，水平边长度设为1.该值表示斜边的长度
     */
    public final float triangleBevelEdge;

    /**
     * 计算物体边缘的阈值参数1
     */
    public final double cannyLowerThreshold;

    /**
     * 计算物体边缘的阈值参数2
     */

    public final double cannyUpperThreshold;

    /**
     * 跳跃系数，跳跃距离与按压时间的关系
     */
    public final float jumpFactor;

    /**
     * 保存所有mat截图
     */
    public final boolean storeMat;

    public final int yBelowScore;


    public JumpParams(float scalingRatio, float bottomCenterPercentage,
                      float triangleVerticalEdge, float triangleBevelEdge,
                      double cannyLowerThreshold, double cannyUpperThreshold,
                      float jumpFactor, boolean storeMat, int yBelowScore) {
        this.scalingRatio = scalingRatio;
        this.bottomCenterPercentage = bottomCenterPercentage;
        this.triangleVerticalEdge = triangleVerticalEdge;
        this.triangleBevelEdge = triangleBevelEdge;
        this.cannyLowerThreshold = cannyLowerThreshold;
        this.cannyUpperThreshold = cannyUpperThreshold;
        this.jumpFactor = jumpFactor;
        this.storeMat = storeMat;
        this.yBelowScore = yBelowScore;
    }
}
