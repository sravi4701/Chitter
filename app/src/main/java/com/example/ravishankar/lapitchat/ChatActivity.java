package com.example.ravishankar.lapitchat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mChatBar;
    private String chatUserId;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUser;

    private ImageView mChatAddBtn, mChatSendBtn;
    private EditText mMessageView;

    private TextView mNameView, mLastSeenView;
    private CircleImageView mProfileImage;

    private RecyclerView mMessagesList;

    final private  List<Message> messageList = new ArrayList<>();

    private LinearLayoutManager mLinearLayout;

    private MessageAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

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

        //
        mChatAddBtn = (ImageView)findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageView)findViewById(R.id.chat_send_btn);
        mMessageView = (EditText)findViewById(R.id.chat_message_view);


        mMessagesList = (RecyclerView)findViewById(R.id.message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mAdapter = new MessageAdapter(messageList);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);
        loadMessage();

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
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long last_time = Long.parseLong(online);
                    String last_seen_time = getTimeAgo.getTimeAgo(last_time, getApplicationContext());
                    mLastSeenView.setText(last_seen_time);
                }

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.defaultimage).into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUserId)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();

                    chatUserMap.put("Chat/" + mCurrentUser + "/" + chatUserId, chatAddMap);
                    chatUserMap.put("Chat/" + chatUserId + "/" + mCurrentUser, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Error: ", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadMessage() {

        mRootRef.child("messages").child(mCurrentUser).child(chatUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = mMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUser + "/" + chatUserId;
            String chat_user_ref = "messages/" + chatUserId + "/" + mCurrentUser;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUser).child(chatUserId).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUser);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("Error: ", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}
