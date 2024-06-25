package com.example.agorachatapp.viewmodel;

import static android.os.AsyncTask.execute;

import static io.agora.cloud.HttpClientManager.Method_GET;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.GroupManager;
import io.agora.chat.GroupOptions;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import io.agora.exceptions.ChatException;

public class GroupViewModel extends AndroidViewModel {
    public GroupViewModel(@NonNull Application application) {
        super(application);
    }

    //Create A Group
    MutableLiveData<String> _isGroupCreated = new MutableLiveData<>();
    LiveData<String> isGroupCreated = _isGroupCreated;
    public LiveData<String> createGroup(String mGroupName, String mGroupDesc, String owner) {
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.maxUsers = 50;
        groupOptions.inviteNeedConfirm = true;
        groupOptions.style = GroupManager.GroupStyle.GroupStylePublicOpenJoin;

        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String groupId = ChatClient.getInstance().groupManager().createGroup(mGroupName, mGroupDesc, new String[]{owner}, "", groupOptions).getGroupId();

                    _isGroupCreated.postValue(groupId);
                } catch (ChatException e) {
                    _isGroupCreated.postValue("");
                    throw new RuntimeException(e);
                }

            }
        });

        return isGroupCreated;
    }

    //Get All Groups
    MutableLiveData<String> _mGroupList = new MutableLiveData<>();
    LiveData<String> mGroupList = _mGroupList;
    public LiveData<String> getAllGroups(){

        String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups";
        String appToken = "007eJxTYDhU/qVp47H/Un1bWrWK8t3kdN5eT6gVOVv4eSF7uOLRr6oKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpe5Or0hoCGRkWdBoxMzKwMjAyMDGA+AwMAAxHH64=";
        execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> header = new HashMap<>();
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestURL,header,null,Method_GET);

                    if (response.code == 200){
                        String users = response.content;
                        _mGroupList.postValue(users);
                    }
                } catch (IOException e) {
                    _mGroupList.postValue("");
                    throw new RuntimeException(e);
                }
            }
        });

        return mGroupList;
    }


    //Get All Agora Users
    MutableLiveData<String> _mUsersList = new MutableLiveData<>();
    LiveData<String> mUsersList = _mUsersList;

    public LiveData<String> getAllAgoraUsers() {
        String requestURL = "https://a61.chat.agora.io/611166664/1353835/users?limit=20";
        String appToken = "007eJxTYDhU/qVp47H/Un1bWrWK8t3kdN5eT6gVOVv4eSF7uOLRr6oKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpe5Or0hoCGRkWdBoxMzKwMjAyMDGA+AwMAAxHH64=";
        execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> header = new HashMap<>();
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestURL,header,null,Method_GET);

                    if (response.code == 200){
                        String users = response.content;
                        _mUsersList.postValue(users);
                    }
                } catch (IOException e) {
                    _mUsersList.postValue("");
                    throw new RuntimeException(e);
                }
            }
        });

        return mUsersList;
    }


    //Add Member
    MutableLiveData<Boolean> _isMemberAdded = new MutableLiveData<>();
    LiveData<Boolean> isMemberAdded = _isMemberAdded;
    public LiveData<Boolean> addMemberToGroup(String groupId, String memberName) {

        String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups/"+groupId+"/users/"+memberName;
        String appToken = "007eJxTYDhU/qVp47H/Un1bWrWK8t3kdN5eT6gVOVv4eSF7uOLRr6oKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpe5Or0hoCGRkWdBoxMzKwMjAyMDGA+AwMAAxHH64=";
        execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestURL,header,null,Method_POST);
                    if (response.code == 200){
                        _isMemberAdded.postValue(true);
                    }
                } catch (IOException e) {
                    _isMemberAdded.postValue(false);
                    throw new RuntimeException(e);
                }
            }
        });
        return isMemberAdded;
    }

}
