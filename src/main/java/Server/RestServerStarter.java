package Server;

import Server.Institute.Institute;
import Server.Institute.Studiengang;
import Server.Institute.Termin;
import Server.Institute.Veranstaltung;
import com.sun.net.httpserver.HttpServer;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.sql.Date;
import java.util.*;


public class RestServerStarter {
    public static HSWorms hs;
    public static List<Institute> instituteList;
    public static List<Studiengang> studiengangList;
    public static List<Veranstaltung> veranstaltungList;
    public static List<Termin> terminList;

    public static final String DB_URL = "jdbc:sqlite:./database/lsf.db";

    public static void main(String[] args) {
        new RestServerStarter();
    }

    public RestServerStarter(){
        try {
            createDatabase();
            hs = new HSWorms();
            instituteList = new LinkedList<>();
            studiengangList = new LinkedList<>();
            veranstaltungList = new LinkedList<>();
            terminList = new LinkedList<>();

            instituteList.add(hs.getInstitue());
            studiengangList.addAll(hs.getCurriculli());
            for (Studiengang stdg :
                    studiengangList) {
                System.out.println(stdg);
                veranstaltungList.addAll(hs.getLectures(stdg.getId()));
            }
            for (Veranstaltung va :
                    veranstaltungList) {
                System.out.println(va);
                terminList.addAll(hs.getLectureTimes(va.getName(), va.getId()));
            }

            putIntoDatabase();

            ResourceConfig resourceConfig = new ResourceConfig(LSFResource.class, OpenApiResource.class);

            try {
                System.out.println("Starting Server");
                URI uri = new URI("http://localhost:8090/");
                HttpServer httpServer = JdkHttpServerFactory.createHttpServer(uri, resourceConfig);
                System.out.println("Server started at Port " + httpServer.getAddress().getPort());

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDatabase(){
        File dir = new File("database");
        dir.mkdir();
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL);
            statement = connection.createStatement();
            connection.setAutoCommit(false);

            statement.addBatch("CREATE TABLE IF NOT EXISTS Institutes(" +
                    "Name Varchar(60), id Varchar(20), PRIMARY KEY (id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Studiengaenge(" +
                    "id INTEGER, instituteid VARCHAR, Name VARCHAR, PRIMARY KEY (id, instituteid), UNIQUE (id)," +
                    "FOREIGN KEY (instituteid) REFERENCES Institutes(id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Veranstaltung(" +
                    "id INTEGER, stdgid INTEGER, instituteid VARCHAR, Name VARCHAR," +
                    "PRIMARY KEY (instituteid, id), " +
                    "FOREIGN KEY (stdgid) REFERENCES Studiengaenge(id)," +
                    "FOREIGN KEY (instituteid) REFERENCES Institutes(id))");

            statement.addBatch("CREATE TABLE IF NOT EXISTS Termin (" +
                    "terminid INTEGER, rowid INTEGER, fach VARCHAR, tag INTEGER, " +
                    "start_zeit TIMESTAMP, end_zeit TIMESTAMP," +
                    "start_datum DATE, end_datum DATE," +
                    "raum VARCHAR, prof VARCHAR, bemerkung VARCHAR, art VARCHAR, ausfall VARCHAR, " +
                    "PRIMARY KEY (terminid, fach, rowid), FOREIGN KEY (fach) REFERENCES Veranstaltung(Name))");

            statement.addBatch("CREATE VIEW IF NOT EXISTS VLTERMIN AS " +
                    "SELECT * FROM Veranstaltung JOIN Termin ON Veranstaltung.Name = Termin.fach");

            statement.executeBatch();
            statement.close();
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void putIntoDatabase(){
        System.out.println("Putting data into database...");
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO Institutes VALUES (?, ?)");

            System.out.println(instituteList.size());
            for (Institute i :
                    instituteList) {
                statement.setString(1, i.getName());
                statement.setString(2, i.getId());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();

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

            System.out.println(veranstaltungList.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Veranstaltung VALUES (?, ?, ?, ?)");
            for (Veranstaltung v :
                    veranstaltungList) {
                statement.setInt(1, v.getId());
                statement.setInt(2, v.getStdid());
                statement.setString(3, v.getInstituteid());
                statement.setString(4, v.getName());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();

            System.out.println(terminList.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Termin VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            for (Termin t :
                    terminList) {
                System.out.println(t);
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
            connection.commit();
            System.out.println("Finished putting data into database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
