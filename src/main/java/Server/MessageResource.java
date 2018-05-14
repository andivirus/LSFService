package Server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

@Path( "message" )
public class MessageResource implements MessageContract
{
  @GET
  @Path( "cheer" )
  @Produces( MediaType.TEXT_PLAIN )
  public String message()
  {
    return "Yea!\n";
  }

  @GET
  @Path( "/json/dates" )
  @Produces( MediaType.APPLICATION_JSON )
  public Response getDateJSON(){
    List<TestDate> testDates = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      testDates.add(new TestDate(i, i+1));
    }
    GenericEntity<List<TestDate>> myEntity = new GenericEntity<List<TestDate>>(testDates, List.class);
    return Response.status(200).entity(myEntity).build();
  }

  @GET
  @Path( "/xml/dates" )
  @Produces ( MediaType.APPLICATION_XML )
  public Response getDateXML(){
      List<TestDate> testDates = new LinkedList<>();
      for (int i = 0; i < 10; i++) {
          testDates.add(new TestDate(i, i+1));
      }
      GenericEntity<List<TestDate>> genericEntity = new GenericEntity<List<TestDate>>(testDates) {};
      return Response.ok(genericEntity).build();
  }

  @Override
  public Response getDates() {
    List<TestDate> testDates = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      testDates.add(new TestDate(i, i+1));
    }
    GenericEntity<List<TestDate>> genericEntity = new GenericEntity<List<TestDate>>(testDates) {};
    return Response.ok(genericEntity).build();
  }
}
