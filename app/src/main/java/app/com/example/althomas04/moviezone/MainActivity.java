package app.com.example.althomas04.moviezone;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.com.example.althomas04.moviezone.Data.MoviesContract;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private MovieAdapter mMovieAdapter;

    private NetworkInfo mNetworkStatus;

    public String categoryParam = "popular";

    public int pageParam = 1;

    public Cursor mLoadedCursor;

    private GridView mGridView;

    //TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    //Progress bar that is displayed while the list is being populated
    private ProgressBar mLoadingSpinnerView;

    private static final String MOVIE_ID_URI_KEY = "movie_id_uri_key";

    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int MOVIE_LOADER_ID = 0;
    // For the movies grid view we're showing only a small subset (poster image) of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the category & movies tables in the background
            // (both have an _id column)
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ENTRY_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_ID = 2;
    static final int COL_MOVIE_POSTER_PATH = 3;
    static final int COL_CATEGORY_PARAM = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ProgressBar} in the layout
        mLoadingSpinnerView = (ProgressBar) findViewById(R.id.loading_spinner);

        // Find a reference to the {@link TextView} in the layout
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_state_Text_View);

        /*insertTestData();*/
        startLoader();

        // The MovieAdapter will take data from a source and
        // use it to populate the GridView it's attached to.
        mMovieAdapter = new MovieAdapter(this, null, 0);

        // Get a reference to the GridView, and attach this adapter to it.
        mGridView = (GridView) findViewById(R.id.grid_view_main);
        mGridView.setAdapter(mMovieAdapter);

        //Set ScrollListener on to gridview to automatically load a new page of movies (query the new page)
        //when user reaches the end of current page/list.
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0) {
                    mNetworkStatus = checkConnectionStatus();
                    if(mNetworkStatus != null && mNetworkStatus.isConnected() && !categoryParam.equals("favorites")) {
                        loadNewPage();
                    }
                }
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Uri movieIdUri = MoviesContract.MoviesEntry.buildMoviesCategoryWithMovieId(
                            categoryParam, cursor.getInt(COL_MOVIE_ID));
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(MOVIE_ID_URI_KEY, movieIdUri.toString());
                    startActivity(intent);
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        // Set the empty view on to the screen if movies list is empty.
        mGridView.setEmptyView(mEmptyStateTextView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_most_popular:
                categoryParam = "popular";
                onCategoryChanged();
                return true;
            case R.id.action_top_rated:
                categoryParam = "top_rated";
                onCategoryChanged();
                return true;
            case R.id.action_now_playing:
                categoryParam = "now_playing";
                onCategoryChanged();
                return true;
            case R.id.action_upcoming:
                categoryParam = "upcoming";
                onCategoryChanged();
                return true;
            case R.id.action_favorites:
                categoryParam = "favorites";
                onCategoryChanged();
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected grid item needs to be saved.
        // When no item is selected, mPosition will be set to GridView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void startLoader() {
        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();
        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
    }

    public void onCategoryChanged() {
        //Reset page parameter
        pageParam = 1;
        //Restart the loader with new category param
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        //Reset the savedInstanceState value to 0 to prevent the new gridview from automatically
        //scrolling to an item using the previously saved position.
        mPosition = 0;
        //Set the loading spinner to reappear when reloading view.
        mLoadingSpinnerView.setVisibility(View.VISIBLE);
    }

    private void loadNewPage(){
        int cursorCount = mLoadedCursor.getCount();
        pageParam = (cursorCount/20)+1;
        //Restart the loader with new page param
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

    }

    private NetworkInfo checkConnectionStatus(){
        //Checks network connection status
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.
        mNetworkStatus = checkConnectionStatus();
        if (mNetworkStatus != null && mNetworkStatus.isConnected() && !categoryParam.equals("favorites")) {
            //If network connection exists and the category selected is not "favorites", request & return the new/updated data
            return new MovieLoader(this, categoryParam, pageParam, MOVIE_COLUMNS);
        } else {
            //If network connection does not exist, return the previously stored data for the select category
            Uri moviesInCategoryUri = MoviesContract.MoviesEntry.buildMoviesCategory(categoryParam);
            return new CursorLoader(this,
                    moviesInCategoryUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLoadedCursor = data;
        mMovieAdapter.swapCursor(data);

        // Set empty state text to display "No Movies found."
        mEmptyStateTextView.setText(R.string.no_movies);

        //Set the loading spinner to disappear after the loading has finished.
        mLoadingSpinnerView.setVisibility(View.GONE);

        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.setSelection(mPosition);
        }
//        Log.d(LOG_TAG, "");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mMovieAdapter.swapCursor(null);
    }
}