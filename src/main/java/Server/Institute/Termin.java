package Server.Institute;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Date;

@XmlRootElement ( name = "Termin" )
public class Termin {
    private int id;
    private int rowid; //Reihe innerhalb einer Gruppe
    private String fach;
    private int tag;
    private Calendar start_zeit;
    private Calendar end_zeit;
    private Date start_datum;
    private Date end_datum;
    private String raum;
    private String prof;
    private String bemerkung;
    private String art;
    private String ausfall;

    public Termin(int id, int rowid, String fach, int tag, Calendar start_zeit, Calendar end_zeit, Date start_datum, Date end_datum, String raum, String prof, String bemerkung, String art, String ausfall) {
        this.id = id;
        this.rowid = rowid;
        this.fach = fach;
        this.tag = tag;
        this.start_zeit = start_zeit;
        this.end_zeit = end_zeit;
        this.start_datum = start_datum;
        this.end_datum = end_datum;
        this.raum = raum;
        this.prof = prof;
        this.bemerkung = bemerkung;
        this.art = art;
        this.ausfall = ausfall;
    }

    public Termin(){
    }

    @Override
    public String toString() {
        return "Termin{" +
                "id=" + id +
                ", rowid=" + rowid +
                ", fach='" + fach + '\'' +
                ", tag=" + tag +
                ", start_zeit=" + start_zeit +
                ", end_zeit=" + end_zeit +
                ", start_datum=" + start_datum +
                ", end_datum=" + end_datum +
                ", raum='" + raum + '\'' +
                ", prof='" + prof + '\'' +
                ", bemerkung='" + bemerkung + '\'' +
                ", art='" + art + '\'' +
                ", ausfall='" + ausfall + '\'' +
                '}';
    }

    @XmlElement
    public int getId() {
        return id;
    }

    @XmlElement
    public int getRowid(){
        return rowid;
    }

    @XmlElement
    public String getFach() {
        return fach;
    }

    @XmlElement
    public int getTag() {
        return tag;
    }

    @XmlElement
    public Calendar getStart_zeit() {
        return start_zeit;
    }

    @XmlElement
    public Calendar getEnd_zeit() {
        return end_zeit;
    }

    @XmlElement
    public Date getStart_datum() {
        return start_datum;
    }

    @XmlElement
    public Date getEnd_datum() {
        return end_datum;
    }

    @XmlElement
    public String getRaum() {
        return raum;
    }

    @XmlElement
    public String getProf() {
        return prof;
    }

    @XmlElement
    public String getBemerkung() {
        return bemerkung;
    }

    @XmlElement
    public String getArt() {
        return art;
    }

    @XmlElement
    public String getAusfall() {
        return ausfall;
    }
}
