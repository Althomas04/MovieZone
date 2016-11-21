package app.com.example.althomas04.moviezone;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import app.com.example.althomas04.moviezone.Data.MoviesContract;

/**
 * Created by al.thomas04.
 */

public class MovieLoader extends AsyncTaskLoader<Cursor> {


    private String mCategoryParam;
    private int mPageParam;
    Context mContext;
    String[] mMovieCol;

    public MovieLoader(Context context, String categoryParam, int pageParam, String[] movieCol) {
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

        if (mCategoryParam == "favorites") {
            QueryUtils.addCategory(mContext, mCategoryParam);
            return null;
        }
        //Perform the network request, parse the response, and store the response into the database.
        QueryUtils.fetchMovieData(mContext, mCategoryParam, mPageParam);

        //Creates a cursor with the new data and returns it to the loader
        Uri moviesForCategoryUri = MoviesContract.MoviesEntry.buildMoviesCategory(mCategoryParam);

        Cursor cursor = mContext.getContentResolver().query(moviesForCategoryUri,
                mMovieCol,
                null,
                null,
                null);
        return cursor;

    }
}