package Server.Util.Database;

import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBHandler {

    public static String DB_URL;

    private static DBHandler singleton;

    private DBHandler(String url) {
        DB_URL = "jdbc:sqlite:" + url;
    }

    public static synchronized DBHandler getInstance(String url){
        if(singleton == null){
            singleton = new DBHandler(url);
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
                    if (System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (90 * 60 * 1000)) {
                        System.out.println(System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (90 * 60 * 1000));
                        System.out.println("Skipping Database input");
                        lastupdate.close();
                        return false;
                    } else {
                        System.out.println(System.currentTimeMillis() - resultSet.getTimestamp(1).getTime() < (90 * 60 * 1000));
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
        return false;
    }

    public void createDatabase(){
        File dir = new File("database");
        dir.mkdir();
        Connection connection = null;
        Statement statement = null;

        System.out.println("Creating Database...");

        try {
            connection = DriverManager.getConnection(DB_URL);
            statement = connection.createStatement();
            connection.setAutoCommit(false);

            statement.addBatch("CREATE TABLE IF NOT EXISTS Settings(" +
                    "LastUpdate TIMESTAMP, id INTEGER, " +
                    "PRIMARY KEY (id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Institutes(" +
                    "Name Varchar(60), id Varchar(20), PRIMARY KEY (id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Studiengaenge(" +
                    "id INTEGER, instituteid VARCHAR, Name VARCHAR, PRIMARY KEY (id, instituteid), UNIQUE (id)," +
                    "FOREIGN KEY (instituteid) REFERENCES Institutes(id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Veranstaltung(" +
                    "id INTEGER, stdgid INTEGER, instituteid VARCHAR, Name VARCHAR," +
                    "PRIMARY KEY (instituteid, id, stdgid), " +
                    "FOREIGN KEY (stdgid) REFERENCES Studiengaenge(id)," +
                    "FOREIGN KEY (instituteid) REFERENCES Institutes(id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Termin (" +
                    "terminid INTEGER, rowid INTEGER, fach VARCHAR, tag INTEGER, " +
                    "start_zeit TIMESTAMP, end_zeit TIMESTAMP," +
                    "start_datum DATE, end_datum DATE," +
                    "raum VARCHAR, prof VARCHAR, bemerkung VARCHAR, art VARCHAR, ausfall VARCHAR, " +
                    "FOREIGN KEY (fach) REFERENCES Veranstaltung(Name))");

            statement.addBatch("CREATE VIEW IF NOT EXISTS VLTERMIN AS " +
                    "SELECT * FROM Veranstaltung JOIN Termin ON Veranstaltung.Name = Termin.fach");


            statement.executeBatch();
            statement.close();

            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void putIntoDatabase(List<Institute> instituteList, List<Studiengang> studiengangList,
                                List<Veranstaltung> veranstaltungList, List<Termin> terminList){
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false);
            System.out.println("Putting data into database...");

            PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO Settings VALUES (?, 1)");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));

            statement = connection.prepareStatement("INSERT OR REPLACE INTO Institutes VALUES (?, ?)");

            System.out.println(instituteList.size());
            for (Institute i :
                    instituteList) {
                statement.setString(1, i.getName());
                statement.setString(2, i.getId());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            connection.commit();

            System.out.println(studiengangList.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Studiengaenge VALUES (?, ?, ?)");
            for (Studiengang s :
                    studiengangList) {
                statement.setInt(1, s.getId());
                statement.setString(2, s.getHsid());
                statement.setString(3, s.getName());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            connection.commit();

            System.out.println(veranstaltungList.size());
            Set<Veranstaltung> veranstaltungSet = new HashSet<>();
            veranstaltungSet.addAll(veranstaltungList);
            System.out.println(veranstaltungSet.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Veranstaltung VALUES (?, ?, ?, ?)");
            for (Veranstaltung v :
                    veranstaltungSet) {
                statement.setInt(1, v.getId());
                statement.setInt(2, v.getStdid());
                statement.setString(3, v.getInstituteid());
                statement.setString(4, v.getName());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            connection.commit();

            System.out.println(terminList.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Termin VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (Termin t :
                    terminList) {
                statement.setInt(1, t.getId());
                statement.setInt(2, t.getRowid());
                statement.setString(3, t.getFach());
                statement.setInt(4, t.getTag());
                statement.setTime(5, new Time(t.getStart_zeit().getTime().getTime()));
                statement.setTime(6, new Time(t.getEnd_zeit().getTime().getTime()));
                if(t.getStart_datum() != null)
                    statement.setDate(7, new Date(t.getStart_datum().getTime()));
                if(t.getEnd_datum() != null)
                    statement.setDate(8, new Date(t.getEnd_datum().getTime()));
                statement.setString(9, t.getRaum());
                statement.setString(10, t.getProf());
                statement.setString(11, t.getBemerkung());
                statement.setString(12, t.getArt());
                statement.setString(13, t.getAusfall());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();

            statement = connection.prepareStatement("INSERT OR REPLACE INTO Settings VALUES (?, 1)");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            statement.close();
            connection.commit();
            System.out.println("Finished putting data into database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase(){
        try{
            Connection connection = DriverManager.getConnection(DB_URL);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM Termin");
            statement.executeUpdate("DELETE FROM Veranstaltung");
            statement.executeUpdate("DELETE FROM Studiengaenge");
            statement.executeUpdate("DELETE FROM Institutes");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
