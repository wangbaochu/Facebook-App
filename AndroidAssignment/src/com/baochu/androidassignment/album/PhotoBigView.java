package com.baochu.androidassignment.album;

import com.baochu.androidassignment.BitmapFetcher;
import com.baochu.androidassignment.HttpFetcher;
import com.baochu.androidassignment.R;
import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.HttpFetcher.Params;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PhotoBigView extends LinearLayout implements HttpFetcher.Subscriber<Bitmap> {

    private ImageView mImageView; 
    /** Must be called on the main UI thread */
    private BitmapFetcher mBitmapFetcher;
    private String mImageUrl;
    
    public PhotoBigView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.photo_image);
    }

    /** Update the picture of ImageView */
    public void upateImage(String url) {
        if (url != null) {
            mImageUrl = url;
            getImageFromBackground(url);
        }
    }
    
    /** Download the image via url */
    private void getImageFromBackground(String url) {
        if (null != mBitmapFetcher && !mBitmapFetcher.isCancelled()) {
            if (mBitmapFetcher.getParams().getUrl().equals(url)) {
                // same URL don't cancel... just wait
                return;
            } else {
                cancelBitmapFetcher();
            }
        }
        if (null != url) {
            mBitmapFetcher = new BitmapFetcher(url, this);
            mBitmapFetcher.fetch();
        }
    }

    /** Subscriber for receive the product thumbnail. */
    @Override
    public void onHttpResponseReceived(Bitmap response, Params<Bitmap> params) {
        if(null != mBitmapFetcher && !mBitmapFetcher.isCancelled()){
            final String baseUrl = params.getUrl();
            if (baseUrl.equals(mImageUrl)) {
                Utils.updateImageWithDrawable(response, mImageView);
            }
            mBitmapFetcher = null;
        }
    }

    @Override
    public void onHttpRequestFailed(Params<Bitmap> params) {
        // no-op
    }
    
    /** Must be called on the main UI thread */
    private void cancelBitmapFetcher() {
        if (null != mBitmapFetcher) {
            mBitmapFetcher.cancel();
            mBitmapFetcher = null;
        }
    }
    
    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        cancelBitmapFetcher();
    }
}
