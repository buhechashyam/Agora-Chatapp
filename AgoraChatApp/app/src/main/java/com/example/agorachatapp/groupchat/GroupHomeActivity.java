package com.example.agorachatapp.groupchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agorachatapp.adapter.GroupsAdapter;
import com.example.agorachatapp.databinding.ActivityGroupHomeBinding;
import com.example.agorachatapp.viewmodel.GroupViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupHomeActivity extends AppCompatActivity {
    ActivityGroupHomeBinding binding;
    Map<String, String> mGroups = new HashMap<>();
    GroupViewModel viewModel;
    String username, pw;
    GroupsAdapter adapter;
    //    List<String> groupNames = new ArrayList<>();
    List<String> groupIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(GroupHomeActivity.this).get(GroupViewModel.class);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle("Groups");

        SharedPreferences sharedPreferences = getSharedPreferences("user-login", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        pw = sharedPreferences.getString("pw", "");

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));


        viewModel.getAllGroups().observe(GroupHomeActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String string) {
                try {
                    JSONObject jsonObject = new JSONObject(string);

                    JSONArray groups = jsonObject.getJSONArray("data");
                    mGroups.clear();
//                    groupNames.clear();
                    groupIds.clear();

                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject object = groups.getJSONObject(i);
                        String groupId = object.getString("groupid");
                        String groupName = object.getString("groupname");

                        mGroups.put(groupName, groupId);
//                        groupNames.add(groupName);
                        groupIds.add(groupId);
                    }
                    viewModel.getAllJoinedGroup(username, groupIds).observe(GroupHomeActivity.this, new Observer<List<String>>() {
                        @Override
                        public void onChanged(List<String> joinedGroups) {
                            viewModel.getGroupName(joinedGroups).observe(GroupHomeActivity.this, new Observer<List<String>>() {
                                @Override
                                public void onChanged(List<String> names) {
                                    Log.d("MAIN",names.toString());
                                    binding.rvGroups.setAdapter(new GroupsAdapter(GroupHomeActivity.this, joinedGroups,names));

                                }
                            });
                        }
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }


}