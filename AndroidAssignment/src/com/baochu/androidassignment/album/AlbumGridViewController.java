package com.baochu.androidassignment.album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.album.AlbumGridView.AlbumGridAdapter;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

/**
 * This controller is response for retrieve user's albums data via Facebook Graph API
 */
public class AlbumGridViewController {

    private AlbumActivity mActivity;
    private AlbumGridAdapter mAlbumGridAdapter;
    private boolean mIsAlbumRequestCancelled = false;

    public AlbumGridViewController(AlbumActivity activity, AlbumGridAdapter adapter) {
        mActivity = activity;
        mAlbumGridAdapter = adapter;
    }

    /** make the API call: retrieve the user's albums */
    public void startAlbumRequest() {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            new Request(
                    session,
                    "/me/albums",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            mActivity.getAlbumGridView().dismissProgressDialog();
                            handleAlbumResponse(response);
                        }
                    }).executeAsync();
        }
    }

    /** Cancel the API call: retrieve the user's albums */
    public void cancelAlbumRequest() {
        mIsAlbumRequestCancelled = true;
    }
    public boolean isAlbumRequestCancelled() {
        return mIsAlbumRequestCancelled;
    }

    /** Parse the album response data */
    private void handleAlbumResponse(Response response) {
        if (!mIsAlbumRequestCancelled) {
            if (response != null) {
                if (response.getError() == null) {
                    GraphObject graphObject = response.getGraphObject();
                    if (graphObject != null) {
                        JSONObject jsonObject = graphObject.getInnerJSONObject();
                        if (jsonObject != null) {
                            try {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                if (jsonArray != null) {
                                    int length = jsonArray.length();
                                    if (length > 0) {
                                        mAlbumGridAdapter.getAlbumObjectList().clear();
                                        for (int i =0; i < length; i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            String albumId = object.optString("id");
                                            String albumName = object.optString("name");
                                            String albumCoverPhotoId = object.optString("cover_photo");
                                            AlbumObject newAlbumObject = new AlbumObject(albumId, albumCoverPhotoId, albumName);
                                            mAlbumGridAdapter.getAlbumObjectList().add(newAlbumObject);
                                            
                                            /** Since the "/me/albums" Facebook Graph API return Album objects without 
                                             * cover image url, but only cover photo id, we need call photo Graph API 
                                             * to retrieve the cover image url with the id. */
                                            AlbumCoverImageHelper coverImageHelper = new AlbumCoverImageHelper(mActivity, mAlbumGridAdapter, this, newAlbumObject);
                                            coverImageHelper.startCoverPhotoRequest();
                                        }
                                        //Notify to update album list.
                                        mAlbumGridAdapter.notifyDataSetChanged();
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(mActivity, "AlbumRequest return wrong json data !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    Utils.handleError(mActivity, response.getError(), Utils.getUserAlbumPhotoReadPermission());
                }
            } else {
                Toast.makeText(mActivity, "AlbumRequest return null !", Toast.LENGTH_LONG).show();
            }
        }
    }
}
