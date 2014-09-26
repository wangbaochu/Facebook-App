package com.baochu.androidassignment;

import android.app.Application;

public class AppApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        AppPlatform.setUpInstance(this);
    }
    
    @Override
    public void onLowMemory (){
        // clear the LRU cache memory if application in the low memory condition.
        LRUCache.onLowMemory();
        super.onLowMemory();
    }
}
