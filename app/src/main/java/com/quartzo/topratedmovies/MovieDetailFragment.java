package com.quartzo.topratedmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quartzo.topratedmovies.adapters.ReviewAdapter;
import com.quartzo.topratedmovies.adapters.TrailerAdapter;
import com.quartzo.topratedmovies.api.TheMovieDBService;
import com.quartzo.topratedmovies.data.ReviewResponse;
import com.quartzo.topratedmovies.data.TrailerResponse;
import com.quartzo.topratedmovies.provider.MovieContract;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieGridActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    private final String ARG_ITEM_ID = "item_id";
    private final int COL_MOVIE_ID = 0;
    private final int COL_MOVIE_THUMB = 1;
    private final int COL_MOVIE_RELEASE_DATE = 2;
    private final int COL_MOVIE_SYNOPSIS = 3;
    private final int COL_MOVIE_BACKDROP = 4;
    private final int COL_MOVIE_ORIGINAL_TITTLE = 5;
    private final int COL_MOVIE_RATE = 6;
    private final int COL_MOVIE_FLAG_FAVORITE = 7;
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    private final String IMAGE_POSTER_SIZE = "w300";
    private final String IMAGE_BG_SIZE = "w500";
    private final String MOVIE_SHARE_HASHTAG = "#topRatedMotives Hi there! Check this trailer. It's awesome! http://www.youtube.com/watch?v=";

    static final String DETAIL_URI = "URI";

    private static final String[] DETAIL_COLUMNS = {

            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_THUMB,
            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP,
            MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_RATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE

    };

    @BindView(R.id.detail_imageview_poster) ImageView poster;
    @BindView(R.id.detail_textview_overview) TextView overview;
    @BindView(R.id.detail_textview_score) TextView score;
    @BindView(R.id.detail_textview_release) TextView release;
    @BindView(R.id.detail_textview_title) TextView title;
    @BindView(R.id.detail_recycler_trailers) RecyclerView recycleTrailers;
    @BindView(R.id.detail_recycler_reviews) RecyclerView recycleReviews;
    @BindView(R.id.detail_card_trailers) CardView cardViewTrailers;
    @BindView(R.id.detail_card_reviews) CardView cardViewReviews;

    private ImageView backDrop;
    private OnFragmentInteractionListener mListener;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private ShareActionProvider mShareActionProvider;
    private MenuItem menushareItem;
    private TheMovieDBService theMovieDBService;
    public Uri mUri;

    Callback<TrailerResponse> callbackTrailers = new Callback<TrailerResponse>() {
        @Override
        public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
            if (response.body() != null && !response.body().getResults().isEmpty()) {
                menushareItem.setVisible(true);
                cardViewTrailers.setVisibility(View.VISIBLE);
                mTrailerAdapter.setTrailers(response.body().getResults());
                mTrailerAdapter.notifyDataSetChanged();
                String firstTrailer = response.body().getResults().get(0).getKey();
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareTrailerIntent(firstTrailer));
                }
            }
        }

        @Override
        public void onFailure(Call<TrailerResponse> call, Throwable t) {

        }
    };
    Callback<ReviewResponse> callbackReviews = new Callback<ReviewResponse>() {
        @Override
        public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
            if (response.body() != null && !response.body().getResults().isEmpty()) {
                cardViewReviews.setVisibility(View.VISIBLE);
                mReviewAdapter.setReviewList(response.body().getResults());
                mReviewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailure(Call<ReviewResponse> call, Throwable t) {

        }
    };

    private int DETAIL_LOADER = 0;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ButterKnife.bind(this, rootView);
        Bundle arguments = getArguments();

        if (arguments != null) {
            mUri = arguments.getParcelable(MovieDetailFragment.DETAIL_URI);
        }

        mTrailerAdapter = new TrailerAdapter(getContext());
        mReviewAdapter = new ReviewAdapter(getContext());
        backDrop = (ImageView) getActivity().findViewById(R.id.detail_imageview_backdrop);
        recycleTrailers.setAdapter(mTrailerAdapter);
        recycleReviews.setAdapter(mReviewAdapter);
        theMovieDBService = TheMovieDBService.getInstance();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.movie_detail_fragment, menu);
        menushareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menushareItem);

    }

    private Intent createShareTrailerIntent(String mFirstTrailer) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, MOVIE_SHARE_HASHTAG + mFirstTrailer);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void loadImageView(String thumbPath, ImageView imageView, String imgSize) {
        Picasso.with(getActivity().getApplicationContext())
                .load(createURIByPhoto(thumbPath, imgSize))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .fit()
                .centerCrop()
                .tag(getActivity().getApplicationContext())
                .into(imageView);
    }

    public Uri createURIByPhoto(String imagePath, String imgSize) {
        Uri builtUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendPath(imgSize)
                .appendEncodedPath(imagePath)
                .build();

        return builtUri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    private void updateFloatButton(boolean flag) {
        FloatingActionButton floatingActionButton;

        if (getActivity() instanceof MovieDetailActivity) {
            floatingActionButton = ((MovieDetailActivity) getActivity()).fab;
        } else {
            floatingActionButton = ((MovieGridActivity) getActivity()).fab;
        }

        if (flag) {
            floatingActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        if (!floatingActionButton.isShown()) floatingActionButton.show();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            int movieId = data.getInt(COL_MOVIE_ID);

            overview.setText(data.getString(COL_MOVIE_SYNOPSIS));
            score.setText(String.format(getContext().getString(R.string.detail_rate),
                    data.getString(COL_MOVIE_RATE)));
            release.setText(String.format(getContext().getString(R.string.detail_release),
                    data.getString(COL_MOVIE_RELEASE_DATE)));
            title.setText(data.getString(COL_MOVIE_ORIGINAL_TITTLE));

            if (mListener != null) {
                mListener.onFragmentInteraction(data.getString(COL_MOVIE_ORIGINAL_TITTLE));
            }

            if (!data.isNull(COL_MOVIE_THUMB))
                loadImageView(data.getString(COL_MOVIE_THUMB), poster, IMAGE_POSTER_SIZE);
            if (!data.isNull(COL_MOVIE_BACKDROP) && backDrop != null)
                loadImageView(data.getString(COL_MOVIE_BACKDROP), backDrop, IMAGE_BG_SIZE);

            updateFloatButton(data.getInt(COL_MOVIE_FLAG_FAVORITE) == 1 ? true : false);

            theMovieDBService.getMovieTrailers(movieId, callbackTrailers);
            theMovieDBService.getMovieReviews(movieId, callbackReviews);

        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String title);
    }
}
