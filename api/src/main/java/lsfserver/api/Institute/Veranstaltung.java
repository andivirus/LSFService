package lsfserver.api.Institute;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement ( name = "Veranstaltung" )
public class Veranstaltung {
    private String instituteid;
    private int id;
    private int stdid;
    private String name;

    public Veranstaltung(int id, String instituteid, int stdid, String name) {
        this.instituteid = instituteid;
        this.stdid = stdid;
        this.name = name;
        this.id = id;
    }

    public Veranstaltung(){
    }

    @XmlElement
    public String getInstituteid() {
        return instituteid;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    @XmlElement
    public int getStdid() {
        return stdid;
    }

    @Override
    public String toString() {
        return "Veranstaltung{" +
                "instituteid='" + instituteid + '\'' +
                ", id=" + id +
                ", stdid=" + stdid +
                ", name='" + name + '\'' +
                '}';
    }
}
