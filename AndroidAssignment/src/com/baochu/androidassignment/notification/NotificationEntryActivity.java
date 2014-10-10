package com.baochu.androidassignment.notification;

import com.baochu.assignment.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NotificationEntryActivity extends Activity {

    private Button mGCMNotification;
    private Button mIGetuiNotification;
    private Context mContext;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_entry);
        mContext = this;
        
        mGCMNotification = (Button)findViewById(R.id.gcm_notification_button);
        mGCMNotification.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GcmActivity.class);
                mContext.startActivity(intent);
            }
        });
        
        mIGetuiNotification = (Button)findViewById(R.id.igetui_notification_button);
        mIGetuiNotification.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, IGetuiActivity.class);
                mContext.startActivity(intent);
            }
        });
    }
}
