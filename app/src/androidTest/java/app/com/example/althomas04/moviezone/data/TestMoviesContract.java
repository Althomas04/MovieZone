
package app.com.example.althomas04.moviezone.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import app.com.example.althomas04.moviezone.Data.MoviesContract;

/*
    This is NOT a complete test for the MoviesContract --- just for the functions
    that we expect you to write.
 */
public class TestMoviesContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_MOVIES_CATEGORY = "/popular";

    /*
        Uncomment this out to test your movies category function.
     */
    public void testBuildMoviesCategory() {
        Uri categoryUri = MoviesContract.MoviesEntry.buildMoviesCategory(TEST_MOVIES_CATEGORY);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildMoviesCategory in " +
                        "MoviesContract.",
                categoryUri);
        assertEquals("Error: Movie Category not properly appended to the end of the Uri",
                TEST_MOVIES_CATEGORY, categoryUri.getLastPathSegment());
        assertEquals("Error: Movies Category Uri doesn't match our expected result",
                categoryUri.toString(),
                "content://app.com.example.althomas04.moviezone/movies/%2Fpopular");
    }
}
