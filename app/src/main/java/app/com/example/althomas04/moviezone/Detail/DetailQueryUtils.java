package app.com.example.althomas04.moviezone.Detail;

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
import java.util.ArrayList;

import app.com.example.althomas04.moviezone.BuildConfig;
import app.com.example.althomas04.moviezone.Main.MainQueryUtils;

/**
 * Created by al.thomas04.
 */

public class DetailQueryUtils {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainQueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link DetailQueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name DetailQueryUtils (and an object instance of DetailQueryUtils is not needed).
     */
    private DetailQueryUtils() {
    }

    public static ArrayList<ExtraMovieData> fetchExtraMovieData(int pathParam) {
        Log.d(LOG_TAG, "Starting Detail JSON request");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection;
        BufferedReader reader;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr;
        ArrayList<ExtraMovieData> extraMovieInfo = new ArrayList<>();

        try {
            // Construct the URL for the TMDB query
            // Possible parameters are avaiable at TMDB's forecast API page, at
            // http://tmdb.org/
            final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
            String pathParamString = Integer.toString(pathParam);
            final String APIKEY_PARAM = "api_key";
            final String APPEND_PARAM = "append_to_response";
            final String APPEND_VALUES = "videos,reviews";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendPath(pathParamString)
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.TMDB_API_KEY)
                    .appendQueryParameter(APPEND_PARAM, APPEND_VALUES)
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
                return null;
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
                return null;
            }

            moviesJsonStr = buffer.toString();
            extraMovieInfo = getMovieDataFromJson(moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error IOException ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return extraMovieInfo;
    }

    /**
     * Take the String representing the complete movie data in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */
    private static ArrayList<ExtraMovieData> getMovieDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        // Weather information. Each movie info is an element of the "list" array.
        final String TMDB_GENRES = "genres";
        final String TMDB_NAME = "name";
        final String TMDB_RUNTIME = "runtime";
        final String TMDB_LANGUAGE = "original_language";
        final String TMDB_RESULTS = "results";
        final String TMDB_VIDEOS = "videos";
        final String TMDB_VIDEO_KEY = "key";
        final String TMDB_REVIEWS = "reviews";
        final String TMDB_REVIEW_AUTHOR = "author";
        final String TMDB_REVIEW_CONTENT = "content";


        ArrayList<ExtraMovieData> extraMovieInfo = new ArrayList<>();
        ArrayList<String> collectedGenres = new ArrayList<String>();
        ArrayList<ReviewsData> collectedReviews = new ArrayList<>();

        try {

            JSONObject moviesJson = new JSONObject(moviesJsonStr);

            JSONArray genresArray = moviesJson.getJSONArray(TMDB_GENRES);
            for (int i = 0; i < genresArray.length(); i++) {
                JSONObject genreInfo = genresArray.getJSONObject(i);
                String genre = genreInfo.getString(TMDB_NAME);
                collectedGenres.add(genre);
            }

            int runtime = moviesJson.getInt(TMDB_RUNTIME);

            String language = moviesJson.getString(TMDB_LANGUAGE);

            JSONObject videoObject = moviesJson.getJSONObject(TMDB_VIDEOS);
            JSONArray videoResultArray = videoObject.getJSONArray(TMDB_RESULTS);
            JSONObject videoInfo = videoResultArray.getJSONObject(0);
            String trailer = videoInfo.getString(TMDB_VIDEO_KEY);

            JSONObject reviewsObject = moviesJson.getJSONObject(TMDB_REVIEWS);
            JSONArray reviewResultArray = reviewsObject.getJSONArray(TMDB_RESULTS);
            for (int i = 0; i < reviewResultArray.length(); i++) {
                JSONObject reviewInfo = reviewResultArray.getJSONObject(i);
                String review_author = reviewInfo.getString(TMDB_REVIEW_AUTHOR);
                String review_content = reviewInfo.getString(TMDB_REVIEW_CONTENT);
                collectedReviews.add(new ReviewsData(review_author, review_content));
            }

            extraMovieInfo.add(new ExtraMovieData(collectedGenres, runtime, language, trailer, collectedReviews));

            Log.d(LOG_TAG, "Detailed JSON Request Complete.");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return extraMovieInfo;
    }
}
