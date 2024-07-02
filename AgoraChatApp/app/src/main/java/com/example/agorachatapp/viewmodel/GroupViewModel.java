package com.example.agorachatapp.viewmodel;

import static android.os.AsyncTask.execute;
import static io.agora.cloud.HttpClientManager.Method_GET;
import static io.agora.cloud.HttpClientManager.Method_POST;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //Create a Group
    MutableLiveData<String> _isGroupCreated = new MutableLiveData<>();
    LiveData<String> isGroupCreated = _isGroupCreated;

    public LiveData<String> createGroup(String mGroupName, String mGroupDesc, String owner) {
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.maxUsers = 50;
        groupOptions.inviteNeedConfirm = true;
        groupOptions.style = GroupManager.GroupStyle.GroupStylePublicOpenJoin;

        execute(() -> {
            try {
                String groupId = ChatClient.getInstance().groupManager().createGroup(mGroupName, mGroupDesc, new String[]{owner}, "", groupOptions).getGroupId();
                _isGroupCreated.postValue(groupId);
            } catch (ChatException e) {
                _isGroupCreated.postValue("");
                throw new RuntimeException(e);
            }

        });

        return isGroupCreated;
    }

    //Get All Groups
    MutableLiveData<String> _mGroupList = new MutableLiveData<>();
    LiveData<String> mGroupList = _mGroupList;

    public LiveData<String> getAllGroups() {

        String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups";
        String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
        execute(() -> {
            Map<String, String> header = new HashMap<>();
            header.put("Accept", "application/json");
            header.put("Authorization", "Bearer " + appToken);

            try {
                HttpResponse response = HttpClientManager.httpExecute(requestURL, header, null, Method_GET);

                if (response.code == 200) {
                    String users = response.content;
                    _mGroupList.postValue(users);
                }
            } catch (IOException e) {
                _mGroupList.postValue("");
                throw new RuntimeException(e);
            }
        });

        return mGroupList;
    }


    //Get All Agora Users
    MutableLiveData<String> _mUsersList = new MutableLiveData<>();
    LiveData<String> mUsersList = _mUsersList;

    public LiveData<String> getAllAgoraUsers() {
        String requestURL = "https://a61.chat.agora.io/611166664/1353835/users?limit=20";
        String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
        execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> header = new HashMap<>();
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestURL, header, null, Method_GET);

                    if (response.code == 200) {
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

        String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups/" + groupId + "/users/" + memberName;
        String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
        execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                try {
                    HttpResponse response = HttpClientManager.httpExecute(requestURL, header, null, Method_POST);
                    if (response.code == 200) {
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

    //Check if user id member of group or not
    MutableLiveData<List<String>> _mAllJoinedGroup = new MutableLiveData<>();
    LiveData<List<String>> mAllJoinedGroup = _mAllJoinedGroup;

    public LiveData<List<String>> getAllJoinedGroup(String userId, List<String> mAllGroups) {

        execute(new Runnable() {
            @Override
            public void run() {
                String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
                String appKey = "611166664#1353835";

                Map<String, String> header = new HashMap<>();
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                ArrayList<String> mJoinedGroups = new ArrayList<>();

                for (int i = 0; i < mAllGroups.size(); i++) {
                    String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups/" + mAllGroups.get(i) + "/user/" + userId + "/is_joined";

                    try {
                        HttpResponse response = HttpClientManager.httpExecute(requestURL, header, null, Method_GET);

                        if (response.code == 200) {
                            JSONObject jsonObject = new JSONObject(response.content);
                            boolean isJoined = jsonObject.getBoolean("data");
                            if (isJoined == true) {
                                mJoinedGroups.add(mAllGroups.get(i));
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                _mAllJoinedGroup.postValue(mJoinedGroups);

            }
        });


        return mAllJoinedGroup;
    }

    //Get Group Name
    MutableLiveData<List<String>> _mGroupName = new MutableLiveData<>();
    LiveData<List<String>> mGroupName = _mGroupName;

    public LiveData<List<String>> getGroupName(List<String> groupId) {

        execute(new Runnable() {
            @Override
            public void run() {
                String appToken = "007eJxTYDCd7rFSNWB3X7AJr4JB4IzcAxcjt1mF73sYOmNze4jr2wUKDMapFhbGyUlJpkZmFiamSWmWBomGaYnJaYmJKYbJaakpxnOb0xoCGRkazDyZGRlYGRgZmBhAfAYGAFKEHVc=";
                Map<String, String> header = new HashMap<>();
                header.put("Accept", "application/json");
                header.put("Authorization", "Bearer " + appToken);

                ArrayList<String> names = new ArrayList<>();

                for (int i=0;i<groupId.size();i++){
                    String requestURL = "https://a61.chat.agora.io/611166664/1353835/chatgroups/" + groupId.get(i);

                    try {
                        HttpResponse response = HttpClientManager.httpExecute(requestURL, header, null, Method_GET);

                        if (response.code == 200) {

                            Gson gson = new Gson();
                            JsonObject jsonObject = JsonParser.parseString(response.content).getAsJsonObject();

                            // Accessing the 'name' field
                            JsonArray dataArray = jsonObject.getAsJsonArray("data");
                            if (dataArray != null && dataArray.size() > 0) {
                                JsonObject dataObject = dataArray.get(0).getAsJsonObject();
                                String name = dataObject.get("name").getAsString();

                                names.add(name);
                                Log.d("LOG",name);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                _mGroupName.postValue(names);


            }
        });

        return mGroupName;

    }

}
