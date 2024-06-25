package com.example.agorachatapp.chatgroup.using_restapi;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.agorachatapp.databinding.ActivityGroupChatDashboardBinding;
import com.example.agorachatapp.viewmodel.GroupViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;

public class GroupChatDashboard extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ActivityGroupChatDashboardBinding binding;
    String appKey = "611166664#1353835";
    String username, password;
    String groupName;
    ArrayList<String> mAgoraUsers = new ArrayList<>();
    String selectedMember;
    String selectedGroup;
    Map<String, String> mGroups = new HashMap<>();
    List<String> groupNames = new ArrayList<>();
    GroupViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpClient();
        setupListener();

        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("pw");

        joinUser(username, password);

        //setup user spinner
        binding.spnUsers.setOnItemSelectedListener(this);
        setupUserSpinner();

        //setup group spinner
        setUpGroupSpinner();
        binding.spnGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = groupNames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mGroupName = binding.etGroupName.getText().toString().trim();
                String mGroupDesc = binding.etGroupDesc.getText().toString().trim();

                if (mGroupName.isEmpty() || mGroupDesc.isEmpty()) {
                    Toast.makeText(GroupChatDashboard.this, "Fill a Group name and descriptions", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.etGroupDesc.setText("");
                binding.etGroupName.setText("");
                binding.etGroupDesc.setFocusable(false);
                viewModel.createGroup(mGroupName, mGroupDesc, username).observe(GroupChatDashboard.this, new Observer<String>() {
                    @Override
                    public void onChanged(String groupId) {
                        if (groupId.isEmpty()) {
                            showToast("Group Not Created, Try again");
                        } else {
                            groupName = ChatClient.getInstance().groupManager().getGroup(groupId).getGroupName();
                            binding.textGroupName.setText(groupName);
                            showToast("Group Successfully Created");
                        }
                    }
                });
            }
        });

        binding.btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  if (mAgoraUsers.get(0))
                if (selectedMember == "Select Member") {
                    Toast.makeText(GroupChatDashboard.this, "Select Member", Toast.LENGTH_SHORT).show();
                    return;
                }
                String groupId = mGroups.get(selectedGroup);
                viewModel.addMemberToGroup(groupId, selectedMember).observe(GroupChatDashboard.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isAdded) {
                        if (isAdded) {
                            showToast("Member Added");
                        } else {
                            showToast("Member not Added");
                        }
                    }
                });
            }
        });
    }


    private void setupUserSpinner() {
        //Get all agora users and show in Spinner
        viewModel.getAllAgoraUsers().observe(GroupChatDashboard.this, new Observer<String>() {
            @Override
            public void onChanged(String string) {
                try {
                    JSONObject mainJsonObject = new JSONObject(string);
                    JSONArray jsonArray = mainJsonObject.getJSONArray("entities");

                    mAgoraUsers.clear();
                    mAgoraUsers.add("Select Member");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        mAgoraUsers.add(user.getString("username"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupChatDashboard.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, mAgoraUsers);
                    adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                    binding.spnUsers.setAdapter(adapter);
                } catch (JSONException e) {

                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void setUpGroupSpinner() {
        viewModel.getAllGroups().observe(GroupChatDashboard.this, new Observer<String>() {
            @Override
            public void onChanged(String string) {
                try {
                    JSONObject jsonObject = new JSONObject(string);

                    JSONArray groups = jsonObject.getJSONArray("data");
                    mGroups.clear();
                    groupNames.clear();
                    groupNames.add("Select a Group");

                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject object = groups.getJSONObject(i);
                        String groupId = object.getString("groupid");
                        String groupName = object.getString("groupname");

                        mGroups.put(groupName, groupId);
                        groupNames.add(groupName);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupChatDashboard.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, groupNames);
                    adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                    binding.spnGroups.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void joinUser(String username, String password) {
        ChatClient.getInstance().login(username, password, new CallBack() {
            @Override
            public void onSuccess() {
                Log.d("MAIN", "User Join");
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200) {
                    Log.d("MAIN", "User Join");
                } else {
                    Log.d("MAIN", "User Not Join");
                }
            }
        });
    }

    private void setupListener() {

        ChatClient.getInstance().chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {

            }
        });
        ChatClient.getInstance().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected(int errorCode) {

            }
        });
    }

    private void setUpClient() {

        if (appKey.isEmpty()) {
            showToast("App Key is Empty");
            return;
        }
        ChatOptions chatOptions = new ChatOptions();
        chatOptions.setAppKey(appKey);
        chatOptions.setAutoAcceptGroupInvitation(true);

        ChatClient.getInstance().init(this, chatOptions);
        ChatClient.getInstance().setDebugMode(true);
    }

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedMember = mAgoraUsers.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}