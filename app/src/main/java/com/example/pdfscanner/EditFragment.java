package com.example.pdfscanner;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class EditFragment extends Fragment {
    private Button rotateButton, filterButton, backButton, checkButton, adjustButton, saveButton, cancelButton;
    private ImageView editImage, originalImage , grayImage, bwImage, smoothImage, magicImage;;
    private Bitmap originalBitmap,cropBitmap, grayBitmap, bwBitmap, smoothBitmap, magicBitmap;
    private Bitmap editBitmap;
    private TextView originalText, grayText, magicText, bwText, smoothText, filterText, adjustText;
    private Dialog checkDialog;
    private FormSingleton formSingleton;
    private Filter filter;

    public static EditFragment newInstance() {
        EditFragment fragment = new EditFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formSingleton = FormSingleton.get(getActivity());
        cropBitmap = formSingleton.getForm().getCropBitmap();
        originalBitmap = cropBitmap.copy(cropBitmap.getConfig(), true);
        filter = new Filter(originalBitmap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit,container,false);
        rotateButton = v.findViewById(R.id.rotateButton);
        filterButton = v.findViewById(R.id.filterButton);
        backButton = v.findViewById(R.id.edit_back);
        checkButton = v.findViewById(R.id.edit_check);
        adjustButton = v.findViewById(R.id.adjustButton);
        filterButton = v.findViewById(R.id.filterButton);
        originalImage = v.findViewById(R.id.originalImageView);
        grayImage = v.findViewById(R.id.grayImageView);
        bwImage = v.findViewById(R.id.bwImageView);
        smoothImage = v.findViewById(R.id.smoothImageView);
        magicImage = v.findViewById(R.id.magicImageView);
        editImage = v.findViewById(R.id.image_edit);
        checkDialog = new Dialog(getActivity());
        checkDialog.setContentView(R.layout.dialog_save);
        checkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        checkDialog.setCancelable(false);
        saveButton = checkDialog.findViewById(R.id.btn_save_yes);
        cancelButton = checkDialog.findViewById(R.id.btn_save_no);

        originalText = v.findViewById(R.id.originalTextView);
        grayText = v.findViewById(R.id.grayTextView);
        bwText = v.findViewById(R.id.bwTextView);
        smoothText = v.findViewById(R.id.smoothTextView);
        magicText = v.findViewById(R.id.magicTextView);

        originalImage.setImageBitmap(originalBitmap);
        grayBitmap = filter.getGrayBitmap();
        grayImage.setImageBitmap(grayBitmap);
        bwBitmap = filter.getBWBitmap();
        bwImage.setImageBitmap(bwBitmap);
        smoothBitmap = filter.getSmoothBitmap();
        smoothImage.setImageBitmap(smoothBitmap);
        magicBitmap = filter.getMagicColorBitmap();
        magicImage.setImageBitmap(magicBitmap);

        editBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        setSelectedFilter(originalText);
        editImage.setImageBitmap(editBitmap);

        originalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                setSelectedFilter(originalText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        grayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = grayBitmap.copy(grayBitmap.getConfig(), true);
                setSelectedFilter(grayText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        magicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = magicBitmap.copy(magicBitmap.getConfig(), true);
                setSelectedFilter(magicText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        bwImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = bwBitmap.copy(bwBitmap.getConfig(), true);
                setSelectedFilter(bwText);
                editImage.setImageBitmap(editBitmap);
            }
        });
        smoothImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = smoothBitmap.copy(smoothBitmap.getConfig(), true);
                setSelectedFilter(smoothText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = rotateImage(editBitmap, 90);
                editImage.setImageBitmap(editBitmap);
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("123123","destroyView");
        if (editBitmap != null) {
            editBitmap.recycle();
            editBitmap = null;
        }

        if (grayBitmap != null) {
            grayBitmap.recycle();
            grayBitmap = null;
        }

        if (bwBitmap != null) {
            bwBitmap.recycle();
            bwBitmap = null;
        }

        if (magicBitmap != null) {
            magicBitmap.recycle();
            magicBitmap = null;
        }

        if (smoothBitmap != null) {
            smoothBitmap.recycle();
            smoothBitmap = null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("123123","destroy");

        if (originalBitmap != null) {
            originalBitmap.recycle();
            originalBitmap = null;
        }

    }

    private void setSelectedFilter(TextView selectedText) {
        originalText.setBackgroundColor(getResources().getColor(R.color.filter));
        grayText.setBackgroundColor(getResources().getColor(R.color.filter));
        magicText.setBackgroundColor(getResources().getColor(R.color.filter));
        bwText.setBackgroundColor(getResources().getColor(R.color.filter));
        smoothText.setBackgroundColor(getResources().getColor(R.color.filter));
        selectedText.setBackgroundColor(getResources().getColor(R.color.primary));
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
