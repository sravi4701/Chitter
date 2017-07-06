package com.example.ravishankar.lapitchat;

import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView mDisplayName;
    private TextView mStatus;
    private CircleImageView mImage;
    private Button mStatusbtn;
    private Button mImageBtn;

    //firebase
    private StorageReference mImageStorage;
    private static final int GALLARY_PIC = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayName = (TextView)findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mImage = (CircleImageView) findViewById(R.id.settings_image);
        mStatusbtn = (Button)findViewById(R.id.settings_status_btn);
        mImageBtn = (Button)findViewById(R.id.settings_image_btn);

        //firebase
        mImageStorage = FirebaseStorage.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                mDisplayName.setText(displayName);
                mStatus.setText(status);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SettingsActivity.this);

                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryIntent, "SELECT IMAGE"), GALLARY_PIC);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLARY_PIC && resultCode == RESULT_OK){

            Uri  imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .start(this);
            System.out.println("com");
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                System.out.println("coming");

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    Uri resultUri = result.getUri();
                    Toast.makeText(SettingsActivity.this, resultUri.toString(), Toast.LENGTH_LONG).show();

                    StorageReference filePath = mImageStorage.child("profile_images").child("profile_image.jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Success", Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(SettingsActivity.this, "Got Some error", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
