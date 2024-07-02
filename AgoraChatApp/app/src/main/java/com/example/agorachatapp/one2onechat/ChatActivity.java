package com.example.agorachatapp.one2onechat;

import static android.os.AsyncTask.execute;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.agorachatapp.R;
import com.example.agorachatapp.databinding.ActivityChatBinding;
import com.example.agorachatapp.room.User;
import com.example.agorachatapp.viewmodel.ChatViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.MessageListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;
import io.agora.chat.Conversation;
import io.agora.chat.CursorResult;
import io.agora.chat.TextMessageBody;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    String appKey = "611166664#1353835";
    String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
    String recipientUserID = "";
    String username,pw;
    boolean isJoined = true;
    ChatViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        setSupportActionBar(binding.toolbarChat);

        setUpClient();
        setUpListener();

        SharedPreferences sharedPreferences = getSharedPreferences("user-login", Context.MODE_PRIVATE);

        username = sharedPreferences.getString("username","");
        pw = sharedPreferences.getString("pw","");

        joinUser(username,pw);

        User user = (User) getIntent().getSerializableExtra("user-id");
        recipientUserID = user.getUserId();

        binding.toolbarChat.setTitle(recipientUserID);

        fetchConversationHistory(recipientUserID);
        binding.toolbarChat.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, HomeActivity.class));
                finish();
            }
        });

        binding.btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(recipientUserID);
            }
        });
    }

    private void fetchConversationHistory(String recipientUserID) {

        execute(new Runnable() {
            @Override
            public void run() {
                ChatClient.getInstance().chatManager().asyncFetchHistoryMessage(recipientUserID, Conversation.ConversationType.Chat, 40, null, new ValueCallBack<CursorResult<ChatMessage>>() {
                    @Override
                    public void onSuccess(CursorResult<ChatMessage> values) {
                        List<ChatMessage> messages = values.getData();
                        for (ChatMessage message : messages) {
                            if (message.getFrom().equals(username)){
                                runOnUiThread(() -> displayMessage(((TextMessageBody) message.getBody()).getMessage(), convertToTime(message.getMsgTime()),username, true));
                            }else {
                                runOnUiThread(() -> displayMessage(((TextMessageBody) message.getBody()).getMessage(), convertToTime(message.getMsgTime()), message.getFrom(), false));
                            }
                        }

                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        showMessage("History not found");
                    }
                });

            }
        });

    }

    private void joinUser(String username, String pw) {
        ChatClient.getInstance().login(username, pw, new CallBack() {
            @Override
            public void onSuccess() {
               showMessage ("Success");
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200){
                    showMessage("Success");
                }else {
                    showMessage("Failed");
                }
            }
        });
    }
    private void sendMessage(String recipientUserID) {
        String content = binding.etTextMsg.getText().toString().trim();
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(content,recipientUserID);

        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        displayMessage(content, convertToTime(chatMessage.getMsgTime()),username, true);
                        binding.etTextMsg.setText("");
                        showMessage("Message Send");
                    }
                });
            }

            @Override
            public void onError(int code, String error) {

            }
        });
        ChatClient.getInstance().chatManager().sendMessage(chatMessage);
    }

    private void setUpListener() {
        ChatClient.getInstance().chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(((TextMessageBody)message.getBody()).getMessage().toString(),convertToTime(message.getMsgTime()),message.getFrom(),false);
                        }
                    });
                }
            }
        });
        ChatClient.getInstance().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(){
                showMessage("Connected");
            }

            @Override
            public void onDisconnected(int errorCode){
                if (isJoined) {
                    showMessage("Disconnect");
                    isJoined = false;
                }
            }

            @Override
            public void onLogout(int errorCode) {
                ConnectionListener.super.onLogout(errorCode);
                showMessage("LogOut");
            }

            @Override
            public void onTokenExpired(){
                ConnectionListener.super.onTokenExpired();
                showMessage("Token Expired");
            }

            @Override
            public void onTokenWillExpire() {

            }
        });

    }

    private void setUpClient() {
        ChatOptions chatOptions = new ChatOptions();
        if (appKey.isEmpty()){
            showMessage("App Key is Empty");
            return;
        }
        chatOptions.setAppKey(appKey);
        ChatClient.getInstance().init(this,chatOptions);
        ChatClient.getInstance().setDebugMode(true);
    }
    private void showMessage(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    private void displayMessage(String textMessage, String textMessageTime, String textSender,boolean isSendMessage) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        View view = getLayoutInflater().inflate(R.layout.item_message, null);

        TextView msg = view.findViewById(R.id.text_message);
        TextView msgTime = view.findViewById(R.id.text_msg_time);
        TextView sender = view.findViewById(R.id.text_sender);

        sender.setText(textSender);
        msg.setText(textMessage);
        msgTime.setText(textMessageTime);
        if (isSendMessage) {
            params.gravity = Gravity.END;
            params.setMargins(100, 4, 4, 4);
        } else {
            params.setMargins(4, 4, 100, 4);

        }

        binding.chats.addView(view, params);
    }
    private String convertToTime(long timeInMilli) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:SS");

        String date = simpleDateFormat.format(new Date(timeInMilli));

        return date;
    }
}