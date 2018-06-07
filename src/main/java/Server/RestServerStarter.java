package Server;

import Server.Util.Config.ConfigReader;
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


    public static void main(String[] args) {
        new RestServerStarter();
    }

    public RestServerStarter(){
        ConfigReader configReader = new ConfigReader();
        DBHandler dbHandler = DBHandler.getInstance(configReader.getProperty(ConfigReader.DATABASE_PATH));
        dbHandler.createDatabase();
        try {
            long begin = System.currentTimeMillis();

            if (dbHandler.isUpdateNecessary()){
                dbHandler.clearDatabase();
                initDatabase(dbHandler);
            }

            long end = System.currentTimeMillis();

            System.out.println("This took: " + (end-begin)/1000);
            ResourceConfig resourceConfig = new ResourceConfig(LSFResource.class, OpenApiResource.class);

            try {
                System.out.println("Starting Server");
                String uristring = configReader.getProperty(ConfigReader.HOSTADRESS) + ":" + configReader.getProperty(ConfigReader.HOSTPORT) + "/";
                URI uri = new URI(uristring);
                HttpServer httpServer = JdkHttpServerFactory.createHttpServer(uri, resourceConfig);
                System.out.println("Server started at adress " + httpServer.getAddress().getHostName());
                System.out.println("Server started at Port " + httpServer.getAddress().getPort());

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDatabase(DBHandler dbHandler) throws IOException {
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
            instituteList.add(hs.getInstitute());
            studiengangList.addAll(hs.getCurriculli());

            ThreadCreator threadCreator = ThreadCreator.instantiate();
            threadCreator.doJob(studiengangList);
            threadCreator.doJob(veranstaltungList);

            dbHandler.putIntoDatabase(instituteList, studiengangList,
                    veranstaltungList, terminList);
            instituteList.clear();
            studiengangList.clear();
            veranstaltungList.clear();
            terminList.clear();
        }
    }
}
