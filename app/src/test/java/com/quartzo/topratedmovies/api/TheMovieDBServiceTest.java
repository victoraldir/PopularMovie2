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

import com.quartzo.topratedmovies.data.Movie;
import com.quartzo.topratedmovies.data.MovieResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by victoraldir on 15/11/2016.
 */
public class TheMovieDBServiceTest {

    static BlockingQueue<List<Movie>> queue;
    TheMovieDBService theMovieDBService;
    private String LOG_TAG = TheMovieDBServiceTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {

        theMovieDBService = TheMovieDBService.getInstance();

        queue = new ArrayBlockingQueue(1024);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldDiscoverMoviesSynchronously() throws IOException {

        long start = System.currentTimeMillis();

        MovieResponse response = theMovieDBService.getDiscoverMovies(TheMovieDBService.SORT_POPULARITY, 1);

        long end = System.currentTimeMillis();

        System.out.print("Process took: " + (end - start) / 1000 + " seconds");

        assertTrue(response.getResults().size() > 0);
        assertNotNull(response.getResults().get(0).getSynopsis());

    }

    @Test
    public void shouldDiscoverMoviesAsynchronously() throws InterruptedException {

        Callback<MovieResponse> callback = new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                try {
                    queue.put(response.body().getResults());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {

            }
        };

        theMovieDBService.getDiscoverMovies(TheMovieDBService.SORT_POPULARITY, 1, callback);

        List<Movie> val = queue.poll(10, TimeUnit.SECONDS);
        assertTrue(val.size() > 0);

    }

}