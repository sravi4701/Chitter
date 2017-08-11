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
import java.util.Map;

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
    private DatabaseReference mRootDatabase;

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

        mDeclineReqBtn.setVisibility(View.INVISIBLE);
        mDeclineReqBtn.setEnabled(false);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        // create new reference with Friend_req
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        // create new reference with Friends
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        //create new reference with notifications
        mNotifactionDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //create reference to root
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
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

                    DatabaseReference newNotificationID = mRootDatabase.child("notifications").child(uid).push();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uid + "/request_type", "sent");
                    requestMap.put("Friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + uid + "/" + newNotificationID.getKey(),notificationData);

                    mRootDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
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

                //------------------CANCEL FRIEND STATES---------------------
                if(mCurrentState.equals("req_sent")){

                    Map cancelData = new HashMap();
                    cancelData.put("Friend_req/" + mCurrentUser.getUid() + "/" + uid + "/request_type", null);
                    cancelData.put("Friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type", null);
                    mRootDatabase.updateChildren(cancelData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mCurrentState = "not_friends";
                                mSendReqBtn.setText("Send Friend Request");
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Error in cancelling request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //--------------------ACCEPT FRIEND REQUEST---------------------------

                if(mCurrentState.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + uid + "/date", currentDate);
                    friendsMap.put("Friends/" + uid + "/" + mCurrentUser.getUid() + "/date", currentDate);
                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + uid + "/request_type", null);
                    friendsMap.put("Friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type", null);
                    mRootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
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

                //------------------------Unfriend the person state----------------------------
                if(mCurrentState.equals("friends")){
                    Map unfriendData = new HashMap();
                    unfriendData.put("Friends/" + mCurrentUser.getUid() + "/" + uid + "/date", null);
                    unfriendData.put("Friends/" + uid + "/" + mCurrentUser.getUid() + "/date", null);
                    mRootDatabase.updateChildren(unfriendData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
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
            }
        });
    }
}
