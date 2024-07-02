package com.example.agorachatapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agorachatapp.one2onechat.ChatActivity;
import com.example.agorachatapp.R;
import com.example.agorachatapp.room.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    Context context;
    List<User> mUserIds;

    public UsersAdapter(Context context, List<User> mUserIds) {
        this.context = context;
        this.mUserIds = mUserIds;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user,null);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.mTextViewUserIds.setText(mUserIds.get(position).getUserId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("user-id", mUserIds.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserIds.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewUserIds;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextViewUserIds = itemView.findViewById(R.id.text_username);
        }
    }
}
