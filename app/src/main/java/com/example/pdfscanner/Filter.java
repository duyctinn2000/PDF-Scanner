package com.example.pdfscanner;

import static org.opencv.imgproc.Imgproc.cvtColor;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Filter {

    private Mat originalMat;

    public Filter(Bitmap originalBitmap) {
        this.originalMat = new Mat(originalBitmap.getWidth(), originalBitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(originalBitmap, originalMat);
    }

    public Bitmap getGrayBitmap() {
        Mat grayMat = new Mat();
        cvtColor(this.originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Bitmap grayBitmap = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(grayMat, grayBitmap);
        return grayBitmap;
    }

    public Bitmap getBWBitmap() {
        Mat bwMat = new Mat();
        Mat grayMat = new Mat();
        cvtColor(this.originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayMat, bwMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Bitmap bwBitmap = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(bwMat, bwBitmap);
        return bwBitmap;
    }

    public Bitmap getMagicColorBitmap() {
        Mat margicMat = new Mat(originalMat.cols(), originalMat.rows(), CvType.CV_8UC1);
        originalMat.copyTo(margicMat);
        margicMat.convertTo(margicMat,-1,1.9,-80);
        Bitmap magicBitmap = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(margicMat, magicBitmap);
        return magicBitmap;
    }

    public Bitmap getSmoothBitmap() {
        Mat blurMat = new Mat();
        Imgproc.medianBlur(originalMat, blurMat, 7);
        Bitmap blurBitmap = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(blurMat, blurBitmap);
        return blurBitmap;
    }
}
