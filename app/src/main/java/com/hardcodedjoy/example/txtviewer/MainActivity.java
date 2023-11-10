package com.hardcodedjoy.example.txtviewer;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) { processIntent(intent); }

    private void processIntent(Intent intent) {
        switch(intent.getAction()) {
            case Intent.ACTION_MAIN:
                TextView tvText = findViewById(R.id.tv_text);
                tvText.setText(R.string.text_explain_app_usage);
                break;
            case Intent.ACTION_VIEW:
            case Intent.ACTION_SEND:
                try {
                    onViewSend(intent);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                break;
            default:
                break;
        }
    }

    private void onViewSend(Intent intent) throws Exception {
        if(intent.getType() == null) { return; }
        if(!intent.getType().equals("text/plain")) { return; }

        Uri receivedURI = getUri(intent);
        if(receivedURI == null) { return; }

        String fileName = getFileName(receivedURI);
        setTitle(fileName);

        InputStream is = getContentResolver().openInputStream(receivedURI);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] block = new byte[1024];
        int bytesRead;
        while((bytesRead = is.read(block)) != -1) {
            baos.write(block, 0, bytesRead);
        }
        is.close();
        String text = baos.toString("UTF-8");

        TextView tvText = findViewById(R.id.tv_text);
        tvText.setText(text);
    }

    private String getFileNameFromContentURI(Uri uri) {
        if(!uri.getScheme().equals("content")) { return null; }
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor == null) { return null; }
        if(!cursor.moveToFirst()) { cursor.close(); return null; }
        int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if(columnIndex == -1) { cursor.close(); return null; }
        String result = cursor.getString(columnIndex);
        cursor.close();
        return result;
    }

    private String getFileNameFromFileURI(Uri uri) {
        String result = uri.getPath();
        if(result.contains("/")) {
            result = result.substring(result.lastIndexOf("/") + 1);
        }
        return result;
    }

    private String getFileName(Uri uri) {
        String result = getFileNameFromContentURI(uri);
        if(result == null) { result = getFileNameFromFileURI(uri); }
        return result;
    }

    private Uri getUri(Intent intent) {
        if(android.os.Build.VERSION.SDK_INT < 16) { return intent.getData(); }

        ClipData clipData = intent.getClipData();

        if(clipData != null) {
            if(clipData.getItemCount() > 0) {
                return clipData.getItemAt(0).getUri();
            } else {
                return null;
            }
        } else {
            return intent.getData();
        }
    }
}