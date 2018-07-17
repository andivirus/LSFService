package Server.Util.Config;

import java.io.*;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ConfigReader {
    public static final String DATABASE_PATH = "databasePath";
    public static final String UPDATE_TIME = "updateTime";
    public static final String HOSTADRESS = "hostadress";
    public static final String HOSTPORT = "hostport";

    private static String path = "props.cfg";
    private Properties properties;

    public ConfigReader(){
        File file = new File(path);

        File parent = new File(file.getAbsoluteFile().getParent());
        if(!parent.canWrite() || !parent.canRead()){
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
        Scanner sc = new Scanner(System.in).useDelimiter(Pattern.compile("\\n|(\\r\\n)"));
        System.out.println(sc.delimiter().toString());
        System.out.print("Please enter database path (default value: ./database/lsf.db): ");
        String input = sc.nextLine();
        System.out.println(input);
        if(input.equals("")){
            properties.setProperty(DATABASE_PATH, "./database/lsf.db");
        }
        else {
            properties.setProperty(DATABASE_PATH, input);
        }
        System.out.print("Please enter port (default: 8090): ");
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
