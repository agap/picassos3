package aga.library.picassos3;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.squareup.picasso.Downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created on 15.04.16
 * @author artem
 */
public final class S3Downloader implements Downloader {
    private static final String TAG = "S3Downloader";

    private final TransferUtility transferUtility;

    private final DiskCache cache;

    private final String bucket;
    private final String slashedBucket;

    private final Context context;

    public S3Downloader(@NonNull final Context context, @NonNull final AmazonS3Client client, @NonNull final String bucket) {
        this.transferUtility = new TransferUtility(client, context.getApplicationContext());

        this.bucket  = bucket;
        this.cache   = new DiskCache(context.getApplicationContext());
        this.context = context.getApplicationContext();

        this.slashedBucket = "/" + bucket + "/";
    }

    @Override
    public Response load(@NonNull final Uri uri, int networkPolicy) throws IOException {
        if (cache.contains(uri)) {
            final InputStream in = cache.get(uri);

            if (in != null) {
                return new Response(in, true, cache.getSize(uri));
            }
        }

        final File file  = CacheUtils.getTemporaryCacheFile(context);
        final String key = uri.getPath().replace(slashedBucket, "");

        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            final TransferObserver observer = transferUtility.download(bucket, key, file);

            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) { }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    if (bytesCurrent == bytesTotal) {
                        try {
                            cache.put(uri, new FileInputStream(file));
                        } catch (FileNotFoundException ignored) {

                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                }

                @Override
                public void onError(int id, Exception ex) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();

            return new Response(new FileInputStream(file), false, file.length());
        } catch (InterruptedException ignored) {
            Log.e(TAG, ignored.toString());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return null;
    }

    @Override
    public void shutdown() { }
}
