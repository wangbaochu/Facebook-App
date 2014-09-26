package com.baochu.androidassignment.album;

import com.baochu.androidassignment.R;
import com.baochu.androidassignment.RequestCodes;
import com.facebook.Session;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

public class AlbumActivity extends Activity {

    /** The key used to set album id in intent */
    public static String ALBUM_ID_KEY = "album_id";
    public static String ALBUM_NAME_KEY = "album_name";
    private static Session mUserSession;
    private AlbumGridView mAlbumGridView;
    public AlbumGridView getAlbumGridView() {
        return mAlbumGridView;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumGridView = (AlbumGridView) LayoutInflater.from(this).inflate(R.layout.albums_gird_view, null);
        setContentView(mAlbumGridView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.PHOTO_PERMISSION_REQUEST) {
            if (mUserSession != null) {
                mUserSession.onActivityResult(this, requestCode, resultCode, data);
            }
            mAlbumGridView.startAlbumRequest();
        }
    }
}
