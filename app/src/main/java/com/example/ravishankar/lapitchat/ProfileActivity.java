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

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mDisplayName, mStatus, mFriendCounts;
    private Button mSendReqBtn, mDeclineReqBtn;

    private ProgressDialog mProgressDialog;

    //firebase
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
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

                mProgressDialog.dismiss();
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
                                        .setValue("Received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCurrentState = "req_sent";
                                        mSendReqBtn.setEnabled(true);
                                        mSendReqBtn.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
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
                                            mSendReqBtn.setEnabled(true);
                                            mCurrentState = "not_friends";
                                            mSendReqBtn.setText("Send Friend Request");
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
                        }
                    });
                }
            }
        });
    }
}
