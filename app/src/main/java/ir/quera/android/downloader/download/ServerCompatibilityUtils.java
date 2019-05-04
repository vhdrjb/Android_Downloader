package ir.quera.android.downloader.download;

import android.net.Uri;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerCompatibilityUtils {
    public static boolean isMultiThreadSupport(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        return httpURLConnection.getHeaderField("Accept-Ranges").equals("bytes");
    }
}
