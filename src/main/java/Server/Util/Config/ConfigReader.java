package Server.Util.Config;

import java.io.*;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

public class ConfigReader {
    public static final String DATABASE_PATH = "databasePath";
    public static final String UPDATE_TIME = "updateTime";
    public static final String HOSTADRESS = "hostadress";
    public static final String HOSTPORT = "hostport";

    private static String path = "props.cfg";
    private Properties properties;

    public ConfigReader(){
        File file = new File(path);

        System.out.println("FILE CAN WRITE: " + file.canWrite());
        if(!file.canWrite() || !file.canRead()){
            System.err.println("Cant write and/or read files from this directory.");
            System.err.println("Please fix permissions or move the application");
            System.exit(1);
        }

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
        //properties.setProperty(DATABASE_PATH, "./database/lsf.db");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter database path (default value: ./database/lsf.db): ");
        String input = sc.next();
        if(input.equals("")){
            properties.setProperty(DATABASE_PATH, "./database/lsf.db");
        }
        else {
            properties.setProperty(DATABASE_PATH, input);
        }
        //properties.setProperty(UPDATE_TIME, "6");
        //properties.setProperty(HOSTADRESS, "http://localhost");
        System.out.println("Please enter port (default: 8090): ");
        try {
            int in_val = sc.nextInt();
            properties.setProperty(HOSTPORT, String.valueOf(in_val));
        }
        catch (InputMismatchException e){
            properties.setProperty(HOSTPORT, "8090");
        }

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
        System.out.println("Getting Property: " + key + "\n" + "Value: " + properties.getProperty(key));
        return properties.getProperty(key);
    }
}
