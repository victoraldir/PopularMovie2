package com.quartzo.topratedmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.quartzo.topratedmovies.provider.MovieContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieGridActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity implements MovieDetailFragment.OnFragmentInteractionListener {

    private final String FRAGMENT_DETAIL_TAG = "fragmentDetail";

    @Nullable
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private ContentResolver resolver;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);


        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        resolver = getContentResolver();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.DETAIL_URI,
                    getIntent().getData());

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment, FRAGMENT_DETAIL_TAG)
                    .commit();
        }
    }

    @Optional
    @OnClick(R.id.fab)
    void onFabClicked() {

        String columnFlagFavorite = MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE;
        MovieDetailFragment movieDetailFragment = (MovieDetailFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        Cursor movie = resolver.query(movieDetailFragment.mUri, new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE}, null, null, null);

        movie.moveToNext();

        boolean flag = movie.getInt(0) == 1 ? true : false;
        ContentValues contentValues = new ContentValues();

        contentValues.put(columnFlagFavorite, flag ? 0 : 1);

        resolver.update(movieDetailFragment.mUri, contentValues, null, null);

        String msg;

        if (!flag) {
            fab.setImageResource(R.drawable.ic_favorite_white_24dp);
            msg = String.format(getString(R.string.snackbar_add_favorite_message), movie.getString(1));
        } else {
            fab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            msg = String.format(getString(R.string.snackbar_remove_favorite_message), movie.getString(1));
        }

        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG).show();

        movie.close();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            NavUtils.navigateUpTo(this, new Intent(this, MovieGridActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String title) {
        getSupportActionBar().setTitle(title);
    }
}
