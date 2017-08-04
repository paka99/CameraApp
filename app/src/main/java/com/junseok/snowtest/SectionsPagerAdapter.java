package com.junseok.snowtest;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by PEM_ljs on 2017-08-04.
 */

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    static ImageAdapter mAdapter;

    private static ContentResolver mCr;
    private static Cursor mCursor;
    private static GridView mGrid;
    static int mNumPicture;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
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

    public void setContentResolver(ContentResolver resolver){
        mCr = resolver;
    }

    /* *****************************************
    ********************************************
    **********Camera Preview Fragment***********
    ********************************************
    *******************************************/
    public static class PreviewFrag extends Fragment {
        private MyCameraSurface mSurface;
        boolean mFocus;

        public PreviewFrag() {
            mFocus = true;
        }

        public static PreviewFrag newInstance() {
            PreviewFrag fragment = new PreviewFrag();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.frag_preview, container, false);
            Log.d("CREATE", "Preview is created");
            mSurface = (MyCameraSurface)rootView.findViewById(R.id.preview);

            mSurface.setOnClickListener(new MyCameraSurface.OnClickListener() {
                public void onClick(View v) {
                    mSurface.mCamera.takePicture(null, null, mPicture);
                }
            });

            return rootView;
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

                Toast.makeText(getActivity(), "사진 저장 완료 : " + path,
                        Toast.LENGTH_SHORT).show();

//                while(mNumPicture == mCursor.getCount()){
//                    mCursor.close();
//                    mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] {"SnowTest"}, MediaStore.Images.Media.DATE_ADDED + " desc");
//                    Log.d("CALLBACK", "mCursorCnt is " + mCursor.getCount());
//                }
//
//                mNumPicture = mCursor.getCount();
//                mAdapter.updateCursor(mCursor);
//                mAdapter.notifyDataSetChanged();

                mSurface.mCamera.startPreview();
            }
        };
    }

    /* *****************************************
    ********************************************
    **********Album List Fragment***********
    ********************************************
    *******************************************/

    public static class PictureList extends Fragment {
        public PictureList() {}

        ContentObserver contentObserver = new ContentObserver( new Handler() ){
            @Override
            public void onChange( boolean selfChange ){
                super.onChange( selfChange );
                mCursor.close();
                mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] {"SnowTest"}, MediaStore.Images.Media.DATE_ADDED + " desc");

                mAdapter.updateCursor(mCursor);
                mAdapter.notifyDataSetChanged();
            }
        };

        public static PictureList newInstance() {
            PictureList fragment = new PictureList();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_picture, container, false);
            //rootView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 5));
            Log.d("CREATE", "PictureList is created");

            mGrid = (GridView) rootView.findViewById(R.id.list_picture);
            mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?", new String[] {"SnowTest"}, MediaStore.Images.Media.DATE_ADDED + " desc");
//            mCursor = mCr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    null, null, null, null);

            mNumPicture = mCursor.getCount();
            mAdapter = new ImageAdapter(getActivity(), mCr, mCursor);
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(mItemClickListener);
            mCr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, contentObserver );


            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
            Log.i("LISTFRAG", "Here is List Fragment Resume");
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

