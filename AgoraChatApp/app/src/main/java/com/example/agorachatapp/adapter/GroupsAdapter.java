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

import com.example.agorachatapp.R;
import com.example.agorachatapp.groupchat.GroupChatActivity;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    Context context;
    List<String> mListGroups;
    List<String> mListNames;

    public GroupsAdapter(Context context, List<String> mListGroups, List<String> mListNames) {
        this.context = context;
        this.mListGroups = mListGroups;
        this.mListNames = mListNames;

    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mTextViewGroupName.setText(mListNames.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("group-id",mListGroups.get(position));
                intent.putExtra("group-name",mListNames.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListGroups.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView mTextViewGroupName;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewGroupName = itemView.findViewById(R.id.text_username);
        }
    }
}
