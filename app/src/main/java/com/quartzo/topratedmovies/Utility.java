/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quartzo.topratedmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.quartzo.topratedmovies.sync.MovieSyncAdapter;

public class Utility {

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static String getSortOrderPreference(Context context){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String orderPref = prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default_value));

        return  orderPref;
    }

    public static void setSortOrderPreference(Context context, String pref){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(context.getString(R.string.pref_sort_key), pref);
        editor.commit();

    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the location status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @MovieSyncAdapter.MovieStatus
    int getMovieStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_movie_status_key), MovieSyncAdapter.MOVIE_STATUS_UNKNOWN);
    }

}