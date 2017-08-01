package com.junseok.snowtest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //MyCameraSurface mSurface;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static ContentResolver mCr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setCurrentItem(pos);

        mCr = getContentResolver();
    }

    @Override
    public void onStart() {
        super.onStart();
//        final int pos = 1;
//        // TODO: 작동 방식 알아보기. && 앱 onStop에 간 뒤 제대로 작동 x
          // TODO: 아마도 Listing 화면이랑 Preview 화면이랑 서로 다른 Thread로 돌려야할듯.
//        mViewPager.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mViewPager.setCurrentItem(pos);
//            }
//        }, 100);
    }


    public static class PreviewFrag extends Fragment {
        private MyCameraSurface mSurface;

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
                mSurface.mCamera.startPreview();
            }
        };
    }

    public static class PictureList extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private GridView mGrid;
        private Cursor mCursor;

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
            mCursor = mCr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            ImageAdapter Adapter = new ImageAdapter(getActivity());
            mGrid.setAdapter(Adapter);

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

        class ImageAdapter extends BaseAdapter {
            private Context mContext;

            public ImageAdapter(Context c) {
                mContext = c;
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
                Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.
                                EXTERNAL_CONTENT_URI,
                        mCursor.getString(mCursor.getColumnIndex(MediaStore.
                                Images.Thumbnails._ID)));
                imageView.setImageURI(uri);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                return imageView;
            }
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 1){
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
            // Show 2 total pages.
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
    }
}

// 미리보기 표면 클래스
class MyCameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;

    public MyCameraSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    // 표면 생성시 카메라 오픈하고 미리보기 설정
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    // 표면 파괴시 카메라도 파괴한다.
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    // 표면의 크기가 결정될 때 최적의 미리보기 크기를 구해 설정한다.
    public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> arSize = params.getSupportedPreviewSizes();
        if (arSize == null) {
            params.setPreviewSize(width, height);
        } else {
            int diff = 10000;
            Camera.Size opti = null;
            for (Camera.Size s : arSize) {
                if (Math.abs(s.height - height) < diff) {
                    diff = Math.abs(s.height - height);
                    opti = s;

                }
            }
            params.setPreviewSize(opti.width, opti.height);
        }
        mCamera.setParameters(params);
        mCamera.startPreview();
    }
}



