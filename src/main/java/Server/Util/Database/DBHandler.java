package Server.Util.Database;

import java.sql.*;

public class DBHandler {

    public static final String DB_URL = "jdbc:sqlite:./database/lsf.db";

    private static DBHandler singleton;

    private DBHandler() {}

    public static synchronized DBHandler getInstance(){
        if(singleton == null){
            singleton = new DBHandler();
        }
        return singleton;
    }

    public boolean isUpdateNecessary() {
        long begin = System.currentTimeMillis();

        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            ResultSet rowcountset = statement.executeQuery("SELECT COUNT(*) as rowcount FROM Settings");
            rowcountset.next();
            int rowcount = rowcountset.getInt("rowcount");
            statement.close();
            if (rowcount != 0) {
                Statement lastupdate = connection.createStatement();
                ResultSet resultSet = lastupdate.executeQuery("SELECT LastUpdate FROM Settings");
                while (resultSet.next()) {
                    System.out.println("LastUpdate: " + resultSet.getTimestamp(1).toString());
                    System.out.println("Current Time:  " + new java.util.Date().toString());
                    if (System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (10 * 60 * 1000)) {
                        System.out.println(System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (10 * 60 * 1000));
                        System.out.println("Skipping Database input");
                        lastupdate.close();
                    } else {
                        System.out.println(System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (10 * 60 * 1000));
                        lastupdate.close();
                        return true;
                    }
                }
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLException: SKIPPED");
        }
        return true;
    }
}
