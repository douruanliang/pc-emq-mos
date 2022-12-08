package emq.util;

public class Log {

    public static void d(String tag, String msg){
        System.out.println(String.format(tag+",{===} %d", msg));
    }
}
