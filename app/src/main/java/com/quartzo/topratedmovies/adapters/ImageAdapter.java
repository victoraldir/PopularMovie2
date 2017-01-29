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

package com.quartzo.topratedmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quartzo.topratedmovies.R;
import com.quartzo.topratedmovies.provider.MovieContract;
import com.quartzo.topratedmovies.utils.PicassoUtil;
import com.quartzo.topratedmovies.views.DynamicImageView;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    private final String IMAGE_SIZE = "w300";
    private String TAG_LOG = ImageAdapter.class.getSimpleName();
    private Cursor mCursor;
    private Context mContext;
    private static ImageAdapterOnClickHandler mClickHandler;
    private View mEmptyView;
    private FavoriteImageAdapterOnClickHandler mFavOnclickHandler;

    public ImageAdapter(Context context, ImageAdapterOnClickHandler mClickHandler,FavoriteImageAdapterOnClickHandler mFavOnclickHandler, View mEmptyView) {
        mContext = context;
        this.mClickHandler = mClickHandler;
        this.mEmptyView = mEmptyView;
        this.mFavOnclickHandler = mFavOnclickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if ( parent instanceof RecyclerView ) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_image_movie, parent, false);

            return new ViewHolder(view);
        }else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        mCursor.moveToPosition(position);

        final Uri builtUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendEncodedPath(mCursor.getString(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_THUMB)))
                .build();

        holder.title.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE)));


        boolean flag = mCursor.getInt(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE)) == 1;

        if (flag) {
            holder.favIcon.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            holder.favIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        PicassoUtil.executeLoading(mContext, builtUri.toString(), holder.imageView);

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final DynamicImageView imageView;
        public final TextView title;
        public final ImageView favIcon;

        public ViewHolder(final View view) {
            super(view);
            imageView = (DynamicImageView) view.findViewById(R.id.movie_grid_imageview);

            title = (TextView) view.findViewById(R.id.movie_grid_textview_title);
            favIcon = (ImageView) view.findViewById(R.id.movie_grid_imageview_favorite);

            favIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCursor.moveToPosition(getAdapterPosition());
                    mFavOnclickHandler.onClickFavButton(mCursor.getInt(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry._ID)));
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            mCursor.moveToPosition(getAdapterPosition());
            mClickHandler.onClick( mCursor.getInt(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry._ID)));

        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

    }

    public interface ImageAdapterOnClickHandler {
        void onClick(int movieId);
    }

    public interface FavoriteImageAdapterOnClickHandler {
        void onClickFavButton(int movieId);
    }
}