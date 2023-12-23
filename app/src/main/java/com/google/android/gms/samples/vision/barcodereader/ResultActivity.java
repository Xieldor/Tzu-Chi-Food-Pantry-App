package com.google.android.gms.samples.vision.barcodereader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ResultActivity extends AppCompatActivity {

    private DatabaseHelper db;
    public static final String EXTRA_BUTTON_TYPE = "ButtonType";

    private String buttonType;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            Barcode.DriverLicense driverLicense = (Barcode.DriverLicense) intent.getParcelableExtra("DriverLicense");
            buttonType = getIntent().getStringExtra(EXTRA_BUTTON_TYPE);

            if (driverLicense != null && HomeActivity.BUTTON_SCAN.equals(buttonType)) {

                TextView tvLicenseNumber = findViewById(R.id.tvLicenseNumber);
                TextView tvLicenseName = findViewById(R.id.tvLicenseName);
                TextView tvLicenseSurname = findViewById(R.id.tvLicenseSurname);
                TextView tvLicenseBirthday = findViewById(R.id.tvLicenseBirthday);
                TextView tvCity = findViewById(R.id.tvCity);
                tvLicenseNumber.setText("驾照号码：" + driverLicense.licenseNumber);
                tvLicenseName.setText("名：" + driverLicense.firstName);
                tvLicenseSurname.setText("姓：" + driverLicense.lastName);
                tvLicenseBirthday.setText("生日：" + driverLicense.birthDate);
                tvCity.setText("城市：" + driverLicense.addressCity);

                String licenseNumber = driverLicense.licenseNumber;

                if (!db.checkLicenseNumberExists(licenseNumber)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("添加驾照信息")
                            .setMessage("驾照信息不存在。是否添加？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showPhoneNumberInputDialog(driverLicense);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showCompletionDialog();
                                }
                            })
                            .create();

                    Window window = alertDialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
                        layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
                        window.setAttributes(layoutParams);
                    }

                    alertDialog.show();

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("更新驾照信息")
                            .setMessage("驾照信息已存在。是否更新手机号？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showUpdatePhoneNumberDialog(driverLicense);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showCompletionDialog();
                                }
                            })
                            .create();

                    Window window = alertDialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
                        layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
                        window.setAttributes(layoutParams);
                    }

                    alertDialog.show();
                }
            } else if (driverLicense != null && HomeActivity.BUTTON_PICK_UP.equals(buttonType)) {

                TextView tvLicenseNumber = findViewById(R.id.tvLicenseNumber);
                TextView tvLicenseName = findViewById(R.id.tvLicenseName);
                TextView tvLicenseSurname = findViewById(R.id.tvLicenseSurname);
                TextView tvLicenseBirthday = findViewById(R.id.tvLicenseBirthday);
                TextView tvCity = findViewById(R.id.tvCity);
                tvLicenseNumber.setText("驾照号码：" + driverLicense.licenseNumber);
                tvLicenseName.setText("名：" + driverLicense.firstName);
                tvLicenseSurname.setText("姓：" + driverLicense.lastName);
                tvLicenseBirthday.setText("生日：" + driverLicense.birthDate);
                tvCity.setText("城市：" + driverLicense.addressCity);

                String licenseNumber = driverLicense.licenseNumber;

                if (!db.checkLicenseNumberExists(licenseNumber)) {
                    showLicenseNotRegisteredDialog();
                } else {
                    String lastPickupDate = db.getLastTimeByLicenseNumber(licenseNumber);
                    String currentDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());

                    if (currentDate.equals(lastPickupDate)) {
                        showAlreadyPickedUpDialog();
                    } else {
                        db.updateLastPickupDate(licenseNumber, currentDate);
                        showPickupSuccessDialog();
                    }
                }
            }
        }
    }

    private void showPhoneNumberInputDialog(final Barcode.DriverLicense driverLicense) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("输入手机号")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = input.getText().toString();
                        db.addDriverLicenseWithPhoneNumber(driverLicense, phoneNumber);
                        showCompletionDialog();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showCompletionDialog();
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }

    private void showUpdatePhoneNumberDialog(final Barcode.DriverLicense driverLicense) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("更新手机号")
                .setMessage("请输入新的手机号：")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newPhoneNumber = input.getText().toString();
                        db.updateDriverLicensePhoneNumber(driverLicense, newPhoneNumber);
                        showCompletionDialog();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showCompletionDialog();
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }

    private void showCompletionDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("操作完成")
                .setMessage("请选择下一步操作")
                .setPositiveButton("继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 可以通过启动相应的扫描Activity来继续扫描
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_SCAN);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("返回主菜单", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 返回主菜单
                        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }

    private void showLicenseNotRegisteredDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告</font>"))
                .setMessage("该驾照尚未注册，不能取菜。")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("返回注册", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确定后返回主菜单
                        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 可以通过启动相应的扫描Activity来继续扫描
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_PICK_UP);
                        startActivity(intent);
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }

    private void showAlreadyPickedUpDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告</font>"))
                .setMessage("该驾照今天已经取菜。")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("返回主菜单", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确定后返回主菜单
                        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 可以通过启动相应的扫描Activity来继续扫描
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_PICK_UP);
                        startActivity(intent);
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }

    private void showPickupSuccessDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("成功")
                .setMessage("取菜成功。")
                .setPositiveButton("返回主菜单", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确定后返回主菜单
                        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 可以通过启动相应的扫描Activity来继续扫描
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_PICK_UP);
                        startActivity(intent);
                    }
                })
                .create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.gravity = Gravity.BOTTOM; // 或者其他位置
            window.setAttributes(layoutParams);
        }

        alertDialog.show();
    }
}