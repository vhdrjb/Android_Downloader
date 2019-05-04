package ir.quera.android.downloader.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

import io.reactivex.CompletableEmitter;

public class DownloadThreadFactory {
    private final static String REQUEST_METHOD = "HEAD";
    private final static int BLOCK_SIZE = 1024;
    private final static String ACCEPT_ENCODING = "Accept-Encoding";
    private final static String ENCODING = "identity";

    /**
     * download the file in the single thread
     * if multi thread downloading is not supported by server
     * it will download the file with single thread
     *
     * @return instance of downloader with appropriate headers
     */
    public static DownloadingThreadPool singleThreadDownload(String urlPath, String dst, CompletableEmitter emitter) throws IOException {
        URL url = new URL(urlPath);
        DownloadingThreadPool threadPoolExecutor = new DownloadingThreadPool(1, emitter);
        HttpURLConnection httpURLConnection = createConnection(url);
        long lenght = httpURLConnection.getContentLength();
        DownloadTask downloadTask = new DownloadTask(url, 0L, lenght, dst);
        downloadTask.run();
        return threadPoolExecutor;
    }

    /**
     * download the file by splitting the file download task into
     * separated threads
     *
     * @param threads number of threads
     * @param emitter
     * @return instance of downloader with appropriate headers
     */
    public static DownloadingThreadPool mulitThreadDownload(String urlPath, int threads, String dst, CompletableEmitter emitter) throws IOException {
        URL url = new URL(urlPath);

        DownloadingThreadPool threadPoolExecutor = new DownloadingThreadPool(threads,emitter);
        HttpURLConnection httpURLConnection = createConnection(url);
        long length = httpURLConnection.getContentLength();
        long beginBytes = 0;
        while (beginBytes < length) {
            long endByte;
            if (beginBytes + BLOCK_SIZE <= length) {
                endByte = beginBytes + BLOCK_SIZE;
            } else {
                endByte = length;
            }
            DownloadTask downloadTask = new DownloadTask(url, beginBytes, endByte, dst);
            threadPoolExecutor.submit(downloadTask);
            beginBytes = endByte;
        }
        return threadPoolExecutor;
    }
    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod(REQUEST_METHOD);
        httpURLConnection.setRequestProperty(ACCEPT_ENCODING, ENCODING);
        return httpURLConnection;
    }

}
