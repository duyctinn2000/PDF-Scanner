package com.example.pdfscanner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.exifinterface.media.ExifInterface;

import com.example.pdfscanner.formdetector.FormDetector;

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
import java.util.Objects;

public class CropFragment extends Fragment {
    private static final String SOURCE_IMAGE = "crop_image";
    private String sourcePath;
    private Button deleteButton, cropButton, backButton, forwardButton, deleteNoButton, deleteYesButton;
    private ImageView cropImage;
    private Bitmap rgbFrameBitmap;
    private Bitmap displayBitmap;
    public Bitmap cropResult;
    private PolygonView polygonView;
    private FrameLayout sourceFrame;
    TextView cropTextView;
    private Dialog deleteDialog;
    private Point[] cropPoint;
    private boolean isAutoCrop = true;
    private FormSingleton formSingleton;
    private float scale;

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

        formSingleton = FormSingleton.newDataSingleton(getActivity());

        sourcePath = getArguments().getString(SOURCE_IMAGE);

        rgbFrameBitmap = BitmapFactory.decodeFile(sourcePath);

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ei != null) {
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

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
        formSingleton.getForm().setOriginalBitmap(rgbFrameBitmap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crop,container,false);

        if (this.rgbFrameBitmap==null) {
            formSingleton = FormSingleton.get(getActivity());
            Bitmap tempBitmap = formSingleton.getForm().getOriginalBitmap();
            if (tempBitmap != null) {
                this.rgbFrameBitmap = tempBitmap.copy(tempBitmap.getConfig(), true);
            }
        }

        deleteButton = v.findViewById(R.id.deleteButton);
        cropButton = v.findViewById(R.id.cropButton);
        backButton = v.findViewById(R.id.crop_back);
        forwardButton = v.findViewById(R.id.crop_forward);
        cropImage = v.findViewById(R.id.image_crop);
        polygonView = v.findViewById(R.id.polygonView);
        polygonView.setVisibility(View.GONE);
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
                    cropTextView.setText("Full");
                    cropPoint = FormDetector.detector(rgbFrameBitmap);
                    polygonView.setCropPoints(getScalePoint(cropPoint));
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Point[] points = polygonView.getPoints();
                rgbFrameBitmap = null;
                if (points!=null) {
                    cropResult = perspectiveTransform(formSingleton.getForm().getOriginalBitmap(), points[0], points[1], points[2], points[3]);
                    formSingleton.getForm().setCropBitmap(cropResult);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.edit_container, EditFragment.newInstance(), "EDIT_FRAGMENT")
                            .addToBackStack(null)
                            .commit();
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Error\n \nCannot Crop Image.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });
        return v;
    }

    private void setBitmap(Bitmap original) {
        displayBitmap = setScalePoint(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) (displayBitmap.getWidth()), (int) (displayBitmap.getHeight()));
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
        cropImage.setImageBitmap(displayBitmap);
        this.cropPoint = formSingleton.getForm().getPoints();
        if (this.cropPoint==null) {
            this.cropPoint = FormDetector.detector(rgbFrameBitmap);
        }
        polygonView.setCropPoints(getScalePoint(this.cropPoint));
        polygonView.setVisibility(View.VISIBLE);
    }

    private Point[] getScalePoint(Point[] points) {
        Point point_0 = new Point(points[0].x*scale,points[0].y*scale);
        Point point_1 = new Point(points[1].x*scale,points[1].y*scale);
        Point point_2 = new Point(points[2].x*scale,points[2].y*scale);
        Point point_3 = new Point(points[3].x*scale,points[3].y*scale);
        return new Point[]{point_0,point_1,point_2,point_3};
    }

    public Bitmap setScalePoint(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        scale = Math.min(scaleHeight,scaleWidth);
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale, scale);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    private Bitmap perspectiveTransform(Bitmap inputBitmap, Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
        topLeft = new Point(topLeft.x/scale,topLeft.y/scale);
        topRight = new Point(topRight.x/scale,topRight.y/scale);
        bottomLeft = new Point(bottomLeft.x/scale,bottomLeft.y/scale);
        bottomRight = new Point(bottomRight.x/scale,bottomRight.y/scale);
        formSingleton.getForm().setPoints(new Point[]{topLeft,topRight,bottomRight,bottomLeft});

        Mat inputMat = new Mat(inputBitmap.getWidth(), inputBitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(inputBitmap, inputMat);

        List<Point> source = new ArrayList<Point>();
        source.add(topLeft);
        source.add(topRight);
        source.add(bottomLeft);
        source.add(bottomRight);

        Mat startM = Converters.vector_Point2f_to_Mat(source);

        int resultWidth = (int) (Math.max(getPointsDistance(topLeft, topRight) , getPointsDistance(bottomLeft, bottomRight)));
        int resultHeight = (int) (Math.max(getPointsDistance(topLeft, bottomLeft) , getPointsDistance(topRight, bottomRight)));
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
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (displayBitmap!=null) {
            displayBitmap.recycle();
            displayBitmap = null;
        }
        if (cropResult!=null) {
            cropResult.recycle();
            cropResult = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (formSingleton!=null) {
            formSingleton.Recycle();
            formSingleton = null;
        }
        if (rgbFrameBitmap!=null) {
            rgbFrameBitmap.recycle();
            rgbFrameBitmap = null;
        }
    }
}
