package com.example.ravishankar.lapitchat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mChatBar;
    private String chatUserId;
    private DatabaseReference mUserDatabase;

    private TextView mNameView, mLastSeenView;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        String userName = getIntent().getStringExtra("user_name");
        chatUserId = getIntent().getStringExtra("user_id");

        mChatBar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatBar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle(userName);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //-------Custom Action bar icons---------------
        mNameView = (TextView)findViewById(R.id.custom_bar_name);
        mLastSeenView = (TextView)findViewById(R.id.custom_bar_last_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mNameView.setText(userName);

        mUserDatabase.child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumbnail").getValue().toString();
                if(online.equals("true")){
                    mLastSeenView.setText("online");
                }
                else{
                    mLastSeenView.setText(online);
                }

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.defaultimage).into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
