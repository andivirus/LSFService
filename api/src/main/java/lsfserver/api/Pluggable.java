package lsfserver.api;

import lsfserver.api.Institute.Institute;
import lsfserver.api.Institute.Studiengang;
import lsfserver.api.Institute.Termin;
import lsfserver.api.Institute.Veranstaltung;

import java.io.IOException;
import java.util.List;

public interface Pluggable {
    boolean start();
    boolean stop();

    Institute getInstitute();
    List<Studiengang> getCurriculli() throws IOException;

    /**
     *
     * @param stdgid Studiengangsid, publishid in der URL
     * @return Liste an Veranstaltungen
     */
    List<Veranstaltung> getLectures(int stdgid);

    List<Termin> getLectureTimes(String vaname, int vanummer);
}
