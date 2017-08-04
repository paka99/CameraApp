package com.junseok.snowtest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PEM_ljs on 2017-08-04.
 */

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private static ContentResolver mCr;
    static ImageAdapter mAdapter;
    private static GridView mGrid;
    private static Cursor mCursor;

    // Container Activity must implement this interface
    public interface OnHeadlineSelectedListener {
        public void onArticleSelected(int position);
    }

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setContentResolver(ContentResolver resolver){
        mCr = resolver;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position == 0){
            Log.d("PREVIEW", "This Position is PreviewList");
            return PreviewFrag.newInstance();
        }
        else {
            Log.d("PICTURELIST", "This Position is PictureList");
            return PictureList.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "SECTION 1";
//                case 1:
//                    return "SECTION 2";
//            }
//            return null;
//        }

    public static class PreviewFrag extends Fragment {
        private MyCameraSurface mSurface;

        OnHeadlineSelectedListener mCallback;

        public PreviewFrag() {
        }

        public static PreviewFrag newInstance() {
            PreviewFrag fragment = new PreviewFrag();
            //Bundle args = new Bundle();
            //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            //fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frag_preview, container, false);
            Log.d("CREATE", "Preview is created");
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            mSurface = (MyCameraSurface)rootView.findViewById(R.id.preview);

            mSurface.setOnClickListener(new MyCameraSurface.OnClickListener() {
                public void onClick(View v) {
                    mSurface.mCamera.takePicture(null, null, mPicture);
                    // TODO: List Picture Fragment Notify Change
                    //mCallback.onArticleSelected(1);
                }
            });

            return rootView;
        }

        @Override
        public void onAttach(Activity activity){
            super.onAttach(activity);

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (OnHeadlineSelectedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnHeadlineSelectedListener");
            }


        }

        Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.v("CallCamera", "Take Picure!!!");
                String sd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SnowTest";
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String formatDate = sdfNow.format(date);

                File dirPath = new File(sd);
                if(!dirPath.exists())   dirPath.mkdirs();


                String path = sd + "/" + formatDate + ".jpg";

                File file = new File(path);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "파일 저장 중 에러 발생 : " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.parse("file://" + path);
                intent.setData(uri);
                getActivity().sendBroadcast(intent);

                MediaScannerConnection.scanFile(getActivity(),
                        new String[] { sd }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("TAG", "Finished scanning " + path);
                                mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] {"SnowTest"}, null);
                                Log.d("THUMB", "mCursorCnt is " + mCursor.getCount());
                                mAdapter.updateCursor(mCursor);

                                mAdapter.notifyDataSetChanged();
                            }
                        });

                Toast.makeText(getActivity(), "사진 저장 완료 : " + path,
                        Toast.LENGTH_SHORT).show();

//                Bitmap source = BitmapFactory.decodeFile(path);
//                ThumbnailUtils.extractThumbnail(source, 80, 80);

                mSurface.mCamera.startPreview();
            }
        };
    }

    public static class PictureList extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public PictureList() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PictureList newInstance() {
            PictureList fragment = new PictureList();
            //Bundle args = new Bundle();
            //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            //fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_picture, container, false);
            Log.d("CREATE", "PictureList is created");

            mGrid = (GridView) rootView.findViewById(R.id.list_picture);
            mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] {"SnowTest"}, null);

            mAdapter = new ImageAdapter(getActivity(), mCr, mCursor);
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(mItemClickListener);

            return rootView;
        }

        AdapterView.OnItemClickListener mItemClickListener =
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        mCursor.moveToPosition(position);
                        String path = mCursor.getString(mCursor.getColumnIndex(
                                MediaStore.Images.ImageColumns.DATA));
                        Intent intent = new Intent(getActivity(),
                                ImageFull.class);
                        intent.putExtra("path", path);
                        startActivity(intent);
                    }
                };
    }
}

class ImageAdapter extends BaseAdapter {
    private Context mContext;
    DataSetObservable mDataSetObservable = new DataSetObservable();
    private static ContentResolver mCr;
    private Cursor mCursor;


    public ImageAdapter(Context c) {
        mContext = c;
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

        // TODO: 크기를 고정시키면 앱종료
        //thumbnailImage.setHeight(10);
        //thumbnailImage.setWidth(10);
        imageView.setImageBitmap(thumbnailImage);
        imageView.setAdjustViewBounds(true);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        return imageView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer){
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer){ // DataSetObserver의 해제
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged(){ // 위에서 연결된 DataSetObserver를 통한 변경 확인
        Log.d("NOTIFY", "mCursor Count is " + mCursor.getCount());
        mDataSetObservable.notifyChanged();
    }

}