package com.example.ravishankar.lapitchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mDisplayName, mStatus, mFriendCounts;
    private Button mSendReqBtn, mDeclineReqBtn;

    private ProgressDialog mProgressDialog;

    //firebase
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotifactionDatabase;
    private FirebaseUser mCurrentUser;


    private String mCurrentState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String uid = getIntent().getStringExtra("user_id");

        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mDisplayName = (TextView)findViewById(R.id.profile_display_name);
        mStatus = (TextView)findViewById(R.id.profile_status);
        mSendReqBtn = (Button)findViewById(R.id.profile_send_req);
        mDeclineReqBtn = (Button)findViewById(R.id.profile_decline_req);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        // create new reference with Friend_req
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        // create new reference with Friends
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        //create new reference with notifications
        mNotifactionDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //current state of friend request
        mCurrentState = "not_friends";

        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultimage).into(mProfileImage);

                // ---------------------Friend list / Request feature------------------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(uid)){
                            String req_type = dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrentState = "req_received";
                                mSendReqBtn.setText("Accept Friend Request");

                                mDeclineReqBtn.setVisibility(View.VISIBLE);
                                mDeclineReqBtn.setEnabled(true);
                            }
                            else if(req_type.equals("sent")){
                                mCurrentState = "req_sent";
                                mSendReqBtn.setText("Cancel friend request");

                                mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineReqBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(uid)){
                                        mCurrentState = "friends";
                                        mSendReqBtn.setText("Unfriend");

                                        mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                        mDeclineReqBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //------------------NOT FRIEND STATES---------------------
                if(mCurrentState.equals("not_friends")){
                    mSendReqBtn.setEnabled(false);
                    // if the Current user is not friend of profiled user then set the database
                    // as below with flag request_type
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser.getUid());
                                        notificationData.put("type", "request");

                                        mNotifactionDatabase.child(uid).push().setValue(notificationData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mCurrentState = "req_sent";
                                                    mSendReqBtn.setText("Cancel Friend Request");

                                                    mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                    mDeclineReqBtn.setEnabled(false);

                                                    Toast.makeText(ProfileActivity.this, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            mSendReqBtn.setEnabled(true);
                        }
                    });
                }

                //------------------CANCEL FRIEND STATES---------------------
                if(mCurrentState.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mCurrentState = "not_friends";
                                            mSendReqBtn.setText("Send Friend Request");

                                            mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                            mDeclineReqBtn.setEnabled(false);
                                        }
                                        else{
                                            Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                            }
                            mSendReqBtn.setEnabled(true);
                        }
                    });
                }

                //--------------------ACCEPT FRIEND REQUEST---------------------------

                if(mCurrentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrentUser.getUid()).child(uid).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendDatabase.child(uid).child(mCurrentUser.getUid()).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendReqDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mFriendReqDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                mCurrentState = "friends";
                                                                mSendReqBtn.setText("Unfriend");

                                                                mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                                mDeclineReqBtn.setEnabled(false);
                                                            }
                                                            else{
                                                                Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                else{
                                                    Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                                                }
                                                mSendReqBtn.setEnabled(true);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }

                //------------------------Unfriend the person state----------------------------
                if(mCurrentState.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mFriendDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mCurrentState = "not_friends";
                                                mSendReqBtn.setText("Send Friend Request");

                                                mDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                mDeclineReqBtn.setEnabled(false);
                                            }
                                            else{
                                                Toast.makeText(ProfileActivity.this, "Error in Unfriending", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(ProfileActivity.this, "Error in Unfriending", Toast.LENGTH_SHORT).show();
                                }
                            mSendReqBtn.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
