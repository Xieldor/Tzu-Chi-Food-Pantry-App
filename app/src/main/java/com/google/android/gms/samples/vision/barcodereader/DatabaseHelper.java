package com.google.android.gms.samples.vision.barcodereader;

import com.google.android.gms.vision.barcode.Barcode;
import com.mysql.jdbc.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String TABLE_NAME = "tzuchi";

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

    private static final String KEY_APT_NUMBER = "apt_number";
    private static final String KEY_POPULATION_OVER_65 = "population_over_65";
    private static final String KEY_POPULATION_17_TO_64 = "population_17_to_64";
    private static final String KEY_POPULATION_UNDER_17 = "population_under_17";
    private static final String KEY_VETERAN = "veteran";


    public static Connection getConnection(String dbName) throws SQLException, ClassNotFoundException {
        Connection conn;
        try {
            // 加载 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 数据库连接信息
            String ip = "rm-rj9ke9lk70lgl78gz1o.mysql.rds.aliyuncs.com";
            String port = "3306";
            String username = "haixin_guan";
            String password = "TzuChi2024!";

            // 建立连接
            conn = (Connection) DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName, username, password);

            // 连接成功
            MainActivity.conn_on = 1;
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 连接失败
            MainActivity.conn_on = 2;
            throw ex;
        }
        return conn;
    }

    public static boolean checkLicenseNumberExists(String licenseNumber) throws SQLException, ClassNotFoundException {
        boolean exists;

        // 使用 try-with-resources 语句自动关闭资源
        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_LICENSE_NUMBER + " = ?")) {
            pstmt.setString(1, licenseNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                // 检查是否有返回的结果
                exists = ((ResultSet) rs).next();
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }
        return exists;
    }

    public static void addDriverLicenseWithPhoneNumber(Barcode.DriverLicense driverLicense, String phoneNumber, String aptNumber, String over65, String age17To64, String under17, String isVeteran) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                KEY_LICENSE_NUMBER + ", " +
                KEY_FIRST_NAME + ", " +
                KEY_MIDDLE_NAME + ", " +
                KEY_LAST_NAME + ", " +
                KEY_GENDER + ", " +
                KEY_ADDRESS_STREET + ", " +
                KEY_APT_NUMBER + ", " +
                KEY_ADDRESS_CITY + ", " +
                KEY_ADDRESS_STATE + ", " +
                KEY_ADDRESS_ZIP + ", " +
                KEY_BIRTH_DATE + ", " +
                KEY_POPULATION_OVER_65 + ", " +
                KEY_POPULATION_17_TO_64 + ", " +
                KEY_POPULATION_UNDER_17 + ", " +
                KEY_VETERAN + ", " +
                KEY_PHONE_NUMBER + ", " +
                KEY_LAST_TIME +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driverLicense.licenseNumber);
            pstmt.setString(2, driverLicense.firstName);
            pstmt.setString(3, driverLicense.middleName);
            pstmt.setString(4, driverLicense.lastName);
            pstmt.setString(5, driverLicense.gender);
            pstmt.setString(6, driverLicense.addressStreet);
            pstmt.setString(7, aptNumber);
            pstmt.setString(8, driverLicense.addressCity);
            pstmt.setString(9, driverLicense.addressState);
            pstmt.setString(10, driverLicense.addressZip);
            pstmt.setString(11, driverLicense.birthDate);
            pstmt.setString(12, over65);
            pstmt.setString(13, age17To64);
            pstmt.setString(14, under17);
            pstmt.setString(15, isVeteran);
            pstmt.setString(16, phoneNumber);
            pstmt.setString(17, "00000000"); // Assuming this is for KEY_LAST_TIME

            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }
    }


    public static void updateDriverLicensePhoneNumber(Barcode.DriverLicense driverLicense, String newPhoneNumber, String over65, String age17To64, String under17, String isVeteran) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                KEY_PHONE_NUMBER + " = ?, " +
                KEY_POPULATION_OVER_65 + " = ?, " +
                KEY_POPULATION_17_TO_64 + " = ?, " +
                KEY_POPULATION_UNDER_17 + " = ?, " +
                KEY_VETERAN + " = ? " +
                "WHERE " + KEY_LICENSE_NUMBER + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPhoneNumber);
            pstmt.setString(2, over65);
            pstmt.setString(3, age17To64);
            pstmt.setString(4, under17);
            pstmt.setString(5, isVeteran);
            pstmt.setString(6, driverLicense.licenseNumber);

            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }
    }

    public static List<String> getDriverLicensesPaged(int pageNumber, int pageSize) throws SQLException, ClassNotFoundException {
        List<String> licenses = new ArrayList<>();

        // 计算分页的起始点
        int offset = (pageNumber - 1) * pageSize;

        // SQL 查询语句
        String sql = "SELECT * FROM " + TABLE_NAME + " LIMIT ? OFFSET ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String data = "License Number: " + rs.getString(KEY_LICENSE_NUMBER)
                            + "\nFirst Name: " + rs.getString(KEY_FIRST_NAME)
                            + "\nMiddle Name: " + rs.getString(KEY_MIDDLE_NAME)
                            + "\nLast Name: " + rs.getString(KEY_LAST_NAME)
                            + "\nGender: " + rs.getString(KEY_GENDER)
                            + "\nStreet: " + rs.getString(KEY_ADDRESS_STREET)
                            + "\nApt: " + rs.getString(KEY_APT_NUMBER)
                            + "\nCity: " + rs.getString(KEY_ADDRESS_CITY)
                            + "\nState: " + rs.getString(KEY_ADDRESS_STATE)
                            + "\nZip: " + rs.getString(KEY_ADDRESS_ZIP)
                            + "\nBirth Date: " + rs.getString(KEY_BIRTH_DATE)
                            + "\nPhone Number: " + rs.getString(KEY_PHONE_NUMBER)
                            + "\nLast Pickup Time: " + rs.getString(KEY_LAST_TIME);
                    licenses.add(data);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }

        return licenses;
    }


    public static String getLastTimeByLicenseNumber(String licenseNumber) throws SQLException, ClassNotFoundException {
        String lastTime = null;
        String sql = "SELECT " + KEY_LAST_TIME + " FROM " + TABLE_NAME + " WHERE " + KEY_LICENSE_NUMBER + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, licenseNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    lastTime = rs.getString(KEY_LAST_TIME);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }

        return lastTime;
    }

    // 更新驾照的最后取菜日期
    public static void updateLastPickupDate(String licenseNumber, String newDate) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE " + TABLE_NAME + " SET " + KEY_LAST_TIME + " = ? WHERE " + KEY_LICENSE_NUMBER + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDate);
            pstmt.setString(2, licenseNumber);

            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }
    }

    public static void exportDatabaseToCSV(OutputStream outputStream) throws IOException, ClassNotFoundException {
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

            // 获取结果集元数据
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 写入列标题
            for (int i = 1; i <= columnCount; i++) {
                writer.append(metaData.getColumnName(i)).append(",");
            }
            writer.append("\n");

            // 写入行数据
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(rs.getString(i)).append(",");
                }
                writer.append("\n");
            }

            writer.flush();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw new IOException("Failed to export database to CSV", ex);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void insertOrUpdateFromCSV(InputStream inputStream) throws IOException, ClassNotFoundException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); Connection conn = getConnection(TABLE_NAME)) {
            String line = reader.readLine(); // 读取并丢弃标题行
            if (line == null) return;

            String sql = "INSERT INTO " + TABLE_NAME + " (" +
                    KEY_LICENSE_NUMBER + ", " +
                    KEY_FIRST_NAME + ", " +
                    KEY_MIDDLE_NAME + ", " +
                    KEY_LAST_NAME + ", " +
                    KEY_GENDER + ", " +
                    KEY_ADDRESS_STREET + ", " +
                    KEY_APT_NUMBER + ", " +
                    KEY_ADDRESS_CITY + ", " +
                    KEY_ADDRESS_STATE + ", " +
                    KEY_ADDRESS_ZIP + ", " +
                    KEY_BIRTH_DATE + ", " +
                    KEY_POPULATION_OVER_65 + ", " +
                    KEY_POPULATION_17_TO_64 + ", " +
                    KEY_POPULATION_UNDER_17 + ", " +
                    KEY_VETERAN + ", " +
                    KEY_PHONE_NUMBER + ", " +
                    KEY_LAST_TIME +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    KEY_FIRST_NAME + " = VALUES(" + KEY_FIRST_NAME + "), " +
                    KEY_MIDDLE_NAME + " = VALUES(" + KEY_MIDDLE_NAME + "), " +
                    KEY_LAST_NAME + " = VALUES(" + KEY_LAST_NAME + "), " +
                    KEY_GENDER + " = VALUES(" + KEY_GENDER + "), " +
                    KEY_ADDRESS_STREET + " = VALUES(" + KEY_ADDRESS_STREET + "), " +
                    KEY_APT_NUMBER + " = VALUES(" + KEY_APT_NUMBER + "), " +
                    KEY_ADDRESS_CITY + " = VALUES(" + KEY_ADDRESS_CITY + "), " +
                    KEY_ADDRESS_STATE + " = VALUES(" + KEY_ADDRESS_STATE + "), " +
                    KEY_ADDRESS_ZIP + " = VALUES(" + KEY_ADDRESS_ZIP + "), " +
                    KEY_BIRTH_DATE + " = VALUES(" + KEY_BIRTH_DATE + "), " +
                    KEY_POPULATION_OVER_65 + " = VALUES(" + KEY_POPULATION_OVER_65 + "), " +
                    KEY_POPULATION_17_TO_64 + " = VALUES(" + KEY_POPULATION_17_TO_64 + "), " +
                    KEY_POPULATION_UNDER_17 + " = VALUES(" + KEY_POPULATION_UNDER_17 + "), " +
                    KEY_VETERAN + " = VALUES(" + KEY_VETERAN + "), " +
                    KEY_PHONE_NUMBER + " = VALUES(" + KEY_PHONE_NUMBER + "), " +
                    KEY_LAST_TIME + " = VALUES(" + KEY_LAST_TIME + ")";

            PreparedStatement pstmt = conn.prepareStatement(sql);

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(","); // 假设为逗号分隔的值

                if (columns.length >= 17) {
                    pstmt.setString(1, columns[0]);
                    pstmt.setString(2, columns[1]);
                    pstmt.setString(3, columns[2]);
                    pstmt.setString(4, columns[3]);
                    pstmt.setString(5, columns[4]);
                    pstmt.setString(6, columns[5]);
                    pstmt.setString(7, columns[6]);
                    pstmt.setString(8, columns[7]);
                    pstmt.setString(9, columns[8]);
                    pstmt.setString(10, columns[9]);
                    pstmt.setString(11, columns[10]);
                    pstmt.setString(12, columns[11]);
                    pstmt.setString(13, columns[12]);
                    pstmt.setString(14, columns[13]);
                    pstmt.setString(15, columns[14]);
                    pstmt.setString(16, columns[15]);
                    pstmt.setString(17, columns[16]);

                    pstmt.addBatch();
                }
            }

            pstmt.executeBatch();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw new IOException("Failed to insert or update data from CSV", ex);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static List<String> checkAddressAndRetrieveNames(String addressStreet, String addressCity, String addressState, String addressZip, String aptNumber) throws SQLException, ClassNotFoundException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME +
                " FROM " + TABLE_NAME +
                " WHERE " + KEY_ADDRESS_STREET + " = ? AND " +
                KEY_APT_NUMBER + " = ? AND " +
                KEY_ADDRESS_CITY + " = ? AND " +
                KEY_ADDRESS_STATE + " = ? AND " +
                KEY_ADDRESS_ZIP + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, addressStreet);
            pstmt.setString(2, aptNumber);
            pstmt.setString(3, addressCity);
            pstmt.setString(4, addressState);
            pstmt.setString(5, addressZip);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(KEY_FIRST_NAME) + " " + rs.getString(KEY_LAST_NAME);
                    names.add(name);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;

        }

        return names;
    }

    public static void deleteRecordsByAddress(String addressStreet, String addressCity, String addressState, String addressZip, String aptNumber) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " +
                KEY_ADDRESS_STREET + " = ? AND " +
                KEY_APT_NUMBER + " = ? AND " +
                KEY_ADDRESS_CITY + " = ? AND " +
                KEY_ADDRESS_STATE + " = ? AND " +
                KEY_ADDRESS_ZIP + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, addressStreet);
            pstmt.setString(2, aptNumber);
            pstmt.setString(3, addressCity);
            pstmt.setString(4, addressState);
            pstmt.setString(5, addressZip);

            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            // 适当的错误处理
            throw ex;
        }
    }

    public static class DriverLicenseDetails {
        private String phoneNumber;
        private String populationOver65;
        private String population17To64;
        private String populationUnder17;
        private String isVeteran;

        // 默认构造函数
        public DriverLicenseDetails() {
            // 初始化所有属性
            this.phoneNumber = "";
            this.populationOver65 = "";
            this.population17To64 = "";
            this.populationUnder17 = "";
            this.isVeteran = "";
        }

        // Getter 和 Setter 方法
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPopulationOver65() {
            return populationOver65;
        }

        public void setPopulationOver65(String populationOver65) {
            this.populationOver65 = populationOver65;
        }

        public String getPopulation17To64() {
            return population17To64;
        }

        public void setPopulation17To64(String population17To64) {
            this.population17To64 = population17To64;
        }

        public String getPopulationUnder17() {
            return populationUnder17;
        }

        public void setPopulationUnder17(String populationUnder17) {
            this.populationUnder17 = populationUnder17;
        }

        public String isVeteran() {
            return isVeteran;
        }

        public void setVeteran(String isVeteran) {
            this.isVeteran = isVeteran;
        }

    }

    public static DriverLicenseDetails getDriverLicenseDetails(String licenseNumber) throws SQLException, ClassNotFoundException {
        DriverLicenseDetails details = new DriverLicenseDetails();
        String sql = "SELECT " +
                "phone_number, population_over_65, population_17_to_64, population_under_17, veteran " +
                "FROM " + TABLE_NAME + " WHERE " + KEY_LICENSE_NUMBER + " = ?";

        try (Connection conn = getConnection(TABLE_NAME);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, licenseNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    details.setPhoneNumber(rs.getString("phone_number"));
                    details.setPopulationOver65(rs.getString("population_over_65"));
                    details.setPopulation17To64(rs.getString("population_17_to_64"));
                    details.setPopulationUnder17(rs.getString("population_under_17"));
                    details.setVeteran(rs.getString("veteran"));
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            throw ex;
        }

        return details;
    }

}
