package com.junseok.snowtest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by PEM_ljs on 2017-08-05.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ContentResolver mCr;
    private Cursor mCursor;
    private LruCache<String, Bitmap> mImageCache;
    int mSize;
    private ArrayList<ImageItem> mImageList;

    private final String TAG = "IMGADT";

    public ImageAdapter(Context c, ContentResolver mCr, Cursor mCursor) {
        mContext = c;
        this.mCr = mCr;
        this.mCursor = mCursor;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mSize = cacheSize;

        mImageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mImageCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mImageCache.get(key);
    }



    public void updateCursor(Cursor mCursor){
        this.mCursor = mCursor;
    }

    @Override
    public int getCount() {
        Log.v(TAG, "Call getCount: " + mCursor.getCount());
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        Log.v(TAG, "Call getItem");
        return position;
    }

    @Override
    public long getItemId(int position) {
        Log.v(TAG, "Call getItemId: " + position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(320,240));
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            imageView = (ImageView) convertView;
        }

        final Bitmap thumbnailImage = getBitmapFromMemCache(String.valueOf(position));
        if (thumbnailImage != null) {
            imageView.setImageBitmap(thumbnailImage);
        } else {
            imageView.setImageResource(R.drawable.empty_photo);
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(position);
        }

//        mCursor.moveToPosition(position);
//        Bitmap thumbnailImage = MediaStore.Images.Thumbnails.getThumbnail(mCr, mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID)), MediaStore.Images.Thumbnails.MINI_KIND, null);
//        imageView.setImageBitmap(thumbnailImage);

        Log.v(TAG, "Picture number " + position + "'s");

        return imageView;
    }

    class ImageItem{
        private Bitmap mBm;
        private String mFileName;

        public ImageItem(Bitmap bm, String fn){
            this.mBm = bm;
            this.mFileName = fn;
        }

        public Bitmap getBitmap(){
            return mBm;
        }

        public String getFileName(){
            return mFileName;
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private WeakReference<ImageView> mImageViewRef;

        public BitmapWorkerTask(ImageView imageView){
            mImageViewRef = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            mCursor.moveToPosition(params[0]);
            final Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(mCr,
                    mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID)), MediaStore.Images.Thumbnails.MINI_KIND, null);
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mImageViewRef != null && bitmap != null) {
                final ImageView imageView = mImageViewRef.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}