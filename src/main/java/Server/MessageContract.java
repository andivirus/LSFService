package Server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path( "message" )
public interface MessageContract {

    @GET
    @Path( "both" )
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response getDates();
}
