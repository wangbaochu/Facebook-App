package com.baochu.androidassignment;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;

public class AppPlatform {

    private static AppPlatform sAppPlatform;
    private Context sApplicationContext;
    private Handler mHandler;
    
    private AppPlatform(final Context context) {
        if(context instanceof Activity){
            this.sApplicationContext = context.getApplicationContext();
        } else {
            this.sApplicationContext = context;
        }
        mHandler = new Handler(context.getMainLooper());
    }
    
    public Context getApplicationContext() {
        return sApplicationContext;
    }

    public static AppPlatform getInstance() {
        return sAppPlatform;
    }
    
    public static void setUpInstance(final Context context) {
        if (sAppPlatform == null) {
            sAppPlatform = new AppPlatform(context);
        }
        initLRUCacheSize(context);
    }
    
    private static void initLRUCacheSize(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        LRUCache.setCacheLimit(am.getMemoryClass() * 1024 * 1024 / 4);        
    }
    
    /**
     * Execute runnable.run() asynchronously on the main UI event dispatching thread,
     * after a delay of delayMillis milliseconds.
     *
     * @param runnable runnable object put into this application's event queue.
     * @param delayMillis runnable will be run after a delay of delayMillis milliseconds
     */
    public void invokeLater(final Runnable runnable, long delayMillis) {
        mHandler.postDelayed(runnable, delayMillis);
    }
    
    public void invokeLater(final Runnable runnable) {
        mHandler.post(runnable);
    }
}
