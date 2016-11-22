package app.com.example.althomas04.moviezone;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.com.example.althomas04.moviezone.Data.MoviesContract;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String MOVIEZONE_SHARE_HASHTAG = " #MovieZoneApp";

    private ShareActionProvider mShareActionProvider;

    private boolean isFavorited;
    private final long FAVORITES_CAT_KEY = 1;

    private static final String TMDB_BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w780";

    private static final String MOVIE_ID_URI_KEY = "movie_id_uri_key";

    private Uri movieIdUri;
    private LikeButton favButton;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT,
            // This works because the MoviesProvider returns category data joined with
            // movie data, even though they're stored in two different tables.
            MoviesContract.CategoryEntry.COLUMN_CATEGORY_PARAM
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
// must change.
    public static final int COL_MOVIE_ENTRY_ID = 0;
    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_ID = 2;
    public static final int COL_MOVIE_POSTER_PATH = 3;
    public static final int COL_MOVIE_OVERVIEW = 4;
    public static final int COL_MOVIE_RELEASE_DATE = 5;
    public static final int COL_MOVIE_BACKDROP_PATH = 6;
    public static final int COL_MOVIE_VOTE_AVERAGE = 7;
    public static final int COL_MOVIE_VOTE_COUNT = 8;
    public static final int COL_CATEGORY_PARAM = 9;

    private String title;
    private int movieId;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private String backdropPath;
    private float voteAverage;
    private int voteCount;

    private Toolbar mToolbar;
    private TextView mReleaseDateView;
    private TextView mOverviewView;
    private ImageView mBackdropImageView;
    private RatingBar mVoteAverageView;
    private TextView mVoteCountView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        movieIdUri = Uri.parse(extras.getString(MOVIE_ID_URI_KEY));


        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        isFavorited = false;

        mToolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        mBackdropImageView = (ImageView) findViewById(R.id.expandedImage);
        mReleaseDateView = (TextView) findViewById(R.id.release_date);
        mOverviewView = (TextView) findViewById(R.id.plot_overview);
        mVoteAverageView = (RatingBar) findViewById(R.id.vote_average);
        mVoteCountView = (TextView) findViewById(R.id.vote_count);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuShareItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuShareItem);
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }

        MenuItem menuFavoriteItem = menu.findItem(R.id.action_favorite);
        favButton = (LikeButton) MenuItemCompat.getActionView(menuFavoriteItem);
        isFavorited = readState();
        if (isFavorited) {
            favButton.setLiked(true);
        } else {
            favButton.setLiked(false);
        }

        favButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MoviesEntry.COLUMN_CAT_KEY, FAVORITES_CAT_KEY);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, backdropPath);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_COUNT, voteCount);

                getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValues);
                Toast.makeText(getApplicationContext(), "Favorite Added", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void unLiked(LikeButton likeButton) {
                getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ? AND " + MoviesContract.MoviesEntry.COLUMN_CAT_KEY + " = ?",
                        new String[]{Integer.toString(movieId), Long.toString(FAVORITES_CAT_KEY)});
//                isFavorited = false;
                Toast.makeText(getApplicationContext(), "Favorite Deleted", Toast.LENGTH_SHORT).show();

            }
        });

        return true;
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, MOVIEZONE_SHARE_HASHTAG);
        return shareIntent;
    }


    private boolean readState() {
        // Check if a movie with this id exists in the favorites cursor
        Cursor favoritesCursor = getContentResolver().query(
                MoviesContract.MoviesEntry.CONTENT_URI,
                new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_ID},
                MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ? AND " + MoviesContract.MoviesEntry.COLUMN_CAT_KEY + " = ?",
                new String[]{Integer.toString(movieId), Long.toString(FAVORITES_CAT_KEY)},
                null);
        if (favoritesCursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Cutom up button returns to the parent activity without restarting the activity (goes to its previous state).
        if (item.getItemId() == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(DetailActivity.this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(DetailActivity.this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != movieIdUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    this,
                    movieIdUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            /*Extract properties from the cursor and populate their
            respective views with the extracted properties.*/
            title = data.getString(COL_MOVIE_TITLE);
            movieId = data.getInt(COL_MOVIE_ID);
            posterPath = data.getString(COL_MOVIE_POSTER_PATH);
            overview = data.getString(COL_MOVIE_OVERVIEW);
            releaseDate = data.getString(COL_MOVIE_RELEASE_DATE);
            backdropPath = data.getString(COL_MOVIE_BACKDROP_PATH);
            voteAverage = data.getFloat(COL_MOVIE_VOTE_AVERAGE);
            voteCount = data.getInt(COL_MOVIE_VOTE_COUNT);

            //Image for the collapsible actionbar
            if (backdropPath.equals("null")) {
                backdropPath = posterPath;
            }
            //Form the complete TMDB image url
            String actionbarImageUrl = TMDB_BASE_IMAGE_URL + backdropPath;
            Picasso.with(this)
                    .load(actionbarImageUrl)
                    .into(mBackdropImageView);

            //Title for the collapsible actionbar
            this.setSupportActionBar(mToolbar);
            ActionBar actionBar = this.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(title);
            }

            //Release Date view info
            Date convertedDate = convertDate(releaseDate);
            String formattedDate = formatDate(convertedDate);
            mReleaseDateView.setText(formattedDate);

            //Plot Overview view info
            mOverviewView.setText(overview);

            //Rating Bar view info
            mVoteAverageView.setRating((voteAverage*5)/10); //converts it to 5 star average

            //Vote count info
            String userVoteCount = "(" + voteCount + " Votes)";
            mVoteCountView.setText(userVoteCount);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Convert Timed Date string into "yyyy-MM-dd" format.
     */
    private Date convertDate(String timedDateObject) {
        SimpleDateFormat timedDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date convertedDate = new Date();
        try {
            convertedDate = timedDateFormat.parse(timedDateObject);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        return dateFormatter.format(dateObject);
    }

}