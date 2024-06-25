package com.example.agorachatapp;

import static android.os.AsyncTask.execute;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.agorachatapp.chatgroup.using_restapi.GroupChatActivity;
import com.example.agorachatapp.chatgroup.using_restapi.GroupChatDashboard;
import com.example.agorachatapp.databinding.ActivityAuthBinding;
import com.example.agorachatapp.one2onechat.using_restapi.One2OneChatActivity;
import com.example.agorachatapp.viewmodel.GroupViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;

public class AuthActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityAuthBinding binding;
    private String appKey = "611166664#1353835";
    ChatClient chatClient;
    String appToken = "007eJxTYDhU/qVp47H/Un1bWrWK8t3kdN5eT6gVOVv4eSF7uOLRr6oKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpe5Or0hoCGRkWdBoxMzKwMjAyMDGA+AwMAAxHH64=";
    String REGISTER_URL = "https://a61.chat.agora.io/611166664/1353835/users";
    GroupViewModel viewModel;
    String selectedGroup;
    Map<String, String> mGroups = new HashMap<>();
    List<String> groupNames = new ArrayList<>();
    boolean groupChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        setupChatClient();

        setUpSpinner();
        binding.spnGroups.setOnItemSelectedListener(this);

        binding.spnGroups.setVisibility(View.VISIBLE);
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_group_chat) {
                groupChat = true;
                binding.spnGroups.setVisibility(View.VISIBLE);
                binding.btnManageGroups.setVisibility(View.VISIBLE);
            } else {
                binding.spnGroups.setVisibility(View.GONE);
                binding.btnManageGroups.setVisibility(View.GONE);
                groupChat = false;

            }
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            if (groupChat) {
                if (binding.username.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedGroup == "Select a Group") {
                    Toast.makeText(AuthActivity.this, "Please Select a group", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUp();
            } else {
                if (binding.username.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUp();
            }

        });

        binding.btnLogin.setOnClickListener(v -> {
            if (groupChat) {
                if (binding.username.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedGroup == "Select a Group") {
                    Toast.makeText(AuthActivity.this, "Please Select a group", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginToAgoraChat();
            } else {
                if (binding.username.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()) {
                    Toast.makeText(AuthActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginToAgoraChat();
            }


        });

        binding.btnManageGroups.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, GroupChatDashboard.class)));
    }

    private void setUpSpinner() {
        viewModel.getAllGroups().observe(AuthActivity.this, new Observer<String>() {
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(AuthActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, groupNames);
                    adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                    binding.spnGroups.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void loginToAgoraChat() {
        //login
        chatClient.login(binding.username.getText().toString(), binding.password.getText().toString(), new CallBack() {
            @Override
            public void onSuccess() {
                String username = binding.username.getText().toString().trim();
                String pw = binding.password.getText().toString().trim();

                runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login Success", Toast.LENGTH_SHORT).show());


                goToDestination(username, pw, groupChat);
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200) {
                    String username = binding.username.getText().toString().trim();
                    String pw = binding.password.getText().toString().trim();

                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login Success", Toast.LENGTH_SHORT).show());
                    goToDestination(username, pw, groupChat);
                } else {
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login failed", Toast.LENGTH_SHORT).show());

                }
            }
        });
    }

    private void setupChatClient() {
        ChatOptions chatOptions = new ChatOptions();

        if (appKey.isEmpty()) {
            Toast.makeText(this, "Api Key is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        chatOptions.setAppKey(appKey);
        //initialize chat client
        chatClient = ChatClient.getInstance();
        chatClient.init(this, chatOptions);

        chatClient.setDebugMode(true);
    }

    public void signUp() {
        String username = binding.username.getText().toString().trim();
        String pwd = binding.password.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            showToast("App Key is Empty");
            return;
        }
        execute(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + appToken);

                JSONObject request = new JSONObject();
                request.putOpt("username", username);
                request.putOpt("password", pwd);


                HttpResponse response = HttpClientManager.httpExecute(REGISTER_URL, headers, request.toString(), Method_POST);
                int code = response.code;

                String responseInfo = response.content;
                showToast(responseInfo);
                if (code == 200) {
                    goToDestination(username, pwd, groupChat);
                } else {
                    showToast(responseInfo);
                }
            } catch (Exception e) {
                showToast(e.toString());
                e.printStackTrace();
            }
        });
    }

    public void goToDestination(String username, String pw, boolean isGroupChat) {


        if (isGroupChat) {
            Intent intent = new Intent(AuthActivity.this, GroupChatActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("pw", pw);


            String groupId = mGroups.get(selectedGroup);

            intent.putExtra("groupId", groupId);
            startActivity(intent);
            showToast("Signup Successfully!");
        } else {
            Intent intent = new Intent(AuthActivity.this, One2OneChatActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("pw", pw);
            startActivity(intent);
            showToast("Signup Successfully!");
        }
    }

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedGroup = groupNames.get(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}