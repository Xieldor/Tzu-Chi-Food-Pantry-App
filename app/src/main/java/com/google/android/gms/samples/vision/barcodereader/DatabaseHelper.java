package com.google.android.gms.samples.vision.barcodereader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.vision.barcode.Barcode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DriverLicenseDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "driver_license";
    private static final String KEY_LICENSE_NUMBER = "license_number";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    // 可以添加其他字段
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_MIDDLE_NAME = "middle_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ADDRESS_STREET = "address_street";
    private static final String KEY_ADDRESS_CITY = "address_city";
    private static final String KEY_ADDRESS_STATE = "address_state";
    private static final String KEY_ADDRESS_ZIP = "address_zip";
    private static final String KEY_BIRTH_DATE = "birthDate";

    private static final String KEY_LAST_TIME = "last_time";


    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + KEY_LICENSE_NUMBER + " TEXT PRIMARY KEY,"
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_MIDDLE_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT,"
            + KEY_GENDER + " TEXT,"
            + KEY_ADDRESS_STREET + " TEXT,"
            + KEY_ADDRESS_CITY + " TEXT,"
            + KEY_ADDRESS_STATE + " TEXT,"
            + KEY_ADDRESS_ZIP + " TEXT,"
            + KEY_BIRTH_DATE + " TEXT,"
            + KEY_PHONE_NUMBER + " TEXT,"
            + KEY_LAST_TIME + " TEXT"
            // 定义其他字段
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        // insertMockData(db);
    }

    private void insertMockData(SQLiteDatabase db) {
        for (int i = 1; i <= 20; i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_LICENSE_NUMBER, "Lic" + i);
            values.put(KEY_FIRST_NAME, "FirstName" + i);
            // 设置其他字段...
            values.put(KEY_PHONE_NUMBER, "1234567890");

            db.insert(TABLE_NAME, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean checkLicenseNumberExists(String licenseNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_LICENSE_NUMBER + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{licenseNumber});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public void addDriverLicenseWithPhoneNumber(Barcode.DriverLicense driverLicense, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LICENSE_NUMBER, driverLicense.licenseNumber);
        values.put(KEY_FIRST_NAME, driverLicense.firstName);
        values.put(KEY_MIDDLE_NAME, driverLicense.middleName);
        values.put(KEY_LAST_NAME, driverLicense.lastName);
        values.put(KEY_GENDER, driverLicense.gender);
        values.put(KEY_ADDRESS_STREET, driverLicense.addressStreet);
        values.put(KEY_ADDRESS_CITY, driverLicense.addressCity);
        values.put(KEY_ADDRESS_STATE, driverLicense.addressState);
        values.put(KEY_ADDRESS_ZIP, driverLicense.addressZip);
        values.put(KEY_BIRTH_DATE, driverLicense.birthDate);
        values.put(KEY_PHONE_NUMBER, phoneNumber);
        values.put(KEY_LAST_TIME, "00000000");

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void updateDriverLicensePhoneNumber(Barcode.DriverLicense driverLicense, String newPhoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, newPhoneNumber);

        db.update(TABLE_NAME, values, KEY_LICENSE_NUMBER + " = ?", new String[]{driverLicense.licenseNumber});
        db.close();
    }

    public List<String> getDriverLicensesPaged(int pageNumber, int pageSize) {
        List<String> licenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        int offset = (pageNumber - 1) * pageSize;
        String limit = offset + "," + pageSize;
        String query = "SELECT * FROM " + TABLE_NAME + " LIMIT " + limit;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String data = "License Number: " + cursor.getString(cursor.getColumnIndex(KEY_LICENSE_NUMBER))
                        + "\nFirst Name: " + cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME))
                        + "\nMiddle Name: " + cursor.getString(cursor.getColumnIndex(KEY_MIDDLE_NAME))
                        + "\nLast Name: " + cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME))
                        + "\nGender: " + cursor.getString(cursor.getColumnIndex(KEY_GENDER))
                        + "\nStreet: " + cursor.getString(cursor.getColumnIndex(KEY_ADDRESS_STREET))
                        + "\nCity: " + cursor.getString(cursor.getColumnIndex(KEY_ADDRESS_CITY))
                        + "\nState: " + cursor.getString(cursor.getColumnIndex(KEY_ADDRESS_STATE))
                        + "\nZip: " + cursor.getString(cursor.getColumnIndex(KEY_ADDRESS_ZIP))
                        + "\nBirth Date: " + cursor.getString(cursor.getColumnIndex(KEY_BIRTH_DATE))
                        + "\nPhone Number: " + cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER))
                        + "\nLast Pickup Time: " + cursor.getString(cursor.getColumnIndex(KEY_LAST_TIME));;
                licenses.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return licenses;
    }

    @SuppressLint("Range")
    public String getLastTimeByLicenseNumber(String licenseNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastTime = null;

        Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_LAST_TIME},
                KEY_LICENSE_NUMBER + "=?", new String[] {licenseNumber},
                null, null, null);

        if (cursor.moveToFirst()) {
            lastTime = cursor.getString(cursor.getColumnIndex(KEY_LAST_TIME));
        }
        cursor.close();
        db.close();

        return lastTime;
    }

    // 更新驾照的最后取菜日期
    public void updateLastPickupDate(String licenseNumber, String newDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAST_TIME, newDate);

        db.update(TABLE_NAME, values, KEY_LICENSE_NUMBER + "=?", new String[]{licenseNumber});
        db.close();
    }

    public void exportDatabaseToCSV(OutputStream outputStream) throws IOException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        // 写入列标题
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            writer.append(cursor.getColumnName(i)).append(",");
        }
        writer.append("\n");

        // 写入行数据
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                writer.append(cursor.getString(i)).append(",");
            }
            writer.append("\n");
        }

        writer.flush();
        writer.close();
        cursor.close();
        db.close();
    }
}
