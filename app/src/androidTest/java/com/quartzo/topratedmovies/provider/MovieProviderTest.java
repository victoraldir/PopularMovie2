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

package com.quartzo.topratedmovies.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.quartzo.topratedmovies.api.TheMovieDBService;
import com.quartzo.topratedmovies.data.Movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * Created by victoraldir on 18/11/2016.
 */
@RunWith(AndroidJUnit4.class)
public class MovieProviderTest {

    public static final String LOG_TAG = MovieProviderTest.class.getSimpleName();
    private Context mContext;
    private ContentResolver resolver;

    static ContentValues createMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry._ID, "284052");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_THUMB, "/xfWac8MTYDxujaxgPVcRD9yZaul.jpg");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS, "After his career is destroyed, a brilliant but arrogant surgeon gets a new lease on life when a sorcerer takes him under his wing and trains him to defend the world against evil.");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, "2016-10-25");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE, "Doctor Strange");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP, "Doctor Strange");
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, 46.696892);
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATE, 6);
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, "0");

        return testValues;
    }

    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        resolver = mContext.getContentResolver();
    }

    @Test
    public void deleteAllRecordsFromProvider() {

        mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testBasicMovieQuery() {


        MovieDbHelperTest.deleteTheDatabase();
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues movieValues = createMovieValues();
        ;

        long weatherRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue("Unable to Insert MovieEntry into the Database", weatherRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor weatherCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        //TestUtilities.validateCursor("testBasicWeatherQuery", weatherCursor, movieValues);
    }

    @Test
    public void testBulkInsertionMovie() throws IOException {

        List<Movie> movieListPage1 = TheMovieDBService.getInstance().getDiscoverMovies(TheMovieDBService.SORT_POPULARITY, 1).getResults();


        for (int x = 0; x < 2; x++) {
            Vector<ContentValues> cVVector = parsetoVectorContentValuesMovies(movieListPage1);

            if (cVVector.size() > 0) {

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

            }
        }

    }

    @Test
    public void shouldUpdateFlagFavoriteMovie() {

        String columnFlagFavorite = MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE;

        deleteAllRecords();

        //Insert the movie to be Updated
        Uri movieCreated = resolver.insert(MovieContract.MovieEntry.CONTENT_URI, createMovieValues());

        //Query the movie to be Updated
        Cursor movie = resolver.query(movieCreated, new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE}, null, null, null);

        ContentValues contentValues = new ContentValues();

        movie.moveToNext();

        contentValues.put(columnFlagFavorite, movie.getInt(0) == 1 ? 0 : 1);

        //Update the movie to be Updated
        resolver.update(movieCreated, contentValues, null, null);

        //Check if flag is sure enough updated
        Cursor movieFlagChanged = resolver.query(movieCreated, new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE}, null, null, null);

        movieFlagChanged.moveToNext();

        assertNotSame(movieFlagChanged.getInt(0), movie.getInt(0));


    }

    private Vector<ContentValues> parsetoVectorContentValuesMovies(List<Movie> movieList) {

        Vector<ContentValues> cVVector = new Vector<ContentValues>(movieList.size());

        for (int i = 0; i < movieList.size(); i++) {

            Movie currMovie = movieList.get(i);

            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry._ID, currMovie.getId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP, currMovie.getBackdrop());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, currMovie.getPopularity());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_THUMB, currMovie.getThumbImagePath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATE, currMovie.getRate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS, currMovie.getSynopsis());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ORIGINAL_TITTLE, currMovie.getOriginalTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, currMovie.getReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_FLAG_FAVORITE, "0");

            cVVector.add(movieValues);
        }

        return cVVector;

    }
}