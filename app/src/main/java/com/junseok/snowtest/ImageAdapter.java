package com.junseok.snowtest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by PEM_ljs on 2017-08-05.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ContentResolver mCr;
    private Cursor mCursor;

    private final String TAG = "IMGADT";

    public ImageAdapter(Context c, ContentResolver mCr, Cursor mCursor) {
        mContext = c;
        this.mCr = mCr;
        this.mCursor = mCursor;
    }

    public void updateCursor(Cursor mCursor){
        this.mCursor = mCursor;
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        mCursor.moveToPosition(position);
        Bitmap thumbnailImage = MediaStore.Images.Thumbnails.getThumbnail(mCr, mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID)), MediaStore.Images.Thumbnails.MINI_KIND, null);
        imageView.setImageBitmap(thumbnailImage);

        // TODO: 크기를 고정시키면 앱종료
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int numPicture = mCursor.getCount();

        if(position == numPicture - 1){
            Log.v(TAG, "Call last getView: Num of Pictures is " + numPicture);
        }

        return imageView;
    }
}