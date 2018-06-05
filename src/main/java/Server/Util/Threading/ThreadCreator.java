package Server.Util.Threading;


import java.util.*;

public class ThreadCreator {

    private volatile static ThreadCreator singleton = null;

    private ThreadCreator(){

    }

    public static synchronized ThreadCreator instantiate(){

        if(singleton == null){
            singleton = new ThreadCreator();
        }
        return singleton;
    }

    public <T> void doJob(List<T> e){
        Set<Thread> threaders = new HashSet<>();
        int i = 0;
        for (List<T> split :
                split(e, 4)) {
            threaders.add(new Thread(new GenericThreader(split, i)));
            i++;
        }
        for (Thread t :
                threaders) {
            t.run();
        }
        for (Thread t :
                threaders) {
            try {
                t.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private <T> List<List<T>> split(List<T> list, final int number){
        List<List<T>> parts = new ArrayList<>();
        /*
        final int size = list.size();
        final int length = list.size()/number;
        for (int i = 0; i < size; i += length) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(size, i + length))));
            System.out.println("LENGTH: " + length);
        }
        */
        int size = (int) Math.ceil(list.size() / number);
        System.out.println(size);
        for (int start = 0; start < list.size(); start += size) {
            int end = Math.min(start + size, list.size());
            List<T> sublist = list.subList(start, end);
            parts.add(new ArrayList<>(sublist));
        }

        System.out.println(parts.size());
        return parts;
    }
}
