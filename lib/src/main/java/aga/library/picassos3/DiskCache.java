package aga.library.picassos3;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 15.04.16
 * @author artem
 */
final class DiskCache {
    private static final String TAG = "DiskCache";

    @Nullable
    private DiskLruCache cache;

    public DiskCache(@NonNull final Context context) {
        final File cacheDir = CacheUtils.createDefaultCacheDir(context);
        try {
            cache = DiskLruCache.open(cacheDir, 1, 1, CacheUtils.calculateDiskCacheSize(cacheDir));
        } catch (IOException e) {
            logError(e);
            cache = null;
        }
    }

    private void logError(@NonNull final Exception e) {
        Log.e(TAG, e.toString());
    }

    public long getSize(@NonNull final Uri uri) {
        if (cache == null) {
            return 0;
        }

        DiskLruCache.Snapshot snapshot = null;

        try {
            snapshot = cache.get(toKey(uri));

            return snapshot.getLength(0);
        } catch (IOException e) {
            logError(e);
            return 0;
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    public boolean contains(@NonNull final Uri uri) {
        if (cache == null) {
            return false;
        }

        DiskLruCache.Snapshot snapshot = null;

        try {
            snapshot = cache.get(toKey(uri));

            return snapshot != null;
        } catch (IOException e) {
            logError(e);
            return false;
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    @Nullable
    public InputStream get(@NonNull final Uri uri) {
        if (cache == null) {
            return null;
        }

        DiskLruCache.Snapshot snapshot = null;

        try {
            snapshot = cache.get(toKey(uri));

            return snapshot.getInputStream(0);
        } catch (IOException e) {
            logError(e);
            return null;
        } finally {
           // if (snapshot != null) {
          //     snapshot.close();
          //  }
        }
    }

    public void put(@NonNull final Uri uri, @NonNull final InputStream in) {
        if (cache == null) {
            return;
        }

        try {
            final DiskLruCache.Editor editor = cache.edit(toKey(uri));

            if (editor == null) {
                return;
            }

            final OutputStream out = editor.newOutputStream(0);

            writeStreamContent(in, out);

            cache.flush();
            editor.commit();
        } catch (IOException e) {
            logError(e);
        }
    }

    private void writeStreamContent(@NonNull final InputStream in, @NonNull final OutputStream out) {
        try {
            final byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logError(e);
        }
    }

    private String toKey(@NonNull final Uri uri) {
        final String key = uri.getLastPathSegment().toLowerCase();
        final String cleanKey = key.replaceAll("[^a-zA-Z0-9]", "");
        final int length = cleanKey.length();

        return length <= 120 ? cleanKey : cleanKey.substring(length - 120, length);
    }
}
