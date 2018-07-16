package Server.v2;

import Server.v1.LSFResourcev1;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Api(value = "Version 2")
@Path( "/v2/" )
@SwaggerDefinition(info = @Info(title = "LSF Resource API", version = "2", description = "Version 2"))
@Singleton
public class LSFResourcev2 extends LSFResourcev1 {

    public LSFResourcev2(){
        super();

    }

    @Override
    public Response getListOfInstitutes() {
        return super.getListOfInstitutes();
    }

    @GET
    @Path( "/institute/{hsid}/majors" )
    @Override
    public Response getListOfMajors(@PathParam("hsid") String hsid){
        return super.getListOfMajors(hsid);
    }

    @GET
    @Path( "/institute/{hsid}/major/{stid}/courses" )
    @Override
    public Response getListOfCourses(@PathParam("hsid") String hsid, @PathParam("stid") int stid){
        return super.getListOfCourses(hsid, stid);
    }

    @GET
    @Path( "/institute/{hsid}/course/{vstid}/appointments" )
    @Override
    public Response getListOfLectures(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid){
        return super.getListOfLectures(hsid, vstid);
    }

    @GET
    @Path( "/institute/{hsid}/course/{vstid}/appointment/{terminid}" )
    @Override
    public Response getDetailsOfCourse(@PathParam("hsid") String hsid, @PathParam("vstid") int vstid, @PathParam("terminid") int terminid){
        return super.getDetailsOfCourse(hsid, vstid, terminid);
    }
}
