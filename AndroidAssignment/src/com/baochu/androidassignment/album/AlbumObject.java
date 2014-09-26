package com.baochu.androidassignment.album;

import com.baochu.androidassignment.BaseImageObject;

public class AlbumObject implements BaseImageObject {

    /** The id of the album object */
    private String mAlbumId;
    
    /** The cover photo id of the album */
    private String mCoverImageId;
    
    /** The album cover photo url */
    private String mAlbumCoverUrl;
    
    /** The name of the album*/
    private String mAlbumName;
    
    public AlbumObject(String albumId, String coverId, String name) {
        mAlbumId = albumId;
        mCoverImageId = coverId;
        mAlbumCoverUrl = null;
        mAlbumName = name;
    }
    
    public void setAlbumId(String id) {
        mAlbumId = id;
    }
    
    public void setCoverImageId(String id) {
        mCoverImageId = id;
    }
    
    public void setAlbumCoverUrl(String coverUrl) {
        mAlbumCoverUrl = coverUrl;
    }
    
    public void setAlbumName(String name) {
        mAlbumName = name;
    }
    
    public String getAlbumId() {
        return mAlbumId;
    }
    
    public String getCoverImageId() {
        return mCoverImageId;
    }
    
    public String getAlbumName() {
        return mAlbumName;
    }

    @Override
    public String getImageThumbnailUrl() {
        return mAlbumCoverUrl;
    }

    @Override
    public String getImageName() {
        return mAlbumName;
    }  
}



