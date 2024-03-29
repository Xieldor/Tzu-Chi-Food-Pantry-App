package com.google.android.gms.samples.vision.barcodereader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ResultActivity extends AppCompatActivity {

    private DatabaseHelper db;
    public static final String EXTRA_BUTTON_TYPE = "ButtonType";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            Barcode.DriverLicense driverLicense = (Barcode.DriverLicense) intent.getParcelableExtra("DriverLicense");
            String buttonType = getIntent().getStringExtra(EXTRA_BUTTON_TYPE);

            if (driverLicense != null && HomeActivity.BUTTON_SCAN.equals(buttonType)) {

                TextView tvLicenseNumber = findViewById(R.id.tvLicenseNumber);
                TextView tvLicenseName = findViewById(R.id.tvLicenseName);
                TextView tvLicenseSurname = findViewById(R.id.tvLicenseSurname);
                TextView tvLicenseBirthday = findViewById(R.id.tvLicenseBirthday);
                TextView tvCity = findViewById(R.id.tvCity);
                TextView tvExp = findViewById(R.id.tvExp);
                tvLicenseNumber.setText("驾照号码：" + driverLicense.licenseNumber);
                tvLicenseName.setText("名：" + driverLicense.firstName);
                tvLicenseSurname.setText("姓：" + driverLicense.lastName);
                tvLicenseBirthday.setText("生日：" + driverLicense.birthDate);
                tvCity.setText("城市：" + driverLicense.addressCity);
                tvExp.setText("驾照有效期：" + driverLicense.expiryDate);

                String licenseNumber = driverLicense.licenseNumber;

                String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String expDate = driverLicense.expiryDate.substring(4, 8) + driverLicense.expiryDate.substring(0, 2) + driverLicense.expiryDate.substring(2, 4);

                if (currentDate.compareTo(expDate) > 0) {
                    showExpiryWarningDialogBUTTONSCAN();
                } else if (!isZipCodeInRange(driverLicense.addressZip)) {
                    showZipCodeWarningDialog(driverLicense);
                } else if (!db.checkLicenseNumberExists(licenseNumber)) {
                    showAptNumberInputDialog(driverLicense);
                } else if (db.checkLicenseNumberExists(licenseNumber)) {
                    showLicenseUpdateRegisterDialog(driverLicense);
                }

            } else if (driverLicense != null && HomeActivity.BUTTON_PICK_UP.equals(buttonType)) {

                TextView tvLicenseNumber = findViewById(R.id.tvLicenseNumber);
                TextView tvLicenseName = findViewById(R.id.tvLicenseName);
                TextView tvLicenseSurname = findViewById(R.id.tvLicenseSurname);
                TextView tvLicenseBirthday = findViewById(R.id.tvLicenseBirthday);
                TextView tvCity = findViewById(R.id.tvCity);
                TextView tvExp = findViewById(R.id.tvExp);
                tvLicenseNumber.setText("驾照号码：" + driverLicense.licenseNumber);
                tvLicenseName.setText("名：" + driverLicense.firstName);
                tvLicenseSurname.setText("姓：" + driverLicense.lastName);
                tvLicenseBirthday.setText("生日：" + driverLicense.birthDate);
                tvCity.setText("城市：" + driverLicense.addressCity);
                tvExp.setText("驾照有效期：" + driverLicense.expiryDate);

                String licenseNumber = driverLicense.licenseNumber;
                String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String expDate = driverLicense.expiryDate.substring(4, 8) + driverLicense.expiryDate.substring(0, 2) + driverLicense.expiryDate.substring(2, 4);

                if (currentDate.compareTo(expDate) > 0) {
                    showExpiryWarningDialogBUTTONPICKUP();
                } else if (!db.checkLicenseNumberExists(licenseNumber)) {
                    showLicenseNotRegisteredDialog();
                } else {
                    String lastPickupDate = db.getLastTimeByLicenseNumber(licenseNumber);
                    String currentDate2 = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());

                    if (currentDate2.equals(lastPickupDate)) {
                        showAlreadyPickedUpDialog();
                    } else {
                        db.updateLastPickupDate(licenseNumber, currentDate2);
                        showPickupSuccessDialog();
                    }
                }
            }
        }
    }

    private boolean isZipCodeInRange(String zipCode) {
        if (zipCode != null && zipCode.length() >= 5) {
            try {
                int zipPrefix = Integer.parseInt(zipCode.substring(0, 5));
                return zipPrefix >= 2110 && zipPrefix <= 2129;
            } catch (NumberFormatException e) {
                // 日志或处理解析错误
            }
        }
        return false;
    }

    private void showZipCodeWarningDialog(final Barcode.DriverLicense driverLicense) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>邮政编码警告</font>"))
                .setMessage("邮政编码不在 02110 到 02129 的范围内")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("仍然录入此驾照", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击确定后返回主菜单
                        showAptNumberInputDialog(driverLicense);
                    }
                })
                .setNegativeButton("跳过此驾照，继续扫描", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 可以通过启动相应的扫描Activity来继续扫描
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_SCAN);
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

    private void showAptNumberInputDialog(final Barcode.DriverLicense driverLicense) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("输入门牌号，没有则不填");

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("输入门牌号")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String aptNumber = input.getText().toString();
                        if (aptNumber.isEmpty()) {
                            aptNumber = "NULL";
                        }
                        List<String> existingNames;
                        if (!(existingNames = db.checkAddressAndRetrieveNames(driverLicense.addressStreet, driverLicense.addressCity, driverLicense.addressState, driverLicense.addressZip, aptNumber)).isEmpty() ) {
                            String namesList = TextUtils.join(", ", existingNames);
                            showAddressWarningDialog(namesList, driverLicense, aptNumber);
                        } else {
                            showLicenseNewRegisterDialog(driverLicense, aptNumber);
                        }
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

    private EditText addLabeledEditText(LinearLayout layout, String label, int inputType, String defaultValue) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView textView = new TextView(this);
        textView.setText(label);
        linearLayout.addView(textView);

        EditText editText = new EditText(this);
        editText.setInputType(inputType);
        editText.setText(defaultValue);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        // 设置EditText的权重
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                0, // 0表示权重将决定宽度
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f); // 权重为1
        editText.setLayoutParams(editTextParams);
        linearLayout.addView(editText);

        layout.addView(linearLayout);
        return editText;
    }

    private void showPhoneNumberInputDialog(final Barcode.DriverLicense driverLicense, String aptNumber) {
        // 创建包含多个输入字段的布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // 添加带标签的输入框
        EditText inputPhone = addLabeledEditText(layout, "手机号", InputType.TYPE_CLASS_PHONE,"");
        EditText inputOver65 = addLabeledEditText(layout, "65岁以上人口数", InputType.TYPE_CLASS_NUMBER, "0");
        EditText input17To64 = addLabeledEditText(layout, "17-64岁人口数", InputType.TYPE_CLASS_NUMBER, "0");
        EditText inputUnder17 = addLabeledEditText(layout, "17岁以下人口数", InputType.TYPE_CLASS_NUMBER, "0");

        TextView veteranQuestion = new TextView(this);
        veteranQuestion.setText("家庭是否有退役军人");
        layout.addView(veteranQuestion);

        RadioGroup radioGroupVeteran = new RadioGroup(this);
        RadioButton radioButtonYes = new RadioButton(this);
        radioButtonYes.setText("是");
        RadioButton radioButtonNo = new RadioButton(this);
        radioButtonNo.setText("否");
        radioGroupVeteran.addView(radioButtonYes);
        radioGroupVeteran.addView(radioButtonNo);
        layout.addView(radioGroupVeteran);

        // 设置默认选中状态，或者基于之前保存的状态设置
        radioButtonNo.setChecked(true); // 默认选择“否”

        // 创建对话框
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("输入信息")
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = inputPhone.getText().toString();
                        String over65 = inputOver65.getText().toString();
                        String age17To64 = input17To64.getText().toString();
                        String under17 = inputUnder17.getText().toString();
                        String isVeteran = radioButtonYes.isChecked() ? "1" : "0"; // 退役军人状态为字符串

                        // 调用方法以更新数据库
                        // 注意：这里需要实现该方法
                        db.addDriverLicenseWithPhoneNumber(driverLicense, phoneNumber, aptNumber, over65, age17To64, under17, isVeteran);
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
        // 创建包含多个输入字段的布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // 添加带标签的输入框
        EditText inputPhone = addLabeledEditText(layout, "手机号", InputType.TYPE_CLASS_PHONE,"");
        EditText inputOver65 = addLabeledEditText(layout, "65岁以上人口数", InputType.TYPE_CLASS_NUMBER, "0");
        EditText input17To64 = addLabeledEditText(layout, "17-64岁人口数", InputType.TYPE_CLASS_NUMBER, "0");
        EditText inputUnder17 = addLabeledEditText(layout, "17岁以下人口数", InputType.TYPE_CLASS_NUMBER, "0");

        TextView veteranQuestion = new TextView(this);
        veteranQuestion.setText("家庭是否有退役军人");
        layout.addView(veteranQuestion);

        RadioGroup radioGroupVeteran = new RadioGroup(this);
        RadioButton radioButtonYes = new RadioButton(this);
        radioButtonYes.setText("是");
        RadioButton radioButtonNo = new RadioButton(this);
        radioButtonNo.setText("否");
        radioGroupVeteran.addView(radioButtonYes);
        radioGroupVeteran.addView(radioButtonNo);
        layout.addView(radioGroupVeteran);

        // 设置默认选中状态，或者基于之前保存的状态设置
        radioButtonNo.setChecked(true); // 默认选择“否”

        // 创建对话框
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("更新信息")
                .setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = inputPhone.getText().toString();
                        String over65 = inputOver65.getText().toString();
                        String age17To64 = input17To64.getText().toString();
                        String under17 = inputUnder17.getText().toString();
                        String isVeteran = radioButtonYes.isChecked() ? "1" : "0"; // 退役军人状态为字符串

                        // 调用方法以更新数据库
                        db.updateDriverLicensePhoneNumber(driverLicense, phoneNumber, over65, age17To64, under17, isVeteran); // 添加其他参数
                        showCompletionDialog();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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

    private void showLicenseNewRegisterDialog(final Barcode.DriverLicense driverLicense, String aptNumber) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("添加驾照信息")
                .setMessage("驾照信息不存在。是否添加？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showPhoneNumberInputDialog(driverLicense, aptNumber);
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

    private void showLicenseUpdateRegisterDialog(final Barcode.DriverLicense driverLicense){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("更新驾照信息")
                .setMessage("驾照信息已存在。是否更新个人信息？")
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

    private void showLicenseNotRegisteredDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告，尚未注册</font>"))
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
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告，已经取菜</font>"))
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

    private void showExpiryWarningDialogBUTTONSCAN() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告，驾照过期</font>"))
                .setMessage("该驾照已经过期，无法使用。")
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
                        intent.putExtra(EXTRA_BUTTON_TYPE, HomeActivity.BUTTON_SCAN);
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

    private void showExpiryWarningDialogBUTTONPICKUP() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(Html.fromHtml("<font color='#FF0000'>警告，驾照过期</font>"))
                .setMessage("该驾照已经过期，无法使用。")
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

    private void showAddressWarningDialog(String namesList, final Barcode.DriverLicense driverLicense, String aptNumber) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#FF0000'>警告，地址重复</font>"));
        builder.setMessage("以下人员已在此地址注册：" + namesList + "\n请选择操作：");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("删除该地址记录，添加此驾照", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteRecordsByAddress(driverLicense.addressStreet, driverLicense.addressCity, driverLicense.addressState, driverLicense.addressZip, aptNumber);
                // 可能需要添加新记录
                showLicenseNewRegisterDialog(driverLicense, aptNumber);
            }
        });
        builder.setNegativeButton("直接添加此驾照", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLicenseNewRegisterDialog(driverLicense, aptNumber);
            }
        });
        builder.setNeutralButton("取消添加此驾照，返回主菜单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击确定后返回主菜单
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();

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