package com.example.ravishankar.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    //firebase auth
    private FirebaseAuth mAauth;
    private DatabaseReference mDatabase;

    // toolbar
    private Toolbar mToolbar;

    //ProgressDialog

    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Action bar
        mToolbar = (Toolbar)findViewById(R.id.login_action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //progress dialog
        mRegProgress = new ProgressDialog(this);
        //firebase auth
        mAauth = FirebaseAuth.getInstance();


        // getting fields
        mDisplayName = (TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn = (Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting data
                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(displayName, email, password);
                }
            }
        });
    }

    private void registerUser(final String displayName, String email, String password) {
        mAauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                System.out.println(task.isSuccessful());
                if(task.isSuccessful()){

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(currentUser != null){
                        String uid = currentUser.getUid();
                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                        final String device_token = FirebaseInstanceId.getInstance().getToken();
                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", displayName);
                        userMap.put("status", "Hey there I am using Lapit Chat");
                        userMap.put("image","default");
                        userMap.put("thumbnail","default");

                        mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mRegProgress.dismiss();
                                    mDatabase.child("device_token").setValue(device_token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                            else{
                                                mRegProgress.hide();
                                                Toast.makeText(RegisterActivity.this, "Cannot Register. Please check the form and try again", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                }
                            }
                        });
                    }
                }
                else{
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot Register. Please check the form and try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
