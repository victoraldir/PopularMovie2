package com.quartzo.topratedmovies;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.quartzo.topratedmovies.provider.MovieContract;
import com.quartzo.topratedmovies.sync.MovieSyncAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class MovieGridActivity extends AppCompatActivity implements MovieGridFragment.Callback, MovieDetailFragment.OnFragmentInteractionListener {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    @Nullable
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private boolean mTwoPane;
    private FrameLayout detailContent;
    private Uri movieSelectedUri;
    private CoordinatorLayout coordinatorLayout;
    private ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);


            ButterKnife.bind(this);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);

            resolver = getContentResolver();
            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

            detailContent = (FrameLayout) findViewById(R.id.movie_detail_container);

            if (findViewById(R.id.movie_detail_container) != null) {

                mTwoPane = true;

            }

            MovieSyncAdapter.initializeSyncAdapter(getBaseContext());
        }



    @Optional
    @OnClick(R.id.fab)
    void onFabClicked() {

        setFavoriteMovie(MovieContract.MovieEntry.getIdFromUri(movieSelectedUri));

    }

    public void setFavoriteMovie(long movieId){

        Uri movieUri = MovieContract.MovieEntry.buildMovieIdUri(movieId);

        Cursor movie = resolver.query(movieUri, new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE}, null, null, null);

        movie.moveToNext();

        boolean flag = movie.getInt(0) == 1 ? true : false;

        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, flag ? 0 : 1);

        getContentResolver().update(movieUri, contentValues, null, null);

        if(fab != null) {

            if (!flag) {
                fab.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                fab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }

        }

        showMessage(flag ? String.format(getString(R.string.snackbar_add_favorite_message), movie.getString(1)) :
                String.format(getString(R.string.snackbar_remove_favorite_message), movie.getString(1)));

        movie.close();

    }

    public void showMessage(String msg){
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    public void hiddenDetail(boolean flag) {
        if (detailContent != null) {
            if (flag) {
                detailContent.setVisibility(View.GONE);
            } else {
                detailContent.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {

        movieSelectedUri = contentUri;

        Cursor movie = resolver.query(movieSelectedUri, new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE}, null, null, null);

        movie.moveToNext();

        if (mTwoPane) {

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, contentUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();

            hiddenDetail(false);

        } else {

            Intent it = new Intent(this, MovieDetailActivity.class).setData(contentUri);
            startActivity(it);
        }

        movie.close();
    }

    @Override
    public void onFragmentInteraction(String title) {
        //getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this,query,Toast.LENGTH_SHORT).show();

        }

    }

}
