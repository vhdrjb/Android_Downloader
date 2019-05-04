package ir.quera.android.downloader.download;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Runnable {
    private final static int BUFFER_SIZE = 1024;
    private final URL url;
    private final Long ID;
    private final Long byteDownload;
    private final Long endByte;
    private final String path;
    public DownloadTask(URL url, Long byteDownload, Long endByte, String dst) {
        this.url = url;
        this.byteDownload = byteDownload;
        ID = byteDownload / 1024;
        this.endByte = endByte;
        this.path = dst;
    }

    public Long getID() {
        return ID;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Range", "bytes=" + byteDownload + "-" + endByte);
            RandomAccessFile fileWriter = new RandomAccessFile(path, "rw");
            fileWriter.seek(byteDownload);
            byte[] bfr = new byte[BUFFER_SIZE];
            long bytes = byteDownload;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(httpConnection.getInputStream());
            while (bytes < endByte) {
                int length = bufferedInputStream.read(bfr, 0, BUFFER_SIZE);
                if (length != -1) {
                    fileWriter.write(bfr, 0, length);
                    bytes += length;
                }
            }
            bufferedInputStream.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
