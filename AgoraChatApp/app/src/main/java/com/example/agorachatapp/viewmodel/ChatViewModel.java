package com.example.agorachatapp.viewmodel;

import static android.os.AsyncTask.execute;

import static io.agora.cloud.HttpClientManager.Method_GET;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.agorachatapp.room.AppDatabase;
import com.example.agorachatapp.room.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.CursorResult;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;

public class ChatViewModel extends AndroidViewModel {
    AppDatabase appDatabase;
    public ChatViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getAppDatabase(application);

    }
    MutableLiveData<List<User>> _mAllUsers = new MutableLiveData<>();
    LiveData<List<User>> mAllUsers = _mAllUsers;

    public LiveData<List<User>> getAllUsers() {
        _mAllUsers.postValue(appDatabase.userDao().getAllUsers());
        return mAllUsers;
    }

}
