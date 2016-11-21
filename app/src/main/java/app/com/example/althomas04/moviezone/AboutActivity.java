package app.com.example.althomas04.moviezone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by al.thomas04.
 */

public class AboutActivity extends AppCompatActivity {

    //Email recipient for contact support
    private static final String[] CONTACT_EMAIL_RECIPIENTS = {"al.thomas04@gmail.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        //Set version name according to the gradle build file
        TextView versionNumberTextView = (TextView) findViewById(R.id.version_number);
        versionNumberTextView.setText(BuildConfig.VERSION_NAME);
    }


    //Contact Support request intent
    public void onContactSupportClick(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, CONTACT_EMAIL_RECIPIENTS);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_email_body));

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
