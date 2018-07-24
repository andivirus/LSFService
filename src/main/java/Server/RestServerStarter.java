package Server;

import Server.Util.Config.ConfigReader;
import Server.Util.Database.DBHandler;
import Server.Util.Plugin.JarFilenameFilter;
import Server.Util.Plugin.PluginLoader;
import Server.Util.Threading.ThreadCreator;
import Server.v1.LSFResourcev1;
import Server.v2.LSFResourcev2;
import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class RestServerStarter {
    public static Pluggable hs;
    public static final List<Institute> instituteList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Studiengang> studiengangList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Veranstaltung> veranstaltungList = Collections.synchronizedList(new LinkedList<>());
    public static final List<Termin> terminList = Collections.synchronizedList(new LinkedList<>());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        if(Arrays.asList(args).contains("-h")){
            System.out.println("--noupdate : does not recreate the database upon start, even if its old");
            System.exit(0);
        }
        new RestServerStarter(args);
    }

    public RestServerStarter(String[] args){
        long begin = System.currentTimeMillis();
        ConfigReader configReader = ConfigReader.instance();
        DBHandler dbHandler = DBHandler.getInstance(configReader.getProperty(ConfigReader.DATABASE_PATH));
        if(!Arrays.asList(args).contains("--noupdate")) {
            dbHandler.createDatabase();
                if (dbHandler.isUpdateNecessary()) {
                    //initDatabase(dbHandler);
                    ScheduledFuture<?> task = scheduler.schedule(new DBUpdater(dbHandler), 1, TimeUnit.SECONDS);
                    while(true){
                        if(task.isDone()){
                            break;
                        }
                    }
                }

        }
        long end = System.currentTimeMillis();

        System.out.println("This took: " + (end - begin) / 1000);

        try {
            System.out.println("Starting Server");

            Server server = new Server(Integer.valueOf(configReader.getProperty(ConfigReader.HOSTPORT)));

            final HandlerList handlerList = new HandlerList();

            buildSwagger(configReader);
            handlerList.addHandler(buildSwaggerUI());
            handlerList.addHandler(buildApi());




            server.setHandler(handlerList);

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void buildSwagger(ConfigReader configReader){
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage(LSFContract.class.getPackage().getName());
        beanConfig.setScan(true);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost(configReader.getProperty(ConfigReader.HOSTADRESS) + ":" + configReader.getProperty(ConfigReader.HOSTPORT));
        beanConfig.setBasePath("/");
    }

    private static ContextHandler buildApi(){
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(LSFResourcev1.class.getPackage().getName(),
                LSFResourcev2.class.getPackage().getName(),
                ApiListingResource.class.getPackage().getName());
        ServletContainer container = new ServletContainer(resourceConfig);
        ServletHolder holder = new ServletHolder(container);
        ServletContextHandler lsfcontext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        lsfcontext.setContextPath("/");
        lsfcontext.addServlet(holder, "/*");
        return lsfcontext;
    }

    private static ContextHandler buildSwaggerUI() throws URISyntaxException {
        final ResourceHandler swaggeruiresourcehandler = new ResourceHandler();
        swaggeruiresourcehandler.setResourceBase(RestServerStarter.class.getClassLoader().getResource("dist").toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        //swaggerUIContext.setContextPath("/docs/");
        swaggerUIContext.setContextPath("/docs");
        swaggerUIContext.setHandler(swaggeruiresourcehandler);
        return swaggerUIContext;
    }

    private void initDatabase(DBHandler dbHandler){
        System.out.println("Scraping Data for Database");
        List<Pluggable> plugins;
        try {
            plugins = PluginLoader.loadPlugins(new File("./plugins"));

            if (!plugins.isEmpty()) {
                System.out.println("Found Plugins:");
                for (String s :
                        new File("./plugins").list(new JarFilenameFilter())) {
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
                    } catch (IOException e) {
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
            }
        } catch(IOException e){
            System.err.println("No plugins found. Exiting.");
            System.exit(1);
        }
    }

    private class DBUpdater implements Runnable {
        private DBHandler dbHandler;

        DBUpdater(DBHandler dbHandler){
            this.dbHandler = dbHandler;
        }

        @Override
        public void run() {
            initDatabase(dbHandler);
            LocalDateTime now = LocalDateTime.now();
            LocalTime target = LocalTime.now().withHour(5).withMinute(0).withSecond(0).withNano(0);

            LocalDateTime targetDateTime = target.atDate(LocalDate.now());
            if(now.toLocalTime().isAfter(target)) {
                targetDateTime = targetDateTime.plusDays(1);
            }

            Duration timespan = Duration.between(now, targetDateTime);

            System.out.println("Next update: " + targetDateTime);
            System.out.println("Timespan: " + timespan.toString());

            scheduler.schedule(new DBUpdater(dbHandler), timespan.getSeconds(), TimeUnit.SECONDS);
        }
    }
}
