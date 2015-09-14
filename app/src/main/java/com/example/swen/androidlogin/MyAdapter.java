package com.example.swen.androidlogin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by swen on 15-09-14.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
    private ArrayList<JSONObject> mData;

    public MyAdapter(ArrayList<JSONObject>dataSet) {
        mData = dataSet;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        JSONObject obj = mData.get(position);
        holder.userTextView.setText(obj.optString("email"));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView userTextView;
        public MyViewHolder(View v) {
            super(v);
            userTextView = (TextView) v.findViewById(R.id.user_text_view);
        }
    }
}