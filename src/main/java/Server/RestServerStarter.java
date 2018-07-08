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
    public static final List<Institute> instituteList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Studiengang> studiengangList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Veranstaltung> veranstaltungList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Termin> terminList = Collections.synchronizedList(new LinkedList<>());


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
                if (dbHandler.isUpdateNecessary()) {
                    initDatabase(dbHandler);
                }

        }
        long end = System.currentTimeMillis();

        System.out.println("This took: " + (end - begin) / 1000);
        //ResourceConfig resourceConfig = new ResourceConfig(LSFResource.class, OpenApiResource.class);

        try {
            System.out.println("Starting Server");

            Server server = new Server(Integer.valueOf(configReader.getProperty(ConfigReader.HOSTPORT)));

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
            jerseyServlet.setInitOrder(0);

            jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", LSFResource.class.getCanonicalName() + ", " + HelpExceptionMapper.class.getCanonicalName());

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initDatabase(DBHandler dbHandler){
        System.out.println("Scraping Data for Database");
        List<Pluggable> plugins;
        try {
            plugins = PluginLoader.loadPlugins(new File("./plugins"));

            System.out.println("Found Plugins:");
            for (String s :
                    Objects.requireNonNull(new File("./plugins").list(new JarFilenameFilter()))) {
                System.out.println(s);
            }
            for (Pluggable hs :
                    plugins) {
                RestServerStarter.hs = hs;
                hs.start();
                try {
                    instituteList.add(hs.getInstitute());
                    studiengangList.addAll(hs.getCurriculli());

                    ThreadCreator threadCreator = ThreadCreator.instantiate();
                    threadCreator.doJob(studiengangList);
                    threadCreator.doJob(veranstaltungList);
                }
                catch (IOException e){
                    System.err.println("Fatal error while gathering data. Exiting.");
                    System.exit(1);
                }

                dbHandler.clearDatabase();
                dbHandler.putIntoDatabase(instituteList, studiengangList,
                        veranstaltungList, terminList);

                instituteList.clear();
                studiengangList.clear();
                veranstaltungList.clear();
                terminList.clear();
            }
        } catch (IOException e) {
            System.err.println("No plugins found. Exiting.");
            //e.printStackTrace();
            System.exit(1);
        }

    }
}
