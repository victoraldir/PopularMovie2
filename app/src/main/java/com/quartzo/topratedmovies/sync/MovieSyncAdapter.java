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
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.quartzo.topratedmovies.R;
import com.quartzo.topratedmovies.api.TheMovieDBService;
import com.quartzo.topratedmovies.data.Movie;
import com.quartzo.topratedmovies.data.MovieResponse;
import com.quartzo.topratedmovies.provider.MovieContract;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * Created by victoraldir on 20/11/2016.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {


    private final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final int TOTAL_PAGES = 50;
    private static final int UPDATE_GRID_PAGE = 4;
    private static boolean FLAG_FIRST_TIME = true; // Notify change when

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVIE_STATUS_OK, MOVIE_STATUS_SERVER_DOWN, MOVIE_STATUS_SERVER_INVALID,  MOVIE_STATUS_UNKNOWN})
    public @interface MovieStatus {}

    public static final int MOVIE_STATUS_OK = 0;
    public static final int MOVIE_STATUS_SERVER_DOWN = 1;
    public static final int
            MOVIE_STATUS_SERVER_INVALID = 2;
    public static final int
            MOVIE_STATUS_UNKNOWN = 3;

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

            try {

                MovieResponse movieResponse = movieService.getDiscoverMovies(TheMovieDBService.SORT_POPULARITY, x);

                if (movieResponse != null) {
                    bulkInsert(new HashSet<>(movieResponse.getResults()));
                }

                movieResponse = movieService.getDiscoverMovies(TheMovieDBService.SORT_RATE, x);

                if (movieResponse != null) {
                    bulkInsert(new HashSet<>(movieResponse.getResults()));
                }

                if (x == UPDATE_GRID_PAGE && FLAG_FIRST_TIME) {
                    FLAG_FIRST_TIME = false;
                    getContext().getContentResolver().notifyChange(MovieContract.MovieEntry.CONTENT_URI, null);
                }

                if (movieResponse == null) {
                    setMovieStatus(getContext(), MOVIE_STATUS_SERVER_DOWN);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                setMovieStatus(getContext(), MOVIE_STATUS_SERVER_DOWN);
            }
        }


        setMovieStatus(getContext(), MOVIE_STATUS_OK);

    }

    private void bulkInsert(Set<Movie> movieList){
        Vector<ContentValues> cVVector = parsetoVectorContentValuesMovies(movieList);
        ContentValues[] cvArray = null;

        if (cVVector.size() > 0) {

            cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
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

    /**
     * Sets the location status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param locationStatus The IntDef value to set
     */
    static private void setMovieStatus(Context c, @MovieStatus int locationStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_movie_status_key), locationStatus);
        spe.commit();
    }
}
