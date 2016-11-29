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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quartzo.topratedmovies.R;
import com.quartzo.topratedmovies.provider.MovieContract;
import com.quartzo.topratedmovies.utils.PicassoUtil;
import com.quartzo.topratedmovies.views.DynamicImageView;

public class ImageAdapter extends CursorAdapter {

    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    private final String IMAGE_SIZE = "w300";
    private String TAG_LOG = ImageAdapter.class.getSimpleName();

    public ImageAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View viewRoot = LayoutInflater.from(context).inflate(R.layout.list_item_image_movie, parent, false);

        return viewRoot;
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        final Uri builtUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendEncodedPath(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_THUMB)))
                .build();

        DynamicImageView imageView = (DynamicImageView) view.findViewById(R.id.movie_grid_imageview);
        TextView title = (TextView) view.findViewById(R.id.movie_grid_textview_title);
        ImageView favIcon = (ImageView) view.findViewById(R.id.movie_grid_imageview_favorite);

        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE)));

        boolean flag = cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE)) == 1;

        if (flag) {
            favIcon.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            favIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }

        PicassoUtil.executeLoading(context, builtUri.toString(), imageView);

    }

    @Override
    public Object getItem(int position) {

        return super.getItem(position);
    }
}