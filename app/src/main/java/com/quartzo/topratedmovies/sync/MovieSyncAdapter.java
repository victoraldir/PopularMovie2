/*
 * MIT License
 *
 * Copyright (c) 2016 Victor Hugo Montes Neves
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.quartzo.topratedmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import com.quartzo.topratedmovies.R;
import com.quartzo.topratedmovies.api.TheMovieDBService;
import com.quartzo.topratedmovies.data.Movie;
import com.quartzo.topratedmovies.data.MovieResponse;
import com.quartzo.topratedmovies.provider.MovieContract;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Created by victoraldir on 20/11/2016.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {


    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final int TOTAL_PAGES = 5;
    private static final int UPDATE_GRID_PAGE = 2;
    private static boolean FLAG_FIRST_TIME = true; // Notify change when

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static Account getSyncAccount(Context context) {

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        //syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        TheMovieDBService movieService = TheMovieDBService.getInstance();

        for (int x = 1; x <= TOTAL_PAGES; x++) {

            MovieResponse movieResponse = movieService.getDiscoverMovies(TheMovieDBService.SORT_POPULARITY, x);

            if (movieResponse != null) {
                bulkInsert(new HashSet<>(movieResponse.getResults()));
            }

            movieResponse = movieService.getDiscoverMovies(TheMovieDBService.SORT_RATE, x);

            if (movieResponse != null) {
                bulkInsert(new HashSet<>(movieResponse.getResults()));
            }

            if(x == UPDATE_GRID_PAGE && FLAG_FIRST_TIME){
                FLAG_FIRST_TIME = false;
                getContext().getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI,null);
            }
        }

    }

    private void bulkInsert(Set<Movie> movieList){
        Vector<ContentValues> cVVector = parsetoVectorContentValuesMovies(movieList);
        ContentValues[] cvArray = null;

        if (cVVector.size() > 0) {

            cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

        }
    }

    private Vector<ContentValues> parsetoVectorContentValuesMovies(Set<Movie> movieList) {

        Vector<ContentValues> cVVector = new Vector<ContentValues>(movieList.size());

        Iterator it = movieList.iterator();

        while (it.hasNext()) {

            Movie currMovie = (Movie) it.next();

            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry._ID, currMovie.getId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP, currMovie.getBackdrop());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, currMovie.getPopularity());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_THUMB, currMovie.getThumbImagePath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATE, currMovie.getRate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS, currMovie.getSynopsis());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE, currMovie.getOriginalTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, currMovie.getReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, "0");

            cVVector.add(movieValues);
        }

        return cVVector;

    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
