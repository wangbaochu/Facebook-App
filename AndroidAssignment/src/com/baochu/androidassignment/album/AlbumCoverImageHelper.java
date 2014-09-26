package com.baochu.androidassignment.album;

import android.widget.Toast;

import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.album.AlbumGridView.AlbumGridAdapter;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

/** 
 * Since the "/me/albums" Facebook Graph API return Album objects without 
 * cover image url, but only cover photo id, we need call photo Graph API 
 * to retrieve the cover image url via cover photo id.
 * */
public class AlbumCoverImageHelper {

    private AlbumActivity mActivity;
    private AlbumGridAdapter mAdapter;
    private AlbumGridViewController mAlbumController;
    private AlbumObject mAlbumObject;

    public AlbumCoverImageHelper(AlbumActivity activity, AlbumGridAdapter adapter, AlbumGridViewController controller, AlbumObject object) {
        mActivity = activity;
        mAdapter = adapter;
        mAlbumController = controller;
        mAlbumObject = object;
    }

    /** make the API call: The cover photo of a photo album */
    public void startCoverPhotoRequest() {
        Session session = Session.getActiveSession();
        String photoId = mAlbumObject.getCoverImageId();
        if (session != null && session.isOpened() && photoId != null) {
            new Request(
                    session,
                    "/" + photoId,
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            handleCoverPhotoResponse(response);
                        }
                    }).executeAsync();
        }
    }
    
    /** Handle the response of cover photo request */
    private void handleCoverPhotoResponse(Response response) {
        if (!mAlbumController.isAlbumRequestCancelled()) {
            if (response != null) {
                if (response.getError() == null) {
                    GraphObject object = response.getGraphObject();
                    if (object != null && mAlbumObject != null) {
                        String coverPhotoUrl = (String)object.getProperty("picture");
                        if (coverPhotoUrl != null) {
                            mAlbumObject.setAlbumCoverUrl(coverPhotoUrl);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Utils.handleError(mActivity, response.getError(), Utils.getUserAlbumPhotoReadPermission());
                }
            } else {
                Toast.makeText(mActivity, "AlbumCoverPhotoRequest return null !", Toast.LENGTH_LONG).show();
            }
        }
    }

}
