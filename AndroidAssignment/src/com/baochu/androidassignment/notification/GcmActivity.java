/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baochu.androidassignment.notification;

import com.baochu.androidassignment.Utils;
import com.baochu.assignment.R;
import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class GcmActivity extends Activity {

    public static final String EXTRA_MESSAGE = "my_message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /** The key used to send message to GCM, which is registered on Google API Console */
    private static final String SEND_API_KEY = "AIzaSyCxS2RGz9JGDzGBeBJrVGeEUghcX0omijE"; 
    
    /**
     * Every Android app using Google Play service API must be registered on Google API Console
     * to obtain an API Key. Meanwhile, Google also generates a project number for you app.
     * Set the project number as SENDER_ID.
     */
    private static final String SENDER_ID = "189807095475";
    private static final String TAG = "GcmActivity";
    
    private TextView mDisplay;
    private EditText mMessageBox;
    private Button mSendButton;
    private Button mClearButton;
    
    private GoogleCloudMessaging mGCM;
    private Context mContext;
    private String mRegisterId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_gcm);
        mContext = getApplicationContext();
        
        mDisplay = (TextView) findViewById(R.id.display);
        mMessageBox = (EditText) findViewById(R.id.edit_box);
        mSendButton = (Button) findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(view);
            }
        });
        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(view);
            }
        });
                
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            mGCM = GoogleCloudMessaging.getInstance(this);
            mRegisterId = getRegistrationId(mContext);
            if (mRegisterId.isEmpty()) {
                registerInBackground();
            } else {
                mDisplay.setText("Device registered successfully." + "\n" + "ID=" + mRegisterId);
            }
        } else {
            Log.e(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                mDisplay.setText("This device is not supported Google Play Services");
            }
            
            mSendButton.setEnabled(false);
            return false;
        }
        
        mSendButton.setEnabled(true);
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (mGCM == null) {
                        mGCM = GoogleCloudMessaging.getInstance(mContext);
                        //Sleep 500 milliseconds to wait GoogleCloudMessaging initialized completely.
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            //Do nothing
                        }
                    }
                    mRegisterId = mGCM.register(SENDER_ID);
                    msg = "Device registered successfully." + "\n" + "ID=" + mRegisterId;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, mRegisterId);
                } catch (IOException ex) {
                    msg = "Device registered Failed: " + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onButtonClick(final View view) {
        if (view == findViewById(R.id.send_button)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String status = "";
                    if (mRegisterId == null || mRegisterId.length() == 0) {
                        status = "mRegisterId = null, please wait device registration completed !";
                        return status;
                    }
                    
                    String message = mMessageBox.getText().toString();
                    if (message != null && message.length() > 0) {
                        //status = sendUpstreamMessageToThirdPartyServer(message);
                        status = sendPayloadMessageToClientApp(mRegisterId, message);
                    } else {
                        status = "Please input message.";
                    }
                    return status;
                }

                @Override
                protected void onPostExecute(String statusMsg) {
                    mDisplay.append("\n\n" + statusMsg);
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear_button)) {
            mDisplay.setText("");
            mMessageBox.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences(GcmActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this app.
     */
    private void sendRegistrationIdToBackend() {
        // Do nothing.
    }
    
    /**
     * Send upstream message to the 3rd party server from GCM client app.
     * http://developer.android.com/google/gcm/client.html#sample-send
     */
    private String sendUpstreamMessageToThirdPartyServer(String msgData) {
        String status = null;
        Bundle data = new Bundle();
        data.putString(EXTRA_MESSAGE, msgData); 
        String id = Integer.toString(Utils.getMessageId());
        try {
            mGCM.send(SENDER_ID + "@gcm.googleapis.com", id, data);
            status = "Sent message success.";
        } catch (IOException e) {
            status = "Error :" + e.getMessage();
        }
        return status;
    }
    
    /**
     * Send message to the GCM client app from 3rd party server.
     * http://developer.android.com/google/gcm/http.html
     */
    private String sendPayloadMessageToClientApp(String registerId, String msgData) {
        Result result = null;
        final int retries = 3;
        String status = null;
        try {
            Message message = new Message.Builder()
            .collapseKey("1")
            .timeToLive(3)
            .delayWhileIdle(true)
            .addData("message", msgData).build();

            Sender sender = new Sender(SEND_API_KEY);    
            result = sender.send(message, registerId, retries);   
            if (result.getErrorCodeName() == null) {
                Log.e(TAG, "############## GCM Notification is sent successfully ##############");
                status = "Sent message success.";
            } else {
                Log.e(TAG, "############## Error occurred while sending GCM Notification :" + result.getErrorCodeName() + "##############");
                status = "Error :" + result.getErrorCodeName();
            }
        } catch (InvalidRequestException e) {
            status = "Invalid Request :" + e.getMessage();
        } catch (IOException e) {
            status = "IO Exception :" + e.getMessage();
        }
        return status;
    }
    
}


