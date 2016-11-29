package com.quartzo.topratedmovies.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.quartzo.topratedmovies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by victoraldir on 28/11/2016.
 */

public class PicassoUtil {

    public static void executeLoading(final Context context, final String builtUri, final View view) {
        Picasso.with(context)
                .load(builtUri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .tag(context)
                .centerCrop()
                .into((ImageView) view, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(builtUri.toString())
                                .error(R.drawable.error)
                                .into((ImageView) view);
                    }
                });
    }

}
