package cn.j1angvei.jumpingshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author j1angvei
 * @since 2018/1/30
 */

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    private static Bitmap assetToBitmap(Context context, String assetPath) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetPath);
        } catch (IOException e) {
            Log.e(TAG, "fail to read asset", e);
        }
        return BitmapFactory.decodeStream(inputStream);
    }

    private static Bitmap imageToBitmap(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();

        Image.Plane[] planes = image.getPlanes();

        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        // create bitmap
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }

    private static Mat bitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        try {

            Utils.bitmapToMat(bitmap, mat);
            //Android Bitmap中的颜色channel为BGR，需要转换为RGB
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        } catch (Exception e) {
            Log.e(TAG, "bitmapToMat: fail to convert bitmap to mat ", e);
        }
        return mat;
    }

    public static Mat assetToMat(Context context, String assetPath) {
        Bitmap bitmap = assetToBitmap(context, assetPath);
        return bitmapToMat(bitmap);
    }


    public static Mat imageToMat(Image image) {
        Bitmap bitmap = imageToBitmap(image);
        return bitmapToMat(bitmap);
    }


    private static final String HOME_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/jumpingshow/";

    public static void save(String fileName, Mat mat) {
        Imgcodecs.imwrite(HOME_DIR + fileName, mat);
    }

    public static final Scalar RED = new Scalar(54, 67, 244);
    public static final Scalar PURPLE = new Scalar(176, 39, 156);
    public static final Scalar BLUE = new Scalar(243, 150, 33);
    public static final Scalar WHITE = new Scalar(255, 255, 255);
}
