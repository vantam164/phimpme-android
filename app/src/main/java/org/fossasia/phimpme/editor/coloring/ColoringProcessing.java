package org.fossasia.phimpme.editor.coloring;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.fossasia.phimpme.MyApplication;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pytorch.IValue;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import static org.opencv.core.CvType.CV_32SC3;
import static org.opencv.core.CvType.CV_8UC3;


public class ColoringProcessing {
    private static final String TAG = "ColoringProcessing";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG + " - Error", "Unable to load OpenCV");
        } else {
            System.loadLibrary("nativeimageprocessing");
        }
    }

    private static Bitmap convertResize(Mat input, Size size, boolean cvtColor) {
        Bitmap bmp = null;
        if (cvtColor) {
            Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2RGB);
        }

        Imgproc.resize(input, input, size);
        try {
            bmp = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(input, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }
        return bmp;
    }

    private static float[] denorm(
            final float[] input,
            final int w,
            final int h,
            final float[] normMeanRGB,
            final float[] normStdRGB) {
        float[] outBuffer = new float[input.length];
        for (int i = 0; i < w * h; i++) {
            float r = input[i];
            float g = input[i + w * h];
            float b = input[i + 2 * w * h];
            float rF = r * normStdRGB[0] + normMeanRGB[0];
            float gF = g * normStdRGB[1] + normMeanRGB[1];
            float bF = b * normStdRGB[2] + normMeanRGB[2];
            outBuffer[3 * i] = rF;
            outBuffer[3 * i + 1] = gF;
            outBuffer[3 * i + 2] = bF;
        }
        return outBuffer;
    }

    private static Mat arrayFloatToMat(float[] floatArray, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] intBuffer = new int[width * height * 3];
        for (int i = 0; i < floatArray.length; i++) {
            intBuffer[i] = (int) (floatArray[i] * 255);
        }
        Mat mat = new Mat(height, width, CV_32SC3);
        mat.put(0, 0, intBuffer);
        Mat dst = new Mat(height, width, CV_8UC3);
        mat.convertTo(dst, CV_8UC3);
        return dst;
    }

    public static Bitmap blackwhite2color(Bitmap bitmap, Context context, int ratio_level) {
        int [] valid_ratios = {4, 6, 8, 10, 12, 14, 16, 18, 20, 32};
        if (ratio_level < 0) {
            ratio_level = 0;
        }
        if (ratio_level >= valid_ratios.length) {
            ratio_level = valid_ratios.length - 1;
        }
        int target_size = valid_ratios[ratio_level] * 16;
        Log.d(TAG, "blackwhite2color ratio: " + ratio_level + "  target_size:  " + target_size);
        Mat input = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        Size originSize = input.size();
        Utils.bitmapToMat(bitmap, input);
        Bitmap bmp = ColoringProcessing.convertResize(input, new Size(target_size, target_size), true);
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bmp,
                new float[]{0.4850f, 0.4560f, 0.4060f}, new float[]{0.2290f, 0.2240f, 0.2250f});
        IValue rs = MyApplication.getColorModel((context.getApplicationContext())).forward(IValue.from(inputTensor));
        final Tensor ts = rs.toTensor();
        float[] pixels = ts.getDataAsFloatArray();

        float[] denorm = ColoringProcessing.denorm(pixels, target_size, target_size,
                new float[]{0.4850f, 0.4560f, 0.4060f},
                new float[]{0.2290f, 0.2240f, 0.2250f});
        Mat outMat = ColoringProcessing.arrayFloatToMat(denorm, target_size, target_size);
        Bitmap outBimap = ColoringProcessing.convertResize(outMat, originSize, false);
        return outBimap;
    }

}
