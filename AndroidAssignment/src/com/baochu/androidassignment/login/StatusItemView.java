package com.baochu.androidassignment.login;

import com.baochu.androidassignment.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusItemView extends LinearLayout {

    private TextView mMessageTextView;
    private TextView mMessageUpdateTime;
    
    public StatusItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessageTextView = (TextView) findViewById(R.id.status_message);
        mMessageUpdateTime = (TextView)findViewById(R.id.status_time);
    }
    
    public void update(StatusMessageObject object) {
        mMessageTextView.setText(object.getMessage());
        mMessageUpdateTime.setText(object.getUpdateTime());
    }

}
