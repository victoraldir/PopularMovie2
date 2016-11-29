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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quartzo.topratedmovies.R;
import com.quartzo.topratedmovies.data.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victoraldir on 12/11/2016.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context mContext;
    private List<Review> mReviewArrayList;

    public ReviewAdapter(Context mContext) {
        this.mContext = mContext;
        mReviewArrayList = new ArrayList<>();
    }

    public void setReviewList(@Nullable List<Review> mReviewArrayList) {
        this.mReviewArrayList = mReviewArrayList;
        notifyDataSetChanged();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_review, parent, false);
        mContext = parent.getContext();
        return new ReviewAdapter.ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        if (mReviewArrayList == null) {
            return;
        }
        Review review = mReviewArrayList.get(position);
        holder.contentTextView.setText(review.getContent());
        holder.authorTextView.setText(String.format(mContext.getString(R.string.detail_review_author),
                review.getAuthor()));
    }

    @Override
    public int getItemCount() {
        if (mReviewArrayList == null) {
            return 0;
        }
        return mReviewArrayList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView contentTextView;
        TextView authorTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            contentTextView = (TextView) itemView.findViewById(R.id.detail_textview_review_content);
            authorTextView = (TextView) itemView.findViewById(R.id.detail_textview_review_author);

        }

    }

}
