package com.nibm.madcw.ui.teacher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nibm.madcw.R;

public class GenerateQrActivity extends AppCompatActivity {

    ImageView imageViewQrCode;
    TextView textViewCourseInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        textViewCourseInfo = findViewById(R.id.textViewCourseInfo);

        // Get courseId and courseName from intent
        int courseId = getIntent().getIntExtra("courseId", -1);
        String courseName = getIntent().getStringExtra("courseName");

        if (courseId == -1 || courseName == null) {
            Toast.makeText(this, "Invalid course data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewCourseInfo.setText("Course: " + courseName);

        generateQRCode(courseId, courseName);
    }

    private void generateQRCode(int courseId, String courseName) {
        // Combine data into a JSON or simple String (Here simple JSON string)
        String qrData = "{\"courseId\":" + courseId + ",\"courseName\":\"" + courseName + "\"}";

        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 300;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }

            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
