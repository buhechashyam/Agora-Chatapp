package com.example.agorachatapp.one2onechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agorachatapp.AuthActivity;
import com.example.agorachatapp.R;
import com.example.agorachatapp.adapter.UsersAdapter;
import com.example.agorachatapp.databinding.ActivityHomeBinding;
import com.example.agorachatapp.room.AppDatabase;
import com.example.agorachatapp.room.User;
import com.example.agorachatapp.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import io.agora.CallBack;
import io.agora.chat.ChatClient;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    UsersAdapter adapter;
    ChatViewModel viewModel;
    String username, pw;
    List<User> mUserIds = new ArrayList<User>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(HomeActivity.this).get(ChatViewModel.class);
        binding.chatUsers.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("user-login", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        pw = sharedPreferences.getString("pw", "");

        joinUser(username,pw);

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (R.id.menu_logout == item.getItemId()){
                    ChatClient.getInstance().logout(true, new CallBack() {
                        @Override
                        public void onSuccess() {
                            showLog("Logout");
                            AppDatabase.getAppDatabase(getApplication()).userDao().deleteAllRecords();

                            startActivity(new Intent(HomeActivity.this, AuthActivity.class));
                        }

                        @Override
                        public void onError(int code, String error) {

                        }
                    });
                }
                return false;
            }
        });
        adapter = new UsersAdapter(HomeActivity.this, new ArrayList<>());
        viewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                mUserIds = users;
                adapter = new UsersAdapter(HomeActivity.this, users);
                binding.chatUsers.setAdapter(adapter);

            }
        });

        binding.fabAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);


                TextInputEditText etUser = view.findViewById(R.id.et_user_id);
                MaterialButton btnSubmit = view.findViewById(R.id.btn_add_user);

                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setView(view);

                AlertDialog dialog = builder.create();

                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mUserIds.add(new User(etUser.getText().toString()));
                        AppDatabase.getAppDatabase(getApplication()).userDao().insert(new User(etUser.getText().toString()));
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
    private void joinUser(String username, String pw) {
        ChatClient.getInstance().login(username, pw, new CallBack() {
            @Override
            public void onSuccess() {
                showLog("Success");
            }

            @Override
            public void onError(int code, String error) {
                if (code == 200){
                    showLog("Success");
                }else {
                    showLog("Error");
                }
            }
        });
    }
    private void showLog(String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());

        Log.d("MAIN", text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_home,menu);
        return true;
    }
}