package com.beloushkin.android.learn.fileloadertest;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private String appName;
    private ImageView ivLoadedImage;
    private EditText etFileLink;
    private Button btnLoad, btnShow;
    private String mDownloadFileName;


    private Toast mToast;
    private void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private DownloadManager mDownloadManager;
    List<Long> mDownloadsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onDownloadComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        ivLoadedImage = findViewById(R.id.iv_loadedimage);
        etFileLink = findViewById(R.id.et_filelink);
        btnLoad = findViewById(R.id.btn_download);
        btnShow = findViewById(R.id.btn_show);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImageFile(etFileLink.getText().toString());
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.msg_show_title));
                readImageFile();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        etFileLink.setText(getString(R.string.example_link));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    // Запросим разрешения, достала их установка вручную
    private final int WRITE_EXTERNAL_STORAGE_RC = 1001;
    private final int READ_EXTERNAL_STORAGE_RC = 1002;


    private boolean isPermissionFileReadGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isPermissionFileWriteGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionForFileRead(final AppCompatActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.msg_file_read_rationale))
                    .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    READ_EXTERNAL_STORAGE_RC);
                        }
                    }).create();
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_RC);
        }
    }

    public void requestPermissionForFileWrite(final AppCompatActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.msg_file_write_rationale))
                    .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        WRITE_EXTERNAL_STORAGE_RC);
                            }
                    })
                    .create();
                dialog.show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_RC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length != 1) return;
        if (requestCode == READ_EXTERNAL_STORAGE_RC ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readImageFile();
            } else
                new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.msg_file_read_rationale) +
                            getString(R.string.msg_deny))
                    .setPositiveButton(R.string.lbl_accepted, null).show();


        }else if (requestCode == WRITE_EXTERNAL_STORAGE_RC ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImageFile(etFileLink.getText().toString());
            } else
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.msg_file_write_rationale) +
                                getString(R.string.msg_deny))
                    .setPositiveButton(R.string.lbl_accepted, null).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String getFilenameFromUrl(String url) {
        String lastParcel =  url.substring(url.lastIndexOf("/"));
        return lastParcel.substring(0,lastParcel.length() - lastParcel.lastIndexOf("."));
    }

    private String getFileExtensionFromUrl(String url) {
        return url.substring(url.lastIndexOf("."));
    }

    private boolean isUrlValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void downloadImageFile(String url) {

        if (!isPermissionFileWriteGranted()) {
            requestPermissionForFileWrite(this);
            return;
        }

        if (!isUrlValid(url)) {
            showToast(getString(R.string.err_url_not_valid));
            return;
        }
        String pictureExtension = getFileExtensionFromUrl(url);
        if (!pictureExtension.equalsIgnoreCase(".jpeg") &&
            !pictureExtension.equalsIgnoreCase(".jpg") &&
            !pictureExtension.equalsIgnoreCase(".png") &&
            !pictureExtension.equalsIgnoreCase(".bmp")) {
            showToast(getString(R.string.err_url_not_a_picture));
        }
        String pictureName = getFilenameFromUrl(url);

        Uri pictureUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(pictureUri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(getString(R.string.msg_dman_title));
        request.setDescription(getString(R.string.msg_dman_title));
        request.setVisibleInDownloadsUi(true);

        mDownloadFileName = appName + "/" + pictureName + pictureExtension;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/"  + mDownloadFileName);

        long refid = mDownloadManager.enqueue(request);

        mDownloadsList.add(refid);
    }

    private void readImageFile() {
        if (!isPermissionFileReadGranted()) {
            requestPermissionForFileRead(this);
            return;
        }

        if (mDownloadFileName == null) {
            showToast(getString(R.string.msg_nothing_to_show));
            return;
        }

        Picasso.with(this)
                .load(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        ,mDownloadFileName))
                .into(ivLoadedImage);

    }


    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            mDownloadsList.remove(referenceId);
            if (mDownloadsList.isEmpty())            {
                showToast(getString(R.string.msg_download_complete) + mDownloadFileName);
            }
        }
    };



}
