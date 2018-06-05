package Server;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public interface LSFContract {

    @GET
    @Path( "/institutes" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return name, id der hochschulen
     */
    Response getListOfInstitutes();

    //TODO: FIX TRANSLATION
    @GET
    @Path( "/institute/{hsid}/studiengaenge" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return name, id der studiengänge an der HS
     * TODO: hsid als String aus webadresse?
     */
    Response getListOfStudiengaenge(@PathParam("hsid") String hsid);


    @GET
    @Path( "institute/{hsid}/studiengang/{stid}/veranstaltungen" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Veranstaltungen in einem studiengang
     */
    Response getListOfCourses(@PathParam("hsid") String hsid, @PathParam("stid") int stid);


    @GET
    @Path( "institute/{hsid}/veranstaltung/{vstid}/termine" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Termine einer Veranstaltung
     */
    Response getListOfLectures(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid);

    @GET
    @Path( "institute/{hsid}/veranstaltung/{vstid}/termine/{terminid}" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getDetailsOfCourse(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid, @PathParam("terminid") int terminid);
}

