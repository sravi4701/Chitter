package com.example.ravishankar.lapitchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mChatBar;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        uid = getIntent().getStringExtra("user_id");

        mChatBar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatBar);
        getSupportActionBar().setTitle(uid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        

    }
}
