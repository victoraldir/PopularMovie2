package com.quartzo.topratedmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Created by victoraldir on 28/11/2016.
 */

public class Global extends Application {

    // Size in bytes (10 MB)
    private static final int PICASSO_DISK_CACHE_SIZE = 1024 * 1024 * 10;

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);

        Picasso.Builder builder = new Picasso.Builder(this).memoryCache(new LruCache(PICASSO_DISK_CACHE_SIZE));
        Picasso built = builder.build();
        Picasso.setSingletonInstance(built);


    }
}
