package Server;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return name, id der studiengänge an der HS
     * TODO: hsid als String aus webadresse?
     */
    Response getListOfMajors(@ApiParam(value = "id of institute returned in /institutes request", required = true, example = "hs-worms.de")
                             @PathParam("hsid") String hsid);


    @GET
    @Path( "/institute/{hsid}/studiengang/{stid}/veranstaltungen" )
    @ApiOperation(value = "Gets all Courses of a major at an institute",
            response = Veranstaltung.class,
            responseContainer = "List")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Veranstaltungen in einem studiengang
     */
    Response getListOfCourses(@ApiParam(value = "id of institute in hsid of object returned in /institute/{hsid}/studiengaenge request", required = true, example = "hs-worms.de")
                              @PathParam("hsid") String hsid,
                              @ApiParam(value = "id of object returned in /institute/{hsid}/studiengaenge request", required = true, example = "105")
                              @PathParam("stid") int stid);


    @GET
    @Path( "/institute/{hsid}/veranstaltung/{vstid}/termine" )
    @ApiOperation(value = "Gets all appointments of a course at an institute",
            response = Termin.class,
            responseContainer = "List")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    /**
     * @return liste aller Termine einer Veranstaltung
     */
    Response getListOfLectures(@ApiParam(value = "id of institute", required = true, example = "hs-worms.de")
            @PathParam("hsid") String hsid,
            @ApiParam(value = "id of course appointments are needed of", required = true, example = "40662")
            @PathParam("vstid") int vstid);

    @GET
    @Path( "/institute/{hsid}/veranstaltung/{vstid}/termine/{terminid}" )
    @ApiOperation(value = "Gets a certain appointment",
            response = Termin.class)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getDetailsOfCourse(@ApiParam(value = "id of institute", required = true, example = "hs-worms.de")
                                @PathParam("hsid") String hsid,
                                @ApiParam(value = "id of course appointments are needed of", required = true, example = "40662")
                                @PathParam("vstid") int vstid,
                                @ApiParam(value = "id of appointment in /institute/{hsid}/veranstaltung/{vstid}/termine request", required = true, example = "0")
                                @PathParam("terminid") int terminid);
}

