package com.baochu.androidassignment.album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.album.PhotoGridView.PhotoGridAdapter;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

/**
 * This controller is response for retrieve user's all photos of a album via Facebook Graph API
 */
public class PhotoGridViewController {

    private PhotoActivity mActivity;
    private PhotoGridAdapter mPhotoGridAdapter;
    private boolean mIsPhotoRequestCancelled = false;
    
    public PhotoGridViewController(PhotoActivity activity, PhotoGridAdapter adapter) {
        mActivity = activity;
        mPhotoGridAdapter = adapter;
    }
    
    /** make the API call: retrieve photos of an album */
    public void startPhotoRequest(String albumId) {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            new Request(
                    session,
                    "/" + albumId + "/photos",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            mActivity.getPhotoGridView().dismissProgressDialog();
                            handlePhotoResponse(response);
                        }
                    }
                    ).executeAsync();
        }
    }
    
    /** Cancel the API call: retrieve the user's albums */
    public void cancelPhotoRequest() {
        mIsPhotoRequestCancelled = true;
    }
    
    /** Parse the album response data */
    private void handlePhotoResponse(Response response) {
        if (!mIsPhotoRequestCancelled) {
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
                                        mPhotoGridAdapter.getPhotoObjectList().clear();
                                        for (int i =0; i < length; i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            String photoThumbnailUrl = object.optString("picture");
                                            String photoUrl = object.optString("source");
                                            String photoName = object.optString("name");
                                            mPhotoGridAdapter.getPhotoObjectList().add(new PhotoObject(photoThumbnailUrl, photoUrl, photoName));
                                        }
                                        //Notify to update photo list.
                                        mPhotoGridAdapter.notifyDataSetChanged();
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(mActivity, "PhotoRequest return wrong json data !", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else {
                    Utils.handleError(mActivity, response.getError(), Utils.getUserAlbumPhotoReadPermission());
                }
            } else {
                Toast.makeText(mActivity, "PhotoRequest return null !", Toast.LENGTH_LONG).show();
            }
        }
    }
}
