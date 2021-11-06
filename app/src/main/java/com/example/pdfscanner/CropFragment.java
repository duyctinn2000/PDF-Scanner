package com.example.pdfscanner;

import static com.example.pdfscanner.MainActivity.formDetector;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Map;
import java.util.Objects;

public class CropFragment extends Fragment {
    private static final String SOURCE_IMAGE = "crop_image";
    private String sourcePath;
    private Button deleteButton, cropButton, backButton, forwardButton, deleteNoButton, deleteYesButton;
    private ImageView cropImage;
    private Bitmap rgbFrameBitmap;
    public Bitmap cropResult;
    private PolygonView polygonView;
    private FrameLayout sourceFrame;
    TextView cropTextView;
    private Dialog deleteDialog;
    private Point[] cropPoint;
    private boolean isAutoCrop = true;


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
        polygonView = (PolygonView) v.findViewById(R.id.polygonView);
        sourceFrame = v.findViewById(R.id.sourceFrame);
        deleteDialog = new Dialog(getActivity());
        deleteDialog.setContentView(R.layout.dialog_delete);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);
        cropTextView = v.findViewById(R.id.cropTextView);
        deleteNoButton = deleteDialog.findViewById(R.id.btn_delete_no);
        deleteYesButton = deleteDialog.findViewById(R.id.btn_delete_yes);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.show();
            }
        });

        deleteNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
            }
        });

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                if (rgbFrameBitmap!=null) {
                    setBitmap(rgbFrameBitmap);
                }
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAutoCrop) {
                    isAutoCrop = false;
                    cropTextView.setText("Auto");
                    polygonView.setFullImgCrop();
                } else {
                    isAutoCrop = true;
                    cropTextView.setText("All");
                    polygonView.setCropPoints(cropPoint);
                }

            }
        });


        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Point[] points = polygonView.getPoints();
                if (points!=null) {
                    cropResult = perspectiveTransform(rgbFrameBitmap, points[0], points[1], points[2], points[3]);
                    cropImage.setImageBitmap(cropResult);
                }
            }
        });


        return v;
    }

    private void setBitmap(Bitmap original) {
        this.rgbFrameBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rgbFrameBitmap.getWidth(),rgbFrameBitmap.getHeight());
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
        cropImage.setImageBitmap(rgbFrameBitmap);
        this.cropPoint = formDetector.detector(rgbFrameBitmap);
        polygonView.setCropPoints(this.cropPoint);
        polygonView.setVisibility(View.VISIBLE);
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Bitmap perspectiveTransform(Bitmap inputBitmap, Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {

        Mat inputMat = new Mat(inputBitmap.getWidth(), inputBitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(inputBitmap, inputMat);

        List<Point> source = new ArrayList<Point>();
        source.add(topLeft);
        source.add(topRight);
        source.add(bottomLeft);
        source.add(bottomRight);

        Mat startM = Converters.vector_Point2f_to_Mat(source);

        int resultWidth = (int) (Math.max(getPointsDistance(topLeft, topRight) , getPointsDistance(bottomLeft, bottomRight))/2);
        int resultHeight = (int) (Math.max(getPointsDistance(topLeft, topRight) , getPointsDistance(bottomLeft, bottomRight))/2);

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC1);

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

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private double getPointsDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.x - p2.y, 2));
    }




}
