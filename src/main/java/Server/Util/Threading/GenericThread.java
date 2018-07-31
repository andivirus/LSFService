package Server.Util.Threading;

import Server.RestServerStarter;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;

import java.util.List;

public class GenericThread implements Runnable{
    private List<?> innerCollection;
    RestServerStarter.HSTask target;
    private Pluggable hs;

    public GenericThread(List<?> list, Pluggable hs, RestServerStarter.HSTask target){
        innerCollection = list;
        this.target = target;
        this.hs = hs;
        //System.out.println("GenericThread " + Thread.currentThread().getName());
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
                synchronized (target.veranstaltungList) {
                    target.veranstaltungList.addAll(hs.getLectures(s.getId()));
                }
            }
            if(type == VERANSTALTUNG){
                Veranstaltung va = (Veranstaltung) o;
                synchronized (target.terminList) {
                    target.terminList.addAll(hs.getLectureTimes(va.getName(), va.getId()));
                }
            }
        }
    }
}
