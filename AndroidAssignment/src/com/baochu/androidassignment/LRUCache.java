package com.baochu.androidassignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

/**
 * General purpose Least Recently Used (LRU) Cache.
 * 
 * The cache holds MAX_CACHE_SIZE (50) Items at most.
 * 
 * Items are purged when a low memory warning is received.
 * 
 * Items are purged after being stored for MAX_AGE (one hour).
 * 
 * In ad-hoc testing, the overall cache hit rate was observed to be around 33% (which is very good).
 * 
 * The cache hit rate metric is reported to PMET and further adjustments can be made based on actual customer usage.
 * 
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final String TAG = "Amazon";
    private static final boolean DEBUG = false;

    /**
     * The max cache size. We already limit the size (in bytes) of the bitmap, this parameter intent to limit the count
     * of items, since SearchResultsView has more than 150 item for one search, smaller value makes there always shows
     * no image when scroll up and down and trigger the network requests more frequently.
     */
    private static final int MAX_CACHE_SIZE = 200;

    /**
     * Hash table load factor.
     */
    private static final float LOAD_FACTOR = 0.75f;
    
    /**
     * The global LRU memory cache.
     * 
     * NOTE: LinkedHashMap is not synchronized
     * 
     * Access to sGlobalCache must be guarded by the singleton sGlobalCache
     * instance.
     */
    private static final LRUCache<String, Object> sGlobalCache = new LRUCache<String, Object>();

    /**
     * Purge items from the cache after MAX_AGE (one hour)
     */
    private static final long MAX_AGE = 60 * 60 * 1000;
    
    /**
     * The pre-set max cache size limit in bytes.
     */
    private static final long MAX_CACHE_SIZE_IN_BYTES = 5 * 1024 * 1024;
    
    /**
     * The current limit for cache, changes when called setCacheLimit.
     */
    private static long mCurrentLimit = MAX_CACHE_SIZE_IN_BYTES;
    
    /**
     * Keep track of when items were added to the cache and purge them after
     * MAX_AGE
     */
    private final Map<String, TimeAttachment> mInsertTime = new HashMap<String, TimeAttachment>();

    /**
     * create with: initial capacity of cacheSize, load factor of 0.75f, and
     * order the LinkedList by last access.
     * 
     * Least Recently accessed objects will be evicted when the size of the
     * Collection reaches MAX_CACHE_SIZE.
     * 
     */
    private LRUCache() {
        super(MAX_CACHE_SIZE, LOAD_FACTOR, true);
    }

    private static volatile double sCacheGets = 0;
    private static volatile double sCacheHits = 0;

    /**
     * Stores the total bitmap size in Cache (in bytes).
     */
    private long mBitmapSize = 0;
    
    /**
     * @return the estimated cache hit rate: total hits divided by total gets
     */
    public static double getCacheHitRate() {
        return sCacheHits / sCacheGets;
    }

    /**
     * Return the Object if it is present in the cache and the object is an
     * instance of the specified Class.
     * @param uriStr, the URI string.
     * @return the Object or null
     */
    public static Object getValue(final String uriStr) {
        // Add the null check for uriStr. In some cases that uriStr may be null, for example in search result stub call result, the 
        // image url may be null, thus, it has no need to process any further operation
        Object o = null;
        if( null == uriStr){
            sCacheGets++;
            return o;
        }
        
        final String key = uriStr;

        synchronized (sGlobalCache) {
            TimeAttachment ta = sGlobalCache.mInsertTime.get(key);
            final Long t = (ta != null) ? ta.getTime() : null;
            if (null != t && MAX_AGE < SystemClock.elapsedRealtime() - t) {
                if (DEBUG) {
                    Log.v(TAG, "purging stale object from cache " + key);
                }
                sGlobalCache.remove(key);
                sGlobalCache.mInsertTime.remove(key);
            } else {
                o = sGlobalCache.get(key);
                
                // Update time to remove the eldest correctly.
                if (ta != null)
                    ta.setTime(SystemClock.elapsedRealtime());
            }
            if (null != o) {
                o = null;
                sGlobalCache.remove(key);
                sGlobalCache.mInsertTime.remove(key);
            }

        }
        
        return o;
    }

    /**
     * Put the object into the cache.
     * 
     * @param key, the URI string.
     * @param value, the value to be stored.
     */
    public static void putValue(final String key, final Object value) {
        putValueWithKey(key, value);
    }

    /**
     * Put the object into the cache.
     * 
     * @param key, the MD5 value of the URI string.
     * @param value, the value to be stored.
     */
    public static void putValueWithKey(final String key, final Object value) {
        synchronized (sGlobalCache) {
            sGlobalCache.put(key, value);
            sGlobalCache.mInsertTime.put(key, new TimeAttachment(key, SystemClock.elapsedRealtime()));
            if (value != null && value instanceof Bitmap) {
                Bitmap bitmap = (Bitmap)value;
                sGlobalCache.mBitmapSize += sizeOfBitmap(bitmap);
                if (sGlobalCache.mBitmapSize > mCurrentLimit) {
                    // Reduce more than mBitmapSize - mCurrentLimit, to avoid frequently reduceByPercent call.
                    // 25% of the cache is usually 1.5M ~ 4M, make the reduceByPercent called less
                    reduceByPercent(25); // 25%
                }
            }
            if (DEBUG) {
                Log.d("LRUCache", "From putValueWithKey: size=" + sGlobalCache.mBitmapSize);
            }
        }
    }

    /**
     * Called periodically by LinkedHashMap base class.
     * 
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        synchronized(sGlobalCache) {
            boolean remove = size() > MAX_CACHE_SIZE;
    
            if (!remove) {
                final Long t = mInsertTime.get(eldest.getKey()).getTime();
                if (null != t && MAX_AGE < SystemClock.elapsedRealtime() - t) {
                    remove = true;
                }
            }
            if (remove) {
                if (DEBUG) {
                    Log.v(TAG, "LRUCache removeEldestEntry " + remove + " " + eldest.getKey() + " cache hit rate: " + getCacheHitRate());
                }
                mInsertTime.remove(eldest.getKey());
                Object o = eldest.getValue();
                if (o != null && o instanceof Bitmap) {
                    sGlobalCache.mBitmapSize -= sizeOfBitmap((Bitmap)o);
                }
            }
            return remove;
        }
    }

    /**
     * Clear all cached data
     */
    public static void clearCache() {
        synchronized (sGlobalCache) {
            sGlobalCache.clear();
            sGlobalCache.mInsertTime.clear();
            sGlobalCache.mBitmapSize = 0;
        }
    }
    
    /**
     * Returns the byte size of the bitmap.
     * Refer to Bitmap.getByteCount, API level >= 12
     * @param bitmap the bitmap
     * @return the size in bytes
     */
    public static long sizeOfBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
    
    /**
     * must be called when low memory warnings occur
     */
    public static void onLowMemory() {
        if (DEBUG)
            Log.w(TAG, "LRUCache onLowMemory");
        
        clearCache();
    }

    /**
     * Required because we extend LinkedHashMap which is serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Set the cache limit 
     * @param size the cache limit in bytes
     */
    public static void setCacheLimit(long size) {
        mCurrentLimit = size;
    }

    /**
     * Reduce the LRUCache by size in bytes
     * @param size the space in bytes to reduce
     */
    public static void reduceBySize(long size) {
        reduceBySizeInternal(makeSortedList(), size);
    }
    
    /**
     * Remove from the cache base on the order of list until removed more than size bytes
     * or removed all in the list.
     * @param list the ordered list indicates the remove order.
     * @param size the size of the intended bytes to remove
     */
    private static long reduceBySizeInternal(List<TimeAttachment> list, long size) {
        long reduced = 0;
        synchronized (sGlobalCache) {
            if (size > 0) {
                for (TimeAttachment ta : list) {
                    if (ta != null) {
                        Object o = sGlobalCache.get(ta.getKey());
                        if (o != null && o instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap)o;
                            long bitmapSize = sizeOfBitmap(bitmap);
                            reduced += bitmapSize;
                            sGlobalCache.remove(ta.getKey());
                            sGlobalCache.mInsertTime.remove(ta.getKey());
                            sGlobalCache.mBitmapSize -= bitmapSize;
                            if (reduced >= size) {
                                break;
                            }
                        }
                    }
                } // for
            }
        }
        return reduced;
    }
    
    private static List<TimeAttachment> makeSortedList() {
        List<TimeAttachment> list = new ArrayList<TimeAttachment>();
        synchronized (sGlobalCache) {
            for (TimeAttachment ta : sGlobalCache.mInsertTime.values()) {
                list.add(ta);
            }
            Collections.sort(list);
        }
        return list;
    }
    /**
     * Reduce the LRUCache memory by percent/100 of the current size.
     * @param percent the percentage; if more than 100, clear all cache; if less than 0, do nothing.
     */
    public static void reduceByPercent(int percent) {
        if (percent >= 100) {
            clearCache();
            return;
        }
        
        if (percent < 0) {
            percent = 0;
            return;
        }

        long size = sGlobalCache.mBitmapSize;
        long sizePercent = size * percent / 100;
        reduceBySize(sizePercent);
    }

    private static class TimeAttachment implements Comparable<TimeAttachment> {
        private Long time;
        private String key;
        
        public TimeAttachment(String key, Long time) {
            this.time = time;
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
        
        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }
        
        @Override
        public int compareTo(TimeAttachment another) {
            if (another == null) {
                return -1;
            } 
            return time.compareTo(another.getTime());
        }
        
    }
}
