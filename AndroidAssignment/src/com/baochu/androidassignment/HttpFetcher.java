package com.baochu.androidassignment;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.util.Log;

public abstract class HttpFetcher<T> implements Runnable {

    private static final String TAG = "HttpFetcher";
    private static final boolean DEBUG = false;
    private final boolean mCacheContent;
    
    // Set time out to 30s
    private static final int  sTimeOut = 30000;

    private final Params<T> mParams;

    public Params<T> getParams() {
        return mParams;
    }

    public String getUrl() {
        return getParams().getUrl();
    }

    public HttpFetcher(final Params<T> params) {
        this.mParams = params;
        this.mCacheContent = true;
    }

    public HttpFetcher(final Params<T> params, final boolean cacheContent) {
        this.mParams = params;
        this.mCacheContent = cacheContent;
    }

    /**
     * Subscriber interface to implement to receive HTTP GET responses.
     */
    public interface Subscriber<T> {
        /**
         * NOTE: called on the Main UI Thread after the response is received and processed.
         * 
         * @param response
         *            - will never be null
         * @param params
         */
        public void onHttpResponseReceived(final T response, final Params<T> params);

        /**
         * Called on the main UI Thread when the Http Request fails for some reason.
         * 
         * @param params
         */
        public void onHttpRequestFailed(final Params<T> params);

    }

    public static class Params<T> {
        protected String mUrl;

        /**
         * @return the Url
         */
        public String getUrl() {
            return mUrl;
        }

        /**
         * Get the key for caching this request. Defaults to the URL.
         */
        public String getCacheKey() {
            return getUrl();
        }

        private final Subscriber<T> mSubscriber;

        /**
         * @return the Subscriber
         */
        public Subscriber<T> getSubscriber() {
            return mSubscriber;
        }

        public Params(final String url, final Subscriber<T> subscriber) {
            this.mUrl = url;
            this.mSubscriber = subscriber;
        }
    }

    @SuppressWarnings("unchecked")
    protected T fetchInMemCache() {
        T result = null;
        final Params<T> p = getParams();
        result = (T) LRUCache.getValue(p.getCacheKey());
        if (null != result) {
            onPostExecute(result);
        }
        return result;
    }

    /**
     * Performs the HTTP GET request and processes the results in the background thread.
     */
    @Override
    final public void run() {
        if (null != fetchInMemCache()) {
            return;
        }

        final Params<T> p = getParams();
        T result = null;
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = getConnection(p.getUrl());
            httpConnection.connect();
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode >= 300) {
                throw new IOException(httpConnection.getResponseCode() + " " + httpConnection.getResponseMessage());
            }
            result = handleResponse(httpConnection);
            
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeConnection(httpConnection);
        }

        if (null != result && mCacheContent) {
            // Cache result in LRUCache
            LRUCache.putValue(p.getCacheKey(), result);
        }
        
        onPostExecute(result);
    }

    public static HttpURLConnection getConnection(String url) throws IOException {
        final URL u = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) u.openConnection();
        httpConnection.setConnectTimeout(sTimeOut);
        httpConnection.setReadTimeout(sTimeOut);
        return httpConnection;
    }

    public static void closeConnection(HttpURLConnection httpConnection) {
        if (null != httpConnection) {
            try {
                httpConnection.getInputStream().close();
            } catch (Exception e) {
                // ignore exception during close
                if (DEBUG) {
                    Log.v(TAG, e.toString());
                    e.printStackTrace();
                }
            }
            try {
                httpConnection.disconnect();
            } catch (Exception e) {
                // ignore exception during close
                if (DEBUG) {
                    Log.v(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Processes an HttpURLConnection and returns some value corresponding to that response.
     * 
     * NOTE: called from the Background Worker Thread
     */
    public abstract T handleResponse(final HttpURLConnection httpConnection) throws IOException;

    /**
     * Sends the results to the main UI thread.
     */
    final protected void onPostExecute(final T result) {
        AppPlatform.getInstance().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isCancelled()) {
                    if (null == result) {
                        mParams.getSubscriber().onHttpRequestFailed(mParams);
                    } else {
                        mParams.getSubscriber().onHttpResponseReceived(result, mParams);
                    }
                }
            }
        }, 0);
    }

    
    private volatile boolean cancelled = false;
    
    /**
     * Cancel this Http fetch. Cancellation is asynchronous and may fail.
     */
    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled(){
        return cancelled;
    }
    

    /**
     * Begin the http fetch using the given Executor.
     * 
     * This method will look up the LRUCache first. If the data can be found in the cache, then it just return
     * that data. If the data is not in the cache, then it will apply a thread to do the work.
     * 
     * This way, we can solve this kind of problem (e.g. https://jira1.amazon.com/browse/ANDROID-369):
     * The size of ThreadPool is limited. So, when the network is poor, the previous "fetch" may occupy all the
     * threads in the pool. In this case, later call to "fetch" will be blocked although the data they want to
     * fetch may already been stored in the LRUCache.
     */
    public void fetch(final Executor executor) {
        if (null == fetchInMemCache()) {
            executor.execute(this);
        }
    }

    /** Adjust the thread pool size from 16 to 8. In v2.1, it is 8 and we enlarge it as 16 in v2.2. 
     * Change this value back based on:
     * 1) 8 threads should be enough for most of the devices. Two many threads may cause the kernel scheduling burden and the real 
     *    work thread have less chance to occupy the system resources.
     * 2) For other pages except home view, the critical images to measure the latency does not exceed 8
     * 3) In homeview, since we care more about the first 3 images in HomeCriticalFeature, 8 should not be a problem. 
     */
    protected static final Executor sExecutor = Executors.newFixedThreadPool(8);
    
    /**
     * Begin the http fetch using a small worker thread pool.
     */
    public void fetch() {
        fetch(sExecutor);
    }  
}
