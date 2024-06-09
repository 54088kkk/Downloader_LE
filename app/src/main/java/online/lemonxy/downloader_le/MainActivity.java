package online.lemonxy.downloader_le;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Downloader_LE";
    public String downloadedFile1;
    public String yourFolderPath;
    public final String URL = "http://111.229.209.155/downloads/";
    public EditText editText;
    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.input_text);
        button = findViewById(R.id.submit_button);
        button.setOnClickListener(v -> {
            downloadedFile1 = editText.getText().toString();
            yourFolderPath = "/storage/emulated/0/";
            if (requestStoragePermissions()) {
                startDownloadThread();
            }
        });
    }

    private void startDownloadThread() {
        Thread networkThread = new Thread(() -> {
            try {
                createFileInDirectory(downloadedFile1);
                downloadFile(URL + downloadedFile1, yourFolderPath + '/' + downloadedFile1);
            } catch (Exception e) {
                Log.e(TAG, "下载失败", e);
            }
        });
        networkThread.start();
    }

    public void downloadFile(String fileUrl, String savePath) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(savePath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                Log.i(TAG, "文件下载成功，保存路径：" + savePath);
            } else {
                Log.e(TAG, "文件下载失败，服务器响应码：" + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFileInDirectory(String fileName) {
        File externalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File newFile = new File(externalStorageDirectory, fileName);

        try {
            boolean isCreated = newFile.createNewFile();
            if (isCreated) {
                Log.i(TAG, "文件创建成功");
            } else {
                Log.w(TAG, "文件已存在");
            }
        } catch (IOException e) {
            Log.e(TAG, "文件创建失败", e);
        }
    }

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2;

    private boolean requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
                return false;
            }
            return true;
        } else {
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                return false;
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownloadThread();
            } else {
                Toast.makeText(this, "需要写入外部存储的权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                startDownloadThread();
            } else {
                Toast.makeText(this, "需要管理外部存储的权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
