package app.com.example.althomas04.moviezone.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.Data.MoviesDbHelper;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MoviesContract.CategoryEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.MoviesEntry.TABLE_NAME);

        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.CategoryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> categoryColumnHashSet = new HashSet<String>();
        categoryColumnHashSet.add(MoviesContract.CategoryEntry._ID);
        categoryColumnHashSet.add(MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            categoryColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                categoryColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Test that we can insert and query the category database.
    */
    public void testCategoryTable() {
        insertCategory();
    }

    /*
        Test that we can insert and query the movies database.
     */
    public void testMoviesTable() {
        // First insert the category, and then use the categoryRowId to insert
        // the movie. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testCategoryTable
        // we can move this code to insertCategory and then call insertCategory from both
        // tests. Why move it? We need the code to return the ID of the inserted category
        // and our testCategoryTable can only return void because it's a test.

        long categoryRowId = insertCategory();

        // Make sure we have a valid row ID.
        assertFalse("Error: Category Not Inserted Correctly", categoryRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Movies): Create movie values
        ContentValues moviesValues = TestUtilities.createMoviesValues(categoryRowId);

        // Third Step (Movies): Insert ContentValues into database and get a row ID back
        long moviesRowId = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, moviesValues);
        assertTrue(moviesRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor moviesCursor = db.query(
                MoviesContract.MoviesEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from category query", moviesCursor.moveToFirst() );

        // Fifth Step: Validate the category Query
        TestUtilities.validateCurrentRecord("testInsertReadDb moviesEntry failed to validate",
                moviesCursor, moviesValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from movies query",
                moviesCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        moviesCursor.close();
        dbHelper.close();
    }


    /*
        This is a helper method for the testMoviesTable quiz. You can move your
        code from testCategoryTable to here so that you can call this code from both
        testMoviesTable and testCategoryTable.
     */
    public long insertCategory() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createTestCategoryValues if you wish)
        ContentValues testValues = TestUtilities.createTestCategoryValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long categoryRowId;
        categoryRowId = db.insert(MoviesContract.CategoryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(categoryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.CategoryEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from category query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: category Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from category query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return categoryRowId;
    }
}
