package com.example.agorachatapp.one2onechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import io.agora.chat.ChatMessage;

public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<ChatMessage> messages = intent.getParcelableArrayListExtra("messages");


    }
}
