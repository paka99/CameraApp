package com.junseok.snowtest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by PEM_ljs on 2017-08-05.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    DataSetObservable mDataSetObservable = new DataSetObservable();
    private ContentResolver mCr;
    private Cursor mCursor;

    ArrayList<Bitmap> mThumbnaiList;

    public void addThumbnailImage(Bitmap image){
        if(image == null)   return;
        mThumbnaiList.add(image);
    }

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
//        imageView.setImageBitmap(mThumbnaiList.get(position));

        // TODO: 크기를 고정시키면 앱종료
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if(position == getCount() -1){
            Log.i("LIST", "Listing Complete");
        }

        return imageView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer){
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer){
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged(){
        //Log.d("NOTIFY", "mCursor Count is " + mCursor.getCount());
        mDataSetObservable.notifyChanged();
    }
}