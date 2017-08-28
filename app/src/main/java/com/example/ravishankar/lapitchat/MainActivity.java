package com.example.ravishankar.lapitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private Toolbar mToolbar;

    private ViewPager mViewPager;

    private CustomPagerAdapter mCustomPagerAdapter;

    private TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
        }
        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Chitter");

        mViewPager = (ViewPager)findViewById(R.id.main_tabPager);

        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mCustomPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.main_tab);

        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
//            mUserDatabase.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }
        else{
            mUserDatabase.child("online").setValue("true");
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId() == R.id.main_account_btn){
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }
        if(item.getItemId() == R.id.main_all_btn){
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
        }
        return true;
    }
}
