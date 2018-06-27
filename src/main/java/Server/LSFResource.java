package Server;

import Server.Util.Database.DBHandler;
import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.*;

@Path( "/" )
@Singleton
public class LSFResource implements LSFContract{
    private Pluggable hs;
    private LinkedList<Institute> instituteList;
    private LinkedList<Studiengang> studiengangList;
    private LinkedList<Veranstaltung> veranstaltungList;
    private LinkedList<Termin> terminList;

    private Connection connection;

    public LSFResource(){

        hs = RestServerStarter.hs;
        instituteList = (LinkedList<Institute>) RestServerStarter.instituteList;
        studiengangList = (LinkedList<Studiengang>) RestServerStarter.studiengangList;
        veranstaltungList = (LinkedList<Veranstaltung>) RestServerStarter.veranstaltungList;
        terminList = (LinkedList<Termin>) RestServerStarter.terminList;

        try {
            connection = DriverManager.getConnection(DBHandler.DB_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @Override
    public Response getListOfInstitutes() {
        System.out.println("Incoming Query: List of Institutes");
        List<Institute> responseList = new LinkedList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Institutes");
            while (resultSet.next()){
                responseList.add(new Institute(resultSet.getString("id"), resultSet.getString("Name")));
            }
            GenericEntity<List<Institute>> myEntity = new GenericEntity<List<Institute>>(responseList) {};
            return Response.status(200).entity(myEntity).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @Override
    public Response getListOfStudiengaenge(String hsid) {
        System.out.println("Incoming Query: List of Majors: " + hsid);
        List<Studiengang> responseList = new LinkedList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Studiengaenge WHERE instituteid = ?");
            preparedStatement.setString(1, hsid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                responseList.add(new Studiengang(resultSet.getInt("id"),
                        resultSet.getString("Name"), resultSet.getString("instituteid")));
            }
            GenericEntity<List<Studiengang>> myEntity = new GenericEntity<List<Studiengang>>(responseList) {};
            return Response.status(200).entity(myEntity).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @Override
    public Response getListOfCourses(String hsid, int stid) {
        System.out.println("Incoming Query: List of Courses [Institute - MajorID] " + hsid + " - " + stid);
        List<Veranstaltung> responseList = new LinkedList<>();
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Veranstaltung " +
                    "WHERE instituteid = ? AND stdgid = ?");
            preparedStatement.setString(1, hsid);
            preparedStatement.setInt(2, stid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                responseList.add(new Veranstaltung(resultSet.getInt("id"),
                        resultSet.getString("instituteid"),
                        resultSet.getInt("stdgid"),
                        resultSet.getString("Name")));
            }
            GenericEntity<List<Veranstaltung>> myEntity = new GenericEntity<List<Veranstaltung>>(responseList) {};
            return Response.status(200).entity(myEntity).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @Override
    public Response getListOfLectures(String hsid, int vstid) {
        Set<Termin> responseList = new HashSet<>();

        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM VLTERMIN " +
                    "WHERE instituteid = ? AND id = ?");
            preparedStatement.setString(1, hsid);
            preparedStatement.setInt(2, vstid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                responseList.add(new Termin(resultSet.getInt("terminid"), resultSet.getInt("rowid"),resultSet.getString("fach"),
                        resultSet.getInt("fachid"), resultSet.getInt("tag"), LectureReconstructor.createCalendar(resultSet.getTimestamp("start_zeit")),
                        LectureReconstructor.createCalendar(resultSet.getTimestamp("end_zeit")), resultSet.getDate("start_datum"),
                        resultSet.getDate("end_datum"), resultSet.getString("raum"),
                        resultSet.getString("prof"), resultSet.getString("bemerkung"), resultSet.getString("art"), resultSet.getString("ausfall")));
            }
            GenericEntity<Set<Termin>> myEntity = new GenericEntity<Set<Termin>>(responseList) {};
            return Response.status(200).entity(myEntity).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @Override
    public Response getDetailsOfCourse(String hsid, int vstid, int terminid) {
        Set<Termin> responseList = new HashSet<>();

        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM VLTERMIN " +
                    "WHERE instituteid = ? AND id = ? AND terminid = ?");
            preparedStatement.setString(1, hsid);
            preparedStatement.setInt(2, vstid);
            preparedStatement.setInt(3, terminid);
            ResultSet resultSet = preparedStatement.executeQuery();
            responseList.add(new Termin(resultSet.getInt("terminid"), resultSet.getInt("rowid"), resultSet.getString("fach"),
                    resultSet.getInt("fachid"), resultSet.getInt("tag"), LectureReconstructor.createCalendar(resultSet.getTimestamp("start_zeit")),
                    LectureReconstructor.createCalendar(resultSet.getTimestamp("end_zeit")), resultSet.getDate("start_datum"),
                    resultSet.getDate("end_datum"), resultSet.getString("raum"),
                    resultSet.getString("prof"), resultSet.getString("bemerkung"), resultSet.getString("art"), resultSet.getString("ausfall")));
            GenericEntity<Set<Termin>> myEntity = new GenericEntity<Set<Termin>>(responseList) {};
            return Response.status(200).entity(myEntity).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }


    private static class LectureReconstructor{
        public static Calendar createCalendar(Timestamp t){
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.HOUR_OF_DAY, t.getHours());
            cal.set(Calendar.MINUTE, t.getMinutes());
            return cal;
        }
    }
}
