package com.baochu.androidassignment;

import java.lang.ref.WeakReference;

import android.app.Activity;

/**
 *  This Class is target to handle the OOM exception from UI thread.
 *  We will encapsulate the code previously executed in the UI thread but subject to throw OOM exception
 *  to {@link UiRunnable}, then run it still in the UI thread immediately but under OOM exception protection.
 *  This will drop down the probability of our app's crash due to OOM.
 */
public class UiOOMHandler {

    private WeakReference<Activity> mActivityWeakReference;

    /**
     * Should only be executed under UI thread.
     */
    private UiRunnable mUiRunnable;
    
    public static final String TAG = "OutOfMemory";

    public UiOOMHandler(final Activity activity,
            UiRunnable uiRunnable) {
        mActivityWeakReference = new WeakReference<Activity>(activity);
        mUiRunnable = uiRunnable;
    }

    public void execute() {
    	Activity activity = mActivityWeakReference.get();
        if (null != activity) {
            activity.runOnUiThread(mUiRunnable);
        }
    }

    public static class UiRunnable implements Runnable {
        
        /**
         * The task encapsulated the code from UI should be executed still under UI thread but under OOM protection.
         */
        private Runnable mRunnable;       
        
        /**
         * The flag to control if the task encapsulated the code from UI need to be executed again
         * when it encounters an OOM exception.
         */
        private boolean mNeedRetry;
        
        /**
         * The counter to track how many times the task has been retried when need retry.
         */
        private int mRetryCounter = 0;
        
        /**
         * The max times the task will be executed when need retry.
         */
        private static final int MAX_RETRY_TIMES = 3;
        

        /**
         * The period need to wait before executing the runnable next time. 
         * The value is from {@link BitmapUtil}}.
         */
        private static final long DELAY = 230;

        public UiRunnable(Runnable runnable, boolean needRetry) {
            mRunnable = runnable;
            mNeedRetry = needRetry;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (OutOfMemoryError e) {
                LRUCache.reduceByPercent(50);
                System.gc();
                if (mNeedRetry && mRetryCounter++ < MAX_RETRY_TIMES) {
                    AppPlatform.getInstance().invokeLater(this, DELAY);                    
                }
            }
        }
    }

}
