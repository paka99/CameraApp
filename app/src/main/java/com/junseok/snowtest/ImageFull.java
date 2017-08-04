package com.junseok.snowtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by PEM_ljs on 2017-08-01.
 */

public class ImageFull extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        setContentView(imageView);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        try {
            Bitmap bm = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(bm);
        }
        catch (OutOfMemoryError e) {
            Toast.makeText(ImageFull.this,"이미지가 너무 큽니다.",
                    Toast.LENGTH_SHORT).show();
        }

        imageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }
}
