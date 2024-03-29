package com.example.ravishankar.lapitchat;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String mCurrentUser_id;
    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = (RecyclerView)mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mCurrentUser_id = mAuth.getCurrentUser().getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference("Friends").child(mCurrentUser_id);
        mFriendDatabase.keepSynced(true);


        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("thumbnail").getValue().toString();
                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnlineStatus(userOnline);
                        }
                        viewHolder.setName(userName);
                        viewHolder.setThumbImage(userThumbImage, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.v("clicked", "clicked");
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        // click event for each item
                                        if(i == 0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if(i == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView userNameView = (TextView)mView.findViewById(R.id.users_single_status);
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);
        }
        public void setThumbImage(String thumbimage, Context ctx){
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.users_single_image);
            Picasso.with(ctx).load(thumbimage).placeholder(R.drawable.defaultimage).into(userImageView);
        }
        public  void setOnlineStatus(String isOnline){
            ImageView onlineImage = (ImageView)mView.findViewById(R.id.users_single_online);
            if(isOnline.equals("true")){
                onlineImage.setVisibility(View.VISIBLE);
            }
            else{
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }
    }
}
