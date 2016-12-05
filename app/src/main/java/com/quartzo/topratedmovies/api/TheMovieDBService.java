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

package com.quartzo.topratedmovies.api;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.quartzo.topratedmovies.BuildConfig;
import com.quartzo.topratedmovies.data.MovieResponse;
import com.quartzo.topratedmovies.data.ReviewResponse;
import com.quartzo.topratedmovies.data.TrailerResponse;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by victoraldir on 13/11/2016.
 */

public class TheMovieDBService {

    public static final String SORT_POPULARITY = "popularity.desc";
    public static final String SORT_RATE = "rate.desc";
    private static final String API_URL = "https://api.themoviedb.org";
    private static TheMovieDBService instance;
    private ITheMovieDB mService;


    private TheMovieDBService() {

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addNetworkInterceptor(new StethoInterceptor());
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_URL)
                .client(client)
                .build();

        mService = retrofit.create(ITheMovieDB.class);
    }

    public static TheMovieDBService getInstance() {

        if (instance == null) instance = new TheMovieDBService();
        return instance;
    }

    public MovieResponse getDiscoverMovies(String sort, int page) throws IOException {

        Call<MovieResponse> call = mService.discoverMovies(sort, page, BuildConfig.THE_MOVIE_DATABASE_API_KEY);
        MovieResponse apiResponse = null;

        Response<MovieResponse> response = call.execute();
        apiResponse = response.body();

        return apiResponse;
    }

    public void getDiscoverMovies(String sort, int page, Callback<MovieResponse> callback) {

        mService.discoverMovies(sort, page, BuildConfig.THE_MOVIE_DATABASE_API_KEY).enqueue(callback);

    }

    public void getMovieTrailers(int movieId, Callback<TrailerResponse> callback) {

        mService.getMovieTrailers(movieId, BuildConfig.THE_MOVIE_DATABASE_API_KEY).enqueue(callback);

    }

    public void getMovieReviews(int movieId, Callback<ReviewResponse> callback) {

        mService.getMovieReviews(movieId, BuildConfig.THE_MOVIE_DATABASE_API_KEY).enqueue(callback);

    }

    interface ITheMovieDB {

        @GET("3/movie/{id}/videos")
        Call<TrailerResponse> getMovieTrailers(@Path("id") long movieId, @Query("api_key") String apiKey);

        @GET("3/movie/{id}/reviews")
        Call<ReviewResponse> getMovieReviews(@Path("id") long movieId, @Query("api_key") String apiKey);

        @GET("3/discover/movie")
        Call<MovieResponse> discoverMovies(@Query("sort_by") String sortBy, @Query("page") Integer page, @Query("api_key") String apiKey);

        @GET("3/search/movie")
        Call<MovieResponse> searchMovies(@Query("query") String query, @Query("page") Integer page, @Query("api_key") String apiKey);
    }
}
