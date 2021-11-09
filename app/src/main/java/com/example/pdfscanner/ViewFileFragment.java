package com.example.pdfscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.database.ScannerFile;
import com.example.pdfscanner.database.ScannerFileLab;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.UUID;

public class ViewFileFragment extends Fragment {
    private ScannerFile scannerFile;
    private static final String SCANNER_FILE_ID = "scanner_file_id";

    private PDFView pdfView;
    private ImageView imageView;

    public static ViewFileFragment newIntance(UUID scannerFileID) {
        Bundle args = new Bundle();
        args.putSerializable(SCANNER_FILE_ID, scannerFileID);

        ViewFileFragment fragment = new ViewFileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID scannerFileID = (UUID) getArguments().getSerializable(SCANNER_FILE_ID);
        scannerFile = ScannerFileLab.get(getActivity()).getScannerFile(scannerFileID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewfile, container, false);
        pdfView = v.findViewById(R.id.pdf_view);
        imageView = v.findViewById(R.id.image_view);
        File file = new File(getActivity().getExternalFilesDir("PDFScanner"), scannerFile.getTitle()+"."+scannerFile.getType());

        if (!file.exists()) {
            ScannerFileLab.get(getActivity()).removeScannerFile(scannerFile);
            Toast.makeText(getActivity(), "File no longer exists", Toast.LENGTH_SHORT).show();
            MainFragment mainFragment = new MainFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainFragment, "MAIN_FRAGMENT")
                    .commit();
        } else {
            if (scannerFile.getType().equals("pdf")) {
                pdfView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                pdfView.fromFile(file).load();
            } else {
                pdfView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Bitmap imageBitmap = BitmapFactory.decodeFile(file.getPath());
                imageView.setImageBitmap(imageBitmap);
            }
        }
        return v;
    }
}
