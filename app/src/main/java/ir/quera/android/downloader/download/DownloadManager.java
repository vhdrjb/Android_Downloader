package ir.quera.android.downloader.download;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.URL;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DownloadManager implements Downloader {
    private DownloadingThreadPool downloadingThreadPool;
    private final DownloadStateListener stateListener;
    private static DownloadManager INSTANCE;
    private final static int THEAD_SIZE = 4;

    public static DownloadManager getInstance(DownloadStateListener downloadStateListener) {
        if (INSTANCE == null) {
            INSTANCE = new DownloadManager(downloadStateListener);
        }
        return INSTANCE;
    }

    private DownloadManager(DownloadStateListener stateListener) {
        this.stateListener = stateListener;
    }

    @Override
    public void cancelDownload() {
        if (downloadingThreadPool != null) {
            downloadingThreadPool.cancelDownload();
            stateListener.onDownloadCancel();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void startDownload(String url, String dst) {
        stateListener.onDownloadProgress();
        Completable.create(emitter -> {
            try {
                if (ServerCompatibilityUtils.isMultiThreadSupport(new URL(url))) {
                    downloadingThreadPool = DownloadThreadFactory.mulitThreadDownload(url, THEAD_SIZE, dst, emitter);
                } else {
                    downloadingThreadPool = DownloadThreadFactory.singleThreadDownload(url, dst,emitter);
                }
                emitter.onComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(stateListener::onDownloadFinished, throwable -> {
                    stateListener.onDownloadFailure(throwable.getMessage());
                });

    }
}
