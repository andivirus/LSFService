package Server;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement( name = "Server.TestDate" )
public class TestDate {
    private int starttime;
    private int endtime;
    private List<Participant> participants;


    @XmlElement
    public int getStarttime() {
        return starttime;
    }

    @XmlAttribute
    public int getEndtime() {
        return endtime;
    }


    @XmlElement ( name = "participant" )
    public List<Participant> getParticipants() {
        return participants;
    }


    public TestDate(int starttime, int endtime){
        this.starttime = starttime;
        this.endtime = endtime;

        this.participants = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            participants.add(new Participant("Name" + i));
        }

    }

    public TestDate(){

    }
}
