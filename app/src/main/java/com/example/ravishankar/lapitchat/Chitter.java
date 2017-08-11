package com.example.ravishankar.lapitchat;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by ravishankar on 10/8/17.
 */

public class Chitter extends Application {

    private DatabaseReference mUserDatabas;
    
    @Override
    public void onCreate(){
        super.onCreate();
        // firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        //picasso offline
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

}
