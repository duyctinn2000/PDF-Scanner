package com.example.pdfscanner;

import static com.example.pdfscanner.MainActivity.formDetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.exifinterface.media.ExifInterface;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CropFragment extends Fragment {
    private static final String SOURCE_IMAGE = "crop_image";
    private String sourcePath;
    private Button deleteButton, cropButton, backButton, forwardButton;
    private ImageView cropImage;
    private Bitmap rgbFrameBitmap;


    public static CropFragment newInstance(String imgPath) {
        Bundle args = new Bundle();
        args.putString(SOURCE_IMAGE,imgPath);
        CropFragment fragment = new CropFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sourcePath = getArguments().getString(SOURCE_IMAGE);

        rgbFrameBitmap = BitmapFactory.decodeFile(sourcePath);

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:

                this.rgbFrameBitmap = rotateImage(rgbFrameBitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:

                this.rgbFrameBitmap = rotateImage(rgbFrameBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:

                this.rgbFrameBitmap = rotateImage(rgbFrameBitmap, 270);
                break;

        }
        Point[] point = formDetector.detector(this.rgbFrameBitmap);
        this.rgbFrameBitmap = this.perspectiveTransform(this.rgbFrameBitmap,point[0],point[1],point[2],point[3]);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crop,container,false);
        deleteButton = v.findViewById(R.id.deleteButton);
        cropButton = v.findViewById(R.id.cropButton);
        backButton = v.findViewById(R.id.crop_back);
        forwardButton = v.findViewById(R.id.crop_forward);
        cropImage = v.findViewById(R.id.image_crop);
        cropImage.setImageBitmap(rgbFrameBitmap);
        return v;
    }

    private Bitmap perspectiveTransform(Bitmap inputBitmap, Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {

        int resultWidth = (int)(topRight.x-topLeft.x);
        int bottomWidth = (int)(bottomRight.x - bottomLeft.x);
        if(bottomWidth > resultWidth)
            resultWidth = bottomWidth;

        int resultHeight = (int)(bottomLeft.y - topLeft.y);
        int bottomHeight = (int)(bottomRight.y - topRight.y);
        if(bottomHeight > resultHeight)
            resultHeight = bottomHeight;

        Mat inputMat = new Mat(inputBitmap.getWidth(), inputBitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(inputBitmap, inputMat);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC1);

        List<Point> source = new ArrayList<Point>();
        source.add(topLeft);
        source.add(topRight);
        source.add(bottomLeft);
        source.add(bottomRight);

        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(resultWidth, 0);
        Point ocvPOut3 = new Point(0, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, resultHeight);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight));
        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        return output;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
