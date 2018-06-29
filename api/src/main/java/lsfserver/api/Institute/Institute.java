package lsfserver.api.Institute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement ( name = "Institut" )
public class Institute {
    private String id;
    private String name;

    public Institute(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Notwendig fuer XML Antwort
    public Institute(){

    }

    @XmlElement
    public String getId() {
        return id;
    }

    @XmlElement
    public String getName() {
        return name;
    }
}
