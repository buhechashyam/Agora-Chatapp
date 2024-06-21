package com.example.agorachatapp.chatgroup;

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
import com.example.agorachatapp.databinding.ActivityChatGroupBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.GroupChangeListener;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;
import io.agora.chat.Group;
import io.agora.chat.GroupManager;
import io.agora.chat.GroupOptions;
import io.agora.chat.MucSharedFile;
import io.agora.chat.TextMessageBody;
import io.agora.exceptions.ChatException;

public class ChatGroupActivity extends AppCompatActivity {

    ActivityChatGroupBinding binding;
    private String userId = "user1";
    private String token = "007eJxTYDgaXnwxYMeaCVy5yUr/Q9uSPzs6rBVbtvLb5TBpjnUXcx8oMBinWlgYJyclmRqZWZiYJqVZGiQapiUmpyUmphgmp6Wm3DIpTWsIZGQwUfVgYWRgZWAEQhBfhcEk2cTANNXIQNco1chQ19AwNU3X0tLSTDfN0tTMMCXRMMnIwhIA3jkn1Q==";
    String groupId = "251197486333953";
    String desc = "This is students groups";
    String appKey = "611166664#1353835";
    String groupName = "Study Group";
    String[] groupMembers = {"user1", "user2", "user3", "user4"};
    boolean isJoined = false;

    ChatClient chatClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClient();

        setUpListener();



        binding.btnJoinLeave.setOnClickListener(v -> joinOrLeave());
        binding.btnSendMessage.setOnClickListener(v -> sendMessage());

        binding.btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupRequest();

                    }
                });
            }
        });
    }

    private void joinGroupRequest() {


//        try {
//            ChatClient.getInstance().groupManager().joinGroup(groupId);
//            showToast("User Join Group Successfully");
//        } catch (ChatException e) {
//            showToast("User failed to Join a Group");
//            throw new RuntimeException(e);
//        }
    }

    private void setupGroup() {
        GroupOptions options = new GroupOptions();
        options.maxUsers = 10;
        options.inviteNeedConfirm = true;
        options.style = GroupManager.GroupStyle.GroupStylePublicOpenJoin;

        try {
//            ChatClient.getInstance().groupManager().createGroup(groupName, desc, groupMembers, "reason", options);
//            groupId = ChatClient.getInstance().groupManager().createGroup(groupName, desc, groupMembers, "reason", options).getGroupId();
//            showToast("Group is created");
//            showToast(groupId);

            Group group = ChatClient.getInstance().groupManager().getGroupFromServer(groupId, true);
            List<String> memberList = group.getMembers();// gets regular members.
            memberList.addAll(group.getAdminList());// gets group admins.
            memberList.add(group.getOwner());

            showToast(String.valueOf(memberList.size()));
        } catch (ChatException e) {
            showToast("Group not created");
            throw new RuntimeException(e);
        }

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
                            binding.btnJoinLeave.setText("Join");
                            showToast("Log out");
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    showToast("Logout Field");
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
                            showToast("Login Successfully");
                        }
                    });
                   setupGroup();
                }

                @Override
                public void onError(int code, String error) {
                    if (code == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isJoined = true;
                                binding.btnJoinLeave.setText("Leave");
                                showToast("Login Successfully");
                            }
                        });
                    }
                }
            });

        }
    }

    private void setUpListener() {
        chatClient.chatManager().addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayMessage(((TextMessageBody) message.getBody()).getMessage().toString(), convertMilliToTime(message.getMsgTime()), false);
                        }
                    });
                }
            }
        });

        chatClient.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected() {
                showToast("Connected");
            }

            @Override
            public void onDisconnected(int errorCode) {
                if (isJoined) {
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

        chatClient.groupManager().addGroupChangeListener(new GroupChangeListener() {
            @Override
            public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

            }

            @Override
            public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {

            }

            @Override
            public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {

            }

            @Override
            public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {

            }

            @Override
            public void onInvitationAccepted(String groupId, String invitee, String reason) {

            }

            @Override
            public void onInvitationDeclined(String groupId, String invitee, String reason) {

            }

            @Override
            public void onUserRemoved(String groupId, String groupName) {

            }

            @Override
            public void onGroupDestroyed(String groupId, String groupName) {

            }

            @Override
            public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {

            }

            @Override
            public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {

            }

            @Override
            public void onMuteListRemoved(String groupId, List<String> mutes) {

            }

            @Override
            public void onWhiteListAdded(String groupId, List<String> whitelist) {

            }

            @Override
            public void onWhiteListRemoved(String groupId, List<String> whitelist) {

            }

            @Override
            public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {

            }

            @Override
            public void onAdminAdded(String groupId, String administrator) {

            }

            @Override
            public void onAdminRemoved(String groupId, String administrator) {

            }

            @Override
            public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {

            }

            @Override
            public void onMemberJoined(String groupId, String member) {

            }

            @Override
            public void onMemberExited(String groupId, String member) {

            }

            @Override
            public void onAnnouncementChanged(String groupId, String announcement) {

            }

            @Override
            public void onSharedFileAdded(String groupId, MucSharedFile sharedFile) {

            }

            @Override
            public void onSharedFileDeleted(String groupId, String fileId) {

            }
        });

    }

    private void setupClient() {
        chatClient = ChatClient.getInstance();

        ChatOptions chatOptions = new ChatOptions();
        chatOptions.setAppKey(appKey);
        chatOptions.setAutoAcceptGroupInvitation(true);

        chatClient.init(this, chatOptions);
        chatClient.setDebugMode(true);
    }

    private void sendMessage() {
        String content = binding.etMessageText.getText().toString().trim();

        //If we chat in Group than place group id
        ChatMessage chatMessage = ChatMessage.createTextSendMessage(content, groupId);
        chatMessage.setChatType(ChatMessage.ChatType.GroupChat);
        chatMessage.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayMessage(content, convertMilliToTime(chatMessage.getMsgTime()), true);
                        showToast("Message is Send");
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                showToast("Message not Send");
            }
        });
        chatClient.chatManager().sendMessage(chatMessage);

    }

    private void displayMessage(String content, String messageTime, boolean isSend) {
        View view = getLayoutInflater().inflate(R.layout.item_message, null);
        TextView mTextViewMessage = view.findViewById(R.id.text_message);
        TextView mTextViewTime = view.findViewById(R.id.text_msg_time);

        mTextViewMessage.setText(content);
        mTextViewTime.setText(messageTime);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (isSend) {
            params.gravity = Gravity.END;
            params.setMargins(100, 4, 16, 4);
        } else {
            params.setMargins(16, 4, 100, 4);
        }
        binding.chats.addView(view, params);
    }

    private void showToast(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    private String convertMilliToTime(long timeInMilli) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        String time = dateFormat.format(new Date(timeInMilli));

        return time;
    }

}