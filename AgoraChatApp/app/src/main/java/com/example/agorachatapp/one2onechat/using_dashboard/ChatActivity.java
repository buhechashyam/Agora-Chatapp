package com.example.agorachatapp.one2onechat.using_dashboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agorachatapp.R;
import com.example.agorachatapp.databinding.ActivityChatBinding;

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

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    //for user1
    String userId = "user10";  //User ID
    //Chat User Temp Token:
    String token = "007eJxTYFDafNDq15J/GfvPOEziZsrgLN4xpSbs8Cylna9nTnxfeaFYgcE41cLCODkpydTIzMLENCnN0iDRMC0xOS0xMcUwOS01pUOmMq0hkJEh5lwFIyMDKwMjEIL4KgzJBmmGFompBrpGaclmuoaGqWm6FsYplrpmhkZmKRZGBqZJyUkAXjgp5g==";

    String appKey = "611166664#1353835";
    private ChatClient agoraChatClient;
    boolean isJoined = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpClient();
        setUpListener();

        binding.btnJoin.setOnClickListener(v -> {
            userJoinLeave();
        });

        binding.btnSendMsg.setOnClickListener(v -> {
            sendMessage();
        });
    }

    private void sendMessage() {
        String toSend = binding.etReceiver.getText().toString().trim();
        String message = binding.etMessage.getText().toString().trim();

        if (toSend.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Enter a Username and message", Toast.LENGTH_SHORT).show();
            return;
        }
        //Create a ChatMessage
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(message, toSend);

        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        displayMessage(message, convertToTime(chatMessage.getMsgTime()), userId,true);
                        binding.etMessage.setText("");
                        showLog("Message Send");
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                showLog("Message not send");
            }
        });

        agoraChatClient.chatManager().sendMessage(chatMessage);
    }

    private void userJoinLeave() {

        if (isJoined) {
            agoraChatClient.logout(true, new CallBack() {
                @Override
                public void onSuccess() {
                    showLog("logout successful");

                    runOnUiThread(() -> binding.btnJoin.setText("Join"));
                    isJoined = false;
                }

                @Override
                public void onError(int code, String error) {
                    showLog("logout field");
                }
            });
        } else {
            agoraChatClient.loginWithAgoraToken(userId, token, new CallBack() {
                @Override
                public void onSuccess() {
                    showLog("Join successful");
                    isJoined = true;
                    runOnUiThread(() -> {
                        binding.btnJoin.setText("Leave");
                    });
                }

                @Override
                public void onError(int code, String error) {
                    if (code == 200) { // Already joined
                        isJoined = true;
                        runOnUiThread(() -> {
                            binding.btnJoin.setText("Leave");
                            showLog("Join successful");
                        });

                    } else {
                        showLog( error);
                    }
                }
            });
        }
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
        agoraChatClient = ChatClient.getInstance();
        agoraChatClient.init(this, chatOptions);

        agoraChatClient.setDebugMode(true);


    }

    private void setUpListener() {
        agoraChatClient.chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(((TextMessageBody) message.getBody()).getMessage(), convertToTime(message.getMsgTime()),message.conversationId() ,false);

                        }
                    });
                }
            }
        });

        agoraChatClient.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                showLog("Connected");
            }

            @Override
            public void onDisconnected(int errorCode){
                if (isJoined) {
                    showLog("Disconnect");
                    isJoined = false;
                }
            }

            @Override
            public void onLogout(int errorCode) {
                ConnectionListener.super.onLogout(errorCode);
                showLog("LogOut");
            }

            @Override
            public void onTokenExpired(){
                ConnectionListener.super.onTokenExpired();
                showLog("Token Expired");
            }

            @Override
            public void onTokenWillExpire() {

            }
        });
    }

    private void displayMessage(String textMessage, String textMessageTime,String textSender ,boolean isSendMessage) {
        //create a textview to set a message
        TextView textView = new TextView(this);
        textView.setText(textMessage);
        textView.setPadding(4, 4, 4, 4);


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
            textView.setBackgroundColor(Color.YELLOW);
        } else {
            params.setMargins(4, 4, 100, 4);
            textView.setBackgroundColor(Color.YELLOW);
        }

        binding.chats.addView(view, params);
    }

    private void showLog(String msg) {
        Log.d("MAIN", msg);
    }

    private String convertToTime(long timeInMilli) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:SS");

        String date = simpleDateFormat.format(new Date(timeInMilli));

        return date;
    }
}

