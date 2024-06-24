package com.example.agorachatapp;

import static android.os.AsyncTask.execute;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agorachatapp.chatgroup.using_restapi.GroupChatActivity;
import com.example.agorachatapp.databinding.ActivityAuthBinding;
import com.example.agorachatapp.one2onechat.using_restapi.One2OneChatActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;

public class AuthActivity extends AppCompatActivity {

    ActivityAuthBinding binding;
    private String appKey = "611166664#1353835";
    ChatClient chatClient;
    String appToken = "007eJxTYKiL/jUl6pVGY7bDAyudCIHdfLlcr0NaFjtMOx1s4vt2/zoFBuNUCwvj5KQkUyMzCxPTpDRLg0TDtMTktMTEFMPktNQUZqbKtIZARoZw1Z0MjAysQMzEAOIzMAAAa6kdBQ==";
    String REGISTER_URL = "https://a61.chat.agora.io/611166664/1353835/users";

    boolean groupChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupChatClient();


        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_group_chat){
                    groupChat = true;
                }else {
                    groupChat = false;

                }
            }
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            try {
                signUp();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        binding.btnLogin.setOnClickListener(v -> loginToAgoraChat());
    }

    private void loginToAgoraChat() {
        //login
        chatClient.login(binding.username.getText().toString(), binding.password.getText().toString(), new CallBack() {
            @Override
            public void onSuccess() {
                String username = binding.username.getText().toString().trim();
                String pw = binding.password.getText().toString().trim();

                runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login Success", Toast.LENGTH_SHORT).show());

                goToDestination(username,pw,groupChat);
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200) {
                    String username = binding.username.getText().toString().trim();
                    String pw = binding.password.getText().toString().trim();
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login Success", Toast.LENGTH_SHORT).show());
                    goToDestination(username,pw,groupChat);
                }else {
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
                    goToDestination(username,pwd, groupChat);
                } else {
                    showToast(responseInfo);
                }
            } catch (Exception e) {
                showToast(e.toString());
                e.printStackTrace();
            }
        });
    }

    public void goToDestination(String username,String pw, boolean isGroupChat){
        if (isGroupChat){
            Intent intent = new Intent(AuthActivity.this, GroupChatActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("pw", pw);
            startActivity(intent);
            showToast("Signup Successfully!");
        }else {
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
}