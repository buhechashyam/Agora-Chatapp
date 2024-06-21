package com.example.agorachatapp.chatgroup;

import static android.os.AsyncTask.execute;
import static io.agora.cloud.HttpClientManager.Method_GET;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agorachatapp.databinding.ActivityAddUserBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.agora.ConnectionListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;

public class AddUserActivity extends AppCompatActivity {

    ActivityAddUserBinding binding;
    String appKey = "611166664#1353835";
    //Chat App Temp Token
    String appToken = "007eJxTYHh244f2mvTGs7GSHQGm31VZGB8/fRR+50tO2N+M/ZN1ndsUGIxTLSyMk5OSTI3MLExMk9IsDRIN0xKT0xITUwyT01JT9JNL0xoCGRlOJzkzMTKwMjAyMDGA+AwMACSYH88=";
    ChatClient chatClient;
    String REGISTER_URL = "https://a61.chat.agora.io/611166664/1353835/users";
    String LOGIN_URL = "https://a61.chat.agora.io/611166664/1353835/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpClient();

        setUpListener();

        //signup with username and password
        binding.btnSignup.setOnClickListener(v -> signUp());
        //SignIn with user token
        binding.btnSignin.setOnClickListener(v -> signIn());
        //Logout
        binding.btnSignout.setOnClickListener(v -> signOut());
    }

    public void signUp() {
        String username = binding.etUsername.getText().toString().trim();
        String pwd = binding.etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            showToast("App Key is Empty");
            return;
        }
        execute(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Authorization","Bearer " + appToken);
                JSONObject request = new JSONObject();
                request.putOpt("username", username);
                request.putOpt("password", pwd);


                HttpResponse response = HttpClientManager.httpExecute(REGISTER_URL, headers, request.toString(), Method_POST);
                int code = response.code;

                String responseInfo = response.content;
                if (code == 200) {
                    showToast("Signup Successfully!");
                } else {
                    showToast(responseInfo);
                }
            } catch (Exception e) {
                showToast(e.toString());
                e.printStackTrace();
            }
        });
    }

    private void signIn() {

    }

    private void signOut() {

    }


    private void setUpClient() {
        chatClient = ChatClient.getInstance();

        ChatOptions chatOptions = new ChatOptions();
        if (appKey.isEmpty()) {
            showToast("AppKey is Empty");
            return;
        }
        chatOptions.setAppKey(appKey);
        chatClient.init(this, chatOptions);
        chatClient.setDebugMode(true);

    }

    private void setUpListener() {

        chatClient.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                showToast("Connected");
            }

            @Override
            public void onDisconnected(int errorCode) {
                showToast("Disconnected");
            }

            @Override
            public void onLogout(int errorCode) {
                ConnectionListener.super.onLogout(errorCode);
            }

            @Override
            public void onTokenExpired() {
                ConnectionListener.super.onTokenExpired();
            }
        });
    }


    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }
}