package app.com.example.althomas04.moviezone.Detail;

import java.util.ArrayList;

/**
 * Created by al.thomas04 on 11/22/2016.
 */

public final class ExtraMovieData {

    private ArrayList<String> mGenres;
    private int mRuntime;
    private String mLanguage;
    private String mTrailerPath;
    private ArrayList<ReviewsData> mReviews;

    public ExtraMovieData(ArrayList<String> genres, int runtime, String language, String trailerPath, ArrayList<ReviewsData> reviews) {
        mGenres = genres;
        mRuntime = runtime;
        mLanguage = language;
        mTrailerPath = trailerPath;
        mReviews = reviews;
    }

    public ArrayList<String> getGenres() {
        return mGenres;
    }

    public int getRuntime() {
        return mRuntime;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public String getTrailerPath() {
        return mTrailerPath;
    }

    public ArrayList<ReviewsData> getReviews() {
        return mReviews;
    }
}
