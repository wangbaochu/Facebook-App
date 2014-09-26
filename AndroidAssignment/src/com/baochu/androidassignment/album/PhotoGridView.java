package com.baochu.androidassignment.album;

import java.util.ArrayList;
import java.util.List;

import com.baochu.androidassignment.R;
import com.baochu.androidassignment.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoGridView extends LinearLayout {
    
    private PhotoActivity mActivity;
    private PhotoGridAdapter mPhotoGridAdapter;
    private GridView mGridView;
    private TextView mTitleBarText;
    private PhotoGridViewController mPhotoGridViewController;
    private AlertDialog mProgressDialog;
    
    public PhotoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (PhotoActivity) context;
        mPhotoGridAdapter = new PhotoGridAdapter(mActivity);
        mPhotoGridViewController = new PhotoGridViewController(mActivity, mPhotoGridAdapter);
    }

    private OnClickListener mPhotoOnClicklistener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            String imageUrl = (String) view.getTag();
            PhotoBigView photoBigView = (PhotoBigView) LayoutInflater.from(mActivity).inflate(R.layout.photo_big_view, null);
            mActivity.pushView(photoBigView, true);
            photoBigView.upateImage(imageUrl);
        }
    };
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mTitleBarText = (TextView) findViewById(R.id.title_bar_text);
        String albumName = mActivity.getPhotoAlbumName();
        if (albumName != null) {
            mTitleBarText.setText(mActivity.getResources().getString(R.string.photoes_in_album, albumName));
        }
        mGridView = (GridView) findViewById(R.id.photos_gridview);
        mGridView.setAdapter(mPhotoGridAdapter);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        String albumId = mActivity.getPhotoAlbumId();
        if (albumId != null) {
            showProgressDialog();
            mPhotoGridViewController.startPhotoRequest(albumId);
        }
    }
    
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPhotoGridViewController.cancelPhotoRequest();
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
            //Toast to indicate user waiting for photo download patiently.
            Toast menuToast = Toast.makeText(mActivity, "Waiting download...", Toast.LENGTH_LONG);
            menuToast.setGravity(Gravity.BOTTOM, 20, 20);
            menuToast.show();
        }
    }
    
    /** A Adapter to render the view of GridView */
    public class PhotoGridAdapter extends BaseAdapter {
        private Context mContext;
        private List<PhotoObject> mPhotoObjectList;
        
        public PhotoGridAdapter(Context c) {
            mContext = c;
            mPhotoObjectList = new ArrayList<PhotoObject>();
        }

        public List<PhotoObject> getPhotoObjectList() {
            return mPhotoObjectList;
        }
        
        public int getCount() {
            return mPhotoObjectList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            PhotoObject object = mPhotoObjectList.get(position);
            GridItemView itemView;
            if (convertView == null) {
                itemView = (GridItemView) LayoutInflater.from(mContext).inflate(R.layout.image_item_view, null);
            } else {
                itemView = (GridItemView) convertView;
            }
            
            itemView.setTag(object.getOriginPictureUrl());
            itemView.setOnClickListener(mPhotoOnClicklistener);
            itemView.update(object); 
            return itemView;
        }
    }
    
}
