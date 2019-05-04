package ir.quera.android.downloader.download;

public interface Downloader {
    void cancelDownload();

    void startDownload(String url,String dst
    );
}
