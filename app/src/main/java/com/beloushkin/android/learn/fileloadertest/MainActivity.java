package com.beloushkin.android.learn.fileloadertest;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

        ivLoadedImage = findViewById(R.id.iv_loadedimage);
        etFileLink = findViewById(R.id.et_filelink);
        btnLoad = findViewById(R.id.btn_download);
        btnShow = findViewById(R.id.btn_show);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Download");
                downloadImageFile(etFileLink.getText().toString());
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Show!");
                readImageFile("");
            }
        });
    }

    // Запросим разрешения, достала их установка вручную
    private final int WRITE_EXTERNAL_STORAGE_RC = 1001;
    private final int READ_EXTERNAL_STORAGE_RC = 1002;

    public void requestPermissionForFileRead() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.msg_file_read_rationale))
                        .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        READ_EXTERNAL_STORAGE_RC);
                            }
                        }).show();
            } else
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_RC);
        }
    }

    public void requestPermissionForFileWrite() {

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.msg_file_write_rationale))
                        .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        WRITE_EXTERNAL_STORAGE_RC);
                            }
                        }).show();
            } else
                ActivityCompat.requestPermissions(this,
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
                //requestPermissionForFileRead();
                readImageFile("");
            } else
                new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.msg_file_read_rationale) +
                            getString(R.string.msg_deny))
                    .setPositiveButton(R.string.lbl_accepted, null).show();


        }else if (requestCode == WRITE_EXTERNAL_STORAGE_RC ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //requestPermissionForFileWrite();
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
        requestPermissionForFileWrite();

        if (!isUrlValid(url)) {
            showToast("URL is not valid !");
            return;
        }
        String pictureExtension = getFileExtensionFromUrl(url);
        if (!pictureExtension.equalsIgnoreCase(".jpeg") &&
            !pictureExtension.equalsIgnoreCase(".jpg") &&
            !pictureExtension.equalsIgnoreCase(".png") &&
            !pictureExtension.equalsIgnoreCase(".bmp")) {
            showToast("Not a picture url!");
        }
        String pictureName = getFilenameFromUrl(url);

        Uri pictureUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(pictureUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("Downloading image..." + "");
        request.setDescription("Downloading image..." + "");
        request.setVisibleInDownloadsUi(true);

        mDownloadFileName = appName + "/" + pictureName + pictureExtension;
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/"  + mDownloadFileName);

        long refid = mDownloadManager.enqueue(request);
        mDownloadsList.add(refid);
    }

    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            // remove it from our list
            mDownloadsList.remove(referenceId);
            // if list is empty means all downloads completed
            if (mDownloadsList.isEmpty())
            {
                // show a notification
                Log.e("INSIDE", "" + referenceId);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Download manager")
                                .setContentText("All Download completed");

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());
            }
        }
    };

    private void readImageFile(String fileUrl) {
        requestPermissionForFileRead();

        Picasso.with(this)
                .load(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        ,mDownloadFileName))
                .into(ivLoadedImage);

    }


}
