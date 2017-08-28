package com.example.ravishankar.lapitchat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ravishankar on 13/8/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Message> messageList){
        this.mMessageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView)itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            displayName = (TextView)itemView.findViewById(R.id.message_display_name);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        Message c = mMessageList.get(position);
        mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getUid();
        String fromUser = c.getFrom();
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users").child(fromUser);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumbnail").getValue().toString();
                holder.displayName.setText(name);
                Picasso.with(holder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.defaultimage).into(holder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
