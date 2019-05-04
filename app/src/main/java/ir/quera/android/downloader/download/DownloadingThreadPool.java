package ir.quera.android.downloader.download;

import android.arch.core.BuildConfig;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.CompletableEmitter;

public class DownloadingThreadPool extends ThreadPoolExecutor {
    private final static int MAX_THREADS_SIZE = 20;
    private final static String THREAD_EXECUTION_EXCEPTION_LOG = "thread exec exception";
    private final static String THREAD_EXECUTION_LOG = "thread exec";
    private final static String THREAD_EXECUTION_MESSAGE = "%d successful";
    private final static String THREAD_EXECUTION_EXCEPTION_MESSAGE_STYLE = "%s error at %d";
    private ConcurrentHashMap<Long, Future> downloadTask = new ConcurrentHashMap<>();
    private CompletableEmitter emitter;

    public DownloadingThreadPool(int corePoolSize, CompletableEmitter emitter) {
        super(corePoolSize, MAX_THREADS_SIZE, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        this.emitter = emitter;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable throwable) {
        super.afterExecute(r, throwable);
        long id = Thread.currentThread().getId();
        if (throwable != null) {
            if (BuildConfig.DEBUG) {
                Log.e(THREAD_EXECUTION_EXCEPTION_LOG, String.format(THREAD_EXECUTION_EXCEPTION_MESSAGE_STYLE,
                        throwable.getMessage(), id));
                throwable.printStackTrace();
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.i(THREAD_EXECUTION_LOG, String.format(THREAD_EXECUTION_MESSAGE, id));
            }
        }
        Set<Long> keySet = downloadTask.keySet();
        for (Long key : keySet) {
            Future future = downloadTask.get(key);
            if (future != null && future.isDone()) {
                downloadTask.remove(key);
            }
        }
        if (downloadTask.size() == 0) {
            emitter.onComplete();
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        Future<?> submit = super.submit(task);
        if (task instanceof DownloadTask) {
            DownloadTask currentTask = ((DownloadTask) task);
            downloadTask.put(currentTask.getID(), submit);
        }
        return submit;
    }

    public void cancelDownload() {
        Set<Long> keySet = downloadTask.keySet();
        for (Long key : keySet) {
            Future future = downloadTask.remove(key);
            if (!future.isCancelled() || !future.isDone()) {
                future.cancel(true);
            }
        }
    }
}
