package com.baochu.androidassignment.login;

import java.util.ArrayList;
import java.util.List;

import com.baochu.androidassignment.R;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StatusListAdapter extends BaseAdapter {
    
    private FragmentActivity mActivity;
    private List<StatusMessageObject> mStatusObjectList;
    public List<StatusMessageObject> getStatusObjectList() {
        return mStatusObjectList;
    }
    
    public StatusListAdapter(FragmentActivity activity) {
        mActivity = activity;
        mStatusObjectList = new ArrayList<StatusMessageObject>();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mStatusObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StatusMessageObject object = mStatusObjectList.get(position);
        StatusItemView itemView;
        if (convertView == null) {
            itemView = (StatusItemView) LayoutInflater.from(mActivity).inflate(R.layout.status_item_view, null); 
        } else {
            itemView = (StatusItemView)convertView;
        }
        
        itemView.update(object);
        return itemView;
    }

}
