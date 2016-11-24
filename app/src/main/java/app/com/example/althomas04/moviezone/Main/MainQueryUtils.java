package app.com.example.althomas04.moviezone.Main;

/**
 * Created by al.thomas04 on 11/17/2016.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import app.com.example.althomas04.moviezone.BuildConfig;
import app.com.example.althomas04.moviezone.Data.MoviesContract;

/**
 * Helper methods related to requesting and receiving Movie data from TMDB.
 */
public final class MainQueryUtils {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainQueryUtils.class.getSimpleName();

    public static int mPageParam;
    /**
     * Create a private constructor because no one should ever create a {@link MainQueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name MainQueryUtils (and an object instance of MainQueryUtils is not needed).
     */
    private MainQueryUtils() {
    }

    public static void fetchMovieData(Context context, String pathParam, int pageParam) {
        Log.d(LOG_TAG, "Starting JSON request");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr;

        mPageParam = pageParam;
        String pageParamString = Integer.toString(mPageParam);

        try {
            // Construct the URL for the TMDB query
            // Possible parameters are avaiable at TMDB's forecast API page, at
            // http://tmdb.org/
            final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/";

            final String APIKEY_PARAM = "api_key";
            final String PAGE_PARAM = "page";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(pathParam)
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.TMDB_API_KEY)
                    .appendQueryParameter(PAGE_PARAM, pageParamString)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            moviesJsonStr = buffer.toString();
            getMovieDataFromJson(context, moviesJsonStr, pathParam);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error IOException ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return;
    }

    /**
     * Take the String representing the complete movie data in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */
    private static void getMovieDataFromJson(Context context, String moviesJsonStr,
                                             String categoryParam)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        // Weather information. Each movie info is an element of the "list" array.
        final String TMDB_RESULTS = "results";

        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_BACKDROP_PATH = "backdrop_path";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        try {
            long categoryId = addCategory(context, categoryParam);

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(TMDB_RESULTS);

            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsArray.length());


            for (int i = 0; i < resultsArray.length(); i++) {
                // Get the JSON object representing the movie
                JSONObject movieInfo = resultsArray.getJSONObject(i);

                // These are the values that will be collected.
                String posterPath = movieInfo.getString(TMDB_POSTER_PATH);
                String overview = movieInfo.getString(TMDB_OVERVIEW);
                String releaseDate = movieInfo.getString(TMDB_RELEASE_DATE);
                int movieId = movieInfo.getInt(TMDB_MOVIE_ID);
                String title = movieInfo.getString(TMDB_TITLE);
                String backdropPath = movieInfo.getString(TMDB_BACKDROP_PATH);
                int voteCount = movieInfo.getInt(TMDB_VOTE_COUNT);
                double voteAverage = movieInfo.getDouble(TMDB_VOTE_AVERAGE);


                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MoviesEntry.COLUMN_CAT_KEY, categoryId);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, voteCount);

                cVVector.add(movieValues);
            }

            int inserted = 0;

            if (cVVector.size() > inserted) {
                if(mPageParam == 1) {
                    // delete old data so we don't build up an endless history
                    context.getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                            MoviesContract.MoviesEntry.COLUMN_CAT_KEY + " = ?",
                            new String[]{Long.toString(categoryId)});
                }
                // then add the new data to database
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                context.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);

            }

            Log.d(LOG_TAG, "JSON Request Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }


    /**
     * Helper method to handle insertion of a new category in the movies database.
     *
     * @param categoryParam The category string used to request updates from the server.
     * @return the row ID of the added category.
     */
    public static long addCategory(Context context, String categoryParam) {
        long categoryId;

        // First, check if a category with this name exists in the db
        Cursor categoryCursor = context.getContentResolver().query(
                MoviesContract.CategoryEntry.CONTENT_URI,
                new String[]{MoviesContract.CategoryEntry._ID},
                MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM + " = ?",
                new String[]{categoryParam},
                null);

        if (categoryCursor != null && categoryCursor.moveToFirst()) {
            int categoryIdIndex = categoryCursor.getColumnIndex(MoviesContract.CategoryEntry._ID);
            categoryId = categoryCursor.getLong(categoryIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues categoryValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            categoryValues.put(MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM, categoryParam);

            // Finally, insert category data into the database.
            Uri insertedUri = context.getContentResolver().insert(
                    MoviesContract.CategoryEntry.CONTENT_URI,
                    categoryValues
            );

            // The resulting URI contains the ID for the row.  Extract the categoryId from the Uri.
            categoryId = ContentUris.parseId(insertedUri);
        }

        categoryCursor.close();

        return categoryId;
    }
}
