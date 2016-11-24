package app.com.example.althomas04.moviezone.Detail;

/**
 * Created by al.thomas04.
 */

public class ReviewsData {
    private String mAuthor;
    private String mContent;

    public ReviewsData(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }
}
