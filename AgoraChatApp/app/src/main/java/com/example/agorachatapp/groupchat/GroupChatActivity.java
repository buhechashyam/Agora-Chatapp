package com.example.agorachatapp.groupchat;

import static android.os.AsyncTask.execute;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.agorachatapp.databinding.ActivityGroupChatBinding;

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

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    String appKey = "611166664#1353835";
    String username = "";
    String groupId = "";
    String groupName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpClient();
        setUpListener();

        SharedPreferences sharedPreferences = getSharedPreferences("user-login", Context.MODE_PRIVATE);

        username = sharedPreferences.getString("username","");

        groupId = getIntent().getStringExtra("group-id");
        groupName = getIntent().getStringExtra("group-name");

        binding.toolbar.setTitle(groupName);
        fetchGroupConversionHistory(groupId);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupChatActivity.this,GroupHomeActivity.class));
                finish();
            }
        });
        binding.btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                sendMessageToGroup();
            }
        });
    }

    private void fetchGroupConversionHistory(String groupId) {
        execute(new Runnable() {
            @Override
            public void run() {
                ChatClient.getInstance().chatManager().asyncFetchHistoryMessage(groupId, Conversation.ConversationType.GroupChat, 40, null, new ValueCallBack<CursorResult<ChatMessage>>() {
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
                        showToast("History not found");
                    }
                });

            }
        });
    }

    private void sendMessageToGroup() {
        if (appKey.isEmpty()){
            return;
        }
        ChatOptions chatOptions = new ChatOptions();
        chatOptions.setAppKey(appKey);

        String msg = binding.etMessageText.getText().toString();
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(msg,groupId);
        chatMessage.setChatType(ChatMessage.ChatType.GroupChat);

        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {

                runOnUiThread(() -> {
                    displayMessage(msg,convertToTime(chatMessage.getMsgTime()),username,true);
                    binding.etMessageText.setText("");
                    showToast("Send Message");
                });

            }

            @Override
            public void onError(int code, String error) {
                showToast("Message Not Send");
            }
        });

        ChatClient.getInstance().chatManager().sendMessage(chatMessage);
    }

    private void setUpListener() {
        ChatClient.getInstance().chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message: messages) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(((TextMessageBody)message.getBody()).getMessage(),convertToTime(message.getMsgTime()),message.getFrom(),false);

                        }
                    });
                }
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
            return;
        }
        ChatOptions chatOptions = new ChatOptions();
        chatOptions.setAppKey(appKey);

        ChatClient.getInstance().init(this, chatOptions);
        ChatClient.getInstance().setDebugMode(true);

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

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }
    private String convertToTime(long timeInMilli) {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:SS");

        String date = simpleDateFormat.format(new Date(timeInMilli));

        return date;
    }

}