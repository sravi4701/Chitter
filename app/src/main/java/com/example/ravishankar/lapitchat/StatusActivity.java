package com.example.ravishankar.lapitchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //firebase
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //getting intent data
        String status_value = getIntent().getStringExtra("status_value");

        mToolbar = (Toolbar)findViewById(R.id.status_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn = (Button)findViewById(R.id.status_save_btn);

        mStatus.getEditText().setText(status_value);
        //firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = mStatus.getEditText().getText().toString();
                //progressbar
                mProgressbar = new ProgressDialog(StatusActivity.this);
                mProgressbar.setTitle("Saving Changes");
                mProgressbar.setMessage("Please wait while we save your changes");
                mProgressbar.setCanceledOnTouchOutside(false);
                mProgressbar.show();

                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressbar.dismiss();
                        }
                        else{
                            Toast.makeText(StatusActivity.this,"There was some erros on saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
