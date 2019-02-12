package com.beloushkin.android.learn.fileloadertest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageView ivLoadedImage;
    private EditText etFileLink;
    private Button btnLoad, btnShow;
    private String filename;


    private Toast mToast;
    private void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


    private void downloadImageFile(String fileUrl) {
        requestPermissionForFileWrite();
    }

    private void readImageFile(String fileUrl) {
        requestPermissionForFileRead();
    }


}
