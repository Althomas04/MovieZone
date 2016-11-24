package app.com.example.althomas04.moviezone.Detail;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by al.thomas04.
 */

public class ExtraMovieListLoader extends AsyncTaskLoader<ArrayList<ExtraMovieData>> {

    int mMovieIdParam;

    public ExtraMovieListLoader(Context context, int movieIdParam) {
        super(context);
        mMovieIdParam = movieIdParam;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<ExtraMovieData> loadInBackground() {
        if (mMovieIdParam <= 0) {
            return null;
        }
        // Perform the network request, parse the response, and extract the extra movie info
        ArrayList<ExtraMovieData> extraMovieInfo = DetailQueryUtils.fetchExtraMovieData(mMovieIdParam);
        return extraMovieInfo;
    }
}


