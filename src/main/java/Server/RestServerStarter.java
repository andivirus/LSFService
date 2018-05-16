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
            long begin = System.currentTimeMillis();
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

            Set<Thread> threaders = new HashSet<>();
            int i = 0;
            for (List<Veranstaltung> vlist:
                 split(veranstaltungList, 4)) {
                        threaders.add(new Thread(new Threader(vlist, i)));
                        i++;
            }
            for (Thread t :
                    threaders) {
                t.run();
            }
            for (Thread t :
                    threaders) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            /*
            for (Veranstaltung va :
                    veranstaltungList) {
                System.out.println(va);
                terminList.addAll(hs.getLectureTimes(va.getName(), va.getId()));
            }
            */

            putIntoDatabase();

            long end = System.currentTimeMillis();

            System.out.println("This took: " + (end-begin)/1000);
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
                    "PRIMARY KEY (instituteid, id, stdgid), " +
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
            connection.commit();

            System.out.println(veranstaltungList.size());
            Set<Veranstaltung> veranstaltungSet = new HashSet<>();
            veranstaltungSet.addAll(veranstaltungList);
            System.out.println(veranstaltungSet.size());
            statement = connection.prepareStatement("INSERT OR REPLACE INTO Veranstaltung VALUES (?, ?, ?, ?)");
            int i = 0;
            for (Veranstaltung v :
                    veranstaltungSet) {
                statement.setInt(1, v.getId());
                statement.setInt(2, v.getStdid());
                statement.setString(3, v.getInstituteid());
                statement.setString(4, v.getName());
                statement.addBatch();
                //System.out.println("veranstaltungList" + i);
            }
            statement.executeBatch();
            statement.close();

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
            connection.commit();
            System.out.println("Finished putting data into database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T> List<List<T>> split(List<T> list, final int number){
        List<List<T>> parts = new ArrayList<>();
        final int size = list.size();
        final int length = list.size()/number;
        for (int i = 0; i < size; i += length) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(size, i + length))));
        }

        return parts;
    }

    private class Threader implements Runnable{
        List<Veranstaltung> innerVeranstaltungList;

        public Threader(List<Veranstaltung> list, final int i){
            innerVeranstaltungList = list;
            System.out.println("THREAD " + i);
        }

        @Override
        public void run() {
            for (Veranstaltung va :
                    innerVeranstaltungList) {
                terminList.addAll(hs.getLectureTimes(va.getName(), va.getId()));
            }

        }
    }
}
