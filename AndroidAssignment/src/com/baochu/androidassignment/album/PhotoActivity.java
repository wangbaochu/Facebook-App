package com.baochu.androidassignment.album;

import com.baochu.assignment.R;
import com.baochu.androidassignment.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import android.widget.ViewAnimator;

public class PhotoActivity extends Activity {

    /** which album this photo activity belong to */
    private String mPhotoAlbumId;
    public String getPhotoAlbumId() {
        return mPhotoAlbumId;
    }
    /** The Album name that will be used to display on the title bar. */
    private String mPhotoAlbumName;
    public String getPhotoAlbumName() {
        return mPhotoAlbumName;
    }
    
    private ViewAnimator mViewAnimator;
    private PhotoGridView mPhotoGridView;
    public PhotoGridView getPhotoGridView() {
        return mPhotoGridView;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoAlbumId = getIntent().getStringExtra(AlbumActivity.ALBUM_ID_KEY);
        mPhotoAlbumName = getIntent().getStringExtra(AlbumActivity.ALBUM_NAME_KEY);
        mPhotoGridView = (PhotoGridView) LayoutInflater.from(this).inflate(R.layout.photos_grid_view, null);
        setRootView(mPhotoGridView);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String id = intent.getStringExtra(AlbumActivity.ALBUM_ID_KEY);
        if (id != null) {
            mPhotoAlbumId = id;
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mViewAnimator != null) {
            if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                // only override the Activity Back Key behavior other keys should work as it is.
                final View currentView = getCurrentView();
                if (currentView != null && currentView.dispatchKeyEvent(event)) {
                    return true;
                } else {
                    popView();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    /**
     * Get the current original view of the viewAnimator
     * @return currentView
     */
    public View getCurrentView() {
        if (mViewAnimator != null) {
            return mViewAnimator.getCurrentView();
        }
        return null;
    }
    
    /**
     * Close (dismiss) the soft keyboad if open
     */
    public void closeSoftKeyboard(){
        if ( mViewAnimator != null && 0 < mViewAnimator.getChildCount()) {
            Utils.closeSoftKeyboard(mViewAnimator.getCurrentView());
        }
    }
    
    /** Construct the view animator */
    private ViewAnimator getViewAnimator() {
        if (mViewAnimator == null) {
            mViewAnimator = new ViewAnimator(this);
            mViewAnimator.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            super.setContentView(mViewAnimator);
        }
        return mViewAnimator;
    }
    
    /** A map to cache the animation */
    private SparseArray<Animation> animationMap = new SparseArray<Animation>();
    
    /**
     * Retrieves an animation form id.
     * @param id - resource id of the animation
     * @return Animation or null if id is zero
     */
    private Animation getAnimation(int id) {
        // zero is an invalid resource Id
        if (id == 0) {
            return null;
        }
        
        Animation animation;
        if ((animation = animationMap.get(id)) == null) {
            animation = AnimationUtils.loadAnimation(this, id);
            animationMap.put(id, animation);
        }
        
        return animation;
    }
    
    /**
     * Push a view into ViewAnimator.
     * @param view View the view to transition to. 
     * @param useAnimation Whether we use the in/out animation.
     */
    public void pushView(final View view, boolean useAnimation) {
        if (useAnimation) {
            pushView(view, R.anim.transition_push_in, R.anim.transition_push_out);
        } else {
            pushView(view, 0, 0);
        }
    }
    
    /**
     * Push a view into ViewAnimator with a in/out animation.
     * @param view the view to transition to. 
     * @param inId - resource id of in animation
     * @param outId - resource id of out animation
     */
    public void pushView(final View view, int inId, int outId) {
        closeSoftKeyboard();

        final ViewAnimator va = getViewAnimator();
        va.setInAnimation(getAnimation(inId));
        va.setOutAnimation(getAnimation(outId));
        va.addView(view);
        va.showNext();
    }
    
    /**
     * Pop the topmost view or finish this activity is the ViewAnimator pop empty.
     */
    public void popView() {
        View top = null;
        closeSoftKeyboard();

        if (null != mViewAnimator && 1 < mViewAnimator.getChildCount()) {
            top = mViewAnimator.getCurrentView();
            mViewAnimator.setInAnimation(getAnimation(R.anim.transition_pop_in));
            mViewAnimator.setOutAnimation(getAnimation(R.anim.transition_pop_out));
            mViewAnimator.showPrevious();
            mViewAnimator.removeView(top);
        } else {
            finish();
        }
    }
    
    /**
     * Set the displayed root view for this activity. Removes all 
     * current views and displays the new view.
     * @param view root view
     */
    public void setRootView(final View view) {
        if (null == mViewAnimator) {
            getViewAnimator();
        } else {
            closeSoftKeyboard();
            mViewAnimator.removeAllViews();
        }
        
        // apply animation
        mViewAnimator.setInAnimation(getAnimation(R.anim.transition_push_in));
        mViewAnimator.setOutAnimation(getAnimation(R.anim.transition_push_out));
        mViewAnimator.addView(view);
        mViewAnimator.showNext();
    }
}
