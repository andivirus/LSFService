package Server;

import Server.Util.Database.DBHandler;
import Server.Util.Plugin.JarFilenameFilter;
import Server.Util.Plugin.PluginLoader;
import Server.Util.Threading.ThreadCreator;
import com.sun.net.httpserver.HttpServer;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class RestServerStarter {
    public static Pluggable hs;
    public static List<Institute> instituteList;
    public static List<Studiengang> studiengangList;
    public static List<Veranstaltung> veranstaltungList;
    public static List<Termin> terminList;

    public static final String DB_URL = "jdbc:sqlite:./database/lsf.db";

    public static void main(String[] args) {
        new RestServerStarter();
    }

    public RestServerStarter(){
        DBHandler dbHandler = DBHandler.getInstance();
        dbHandler.createDatabase();
        try {
            long begin = System.currentTimeMillis();

            if (dbHandler.isUpdateNecessary()){
                initDatabase();
                dbHandler.putIntoDatabase(instituteList, studiengangList,
                        veranstaltungList, terminList);
            }

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

    private void initDatabase() throws IOException {
        System.out.println("Scraping Data for Database");
        List<Pluggable> plugins;
        plugins = PluginLoader.loadPlugins(new File("./plugins"));
        instituteList = new LinkedList<>();
        studiengangList = new LinkedList<>();
        veranstaltungList = new LinkedList<>();
        terminList = new LinkedList<>();

        System.out.println("Found Plugins:");
        for (String s :
                new File("./plugins").list(new JarFilenameFilter())) {
            System.out.println(s);
        }
        for (Pluggable hs :
                plugins) {
            RestServerStarter.hs = hs;
            hs.start();
            /*
            List<Institute> tempInstituteList = new LinkedList<>();
            tempInstituteList.add(hs.getInstitute());

            List<Studiengang> tempStudiengangList = new LinkedList<>();
            tempStudiengangList.addAll(hs.getCurriculli());
            */
            instituteList.add(hs.getInstitute());
            studiengangList.addAll(hs.getCurriculli());

            ThreadCreator threadCreator = ThreadCreator.instantiate();
            threadCreator.doJob(studiengangList);
            threadCreator.doJob(veranstaltungList);
        }
    }
}
