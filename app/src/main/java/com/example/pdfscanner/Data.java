package com.example.pdfscanner;

import android.graphics.Bitmap;

import org.opencv.core.Point;

public class Data {
    private Bitmap originalBitmap;
    private Point[] points;
    private Bitmap cropBitmap;
    private Bitmap filterBitmap;
    private float scale;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Data() {
        this.points = null;
        this.originalBitmap = null;
        this.cropBitmap = null;
        this.filterBitmap = null;
        this.scale = 0.0F;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public void setOriginalBitmap(Bitmap original) {
        this.originalBitmap = original.copy(original.getConfig(), true);
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public Bitmap getCropBitmap() {
        return cropBitmap;
    }

    public void setCropBitmap(Bitmap cropBitmap) {
        this.cropBitmap = cropBitmap.copy(cropBitmap.getConfig(), true);;
    }

    public Bitmap getFilterBitmap() {
        return filterBitmap;
    }

    public void setFilterBitmap(Bitmap filterBitmap) {
        this.filterBitmap = filterBitmap;
    }
}
