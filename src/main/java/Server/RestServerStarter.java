package Server;

import Server.Util.Config.ConfigReader;
import Server.Util.Database.DBHandler;
import Server.Util.ExceptionMapper.HelpExceptionMapper;
import Server.Util.Plugin.JarFilenameFilter;
import Server.Util.Plugin.PluginLoader;
import Server.Util.Threading.ThreadCreator;
import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

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
        if(Arrays.asList(args).contains("-h")){
            System.out.println("--noupdate : does not recreate the database upon start, even if its old");
            System.exit(0);
        }
        new RestServerStarter(args);
    }

    public RestServerStarter(String[] args){
        long begin = System.currentTimeMillis();
        ConfigReader configReader = new ConfigReader();
        DBHandler dbHandler = DBHandler.getInstance(configReader.getProperty(ConfigReader.DATABASE_PATH));
        if(!Arrays.asList(args).contains("--noupdate")) {
            dbHandler.createDatabase();
            try {

                if (dbHandler.isUpdateNecessary()) {
                    dbHandler.clearDatabase();
                    initDatabase(dbHandler);
                }
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();

        System.out.println("This took: " + (end - begin) / 1000);
        //ResourceConfig resourceConfig = new ResourceConfig(LSFResource.class, OpenApiResource.class);

        try {
            System.out.println("Starting Server");
            String uristring = configReader.getProperty(ConfigReader.HOSTADRESS) + ":" + configReader.getProperty(ConfigReader.HOSTPORT) + "/";
            URI uri = new URI(uristring);
            /*
            HttpServer httpServer = JdkHttpServerFactory.createHttpServer(uri, resourceConfig);
            System.out.println("Server started at adress " + httpServer.getAddress().getHostName());
            System.out.println("Server started at Port " + httpServer.getAddress().getPort());
            */
            Server server = new Server(Integer.valueOf(configReader.getProperty(ConfigReader.HOSTPORT)));

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
            jerseyServlet.setInitOrder(0);

            jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", LSFResource.class.getCanonicalName() + ", " + HelpExceptionMapper.class.getCanonicalName());

            server.start();
            server.join();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
