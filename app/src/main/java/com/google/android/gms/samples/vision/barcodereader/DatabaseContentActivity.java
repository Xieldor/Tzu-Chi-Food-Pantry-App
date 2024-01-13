package com.google.android.gms.samples.vision.barcodereader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.app.Activity;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseContentActivity extends Activity {
    private DatabaseHelper databaseHelper;
    private TextView txtDataDisplay;
    private TextView txtNoData;
    private Button btnLoadMore;
    private int pageNumber = 1;
    private final int pageSize = 1000;

    private static final int REQUEST_CODE_SAVE_FILE = 10086;
    private static final int REQUEST_CODE_IMPORT_FILE = 10087;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_content);

        databaseHelper = new DatabaseHelper(this);
        txtDataDisplay = findViewById(R.id.txtDataDisplay);
        txtNoData = findViewById(R.id.txtNoData);
        btnLoadMore = findViewById(R.id.btnLoadMore);

        loadMoreData();

        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });

        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主界面
                Intent intent = new Intent(DatabaseContentActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // 关闭当前活动
            }
        });

        Button btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePicker();
            }
        });

        Button btnImportCsv = findViewById(R.id.btnImportCsv);
        btnImportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePickerForImport();
            }
        });
    }

    private void loadMoreData() {
        List<String> newItems = databaseHelper.getDriverLicensesPaged(pageNumber, pageSize);
        if (newItems.isEmpty()) {
            if (pageNumber == 1) {
                // 如果是第一页且没有数据，显示“没有数据”提示
                txtNoData.setVisibility(View.VISIBLE);
                txtDataDisplay.setVisibility(View.GONE);
                btnLoadMore.setVisibility(View.GONE);
            } else {
                // 如果不是第一页但没有更多数据，可以隐藏加载更多按钮
                btnLoadMore.setVisibility(View.GONE);
            }
        } else {
            // 有数据，追加到显示区域
            for (String item : newItems) {
                txtDataDisplay.append(item + "\n\n");
            }
            pageNumber++;
            txtNoData.setVisibility(View.GONE);
            txtDataDisplay.setVisibility(View.VISIBLE);
        }
    }

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "export.csv");

        startActivityForResult(intent, REQUEST_CODE_SAVE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAVE_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            exportCsv(uri);
        }
        if (requestCode == REQUEST_CODE_IMPORT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importCsv(uri);
        }
    }

    private void exportCsv(Uri uri) {
        try {
            databaseHelper.exportDatabaseToCSV(getContentResolver().openOutputStream(uri));
            Toast.makeText(this, "数据已导出", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFilePickerForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE);
    }

    private void importCsv(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            databaseHelper.insertOrUpdateFromCSV(inputStream);
            Toast.makeText(this, "CSV Imported Successfully", Toast.LENGTH_LONG).show();
            refreshDataDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error importing CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshDataDisplay() {
        pageNumber = 1; // Reset to first page
        txtDataDisplay.setText(""); // Clear existing data

        loadMoreData(); // Reload data
    }

}