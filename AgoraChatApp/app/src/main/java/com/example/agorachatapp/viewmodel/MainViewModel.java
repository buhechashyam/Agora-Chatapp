package com.example.agorachatapp.viewmodel;

import static android.os.AsyncTask.execute;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;

public class MainViewModel extends AndroidViewModel {
    public MainViewModel(@NonNull Application application) {
        super(application);
    }
//{"from": "user1","to": ["user2"],"type": "txt","body": {"msg": "testmessages"},"ext": {"em_ignore_notification": true}

    MutableLiveData<Boolean> _isMessageSend = new MutableLiveData<>();
    LiveData<Boolean> isMessageSend = _isMessageSend;
    public LiveData<Boolean> sendOneToOneMessage(String appToken, String fromUser, String toUser, String msg) {

        execute(new Runnable() {
            @Override
            public void run() {

                //Send one to one http request
                String requestUrl = "https://a61.chat.agora.io/611166664/1353835/messages/users";

                Map<String,String> header = new HashMap<>();
                header.put("Content-Type","application/json");
                header.put("Accept","application/json");
                header.put("Authorization"," Bearer "+ appToken);

                //Message Body
                JSONObject msgObject = new JSONObject();
                try {
                    msgObject.putOpt("from",fromUser);

                    msgObject.putOpt("to",new String[]{toUser});
                    msgObject.putOpt("type","text");

                    JSONObject object = new JSONObject();
                    object.putOpt("msg",msg);

                    msgObject.putOpt("body",object);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestUrl,header,msgObject.toString(), Method_POST);

                    if (response.code == 200){
                        _isMessageSend.postValue(true);

                    }else {
                        _isMessageSend.postValue(false);
                    }

                } catch (IOException e) {
                    _isMessageSend.postValue(false);
                    throw new RuntimeException(e);
                }
            }
        });

        return isMessageSend;
    }

    MutableLiveData<String> _mReceivedMessage = new MutableLiveData<>();
    LiveData<String> mReceivedMessage = _mReceivedMessage;
    public LiveData<String> receiveOneToOneMessage(String appToken, String fromUser, String toUser){

        execute(new Runnable() {
            @Override
            public void run() {
                String requestUrl = "https://a61.chat.agora.io/611166664/1353835/messages/users/import";

                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer "+ appToken);

                JSONObject object = new JSONObject();
                try {
                    object.putOpt("target",toUser);
                    object.putOpt("from",fromUser);
                    object.putOpt("type","txt");
                    object.putOpt("is_ack_read",true);

                    JSONObject msgBody = new JSONObject();
                    msgBody.putOpt("msg","import message.");

                    object.putOpt("body",msgBody);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    HttpResponse httpResponse = HttpClientManager.httpExecute(requestUrl,header,object.toString(),Method_POST);

                    if (httpResponse.code == 200) {
                        _mReceivedMessage.postValue(httpResponse.content);
                    }else {
                        _mReceivedMessage.postValue("Error");
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return mReceivedMessage;
    }

}
