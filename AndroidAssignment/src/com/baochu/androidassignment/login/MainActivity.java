package com.baochu.androidassignment.login;

import com.baochu.androidassignment.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {

    private static final int LOGIN = 0;
    private static final int PROFILE = 1;
    private static final int SETTINGS = 2;
    private static final int FRAGMENT_COUNT = 3;

    private boolean mIsResumed = false;
    private UiLifecycleHelper mUIHelper;
    private Fragment[] mFragmentArray = new Fragment[FRAGMENT_COUNT];
    private MenuItem mMenuSettings;
    private Session.StatusCallback mCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIHelper = new UiLifecycleHelper(this, mCallback);
        mUIHelper.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment loginFragment = (LoginFragment) fm.findFragmentById(R.id.loginFragment);
        mFragmentArray[LOGIN] = loginFragment;
        mFragmentArray[PROFILE] = fm.findFragmentById(R.id.profileFragment);
        mFragmentArray[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < mFragmentArray.length; i++) {
            transaction.hide(mFragmentArray[i]);
        }
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsResumed = true;
        mUIHelper.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mIsResumed = false;
        mUIHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mUIHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUIHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUIHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // if the session is already open, try to show the profile fragment
            showFragment(PROFILE, false);
        } else {
            // otherwise present the login screen and ask the user to login.
            showFragment(LOGIN, false);
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mFragmentArray[PROFILE].isVisible()) {
            if (menu.size() == 0) {
                mMenuSettings = menu.add(R.string.settings);
            }
            return true;
        } else {
            menu.clear();
            mMenuSettings = null;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(mMenuSettings)) {
            showFragment(SETTINGS, true);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
            if (mFragmentArray[SETTINGS].isVisible()) {
                FragmentManager manager = getSupportFragmentManager();
                int backStackSize = manager.getBackStackEntryCount();
                for (int i = 0; i < backStackSize; i++) {
                    manager.popBackStack();
                }
                showFragment(PROFILE, false);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (mIsResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            
            if (state.equals(SessionState.OPENED)) {
                showFragment(PROFILE, false);
            } else if (state.isClosed()) {
                /** Make sure to clear the cached Graph User in ProfileFragment.
                 * So that next time when ProfileFragment is visible, it will 
                 * trigger makeMeRequest() to retrieve the latest user data.*/
                ((ProfileFragment) mFragmentArray[PROFILE]).clearGraphUser();
                showFragment(LOGIN, false);
            }
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < mFragmentArray.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(mFragmentArray[i]);
            } else {
                transaction.hide(mFragmentArray[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
