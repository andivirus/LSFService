package Server;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
public interface LSFContract {

    @GET
    @Path( "/institutes" )
    @ApiOperation(value = "Gets all institutes",
    response = Institute.class,
    responseContainer = "List")
    @Consumes("text/plain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return name, id der hochschulen
     */
    Response getListOfInstitutes();

    //TODO: FIX TRANSLATION
    @GET
    @Path( "/institute/{hsid}/studiengaenge" )
    @ApiOperation(value = "Gets all majors of an institute",
            response = Studiengang.class,
            responseContainer = "List")
    @Consumes("text/plain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return name, id der studiengänge an der HS
     * TODO: hsid als String aus webadresse?
     */
    Response getListOfStudiengaenge(@PathParam("hsid") String hsid);


    @GET
    @Path( "/institute/{hsid}/studiengang/{stid}/veranstaltungen" )
    @ApiOperation(value = "Gets all Courses of a major at an institute",
            response = Veranstaltung.class,
            responseContainer = "List")
    @Consumes("text/plain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Veranstaltungen in einem studiengang
     */
    Response getListOfCourses(@PathParam("hsid") String hsid, @PathParam("stid") int stid);


    @GET
    @Path( "/institute/{hsid}/veranstaltung/{vstid}/termine" )
    @Consumes("text/plain")
    @ApiOperation(value = "Gets all appointments of a course at an institute",
            response = Termin.class,
            responseContainer = "List")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Termine einer Veranstaltung
     */
    Response getListOfLectures(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid);

    @GET
    @Path( "/institute/{hsid}/veranstaltung/{vstid}/termine/{terminid}" )
    @Consumes("text/plain")
    @ApiOperation(value = "Gets a certain appointment",
            response = Termin.class)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getDetailsOfCourse(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid, @PathParam("terminid") int terminid);
}

