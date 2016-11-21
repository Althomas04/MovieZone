package app.com.example.althomas04.moviezone.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.Data.MoviesDbHelper;
import app.com.example.althomas04.moviezone.utils.PollingCheck;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your MoviesContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_CATEGORY_PARAM = "popular";
    static final int TEST_MOVIE_ID = 550;  // Fight Club Movie ID

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Use this to create some default movie values for your database tests.
     */
    static ContentValues createMoviesValues(long categoryRowId) {
        ContentValues MovieValues = new ContentValues();
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_CAT_KEY, categoryRowId);
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, "Test Title");
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, "Test Poster");
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, "Test Overview");
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "Test Date");
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, "Test Backdrop");
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 8.5);
        MovieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, 256);

        return MovieValues;
    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the WeatherContract.
     */
    static ContentValues createTestCategoryValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM, TEST_CATEGORY_PARAM);

        return testValues;
    }

    /*
        You can uncomment this function once you have finished creating the
        LocationEntry part of the WeatherContract as well as the WeatherDbHelper.
     */
    static long insertTestCategoryValues(Context context) {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTestCategoryValues();

        long categoryRowId;
        categoryRowId = db.insert(MoviesContract.CategoryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Popular Category TEST Values", categoryRowId != -1);

        return categoryRowId;
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
