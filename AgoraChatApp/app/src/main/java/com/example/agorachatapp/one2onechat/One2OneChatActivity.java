package com.example.agorachatapp.one2onechat;

import static android.os.AsyncTask.execute;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agorachatapp.R;
import com.example.agorachatapp.databinding.ActivityOne2OneChatBinding;
import com.example.agorachatapp.viewmodel.MainViewModel;

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

public class One2OneChatActivity extends AppCompatActivity {

    ActivityOne2OneChatBinding binding;
    MainViewModel viewModel;
    String appKey = "611166664#1353835";
    String appToken = "007eJxTYIhvaHHXjNZrmnaMJ0TnYYSWctG8SnFZJZvIiMCFP1KmLVVgME61sDBOTkoyNTKzMDFNSrM0SDRMS0xOS0xMMUxOS02ZWtuc1hDIyPDwvQ4TIwMrAyMDEwOIz8AAANCpHGQ=";
    String username,pw;
    boolean isJoined = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOne2OneChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpClient();
        setUpListener();

         username = getIntent().getStringExtra("username");
         pw = getIntent().getStringExtra("pw");
         viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        joinUser(username,pw);


        binding.btnSendMessage.setOnClickListener(v -> sendMessage());

        fetchConversationHistory("test2");
    }

    private void sendMessage() {

        String massage = binding.etMessageText.getText().toString().trim();
        String recipient = binding.etRecipient.getText().toString().trim();
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(massage, recipient);
//        chatMessage.setChatType(ChatMessage.ChatType.Chat);

        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        displayMessage(massage, convertToTime(chatMessage.getMsgTime()),username, true);
                        binding.etMessageText.setText("");
                        showToast("Message Send");
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                showToast("Message not Send");
            }
        });

        ChatClient.getInstance().chatManager().sendMessage(chatMessage);

    }

    private void joinUser(String username, String pw) {
        ChatClient.getInstance().login(username, pw, new CallBack() {
            @Override
            public void onSuccess() {
                saveData(username,pw);

                showToast("Success");
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200){
                    showToast("Success");
                    saveData(username,pw);

                }else {
                    showToast("Failed");
                }
            }
        });
    }

    private void saveData(String username, String pw) {
        SharedPreferences sharedPreferences = getSharedPreferences("user-login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("username",username);
        editor.putString("pw",pw);
        editor.apply();
        editor.commit();
    }

    private void setUpClient() {

        ChatOptions chatOptions = new ChatOptions();

        if (appKey.isEmpty()) {
            Toast.makeText(this, "Api key is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        //set api key to chatoptions
        chatOptions.setAppKey(appKey);
        //initialize chat client

        ChatClient.getInstance().init(this, chatOptions);

        ChatClient.getInstance().setDebugMode(true);

    }

    private void setUpListener() {
        ChatClient.getInstance().chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                           displayMessage(((TextMessageBody) message.getBody()).getMessage(), convertToTime(message.getMsgTime()),message.getFrom(), false);
                        }
                    });
                }
            }
        });

        ChatClient.getInstance().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(){
                showToast("Connected");
            }

            @Override
            public void onDisconnected(int errorCode){
                if (isJoined) {
                    showToast("Disconnect");
                    isJoined = false;
                }
            }

            @Override
            public void onLogout(int errorCode) {
                ConnectionListener.super.onLogout(errorCode);
                showToast("LogOut");
            }

            @Override
            public void onTokenExpired(){
                ConnectionListener.super.onTokenExpired();
                showToast("Token Expired");
            }

            @Override
            public void onTokenWillExpire() {

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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    displayMessage(((TextMessageBody) message.getBody()).getMessage(), convertToTime(message.getMsgTime()),message.getFrom(), false);
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        showToast("History not found");
                    }
                });

            }
        });

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

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }
}