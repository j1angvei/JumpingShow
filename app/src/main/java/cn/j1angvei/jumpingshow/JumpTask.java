package cn.j1angvei.jumpingshow;

import android.graphics.Point;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * @author j1angvei
 * @since 2018/2/7
 */

public class JumpTask extends AsyncTask<Image, Void, Integer> {
    private static final String TAG = JumpTask.class.getSimpleName();
    private Mat mJumper;
    private JumpParams mParams;
    private JumpListener mJumpListener;
    private Point pressPosition;
    private String filePrefix;

    public JumpTask(Mat jumper, JumpParams params, JumpListener jumpListener) {
        mJumper = jumper;
        mParams = params;
        mJumpListener = jumpListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mJumpListener.onInit();
        filePrefix = System.currentTimeMillis() + "_";
    }

    @Override
    protected Integer doInBackground(Image... images) {
        //屏幕截图的mat
        Mat screen = ImageUtils.imageToMat(images[0]);
        images[0].close();

        //小人儿的坐上角在屏幕中的坐标
        Point topLeftOfJumper = matchJumper(screen, mJumper);

        //小人儿的底部重心在屏幕中的坐标
        Point bottomCenterOfJumper = new Point(
                topLeftOfJumper.x + mJumper.cols() / 2,
                (int) (topLeftOfJumper.y + mJumper.rows() * mParams.bottomCenterPercentage));

        if (mParams.storeMat) {
            Mat screenWithStart = screen.clone();
            Imgproc.circle(screenWithStart, new org.opencv.core.Point(bottomCenterOfJumper.x, bottomCenterOfJumper.y), 4, ImageUtils.RED);
            ImageUtils.save(filePrefix + "1_screen.png", screenWithStart);
        }
        //跳跃时按压的坐标
        pressPosition = bottomCenterOfJumper;

        //下一个落脚点边缘的最高点
        int pressDuration = calculatePressDuration(screen, mJumper, bottomCenterOfJumper, topLeftOfJumper);

        Log.d(TAG, "doInBackground: final press duration," + pressDuration);
        return pressDuration;
    }

    @Override
    protected void onPostExecute(Integer pressDuration) {
        super.onPostExecute(pressDuration);
        mJumpListener.onReady(pressPosition, pressDuration);
    }

    /**
     * 使用模板匹配寻找屏幕截图中跳瓶的左上角的坐标
     *
     * @param screen 屏幕的mat
     * @param jumper 小人儿的mat
     * @return 匹配完成后，小人儿的左上角在屏幕中的坐标
     */
    private Point matchJumper(Mat screen, Mat jumper) {
        Mat matchMat = new Mat();
        matchMat.create(
                screen.cols() - jumper.cols() + 1,
                screen.rows() - jumper.rows() + 1,
                CvType.CV_32FC1
        );
        Imgproc.matchTemplate(screen, jumper, matchMat, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(matchMat, matchMat, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(matchMat);
        Point topLeftPoint = new Point();
        topLeftPoint.x = (int) minMaxLocResult.maxLoc.x;
        topLeftPoint.y = (int) minMaxLocResult.maxLoc.y;
        Log.d(TAG, "matchJumper: top left point " + topLeftPoint.toString());
        return topLeftPoint;

    }

    /**
     * 计算石头边缘的最高点的x坐标（最高指该点的y坐标最小）
     *
     * @param screen               屏幕截图mat
     * @param jumper               小人儿mat
     * @param bottomCenterOfJumper 小人儿3D底部重心在屏幕上的坐标
     * @param topLeftOfJumper      小人儿2D在屏幕上的左上角坐标
     * @return 下一个落脚点的坐标
     */
    private int calculatePressDuration(Mat screen, Mat jumper, Point bottomCenterOfJumper, Point topLeftOfJumper) {
        //小人儿是在左半边屏幕还是右半边屏幕
        boolean isInLeftScreen = bottomCenterOfJumper.x * 2 < screen.cols();

        //下一个落脚点（石头）的粗略区域
        //石头的下边界，与小人儿的底部相同
        int stoneBottom = (topLeftOfJumper.y + jumper.rows());
        //石头的左边界，小人儿右边或者屏幕左边（为0）
        int stoneLeft = isInLeftScreen ?
                (topLeftOfJumper.x + jumper.cols()) : 0;
        //石头的上边界，满足游戏中的三角形关系
        int stoneTop = (int) (bottomCenterOfJumper.y - (mParams.triangleVerticalEdge *
                Math.max(stoneLeft, screen.cols() - stoneLeft)));
        //石头的右边界，小人儿的左边或者屏幕右边
        int stoneRight = isInLeftScreen ?
                screen.cols() : topLeftOfJumper.x;
        Log.d(TAG, String.format("Stone region, top,%s;left,%s;right,%s;bottom,%s", stoneTop, stoneLeft, stoneRight, stoneBottom));
        //下一个落脚点的粗略区域的mat
        Mat stone = screen.submat(stoneTop, stoneBottom, stoneLeft, stoneRight);

        if (mParams.storeMat) {
            ImageUtils.save(filePrefix + "2_stone.png", stone);
        }

        //提取石头的边缘
        Mat stoneBounds = stone.clone();
        Imgproc.Canny(stone, stoneBounds, mParams.cannyLowerThreshold, mParams.cannyUpperThreshold);

        if (mParams.storeMat) {
            ImageUtils.save(filePrefix + "3_canny.png", stoneBounds);
        }

        //获取石头边缘的最高点（y值最小，相同则取中间点）
        boolean inWhite = false;

        //石头边缘顶部的起点和终点的横坐标x
        int topStartXOfBounds = 0;
        int topEndXOfBounds = 0;
        int topCenterY = 0;

        //遍历石头mat，寻找最顶部的白色中间点的横坐标x，如果为白色线段，取中点的横坐标x
        for (int y = 0; y < stoneBounds.rows(); y++) {
            for (int x = 0; x < stoneBounds.cols(); x++) {
                double[] color = stoneBounds.get(y, x);
                if (!inWhite && color[0] == 255f) {
                    inWhite = true;
                    topStartXOfBounds = x;
                    topCenterY = y;
                    continue;
                }
                if (inWhite && color[0] != 255f) {
                    topEndXOfBounds = x;
                    break;
                }
            }
            if (inWhite) {
                break;
            }
        }
        //石头边缘顶点的横坐标x
        int topCenterX = (topStartXOfBounds + topEndXOfBounds) / 2 + stoneLeft;
        topCenterY += stoneTop;

        if (mParams.storeMat) {
            Mat screenWithTopCenterDot = screen.clone();
            Imgproc.circle(screenWithTopCenterDot, new org.opencv.core.Point(topCenterX, topCenterY), 4, ImageUtils.PURPLE);
            ImageUtils.save(filePrefix + "4_top_center.png", screenWithTopCenterDot);
        }
        Log.d(TAG, "calculatePressDuration: top center x of bounds," + topCenterX);
        //跳跃起点与跳跃终点的水平距离
        int horizontalDistance = Math.abs(bottomCenterOfJumper.x - topCenterX);
        Log.d(TAG, "calculatePressDuration: horizontal distance " + horizontalDistance);
        //跳跃起点与跳跃终点的连线距离
        float jumpDistance = mParams.triangleBevelEdge * horizontalDistance;
        Log.d(TAG, "calculatePressDuration: jump distance," + jumpDistance);
        //按压时间
        int pressDuration = (int) (mParams.jumpFactor * jumpDistance);
        Log.d(TAG, "calculatePressDuration: press duration," + pressDuration);
        return pressDuration;
    }

}