package app.com.example.althomas04.moviezone.Main;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import app.com.example.althomas04.moviezone.Data.MoviesContract;
import app.com.example.althomas04.moviezone.Detail.DetailActivity;

/**
 * Created by al.thomas04.
 */

public class MovieCursorLoader extends AsyncTaskLoader<Cursor> {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private String mCategoryParam;
    private int mPageParam;
    Context mContext;
    String[] mMovieCol;

    public MovieCursorLoader(Context context, String categoryParam, int pageParam, String[] movieCol) {
        super(context);
        mContext = context;
        mCategoryParam = categoryParam;
        mPageParam = pageParam;
        mMovieCol = movieCol;

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        String favoritesParam = "favorites";

        if (mCategoryParam == null) {
            return null;
        }
        //Insert favorites category into category table
        Long catid = MainQueryUtils.addCategory(mContext, favoritesParam);

        //Perform the network request, parse the response, and store the response into the database.
        MainQueryUtils.fetchMovieData(mContext, mCategoryParam, mPageParam);

        //Creates a URI for the specific category
        Uri moviesInCategoryUri = MoviesContract.MoviesEntry.buildMoviesCategory(mCategoryParam);

        //Creates a cursor with the new data and returns it to the loader
        Cursor cursor = mContext.getContentResolver().query(moviesInCategoryUri,
                mMovieCol,
                null,
                null,
                null);
        return cursor;

    }
}