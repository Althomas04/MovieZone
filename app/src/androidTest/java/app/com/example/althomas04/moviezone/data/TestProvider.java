package app.com.example.althomas04.moviezone.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.Data.MoviesContract.CategoryEntry;
import app.com.example.althomas04.moviezone.Data.MoviesContract.MoviesEntry;
import app.com.example.althomas04.moviezone.Data.MoviesDbHelper;
import app.com.example.althomas04.moviezone.Data.MoviesProvider;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MoviesEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                CategoryEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movies table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Category table during delete", 0, cursor.getCount());
        cursor.close();
    }


    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MoviesProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MoviesProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MoviesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://app.com.example.althomas04.moviezone/movies/
        String type = mContext.getContentResolver().getType(MoviesEntry.CONTENT_URI);
        // vnd.android.cursor.dir/app.com.example.althomas04.moviezone/movies/
        assertEquals("Error: the MoviesEntry CONTENT_URI should return MoviesEntry.CONTENT_TYPE",
                MoviesEntry.CONTENT_TYPE, type);

        String testCategory = "popular";
        // content://app.com.example.althomas04.moviezone/movies/popular
        type = mContext.getContentResolver().getType(
                MoviesEntry.buildMoviesCategory(testCategory));
        // vnd.android.cursor.dir/app.com.example.althomas04.moviezone/movies
        assertEquals("Error: the MoviesEntry CONTENT_URI with category should return MoviesEntry.CONTENT_TYPE",
                MoviesEntry.CONTENT_TYPE, type);

        int testMovieId = 550; // Fight Club Movie Id
        // content://app.com.example.althomas04.moviezone/movies/popular/550
        type = mContext.getContentResolver().getType(
                MoviesEntry.buildMoviesCategoryWithMovieId(testCategory, testMovieId));
        // vnd.android.cursor.item/app.com.example.althomas04.moviezone/movies/popular/550
        assertEquals("Error: the MoviesEntry CONTENT_URI with category and movie_id should return MoviesEntry.CONTENT_ITEM_TYPE",
                MoviesEntry.CONTENT_ITEM_TYPE, type);

        // content://app.com.example.althomas04.moviezone/category
        type = mContext.getContentResolver().getType(CategoryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/app.com.example.althomas04.moviezone/category
        assertEquals("Error: the CategoryEntry CONTENT_URI should return CategoryEntry.CONTENT_TYPE",
                CategoryEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic movies query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicWeatherQuery() {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createTestCategoryValues();
        long categoryRowId = TestUtilities.insertTestCategoryValues(mContext);

        // Fantastic.  Now that we have a category, add some movies!
        ContentValues movieValues = TestUtilities.createMoviesValues(categoryRowId);

        long moviesRowId = db.insert(MoviesEntry.TABLE_NAME, null, movieValues);
        assertTrue("Unable to Insert MoviesEntry into the Database", moviesRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor moviesCursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMoviesQuery", moviesCursor, movieValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your category queries are
        performing correctly.
     */
    public void testBasicLocationQueries() {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createTestCategoryValues();
        long categoryRowId = TestUtilities.insertTestCategoryValues(mContext);

        // Test the basic content provider query
        Cursor categoryCursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicCategoryQueries, category query", categoryCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Category Query did not properly set NotificationUri",
                    categoryCursor.getNotificationUri(), CategoryEntry.CONTENT_URI);
        }
    }



    // Make sure we can still delete after adding/updating stuff
    //
    // Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createTestCategoryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(CategoryEntry.CONTENT_URI, true, tco);
        Uri categoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  If this fails, your insert category
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long categoryRowId = ContentUris.parseId(categoryUri);

        // Verify we got a row back.
        assertTrue(categoryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating CategoryEntry.",
                cursor, testValues);

        // Fantastic.  Now that we have a category, add some movie!
        ContentValues movieValues = TestUtilities.createMoviesValues(categoryRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MoviesEntry.CONTENT_URI, true, tco);

        Uri MoviesInsertUri = mContext.getContentResolver()
                .insert(MoviesEntry.CONTENT_URI, movieValues);
        assertTrue(MoviesInsertUri != null);

        // Did our content observer get called? If this fails, your insert movies
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor moviesCursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MoviesEntry insert.",
                moviesCursor, movieValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        movieValues.putAll(testValues);

        // Get the joined Weather and Location data
        moviesCursor = mContext.getContentResolver().query(
                MoviesEntry.buildMoviesCategory(TestUtilities.TEST_CATEGORY_PARAM),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Movies and Category Data.",
                moviesCursor, movieValues);

        // Get the joined Movie data for a specific movie_id
        moviesCursor = mContext.getContentResolver().query(
                MoviesEntry.buildMoviesCategoryWithMovieId(TestUtilities.TEST_CATEGORY_PARAM, TestUtilities.TEST_MOVIE_ID),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Movies and Category data for a specific Movie_id.",
                moviesCursor, movieValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver categoryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(CategoryEntry.CONTENT_URI, true, categoryObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesEntry.CONTENT_URI, true, moviesObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        categoryObserver.waitForNotificationOrFail();
        moviesObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(categoryObserver);
        mContext.getContentResolver().unregisterContentObserver(moviesObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMoviesValues(long categoryRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues MovieValues = new ContentValues();
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_CAT_KEY, categoryRowId);
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, "Test Title");
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, i);
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, "Test Poster");
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, "Test Overview");
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "Test Date");
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, "Test Backdrop");
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 8.5);
            MovieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, 256);
            returnContentValues[i] = MovieValues;
        }
        return returnContentValues;
    }

    // Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        // first, let's create a location value
        ContentValues testValues = TestUtilities.createTestCategoryValues();
        Uri CategoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, testValues);
        long categoryRowId = ContentUris.parseId(CategoryUri);

        // Verify we got a row back.
        assertTrue(categoryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating CategoryEntry.",
                cursor, testValues);

        // Now we can bulkInsert some Movies.  In fact, we only implement BulkInsert for Movies
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMoviesValues(categoryRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesEntry.CONTENT_URI, true, moviesObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MoviesEntry.CONTENT_URI, bulkInsertContentValues);

        // If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        moviesObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(moviesObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MoviesEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
