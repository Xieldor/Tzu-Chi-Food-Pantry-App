package com.google.android.gms.samples.vision.barcodereader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class HomeActivity extends Activity {

    public static final String EXTRA_BUTTON_TYPE = "ButtonType";
    private static final String USERNAME = "Haixin";
    private static final String PASSWORD = "Haixin";

    public static final String BUTTON_SCAN = "Register";
    public static final String BUTTON_PICK_UP = "PickUp";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnFetchDish = findViewById(R.id.btnFetchDish);

        btnScan.setOnClickListener(v -> {
            // 启动原MainActivity（或其新名称）
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_BUTTON_TYPE, BUTTON_SCAN);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> showLoginDialog());

        btnFetchDish.setOnClickListener(v -> {
            // 启动原MainActivity（或其新名称）
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_BUTTON_TYPE, BUTTON_PICK_UP);
            startActivity(intent);
        });

        if (getIntent().getBooleanExtra("SHOW_ERROR_DIALOG", false)) {
            showErrorDialog();
        }
    }

    private void showErrorDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>连接失败</font>"))
                .setMessage("数据库连接失败，请检查网络，稍后再试。")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 用户点击确定后只需关闭对话框
                    dialog.dismiss();
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.CENTER_VERTICAL; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }


    private void showLoginDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View loginView = inflater.inflate(R.layout.dialog_login, null);
        final EditText usernameInput = loginView.findViewById(R.id.username);
        final EditText passwordInput = loginView.findViewById(R.id.password);

        new AlertDialog.Builder(this)
                .setTitle("登录")
                .setView(loginView)
                .setPositiveButton("登录", (dialog, which) -> {
                    String username = usernameInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                        Intent intent = new Intent(HomeActivity.this, DatabaseContentActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(HomeActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}