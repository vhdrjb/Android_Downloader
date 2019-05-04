package ir.quera.android.downloader.download;

public interface DownloadStateListener {
    void onDownloadStart();

    void onDownloadProgress();

    void onDownloadFinished();

    void onDownloadCancel();

    void onDownloadFailure(String message);
}
