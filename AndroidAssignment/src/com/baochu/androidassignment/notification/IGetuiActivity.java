package com.baochu.androidassignment.notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baochu.assignment.R;
import com.igexin.sdk.PushManager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class IGetuiActivity extends Activity implements OnClickListener {
    /**
     * Application secret keys that registered on http://www.igetui.com
     */
    private static final String MASTERSECRET = "xSWAlsXFIHAlm7qGXEH9B9";
    private static String appid = null;
    private static String appkey = null;
    private static String appsecret = null;
    public static String sClientId = "1c7e126264160d47803792df29fbf524";

    private TextView mAppkeyView = null;
    private TextView mAppSecretView = null;
    private TextView mAppIdView = null;
    private TextView mMasterSecretView = null;
    private EditText mMsgInputView = null;
    private Button mMsgSendBtn = null;
    private Button mMsgClearBtn = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_igetui);

        mMsgClearBtn = (Button) findViewById(R.id.msg_clear_button);
        mMsgClearBtn.setOnClickListener(this);

        mAppkeyView = (TextView) findViewById(R.id.tvappkey);
        mAppSecretView = (TextView) findViewById(R.id.tvappsecret);
        mMasterSecretView = (TextView) findViewById(R.id.tvmastersecret);
        mAppIdView = (TextView) findViewById(R.id.tvappid);
        mMsgInputView = (EditText) findViewById(R.id.msg_edit_box);
        mMsgSendBtn = (Button) findViewById(R.id.msg_send_button);
        mMsgSendBtn.setOnClickListener(this);

        String packageName = getApplicationContext().getPackageName();
        ApplicationInfo appInfo;
        try {
            appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                appid = appInfo.metaData.getString("PUSH_APPID");
                appsecret = appInfo.metaData.getString("PUSH_APPSECRET");
                appkey = (appInfo.metaData.get("PUSH_APPKEY") != null) ? appInfo.metaData.get("PUSH_APPKEY").toString() : null;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        //update ui
        mAppkeyView.setText("AppKey=" + appkey);
        mAppSecretView.setText("AppSecret=" + appsecret);
        mMasterSecretView.setText("MasterSecret=" + MASTERSECRET);
        mAppIdView.setText("AppID=" + appid);
        
        // Initialize the SDK
        Log.d("GetuiSdkDemo", "initializing sdk...");
        PushManager.getInstance().initialize(this.getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.d("GetuiSdkDemo", "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Log.d("GetuiSdkDemo", "onStop()");
        super.onStop();
    }

    public void onClick(View view) {
        if (view == mMsgClearBtn) {
            mMsgInputView.setText("");
        } else if (view == mMsgSendBtn) {
            String message = mMsgInputView.getText().toString();
            if (message == null || message.length() == 0) {
                Toast.makeText(this, "Please input message !", Toast.LENGTH_LONG).show();
                return;
            }

            if (isNetworkConnected()) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("action", "pushmessage");
                param.put("appkey", appkey);
                param.put("appid", appid);
                param.put("data", message);
                Date curDate = new Date(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                param.put("time", formatter.format(curDate));
                param.put("clientid", sClientId);
                param.put("expire", 3600);
                param.put("sign", IGetuiSdkHttpPost.makeSign(MASTERSECRET, param));
                postMessage(param);
            } else {
                Toast.makeText(this, "Sorry, network is unavailable !", Toast.LENGTH_LONG).show();
            }
        }
    }

    /** Post message */
    private void postMessage(final Map<String, Object> param) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IGetuiSdkHttpPost.httpPost(param);
            }
        }).start();
    }

    /**
     * Judge whether the network is connected or not
     */
    public boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

}
