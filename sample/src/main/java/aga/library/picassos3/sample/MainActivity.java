package aga.library.picassos3.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.squareup.picasso.Picasso;

import aga.library.picassos3.S3Downloader;

public class MainActivity extends AppCompatActivity {
    private static final String BUCKET = "YOUR-BUCKET";
    private static final String ACCESS_KEY = "YOUR-ACCESS-KEY";
    private static final String SECRET_KEY = "YOUR-SECRET-KEY";
    private static final String IMAGE_PATH = "YOUR-IMAGE-PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView image = (ImageView) findViewById(R.id.image);

        new Picasso.Builder(getApplicationContext())
            .downloader(new S3Downloader(
                getApplicationContext(),
                getS3Client(),
                BUCKET
            ))
            .build()
            .load(getImagePath())
            .into(image);
    }

    private String getImagePath() {
        return IMAGE_PATH;
    }

    private AmazonS3Client getS3Client() {
        return new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
    }
}
