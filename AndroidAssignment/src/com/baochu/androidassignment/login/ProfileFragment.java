/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baochu.androidassignment.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.baochu.assignment.R;
import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.album.AlbumActivity;
import com.baochu.androidassignment.map.GMSMapActivity;
import com.baochu.androidassignment.notification.GcmActivity;
import com.baochu.androidassignment.notification.NotificationEntryActivity;
import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
/**
 * Fragment that represents the main selection screen for Scrumptious.
 */
public class ProfileFragment extends Fragment {

    private TextView mUserInfo;
    private ProfilePictureView mProfilePictureView;
    private Button mAlbumButton;
    private Button mLocationButton;
    private Button mGCMMessageButton;
    private ListView mStatusListView;
    private StatusListAdapter mStatusListAdapter;
    private GraphUser mUser;
    private AlertDialog mProgressDialog;
    private boolean isMakeMeRequestStarted = false;
    private boolean isStatusesRequestStarted = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        mProfilePictureView = (ProfilePictureView)view.findViewById(R.id.profile_Picture);
        mProfilePictureView.setCropped(true);
        mUserInfo = (TextView)view.findViewById(R.id.user_info);
        
        mAlbumButton = (Button)view.findViewById(R.id.album_button);
        mAlbumButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = ProfileFragment.this.getActivity();
                Intent intent = new Intent(context, AlbumActivity.class);
                context.startActivity(intent);
            }
        });
        
        mLocationButton = (Button)view.findViewById(R.id.location_button);
        mLocationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = ProfileFragment.this.getActivity();
                Intent intent = new Intent(context, GMSMapActivity.class);
                context.startActivity(intent);
            }
        });
        
        mGCMMessageButton = (Button)view.findViewById(R.id.gcm_message_button);
        mGCMMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = ProfileFragment.this.getActivity();
                Intent intent = new Intent(context, NotificationEntryActivity.class);
                context.startActivity(intent);
            }
        });
        
        mStatusListView = (ListView)view.findViewById(R.id.status_list);
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.status_list_header, null);
        mStatusListView.addHeaderView(header, null, false);  
        mStatusListAdapter = new StatusListAdapter(getActivity());
        mStatusListView.setAdapter(mStatusListAdapter);
        
        return view;
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        
        final Session session = Session.getActiveSession();
        if (mUser == null && !hidden && session != null && session.isOpened()) {
            if (!isMakeMeRequestStarted) {
                makeMeRequest();
                isMakeMeRequestStarted = true;
            }
            if (!isStatusesRequestStarted) {
                startStatusesRequest();
                isStatusesRequestStarted = true;
            }
            showProgressDialog();
        }  
    }

    private void showProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = Utils.showProgressDialog(getActivity());
        }
    }
    
    private void dismissProgressDialog() {
        if (!isMakeMeRequestStarted && !isStatusesRequestStarted) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }
    
    /**
     * Cache Graph User in ProfileFragment, so next time when enter into ProfileFragment
     * we needn't to retrieve user data again. Make sure to clear it if session is closed.
     * Called in {@link MainActivity#onSessionStateChange}
     */
    public void clearGraphUser() {
        mUser = null;
    }

    /** Get the current user profile info */
    private void makeMeRequest() {
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    isMakeMeRequestStarted = false;
                    dismissProgressDialog();
                    
                    if (session == Session.getActiveSession() && user != null) {
                        mUser = user;
                        updateUI();
                    }
                    if (response != null && response.getError() != null) {
                        Utils.handleError(getActivity(), response.getError(), Utils.getProfileReadPermission());
                    }
                }
            });
            request.executeAsync();
        }
    }
    
    /** Get the user's status */
    private void startStatusesRequest() {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            new Request(
                    session,
                    "/me/statuses",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            isStatusesRequestStarted = false;
                            dismissProgressDialog();
                            
                            handleStatusResponse(response);
                        }
                    }).executeAsync();
        }
    }
    
    /** Parse the interest field from response */
    private void handleStatusResponse(Response response) {
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
                                    mStatusListAdapter.getStatusObjectList().clear();
                                    for (int i = 0; i < length; i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        String status_message = object.optString("message");
                                        String status_time = object.optString("updated_time");
                                        mStatusListAdapter.getStatusObjectList().add(new StatusMessageObject(status_message, status_time));
                                    }
                                    //Notify to update user status message list.
                                    mStatusListAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "StatusesRequest return wrong json data !", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {
                Utils.handleError(getActivity(), response.getError(), Utils.getUserStatusReadPermission());
            }
        } else {
            Toast.makeText(getActivity(), "StatusesRequest return null !", Toast.LENGTH_LONG).show();
        }
    }
    
    /** Update user's profile info */
    private void updateUI() {
        if (mUser != null) {
            mUserInfo.setText(buildUserInfoDisplay(mUser));
            mProfilePictureView.setProfileId(mUser.getId());
        } else {
            mUserInfo.setText(null);
            mProfilePictureView.setProfileId(null);
        }
    }
    
    /**
     * Build user's info to display on the screen
     */
    private String buildUserInfoDisplay(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        /**(name) - no special permissions required */
        userInfo.append(String.format("Name: %s\n", user.getName()));

        /** (birthday) - requires user_birthday permission */
        userInfo.append(String.format("Birthday: %s\n", user.getBirthday()));

        /**  (location) - requires user_location permission */
        GraphPlace location = user.getLocation();
        if (location != null) {
            userInfo.append(String.format("Location: %s\n", location.getProperty("name")));
        }

        /** (locale) - no special permissions required */
        userInfo.append(String.format("Locale: %s\n", user.getProperty("locale")));

        /** (languages) - requires user_likes permission. */
        JSONArray languages = (JSONArray)user.getProperty("languages");
        if (languages != null && languages.length() > 0) {
            ArrayList<String> languageNames = new ArrayList<String>();
            for (int i=0; i < languages.length(); i++) {
                JSONObject language = languages.optJSONObject(i);
                languageNames.add(language.optString("name"));
            }
            userInfo.append(String.format("Languages: %s\n", languageNames.toString()));
        }
        return userInfo.toString();
    }
    
}
