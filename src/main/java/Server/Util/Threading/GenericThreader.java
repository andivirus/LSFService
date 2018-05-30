package Server.Util.Threading;

import Server.Institute.Studiengang;
import Server.Institute.Veranstaltung;
import Server.RestServerStarter;

import java.util.List;

public class GenericThreader implements Runnable{
    List<?> innerCollection;

    public GenericThreader(List<?> list, final int i){
        innerCollection = list;
        System.out.println("GenericThread " + i);
    }

    @Override
    public void run(){
        final int STUDIENGANG = 0;
        final int VERANSTALTUNG = 1;
        int type = -1;
        for (Object o :
                innerCollection) {
            if(o instanceof Studiengang){
                type = STUDIENGANG;
            }
            else if (o instanceof Veranstaltung){
                type = VERANSTALTUNG;
            }
            break;
        }
        for (Object o :
                innerCollection) {
            if(type == STUDIENGANG){
                Studiengang s = (Studiengang) o;
                RestServerStarter.veranstaltungList.addAll(RestServerStarter.hs.getLectures(s.getId()));
            }
            if(type == VERANSTALTUNG){
                Veranstaltung va = (Veranstaltung) o;
                RestServerStarter.terminList.addAll(RestServerStarter.hs.getLectureTimes(va.getName(), va.getId()));
            }
        }
    }
}
