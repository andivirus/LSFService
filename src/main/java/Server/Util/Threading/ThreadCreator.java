package Server.Util.Threading;


import Server.RestServerStarter;
import lsfserver.api.Pluggable;

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

    public <T> void doJob(List<T> e, Pluggable hs, RestServerStarter.HSTask target){
        Set<Thread> threaders = new HashSet<>();
        final int coure_count = Runtime.getRuntime().availableProcessors();
        for (List<T> split :
                split(e, coure_count)) {
            threaders.add(new Thread(new GenericThread(split, hs, target)));
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
        int size = (int) Math.ceil(list.size() / number);
        for (int start = 0; start < list.size(); start += size) {
            int end = Math.min(start + size, list.size());
            List<T> sublist = list.subList(start, end);
            parts.add(new ArrayList<>(sublist));
        }

        return parts;
    }
}
