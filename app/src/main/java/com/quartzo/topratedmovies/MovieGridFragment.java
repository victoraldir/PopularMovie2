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

package com.quartzo.topratedmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.quartzo.topratedmovies.adapters.ImageAdapter;
import com.quartzo.topratedmovies.provider.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        AbsListView.OnScrollListener {
    public static final int MOVIE_LIST_LOADER = 0;
    public static final int COL_ID = 0;
    private static final String SELECTED_KEY = "selected_position";

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_THUMB,
            MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE

    };
    private int previousTotal = 0;
    private int currentPage = 1;
    private ImageAdapter mAdapter;
    private int mPosition = ListView.INVALID_POSITION;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoaderManager loaderManager;

    public MovieGridFragment() {
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    @Override
    public void onResume() {

        //getLoaderManager().restartLoader(MOVIE_LIST_LOADER, null, this);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAdapter = new ImageAdapter(getActivity(), null, 0);
        loaderManager = getLoaderManager();


        View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        TextView textViewNoData = (TextView) rootView.findViewById(R.id.fragment_list_movies_no_data);
        textViewNoData.setText(R.string.no_data);
        GridView gridViewMovies = (GridView) rootView.findViewById(R.id.fragment_list_movies_gridview);

        gridViewMovies.setAdapter(mAdapter);
        gridViewMovies.setOnItemClickListener(this);
        gridViewMovies.setNumColumns(GridView.AUTO_FIT);
        gridViewMovies.setOnScrollListener(this);
        gridViewMovies.setEmptyView(textViewNoData);


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateLoader();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ((Callback) getActivity())
                .onItemSelected(MovieContract.MovieEntry
                        .buildMovieIdUri(((Cursor) parent.getItemAtPosition(position)).getLong(COL_ID)));

    }

//    private void onSwipeRefresh() {
//        MovieSyncAdapter.syncImmediately(getActivity());
//    }

    private void updateLoader() {
        mSwipeRefreshLayout.setRefreshing(true);
        loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_grid_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();

        switch (item.getItemId()) {
            case R.id.action_sort_popularity:

                editor.putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity_value));
                editor.commit();

                hiddenDetailContainer(true);
                mSwipeRefreshLayout.setEnabled(true);

                loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);

                break;
            case R.id.action_sort_rate:

                editor.putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_rate_value));
                editor.commit();

                hiddenDetailContainer(true);
                mSwipeRefreshLayout.setEnabled(true);

                loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);

                break;
            case R.id.action_sort_favorite:

                editor.putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_favorite_value));
                editor.commit();

                hiddenDetailContainer(true);
                mSwipeRefreshLayout.setEnabled(false);

                loaderManager.restartLoader(MOVIE_LIST_LOADER, null, this);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hiddenDetailContainer(boolean flag) {

        if (getActivity() instanceof MovieGridActivity) {
            ((MovieGridActivity) getActivity()).hiddenDetail(flag);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        String sortOrder;
        String selection = null;
        String[] selectionArgs = null;

        String orderPref = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));

        if (orderPref.equals(getString(R.string.pref_sort_popularity_value))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " DESC";
        } else if (orderPref.equals(getString(R.string.pref_sort_rate_value))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_RATE + " DESC";
        } else {

            selection = MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE + " = ? ";
            selectionArgs = new String[]{"1"};

            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_RATE + " DESC";
        }

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.buildMoviePageUri(currentPage),
                MOVIE_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data == null || data.getCount() == 0 && !isOnline(getContext())){
            if (mSwipeRefreshLayout.isShown())
                mSwipeRefreshLayout.setRefreshing(false);
            ((MovieGridActivity) getActivity()).showMessage(getString(R.string.no_internet_connection));
            return;
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        String orderPref = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));

        if (data == null || data.getCount() == 0 && orderPref.equals(getString(R.string.pref_sort_favorite_value))) {
            ((MovieGridActivity) getActivity()).showMessage(getString(R.string.no_favorites));
        }

        mAdapter.swapCursor(data);

        if (mSwipeRefreshLayout.isShown())
            mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (totalItemCount == previousTotal) {
            return;
        }

        if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
            previousTotal = totalItemCount;
            currentPage++;
            updateLoader();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri movieIdUri);
    }
}
