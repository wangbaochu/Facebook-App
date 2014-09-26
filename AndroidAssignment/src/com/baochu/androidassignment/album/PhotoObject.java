package com.baochu.androidassignment.album;

import com.baochu.androidassignment.BaseImageObject;

public class PhotoObject implements BaseImageObject {

    /** The thumbnail of photo */
    private String mThumbnailUrl;
    
    /** The full original size of photo */
    private String mOriginPictureUrl;
    
    /** The name of image */
    private String mImageName;
    
    public PhotoObject(String thumbnailUrl, String photoUrl, String name) {
        mThumbnailUrl = thumbnailUrl;
        mOriginPictureUrl = photoUrl;
        mImageName = name; 
    }
    
    public void setThumbnailUrl(String url) {
        mThumbnailUrl = url;
    }
    
    public void setOriginPictureUrl(String url) {
        mOriginPictureUrl = url;
    }
    
    public void setImageName(String name) {
        mImageName = name;
    }
    
    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }
    
    public String getOriginPictureUrl() {
        return mOriginPictureUrl;
    }

    @Override
    public String getImageThumbnailUrl() {
        return mThumbnailUrl;
    }

    @Override
    public String getImageName() {
        return mImageName;
    }
}
