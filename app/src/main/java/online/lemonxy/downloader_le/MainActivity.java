package online.lemonxy.downloader_le;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
            String downloadedFile = editText.getText().toString();
            createFileInDirectory(downloadedFile);
            Thread networkThread = new Thread(() -> {
                yourFolderPath = String.valueOf(Environment.getExternalStorageDirectory());
                downloadedFile1 = editText.getText().toString();
                createFileInDirectory(downloadedFile1);
                try {
                    done();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            if(requestWriteExternalStoragePermission()) {
                networkThread.start();
            }
        });
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
                System.out.println("文件下载成功，保存路径：" + savePath);
            } else {
                System.out.println("文件下载失败，服务器响应码：" + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void createFileInDirectory(String fileName) {
        // 获取外部存储的根目录
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        // 创建一个新的文件对象，指定文件名和路径
        File newFile = new File(externalStorageDirectory, fileName);

        try {
            // 创建新文件
            boolean isCreated = newFile.createNewFile();
            if (isCreated) {
                System.out.println("文件创建成功");
            } else {
                System.out.println("文件已存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件创建失败");
        }
    }
    protected void done(){
        downloadFile(URL + downloadedFile1, yourFolderPath + '/' + downloadedFile1);
    }
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

    private boolean requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "需要写入外部存储的权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
