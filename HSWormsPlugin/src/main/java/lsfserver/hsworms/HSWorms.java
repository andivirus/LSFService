package lsfserver.hsworms;

import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Pluggable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class HSWorms implements Pluggable {
    private String hsurl = "https://lsf.hs-worms.de/qisserver/rds?state=user" +
            "&type=8&topitem=lectures&breadCrumbSource=portal";

    private String instituteid;

    //private Document connection;

    public HSWorms() throws IOException {
        Document connection = null;
        try {
            connection = Jsoup.connect(hsurl).get();
        }
        catch (UnknownHostException e){
            System.err.println("Host not found. Check your internet connection.");
        }

        Pattern pattern = Pattern.compile("\\.(.*?)/");
        Matcher matcher = pattern.matcher(hsurl);
        if(matcher.find()) {
            instituteid = matcher.group(1);
        }
    }

    @Override
    public boolean start() {
        System.out.println("Starting HSWorms plugin");
        return true;
    }

    @Override
    public boolean stop() {
        System.out.println("Stopping HSWorms plugin");
        return true;
    }

    /**
     * Creates the Institute object
     * @return  An Institute object
     */
    @Override
    public Institute getInstitute(){
        Document connection = null;
        try {
            connection = Jsoup.connect(hsurl).get();
        }
        catch (UnknownHostException e){
            System.err.println("Host not found. Check your internet connection.");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        String title = connection.title().trim();
        return new Institute(instituteid, title);
    }

    /**
     * @return Eine Liste aller Studiengänge des Instituts
     * @throws IOException
     */
    @Override
    public List<Studiengang> getCurriculli(){

        Document connection = null;
        try {
            connection = Jsoup.connect(hsurl).get();
        }
        catch (UnknownHostException e){
            System.err.println("Host not found. Check your internet connection.");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Element element = connection.body();
        Elements link = element.getElementsContainingOwnText("Studiengangpläne (Liste)");
        if(!link.isEmpty()) {
            for (int count = 0; count < 5; count++) {

                try {

                    Document currlistpage = Jsoup.connect(link.get(0).attr("href")).get();

                    element = currlistpage.body().selectFirst("table[summary=List of rooms] > tbody");

                    List<Studiengang> list = new LinkedList<>();
                    Elements reihen = element.getElementsByTag("tr");

                    for (Element reihe :
                            reihen) {

                        Elements spalten = reihe.getElementsByTag("td");
                        String name = spalten.first().text();
                        String id = spalten.last().child(0).attr("href");

                        Pattern pattern = Pattern.compile("abstgvnr=([0-9]*)");
                        Matcher matcher = pattern.matcher(id);
                        if(matcher.find()){
                            id = matcher.group(1);
                        }
                        list.add(new Studiengang(Integer.parseInt(id), name, instituteid));
                    }

                    return list;
                }
                catch (IOException e) {
                    if(count == 4){
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param stdgid
     * @return Eine Liste aller Veranstaltungen innerhalb des gegebenen Studiengangs
     */
    @Override
    public List<Veranstaltung> getLectures(int stdgid){
        String url = "https://lsf.hs-worms.de/qisserver/rds?state=wplan&missing=allTerms&k_parallel.parallelid=" +
                "&k_abstgv.abstgvnr=" + stdgid + "&act=stg" +
                "&pool=stg&show=liste&P.vx=kurz&P.subc=plan";

        for (int count = 0; count < 5; count++) {
            try {
                Document lecturespage = Jsoup.connect(url).get();

                Elements reihen = lecturespage.body().select("table[summary*=Veranstaltungen] > tbody > tr");
                Set<Veranstaltung> veranstaltungList = new HashSet<>();
                reihen.remove(0);

                for (Element reihe :
                        reihen) {

                    Elements spalten = reihe.children();
                    Pattern pattern = Pattern.compile("publishid=([0-9]*)");
                    Matcher matcher = pattern.matcher(spalten.get(1).child(0).attr("href"));

                    int id;

                    if(matcher.find()){
                        id = Integer.valueOf(matcher.group(1));
                        String name = spalten.get(1).text();

                        veranstaltungList.add(new Veranstaltung(id, instituteid, stdgid, name));
                    }
                }
                List<Veranstaltung> veranstaltungsList = new LinkedList<>();
                veranstaltungsList.addAll(veranstaltungList);
                return veranstaltungsList;
            } catch (IOException e) {
                if(count == 4) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param vaname
     * @param vanummer
     * @return Liefert alle Termine einer Veranstaltung
     */
    @Override
    public List<Termin> getLectureTimes(String vaname, int vanummer){
        String url = "https://lsf.hs-worms.de/qisserver/rds?state=verpublish&status=init" +
                "&vmfile=no&publishid=" + vanummer + "&moduleCall=webInfo" +
                "&publishConfFile=webInfo&publishSubDir=veranstaltung";


        for(int count = 0; count < 5; count++) {
            try {
                Element body = Jsoup.connect(url).get().body();
                Elements tables = body.select("form > table[summary*=Veranstaltungstermine]");

                Set<Termin> termine = new HashSet<>();

                int termingruppe = 0;
                for (Element gruppe :
                        tables) {
                    termingruppe++;
                    Elements data = gruppe.select("tbody > tr:not(:first-child)");
                    String art = gruppe.select(".t_capt").text().replace("Termine Gruppe: ", "").trim();
                    int rowid = 0;
                    for (Element zeile :
                            data) {
                        rowid++;
                        int spaltenindex = 0;
                        TerminHelper terminHelper = new TerminHelper();
                        terminHelper.art = art;
                        terminHelper.fach = vaname;
                        terminHelper.fachid = vanummer;
                        terminHelper.rowid = rowid;
                        for (Element spalte :
                                zeile.children()) {
                            terminHelper.id = termingruppe;
                            switch (spaltenindex) {
                                case 1:
                                    terminHelper.setTag(spalte.text());
                                    break;
                                case 2:
                                    terminHelper.setZeit(spalte.text());
                                    break;
                                case 4:
                                    if (spalte.text().contains("am")) {
                                        String formatted = spalte.text().replace("am ", "").replace(".", "").trim();
                                        terminHelper.setStart_datum(formatted);
                                        terminHelper.setEnd_datum(formatted);
                                    }
                                    if (spalte.text().contains("bis")) {
                                        String[] zeiten = spalte.text().split("bis");
                                        if(zeiten[0].length() >= 11){
                                            terminHelper.setStart_datum(zeiten[0].replaceAll("[^0-9]", ""));
                                        }
                                        else {
                                            System.out.println("StartDatum in wrong format");
                                        }
                                        if(zeiten[1].length() >= 11){
                                            terminHelper.setEnd_datum(zeiten[1].replaceAll("[^0-9]", ""));
                                        }
                                        else {
                                            System.out.println("EndDatum in wrong format");
                                            System.err.println(vaname);
                                            System.err.println(art);
                                            System.err.println(rowid);
                                            System.err.println(vanummer);
                                            for (String s :
                                                    zeiten) {
                                                System.out.println(s);
                                            }
                                        }
                                    }
                                    break;
                                case (5):
                                    terminHelper.raum = spalte.text();
                                    break;
                                case (7):
                                    terminHelper.prof = spalte.text();
                                    break;
                                case (9):
                                    terminHelper.bemerkung = spalte.text();
                                    break;
                                case (10):
                                    terminHelper.ausfall = spalte.text();
                            }
                            spaltenindex++;
                        }
                        termine.add(terminHelper.createTermin());
                    }
                }
                List<Termin> terminList = new LinkedList<>();
                terminList.addAll(termine);
                return terminList;
            } catch (IOException e) {
                System.out.println("Retrying for the " + count + " time");
                if(count == 4) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public class TerminHelper {
        public int id;
        public int rowid;
        public String fach;
        public int fachid;
        public int tag;
        public Calendar start_zeit;
        public Calendar end_zeit;
        public Date start_datum;
        public Date end_datum;
        public String raum;
        public String prof;
        public String bemerkung;
        public String art;
        public String ausfall;

        public void setTag(String tag) {
            switch (tag) {
                case "Mo.":
                    this.tag = 0;
                    break;
                case "Di.":
                    this.tag = 1;
                    break;
                case "Mi.":
                    this.tag = 2;
                    break;
                case "Do.":
                    this.tag = 3;
                    break;
                case "Fr.":
                    this.tag = 4;
                    break;
                case "Sa.":
                    this.tag = 5;
                    break;
                case "So.":
                    this.tag = 6;
                    break;
            }
        }

        public void setZeit(String zeit){
            Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{1,2})\\D*(\\d{1,2}:\\d{1,2})");
            Matcher matcher = pattern.matcher(zeit);
            if(matcher.find()){

                start_zeit = new SimpleDateFormat("HHmm").getCalendar();
                /* Setting Start time */
                start_zeit.set(Calendar.HOUR_OF_DAY, Integer.valueOf(matcher.group(1).substring(0, 2)));
                start_zeit.set(Calendar.MINUTE, Integer.valueOf(matcher.group(1).substring(3)));

                /* Setting finish time */
                end_zeit = new SimpleDateFormat("HHmm").getCalendar();
                end_zeit.set(Calendar.HOUR_OF_DAY, Integer.valueOf(matcher.group(2).substring(0, 2)));
                end_zeit.set(Calendar.MINUTE, Integer.valueOf(matcher.group(2).substring(3)));
            }
            else {
                start_zeit = new SimpleDateFormat("HHmm").getCalendar();
                start_zeit.set(Calendar.HOUR_OF_DAY, 0);
                start_zeit.set(Calendar.MINUTE, 0);

                end_zeit = new SimpleDateFormat("HHmm").getCalendar();
                end_zeit.set(Calendar.HOUR_OF_DAY, 23);
                end_zeit.set(Calendar.MINUTE, 59);
            }
        }
        public void setStart_datum(String start_datum) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
            try {
                this.start_datum = dateFormat.parse(start_datum);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public void setEnd_datum(String end_datum) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
            try {
                this.end_datum = dateFormat.parse(end_datum);
            } catch (ParseException e) {
                System.err.println(fach);
                System.err.println(prof);
                System.err.println(raum);
                e.printStackTrace();
            }
        }

        public Termin createTermin(){
            return new Termin(id, rowid, fach, fachid, tag, start_zeit, end_zeit,
                    start_datum, end_datum, raum, prof,
                    bemerkung, art, ausfall);
        }
    }
}
