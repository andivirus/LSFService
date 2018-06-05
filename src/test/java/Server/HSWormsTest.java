package Server;

/*
import lsfserver.api.Institute.Institute;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class HSWormsTest {

    HSWorms hsWorms;
    @Before
    public void setUp() throws Exception {
        hsWorms = new HSWorms();
    }

    @Test
    public void getInstitue() {
        Institute institute = hsWorms.getInstitue();
        assertEquals("hs-worms.de", institute.getId());
        assertEquals("Hochschule Worms", institute.getName());
    }

    @Test
    public void getCurriculli() {
        try {
            LinkedList<Studiengang> studiengangList = (LinkedList<Studiengang>) hsWorms.getCurriculli();
            assertEquals(105, studiengangList.getFirst().getId());
            assertEquals("Angewandte Informatik (Bachelor of Science) (PO-Version 2012)", studiengangList.getFirst().getName());

            assertEquals(65, studiengangList.getLast().getId());
            assertEquals("Wirtschaftsinformatik Master (Master of Science) (PO-Version 2006)", studiengangList.getLast().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getLectures() {
        try {
            LinkedList<Veranstaltung> liste = (LinkedList<Veranstaltung>) hsWorms.getLectures(105);

            assertEquals(40591, liste.getLast().getId());
            assertEquals("WPF Fachenglisch", liste.getLast().getName());
            assertEquals("hs-worms.de", liste.getLast().getInstituteid());
        }
        catch (Exception e) {
        }
    }

    @Test
    public void getLectureTimes() {
        LinkedList<Termin> termine = (LinkedList<Termin>) hsWorms.getLectureTimes("Prozedurale Programmierung (Programmieren 1)", 40557);
        for (Termin termin:
             termine) {
            System.out.println(termin);
            System.out.println("----");
        }
    }

    @Test
    public void testError(){
        hsWorms.getLectureTimes("kektus", 41773);
        System.out.println("kek");
    }
}
*/