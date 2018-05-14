package Server;

import Server.Institute.Institute;
import Server.Institute.Studiengang;
import Server.Institute.Termin;
import Server.Institute.Veranstaltung;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HSWorms {
    private String hsurl = "https://lsf.hs-worms.de/qisserver/rds?state=user" +
            "&type=8&topitem=lectures&breadCrumbSource=portal";

    private String instituteid;

    private Document connection;

    public HSWorms() throws IOException {
        connection = Jsoup.connect(hsurl).get();

        Pattern pattern = Pattern.compile("\\.(.*?)/");
        Matcher matcher = pattern.matcher(hsurl);
        if(matcher.find()) {
            instituteid = matcher.group(1);
        }
    }

    /**
     * Creates the Institute object
     * @return  An Institute object
     */
    public Institute getInstitue(){
        String title = connection.title().trim();
        return new Institute(instituteid, title);
    }

    /**
     * @return Eine Liste aller Studiengänge des Instituts
     * @throws IOException
     */
    public List<Studiengang> getCurriculli() throws IOException{

        Element element = connection.body();
        Elements link = element.getElementsContainingOwnText("Studiengangpläne (Liste)");
        if(!link.isEmpty()) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new IOException("Seite mit Studiengangplänen nicht gefunden");
    }

    /**
     *
     * @param stdgid
     * @return Eine Liste aller Veranstaltungen innerhalb des gegebenen Studiengangs
     */
    public List<Veranstaltung> getLectures(int stdgid){
        //TODO: Dynamischer machen, das Programm selbst auf den Link für Studiengangliste klicken lassen
        String url = "https://lsf.hs-worms.de/qisserver/rds?state=wplan&missing=allTerms&k_parallel.parallelid=" +
                "&k_abstgv.abstgvnr=" + stdgid + "&act=stg" +
                "&pool=stg&show=liste&P.vx=kurz&P.subc=plan";

        try {
            Document lecturespage = Jsoup.connect(url).get();

            Elements reihen = lecturespage.body().select("table[summary*=Veranstaltungen] > tbody > tr");
            List<Veranstaltung> veranstaltungList = new LinkedList<>();
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
            return veranstaltungList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param vaname
     * @param vanummer
     * @return Liefert alle Termine einer Veranstaltung
     */
    public List<Termin> getLectureTimes(String vaname, int vanummer){
        String url = "https://lsf.hs-worms.de/qisserver/rds?state=verpublish&status=init" +
                "&vmfile=no&publishid=" + vanummer + "&moduleCall=webInfo" +
                "&publishConfFile=webInfo&publishSubDir=veranstaltung";


        for(int count = 0; count < 5; count++) {
            try {
                Element body = Jsoup.connect(url).get().body();
                Elements tables = body.select("form > table[summary*=Veranstaltungstermine]");

                List<Termin> termine = new LinkedList<>();

                int termingruppe = 0;
                for (Element gruppe :
                        tables) {
                    termingruppe++;
                    Elements data = gruppe.select("tbody > tr:not(:first-child)");
                    int rowid = 0;
                    for (Element zeile :
                            data) {
                        rowid++;
                        int spaltenindex = 0;
                        TerminHelper terminHelper = new TerminHelper();
                        terminHelper.fach = vaname;
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
                                        String formatted = spalte.text().replace("bis", "").replace(".", "").trim();
                                        terminHelper.setStart_datum(formatted.substring(0, 7));
                                        terminHelper.setEnd_datum(formatted.substring(10));
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
                                //TODO: Ausfall implementieren
                                case (10):
                                    terminHelper.ausfall = spalte.text();
                            }
                            spaltenindex++;
                        }
                        termine.add(terminHelper.createTermin());
                    }
                }
                return termine;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public class TerminHelper {
        public int id;
        public int rowid;
        public String fach;
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
                e.printStackTrace();
            }
        }

        public Termin createTermin(){
            return new Termin(id, rowid, fach, tag, start_zeit, end_zeit,
                    start_datum, end_datum, raum, prof,
                    bemerkung, art, ausfall);
        }
    }
}
