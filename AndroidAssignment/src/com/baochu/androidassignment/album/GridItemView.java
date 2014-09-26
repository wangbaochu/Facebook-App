package com.baochu.androidassignment.album;

import com.baochu.androidassignment.BaseImageObject;
import com.baochu.androidassignment.BitmapFetcher;
import com.baochu.androidassignment.HttpFetcher;
import com.baochu.androidassignment.HttpFetcher.Params;
import com.baochu.androidassignment.LRUCache;
import com.baochu.androidassignment.R;
import com.baochu.androidassignment.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridItemView extends LinearLayout implements HttpFetcher.Subscriber<Bitmap> {
    
    /** This is save for checking the response BitmapFetcher response is still the request one.*/
    public BaseImageObject mImageObject;
    
    /** Must be called on the main UI thread */
    private BitmapFetcher mBitmapFetcher;
    
    private ImageView mImageView;
    private TextView mImageText;
    
    public GridItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.item_image);
        mImageText = (TextView)findViewById(R.id.item_image_text);
    }
    
    /** Update the image item view */
    public void update(BaseImageObject result) {
        mImageObject = result;

        // Try to get product image from LRCCache
        String imageUrl = result.getImageThumbnailUrl();
        if (imageUrl != null) {
            Bitmap productImageBitmap = (Bitmap) LRUCache.getValue(imageUrl);
            if (productImageBitmap != null) {
                Utils.updateImageWithDrawable(productImageBitmap, mImageView);
            } else if (mImageObject != null) {
                // If get nothing from LRCCache, fetch the image by BitmapFetcher 
                getImageFromBackground(mImageObject.getImageThumbnailUrl());
                
            } 
        } else {
            mImageView.setImageResource(R.drawable.noimage);
        }
        
        if (mImageObject != null) {
            mImageText.setText(mImageObject.getImageName());
        }
    }
    
    protected void getImageFromBackground(String url) {
        if (null != mBitmapFetcher && !mBitmapFetcher.isCancelled()) {
            if (mBitmapFetcher.getParams().getUrl().equals(url)) {
                // same URL don't cancel,just wait
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
            if (baseUrl.equals(mImageObject.getImageThumbnailUrl())) {
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
