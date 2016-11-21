/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.com.example.althomas04.moviezone.Data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_CATEGORY = 101;
    public static final int MOVIES_WITH_CATEGORY_AND_MOVIE_ID = 102;
    public static final int CATEGORY = 300;

    private static final SQLiteQueryBuilder sMoviesByCategoryParamQueryBuilder;

    static{
        sMoviesByCategoryParamQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //movies INNER JOIN category ON movies.category_id = category._id
        sMoviesByCategoryParamQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.CategoryEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry.COLUMN_CAT_KEY +
                        " = " + MoviesContract.CategoryEntry.TABLE_NAME +
                        "." + MoviesContract.CategoryEntry._ID);
    }

    //category.category_param = ?
    private static final String sCategoryParamSelection =
            MoviesContract.CategoryEntry.TABLE_NAME+
                    "." + MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM + " = ? ";

    //category.category_param = ? AND movie_id >= ?
    private static final String sCategoryParamWithMovieIdSelection =
            MoviesContract.CategoryEntry.TABLE_NAME+
                    "." + MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM + " = ? AND " +
                    MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " >= ? ";


    private Cursor getMoviesByCategoryParam(Uri uri, String[] projection, String sortOrder) {

        String categoryParam = MoviesContract.MoviesEntry.getCategoryParamFromUri(uri);

        String selection = sCategoryParamSelection;
        String[] selectionArgs = new String[]{categoryParam};

        return sMoviesByCategoryParamQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesByCategoryParamAndMovieId(Uri uri, String[] projection, String sortOrder) {

        String categoryParam = MoviesContract.MoviesEntry.getCategoryParamFromUri(uri);
        int movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);

        String selection = sCategoryParamWithMovieIdSelection;
        String[] selectionArgs = new String[]{categoryParam, Integer.toString(movieId)};

        return sMoviesByCategoryParamQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        This UriMatcher will match each URI to the MOVIES, MOVIES_WITH_CATEGORY, MOVIES_WITH_CATEGORY_AND_MOVIE_ID,
        and CATEGORY integer constants defined above.
     */
    public static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*", MOVIES_WITH_CATEGORY);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/*/#", MOVIES_WITH_CATEGORY_AND_MOVIE_ID);
        matcher.addURI(authority, MoviesContract.PATH_CATEGORY, CATEGORY);
        return matcher;
    }

    /*
        We just create a new MoviesDbHelper for later use here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    /*
        The getType function that uses the UriMatcher.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIES_WITH_CATEGORY_AND_MOVIE_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_CATEGORY:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case CATEGORY:
                return MoviesContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies/*/*"
            case MOVIES_WITH_CATEGORY_AND_MOVIE_ID:
            {
                retCursor = getMoviesByCategoryParamAndMovieId(uri, projection, sortOrder);
                break;
            }
            // "movies/*"
            case MOVIES_WITH_CATEGORY: {
                retCursor = getMoviesByCategoryParam(uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "category"
            case CATEGORY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Adds the ability to insert category params to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(MoviesContract.CategoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.CategoryEntry.buildCategoryUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
/*
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
*/
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(MoviesContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(MoviesContract.CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}