package lsfserver.api.Institute;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement ( name = "Studiengang" )
public class Studiengang {
    private int id;
    private String name;
    private String hsid;

    public Studiengang(int id, String name, String hsid) {
        this.id = id;
        this.name = name;
        this.hsid = hsid;
    }

    public Studiengang(){
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
    public String getHsid(){
        return hsid;
    }

    @Override
    public String toString() {
        return "Studiengang{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hsid='" + hsid + '\'' +
                '}';
    }
}
