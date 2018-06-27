package Server.Util.Config;

import java.io.*;
import java.util.Properties;

public class ConfigReader {
    public static final String DATABASE_PATH = "databasePath";
    public static final String UPDATE_TIME = "updateTime";
    public static final String HOSTADRESS = "hostadress";
    public static final String HOSTPORT = "hostport";

    private static String path = "props.cfg";
    private Properties properties;

    public ConfigReader(){
        File file = new File(path);
        properties = new Properties();
        if(!file.exists()) {
            setDefaultProps(file);
        }

        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            System.err.println("Couldn't load props.cfg");
            e.printStackTrace();
        }
    }

    private void setDefaultProps(File file){
        properties.setProperty(DATABASE_PATH, "./database/lsf.db");
        properties.setProperty(UPDATE_TIME, "6");
        properties.setProperty(HOSTADRESS, "http://localhost");
        properties.setProperty(HOSTPORT, "8090");

        try {
            file.createNewFile();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                properties.store(fileOutputStream, "--none--");
            } catch (FileNotFoundException e) {
                System.err.println("No props.cfg found.. for whatever reason..");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Couldn't write props.cfg file");
            e.printStackTrace();
        }
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }
}
