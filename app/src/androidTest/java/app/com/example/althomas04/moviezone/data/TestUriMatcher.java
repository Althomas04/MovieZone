package app.com.example.althomas04.moviezone.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.Data.MoviesProvider;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String CATEGORY_QUERY = "popular";
    private static final int TEST_MOVIE_ID = 550;  // Fight Club Movie Id

    // content://app.com.example.althomas04.moviezone/movies"
    private static final Uri TEST_WEATHER_DIR = MoviesContract.MoviesEntry.CONTENT_URI;
    private static final Uri TEST_WEATHER_WITH_LOCATION_DIR = MoviesContract.MoviesEntry.buildMoviesCategory(CATEGORY_QUERY);
    private static final Uri TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR = MoviesContract.MoviesEntry.buildMoviesCategoryWithMovieId(CATEGORY_QUERY, TEST_MOVIE_ID);
    // content://app.com.example.althomas04.moviezone/category"
    private static final Uri TEST_LOCATION_DIR = MoviesContract.CategoryEntry.CONTENT_URI;

    /*
        This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MoviesProvider.buildUriMatcher();

        assertEquals("Error: The MOVIES URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_DIR), MoviesProvider.MOVIES);
        assertEquals("Error: The MOVIES_WITH_CATEGORY URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_DIR), MoviesProvider.MOVIES_WITH_CATEGORY);
        assertEquals("Error: The MOVIES_WITH_CATEGORY_AND_MOVIE_ID URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR), MoviesProvider.MOVIES_WITH_CATEGORY_AND_MOVIE_ID);
        assertEquals("Error: The CATEGORY URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), MoviesProvider.CATEGORY);
    }
}
