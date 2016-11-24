package app.com.example.althomas04.moviezone.Main;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import app.com.example.althomas04.moviezone.R;

/**
 * Created by al.thomas04.
 */


public class MovieAdapter extends CursorAdapter {

    private static final String TMDB_BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w300";

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Find the fields to populate in inflated template
        SimpleDraweeView posterImageView = (SimpleDraweeView) view.findViewById(R.id.movie_poster_view);
        TextView titleTextView = (TextView) view.findViewById(R.id.movie_title);

        //Extract properties from the cursor
        String posterPath = cursor.getString(MainActivity.COL_MOVIE_POSTER_PATH);
        String movieTitle = cursor.getString(MainActivity.COL_MOVIE_TITLE);

        //
        if(!posterPath.equals("null")){
            posterImageView.setVisibility(View.VISIBLE);
            titleTextView.setVisibility(View.GONE);
            //Form the complete TMDB image url
            String posterImageUrl = TMDB_BASE_IMAGE_URL + posterPath;
            Uri uri = Uri.parse(posterImageUrl);
            //Populate fields with the extracted properties using fresco.
            posterImageView.setImageURI(uri);
        } else {
            posterImageView.setVisibility(View.GONE);
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(movieTitle);
        }
    }

}

