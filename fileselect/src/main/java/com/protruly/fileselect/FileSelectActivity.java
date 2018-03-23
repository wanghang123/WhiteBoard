package com.protruly.fileselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.protruly.fileselect.DirectoryFragment.DocumentSelectActivityDelegate;

import java.util.ArrayList;

import static com.protruly.fileselect.DirectoryFragment.EXTRA_RESULT;

public class FileSelectActivity extends AppCompatActivity {
    private final static String TAG = FileSelectActivity.class.getSimpleName();

//    private Toolbar toolbar;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction = null;
    private DirectoryFragment mDirectoryFragment;

    public static final String EXTRA_REQUEST_TYPE = "request_type";
    public static final int REQUEST_IMAGE = 2;
    public static final int REQUEST_BACKGROUND = 3;
    public static final int REQUEST_SELECT_VIDEO = 4;
    public static final int REQUEST_SELECT_DOC = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_files_main);

        ApplicationLoader.applicationContext = this.getApplicationContext();
        Intent intent = getIntent();
        int mRequestType = intent.getIntExtra(EXTRA_REQUEST_TYPE, -1);

        // 设置标题
        String titleStr = "选择文件";
        switch (mRequestType){
            case REQUEST_IMAGE:
                titleStr = "选择图片";
                break;
            case REQUEST_BACKGROUND:
                titleStr = "选择背景图片";
                break;
            case REQUEST_SELECT_VIDEO:
                titleStr = "选择视频";
                break;
            case REQUEST_SELECT_DOC:
                titleStr = "选择文档";
                break;
            default:
                break;
        }
        setTitle(titleStr);

        mDirectoryFragment = DirectoryFragment.getInstance(mRequestType);
        mDirectoryFragment.setDelegate(mDelegate);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mDirectoryFragment, "" + mDirectoryFragment.toString());
        fragmentTransaction.commit();
    }
	
    @Override
    protected void onDestroy() {
        mDirectoryFragment.onFragmentDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryFragment.onBackPressed_()) {
            super.onBackPressed();
        }
    }

    /**
     * 文件选择监听回调
     */
    private DocumentSelectActivityDelegate mDelegate = new DocumentSelectActivityDelegate() {
        @Override
        public void startDocumentSelectActivity() {

        }

        @Override
        public void didSelectFiles(DirectoryFragment activity,
                                   ArrayList<String> files) {
//                mDirectoryFragment.showErrorBox(files.get(0).toString());

            Log.d(TAG,"file path->" + files.get(0).toString());
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, files);
            setResult(Activity.RESULT_OK, data);
            finish();
        }

        @Override
        public void updateToolBarName(String name) {
//                toolbar.setTitle(name);

        }
    };

}
