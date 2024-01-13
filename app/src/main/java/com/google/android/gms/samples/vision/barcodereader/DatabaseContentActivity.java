package com.google.android.gms.samples.vision.barcodereader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.app.Activity;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseContentActivity extends Activity {
    private TextView txtDataDisplay;
    private TextView txtNoData;
    private Button btnLoadMore;
    private int pageNumber = 1;
    private final int pageSize = 1000;

    private static final int REQUEST_CODE_SAVE_FILE = 10086;
    private static final int REQUEST_CODE_IMPORT_FILE = 10087;

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_content);

        txtDataDisplay = findViewById(R.id.txtDataDisplay);
        txtNoData = findViewById(R.id.txtNoData);
        btnLoadMore = findViewById(R.id.btnLoadMore);

        loadMoreData();

        btnLoadMore.setOnClickListener(v -> loadMoreData());

        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(v -> {
            // 返回主界面
            Intent intent = new Intent(DatabaseContentActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // 关闭当前活动
        });

        Button btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> showFilePicker());

        Button btnImportCsv = findViewById(R.id.btnImportCsv);
        btnImportCsv.setOnClickListener(v -> showFilePickerForImport());
    }

    private void showErrorDialog() {
        // 首先立即返回主菜单
        Intent intent = new Intent(DatabaseContentActivity.this, HomeActivity.class);
        intent.putExtra("SHOW_ERROR_DIALOG", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // 结束当前活动
    }

    private void loadMoreData() {
        executor.execute(() -> {
            List<String> newItems = null;
            try {
                newItems = DatabaseHelper.getDriverLicensesPaged(pageNumber, pageSize);
            } catch (SQLException | ClassNotFoundException e) {
                // 更新界面的代码，例如显示错误消息
                runOnUiThread(this::showErrorDialog);
            }
            List<String> finalNewItems = newItems;
            runOnUiThread(() -> {
                assert finalNewItems != null;
                if (finalNewItems.isEmpty()) {
                    handleEmptyData();
                } else {
                    appendData(finalNewItems);
                }
            });
        });
    }

    private void handleEmptyData() {
        if (pageNumber == 1) {
            txtNoData.setVisibility(View.VISIBLE);
            txtDataDisplay.setVisibility(View.GONE);
            btnLoadMore.setVisibility(View.GONE);
        } else {
            btnLoadMore.setVisibility(View.GONE);
        }
    }

    private void appendData(List<String> newItems) {
        for (String item : newItems) {
            txtDataDisplay.append(item + "\n\n");
        }
        pageNumber++;
        txtNoData.setVisibility(View.GONE);
        txtDataDisplay.setVisibility(View.VISIBLE);
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
        executor.execute(() -> {
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    DatabaseHelper.exportDatabaseToCSV(outputStream);
                    runOnUiThread(() -> Toast.makeText(this, "数据已导出", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showFilePickerForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE);
    }

    private void importCsv(Uri uri) {
        executor.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    DatabaseHelper.insertOrUpdateFromCSV(inputStream);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "CSV Imported Successfully", Toast.LENGTH_LONG).show();
                        refreshDataDisplay();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error importing CSV", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void refreshDataDisplay() {
        pageNumber = 1; // Reset to first page
        txtDataDisplay.setText(""); // Clear existing data

        loadMoreData(); // Reload data
    }

}