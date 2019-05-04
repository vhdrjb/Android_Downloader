package ir.quera.android.downloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ir.quera.android.downloader.download.DownloadManager;
import ir.quera.android.downloader.download.DownloadStateListener;
import ir.quera.android.downloader.download.DownloadThreadFactory;
import ir.quera.android.downloader.download.Downloader;
import ir.quera.android.downloader.download.DownloadingThreadPool;
import ir.quera.android.downloader.download.ServerCompatibilityUtils;


public class MainActivity extends AppCompatActivity implements DownloadStateListener {
// img : https://s0.2mdn.net/9347156/728x90_Stack_Overflow_v2.png
    private final static String DOWNLOADER_LOG = "downloader";
    private final static String DOWNLOAD_START_MESSAGE = "download started";
    private final static int PERMISSION_EXTERNAL_CHECK_REQUEST = 102;
    private EditText urlField;
    private Downloader downloader;
    private boolean externalPermission = false;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkExternalPermission();
        urlField = findViewById(R.id.download_link_input);
        progressBar = findViewById(R.id.progress_circular);
    }

    private void checkExternalPermission() {
        externalPermission = ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!externalPermission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessage(R.string.external_storage_needed);
            }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_EXTERNAL_CHECK_REQUEST);
            }
    }

    public void onDownloadClicked(View view) {
        if (externalPermission) {

            String url = urlField.getText().toString();
            String dst = getDestination(url);
            downloader = DownloadManager.getInstance(this);
            downloader.startDownload(url, dst);
        }else {
            checkExternalPermission();
        }
    }

    @Override
    public void onDownloadStart() {
        JudgeUtil.isDownloadCompleted = false;
        Log.e(DOWNLOADER_LOG, DOWNLOAD_START_MESSAGE);
    }

    @Override
    public void onDownloadProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDownloadFinished() {
        JudgeUtil.isDownloadCompleted = true;
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Download Finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDownloadCancel() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadFailure(String message) {
        Log.e(DOWNLOADER_LOG, message);
        progressBar.setVisibility(View.GONE);
    }

    public String getDestination(String url) {
        String name = new File(url).getName();
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                + File.separator + name;

    }

    protected void showMessage(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_EXTERNAL_CHECK_REQUEST) {
            externalPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }
}
