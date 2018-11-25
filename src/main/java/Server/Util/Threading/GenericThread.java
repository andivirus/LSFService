package Server.Util.Threading;

import Server.RestServerStarter;
import Server.Util.Database.DBHandler;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;
import lsfserver.api.Pluggable;

import java.util.LinkedList;
import java.util.List;

public class GenericThread implements Runnable{
    private List<?> innerCollection;
    private Pluggable hs;

    public GenericThread(List<?> list, Pluggable hs){
        innerCollection = list;
        this.hs = hs;
    }

    @Override
    public void run(){
        LinkedList<Veranstaltung> veranstaltungList = new LinkedList<>();
        LinkedList<Termin> terminList = new LinkedList<>();
        DBHandler dbHandler = DBHandler.getInstance(DBHandler.DB_URL);
        for (Object o :
                innerCollection) {
                Studiengang s = (Studiengang) o;
                veranstaltungList.addAll(hs.getLectures(s.getId()));

        }
        dbHandler.putVeranstaltungenIntoDatabase(veranstaltungList);
        for (Veranstaltung va :
                veranstaltungList) {
            terminList = new LinkedList<>(hs.getLectureTimes(va.getName(), va.getId()));
        }
        dbHandler.putTermineIntoDatabase(terminList);
    }
}
