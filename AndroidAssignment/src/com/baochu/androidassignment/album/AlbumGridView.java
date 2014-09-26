package com.baochu.androidassignment.album;

import java.util.ArrayList;
import java.util.List;

import com.baochu.androidassignment.R;
import com.baochu.androidassignment.Utils;
import com.facebook.Session;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AlbumGridView extends LinearLayout {

    private AlbumActivity mActivity;
    private GridView mGridView;
    private AlbumGridAdapter mAlbumGridAdapter;
    private AlbumGridViewController mAlbumGridViewController;
    private AlertDialog mProgressDialog;

    public AlbumGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (AlbumActivity) context;
        mAlbumGridAdapter = new AlbumGridAdapter(mActivity);
        mAlbumGridViewController = new AlbumGridViewController(mActivity, mAlbumGridAdapter);
    }
    
    private OnClickListener mAlbumOnClicklistener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            String albumId = (String) view.getTag(R.id.album_id_tag);
            String albumName = (String) view.getTag(R.id.album_name_tag);
            Intent intent = new Intent(mActivity, PhotoActivity.class);
            intent.putExtra(AlbumActivity.ALBUM_ID_KEY, albumId);
            intent.putExtra(AlbumActivity.ALBUM_NAME_KEY, albumName);
            mActivity.startActivity(intent);
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGridView = (GridView) findViewById(R.id.albums_gridview);
        mGridView.setAdapter(mAlbumGridAdapter);
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            if (!session.isPermissionGranted(Utils.PHOTO_PERMISSION)) {
                Utils.requestPermissions(mActivity, session, Utils.getUserAlbumPhotoReadPermission());
            } else {
                startAlbumRequest();
                showProgressDialog();
            }
        }
    }
    
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAlbumGridViewController.cancelAlbumRequest();
    }
    
    
    private void showProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = Utils.showProgressDialog(mActivity);
        }
    }
    
    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            //Toast to indicate user waiting for image download patiently.
            Toast menuToast = Toast.makeText(mActivity, "Waiting download...", Toast.LENGTH_LONG);
            menuToast.setGravity(Gravity.BOTTOM, 20, 20);
            menuToast.show();
        }
    }
    
    public void startAlbumRequest() {
        mAlbumGridViewController.startAlbumRequest();
    }
    
    /** A Adapter to render the view of GridView */
    public class AlbumGridAdapter extends BaseAdapter {
        private Context mContext;
        private List<AlbumObject> mAlbumObjectList;
        
        public AlbumGridAdapter(Context c) {
            mContext = c;
            mAlbumObjectList = new ArrayList<AlbumObject>();
        }

        public List<AlbumObject> getAlbumObjectList() {
            return mAlbumObjectList;
        }
        
        public int getCount() {
            return mAlbumObjectList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        /** create a new ImageView for each item referenced by the Adapter */
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumObject albumObject = mAlbumObjectList.get(position);
            GridItemView itemView;
            if (convertView == null) {
                itemView = (GridItemView) LayoutInflater.from(mContext).inflate(R.layout.image_item_view, null);
            } else {
                itemView = (GridItemView) convertView;
            }
            
            itemView.setTag(R.id.album_id_tag, albumObject.getAlbumId());
            itemView.setTag(R.id.album_name_tag, albumObject.getAlbumName());
            itemView.setOnClickListener(mAlbumOnClicklistener);
            itemView.update(albumObject); 
            return itemView;
        }
    }
    
}
