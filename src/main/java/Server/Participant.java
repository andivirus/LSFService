package Server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "Server.Participant" )
public class Participant {

    String name;

    public Participant(String name) {
        this.name = name;
    }

    public Participant(){

    }

    @XmlElement
    public String getName() {
        return name;
    }

}
