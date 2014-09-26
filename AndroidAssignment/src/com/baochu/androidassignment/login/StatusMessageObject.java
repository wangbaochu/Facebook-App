package com.baochu.androidassignment.login;

public class StatusMessageObject {

    /** The content of status message */
    private String mMessage;
    
    /** When the message is post */
    private String mUpdateTime;
    
    public StatusMessageObject(String message, String time) {
        mMessage = message;
        mUpdateTime = time;
    }
    
    public String getMessage() {
        return mMessage;
    }
    
    public String getUpdateTime() {
        return mUpdateTime;
    }
}
