package Server.Util.ExceptionMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class HelpExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e){
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_XML)
                .entity(e.getCause())
                .build();
    }
}
