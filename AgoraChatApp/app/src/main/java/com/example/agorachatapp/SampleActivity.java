package com.example.agorachatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agorachatapp.databinding.ActivityMainBinding;
import com.example.agorachatapp.databinding.ActivitySampleBinding;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.exceptions.ChatException;

public class SampleActivity extends AppCompatActivity {

    ActivitySampleBinding binding;
    private String appKey = "611166664#1353835";
    ChatClient chatClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupChatClient();

        binding.btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    chatClient.createAccount("user10", "User@123#");
                } catch (ChatException e) {
                    throw new RuntimeException(e);
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
}