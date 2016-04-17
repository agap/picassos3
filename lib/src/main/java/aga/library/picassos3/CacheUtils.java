package aga.library.picassos3;

import android.content.Context;
import android.os.StatFs;

import java.io.File;

/**
 * Created on 15.04.16
 * @author artem
 */
final class CacheUtils {
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private static final String AMAZON_CACHE = "amazon_cache";

    static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            final StatFs statFs = new StatFs(dir.getAbsolutePath());
            //noinspection deprecation
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) { }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    static File createDefaultCacheDir(Context context) {
        final File cache = new File(context.getApplicationContext().getCacheDir(), AMAZON_CACHE);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        return cache;
    }

    static File getTemporaryCacheFile(Context context) {
        return new File(context.getCacheDir(), System.currentTimeMillis() + ".png");
    }
}
