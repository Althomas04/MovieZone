package app.com.example.althomas04.moviezone.Detail;

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
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String MOVIEZONE_SHARE_HASHTAG = " #MovieZoneApp";

    private ShareActionProvider mShareActionProvider;

    private boolean isFavorited;
    private final long FAVORITES_CAT_KEY = 1;

    private static final String TMDB_BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w780";
    private static final String YOUTUBE_BASE_TRAILER_URL = "https://www.youtube.com/watch?v=";

    private static final String MOVIE_ID_URI_KEY = "movie_id_uri_key";

    private Uri movieIdUri;
    private LikeButton favButton;

    private static final int DETAIL_CURSOR_LOADER = 0;
    private static final int DETAIL_LIST_LOADER = 1;

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
    private TextView mGenreView;
    private TextView mLanguageView;
    private TextView mRuntimeView;
    private TextView mReleaseDateView;
    private TextView mOverviewView;
    private SimpleDraweeView mBackdropImageView;
    private RatingBar mVoteAverageView;
    private TextView mVoteCountView;
    private SimpleDraweeView mTrailerView;
    private TextView mEmptyTrailerView;
    private TextView mReviewsView;
    TextView mEmptyReviewsView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        movieIdUri = Uri.parse(extras.getString(MOVIE_ID_URI_KEY));


        getLoaderManager().initLoader(DETAIL_CURSOR_LOADER, null, cursorLoaderListener);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        mBackdropImageView = (SimpleDraweeView) findViewById(R.id.expandedImage);
        mGenreView = (TextView) findViewById(R.id.genre);
        mLanguageView = (TextView) findViewById(R.id.language);
        mRuntimeView = (TextView) findViewById(R.id.runtime);
        mReleaseDateView = (TextView) findViewById(R.id.release_date);
        mOverviewView = (TextView) findViewById(R.id.plot_overview);
        mVoteAverageView = (RatingBar) findViewById(R.id.vote_average);
        mVoteCountView = (TextView) findViewById(R.id.vote_count);
        mTrailerView = (SimpleDraweeView) findViewById(R.id.trailer_view);
        mEmptyTrailerView = (TextView) findViewById(R.id.empty_trailer);
        mReviewsView = (TextView) findViewById(R.id.reviews_view);
        mEmptyReviewsView = (TextView) findViewById(R.id.empty_reviews);
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

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            if (null != movieIdUri) {
                // Now create and return a MovieCursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(
                        DetailActivity.this,
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
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {

            /*Extract properties from the cursor and populate their
            respective views with the extracted properties.*/
                title = cursor.getString(COL_MOVIE_TITLE);
                movieId = cursor.getInt(COL_MOVIE_ID);
                posterPath = cursor.getString(COL_MOVIE_POSTER_PATH);
                overview = cursor.getString(COL_MOVIE_OVERVIEW);
                releaseDate = cursor.getString(COL_MOVIE_RELEASE_DATE);
                backdropPath = cursor.getString(COL_MOVIE_BACKDROP_PATH);
                voteAverage = cursor.getFloat(COL_MOVIE_VOTE_AVERAGE);
                voteCount = cursor.getInt(COL_MOVIE_VOTE_COUNT);

                //Image for the collapsible actionbar
                if (backdropPath.equals("null")) {
                    backdropPath = posterPath;
                }
                //Form the complete TMDB image url and set it using the fresco library
                String actionbarImageUrl = TMDB_BASE_IMAGE_URL + backdropPath;
                Uri uri = Uri.parse(actionbarImageUrl);
                mBackdropImageView.setImageURI(uri);

                //Title for the collapsible actionbar
                DetailActivity.this.setSupportActionBar(mToolbar);
                ActionBar actionBar = DetailActivity.this.getSupportActionBar();
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
                mVoteAverageView.setRating((voteAverage * 5) / 10); //converts it to 5 star average

                //Vote count info
                String userVoteCount = "(" + voteCount + " Votes)";
                mVoteCountView.setText(userVoteCount);

                //Close the cursor and initialize the new loader on finish.
                cursor.close();
                getLoaderManager().initLoader(DETAIL_LIST_LOADER, null, listLoaderListener);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<ExtraMovieData>> listLoaderListener = new LoaderManager.LoaderCallbacks<ArrayList<ExtraMovieData>>() {

        @Override
        public Loader<ArrayList<ExtraMovieData>> onCreateLoader(int i, Bundle bundle) {
            if (movieId > 0) {
                Toast.makeText(DetailActivity.this, "works movieId = " + movieId, Toast.LENGTH_LONG);

                // Create a new loader for the given
                return new ExtraMovieListLoader(DetailActivity.this, movieId);
            } else {
                Toast.makeText(DetailActivity.this, "movieId = " + movieId, Toast.LENGTH_LONG);
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<ExtraMovieData>> loader, ArrayList<ExtraMovieData> extraMovieData) {
            //Extract all the movieinfo from the arraylist
            if (extraMovieData.size() == 0) {
                CardView movieInfoCardOne = (CardView) findViewById(R.id.movie_info_card_one);
                movieInfoCardOne.setVisibility(GONE);

                mTrailerView.setVisibility(GONE);
                mEmptyTrailerView.setVisibility(VISIBLE);

                mReviewsView.setVisibility(GONE);
                mEmptyReviewsView.setVisibility(VISIBLE);

                return;
            }
            ExtraMovieData extramovieInfo = (ExtraMovieData) extraMovieData.get(0);

            ArrayList<String> genres = extramovieInfo.getGenres();
            mGenreView.setText("");
            //Extract genres from the arraylist, and display only three genres max.
            for (int i = 0; i < genres.size() && i < 3; i++) {
                if (i == genres.size() - 1 || i == 2) {
                    mGenreView.append(genres.get(i)); //No comma after the last item
                } else {
                    mGenreView.append(genres.get(i) + ", ");
                }
            }

            //Extract and display runtime
            int runtime = extramovieInfo.getRuntime();
            mRuntimeView.setText(Integer.toString(runtime) + " min");

            //Extract and display language
            String language = extramovieInfo.getLanguage();
            mLanguageView.setText(language);

            //Extract trailer path
            String trailerPath = extramovieInfo.getTrailerPath();
            if (!trailerPath.equals(null)) {
                //Create a complete youtube trailer url.
                String trailerUrl = YOUTUBE_BASE_TRAILER_URL + trailerPath;
                final Uri trailerUri = Uri.parse(trailerUrl);
                //Create a thumbnail url with the trailer path and parse it for the fresco image loader.
                String trailerThumbnailPath = String.format("http://img.youtube.com/vi/%1$s/0.jpg", trailerPath);
                Uri trailerThumbnailUri = Uri.parse(trailerThumbnailPath);
                mTrailerView.setImageURI(trailerThumbnailUri);
                //Set a clickListener on the thumbnail, and create a trailer video view intent.
                mTrailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, trailerUri);
                        startActivity(intent);
                    }
                });
            } else {
                mTrailerView.setVisibility(GONE);
                mEmptyTrailerView.setVisibility(VISIBLE);
            }

            ArrayList<ReviewsData> reviewsList = extramovieInfo.getReviews();
            if (reviewsList.size() != 0) {
                mReviewsView.setText("");
                for (int i = 0; i < reviewsList.size(); i++) {
                    ReviewsData reviews = reviewsList.get(i);
                    String reviewAuthor = reviews.getAuthor();
                    String reviewContent = reviews.getContent();
                    mReviewsView.append(reviewAuthor + "\n");
                    mReviewsView.append(reviewContent + "\n\n");
                }
            } else {
                mReviewsView.setVisibility(GONE);
                mEmptyReviewsView.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<ExtraMovieData>> loader) {

        }
    };

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