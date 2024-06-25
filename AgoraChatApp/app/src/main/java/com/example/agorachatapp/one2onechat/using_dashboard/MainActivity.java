package com.example.agorachatapp.one2onechat.using_dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agorachatapp.R;
import com.example.agorachatapp.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;
import io.agora.chat.TextMessageBody;

public class MainActivity extends AppCompatActivity {
    private String userId = "user10";
    private String token = "007eJxTYFDafNDq15J/GfvPOEziZsrgLN4xpSbs8Cylna9nTnxfeaFYgcE41cLCODkpydTIzMLENCnN0iDRMC0xOS0xMcUwOS01pUOmMq0hkJEh5lwFIyMDKwMjEIL4KgzJBmmGFompBrpGaclmuoaGqWm6FsYplrpmhkZmKRZGBqZJyUkAXjgp5g==";
    private String appKey = "611166664#1353835";
    ActivityMainBinding binding;
    ChatClient chatClient;
    private boolean isJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupChatClient();
        setupListeners();

        binding.btnJoinLeave.setOnClickListener(v -> joinOrLeave());

        binding.btnSendMessage.setOnClickListener(v -> sendMessage());

    }

    private void setupListeners() {
        //Message Listener
        chatClient.chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {

                for (ChatMessage message : messages){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(((TextMessageBody)message.getBody()).getMessage().toString(),convertMilliToTime(message.getMsgTime()),false);
                        }
                    });
                }
            }
        });
        //Connection Listener
        chatClient.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                showToast("User is Connected.");
            }

            @Override
            public void onDisconnected(int errorCode) {
                if (isJoined){
                    showToast("Disconnected");
                    isJoined = false;
                }
            }

            @Override
            public void onTokenExpired() {
                ConnectionListener.super.onTokenExpired();
                showToast("Token is Expired");
            }

            @Override
            public void onLogout(int errorCode) {
                ConnectionListener.super.onLogout(errorCode);
                showToast("Logout");
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

    private void sendMessage() {
        String message = binding.etMessageText.getText().toString().trim();
        String receiver = binding.etRecipient.getText().toString().trim();
        if (message.isEmpty() || receiver.isEmpty()){
            showToast("Enter Message and Receiver Name");
            return;
        }
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(message,receiver);

        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //If message success fully send than show in screen
                        displayMessage(message, convertMilliToTime(chatMessage.getMsgTime()), true);
                        binding.etMessageText.setText("");
                        showToast("Send Message");
                    }
                });}

            @Override
            public void onError(int code, String error) {
                showToast("Message not send");
            }
        });

        chatClient.chatManager().sendMessage(chatMessage);

    }

    private void displayMessage(String message, String messageTime, boolean isSend) {

        View view = getLayoutInflater().inflate(R.layout.item_message, null);
        TextView mTextViewMessage = view.findViewById(R.id.text_message);
        TextView mTextViewTime = view.findViewById(R.id.text_msg_time);

        mTextViewMessage.setText(message);
        mTextViewTime.setText(messageTime);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (isSend){
            params.gravity = Gravity.END;
            params.setMargins(100,4,4,4);
        }else {
            params.setMargins(4, 4, 100, 4);
        }

        binding.chats.addView(view,params);
    }

    private void joinOrLeave() {
        if (isJoined) {
            chatClient.logout(true, new CallBack() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isJoined = false;
                            binding.btnJoinLeave.setText("join");
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {

                }
            });
        } else {
            chatClient.loginWithAgoraToken(userId, token, new CallBack() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isJoined = true;
                            binding.btnJoinLeave.setText("Leave");
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    if (code == 200) {
                        //user already login
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isJoined = true;
                                binding.btnJoinLeave.setText("Leave");
                            }
                        });
                    } else {
                        showToast("Login Faield: " + error);
                    }
                }
            });
        }
    }

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    private String convertMilliToTime(long timeInMilli){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        String time = dateFormat.format(new Date(timeInMilli));

        return time;
    }
}