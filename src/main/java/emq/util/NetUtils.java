package emq.util;

import java.net.HttpURLConnection;
import java.net.URL;


public class NetUtils {

    public NetUtils() {
    }

    public static boolean connectTest(String address) {
        boolean result = false;

        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setReadTimeout(3000);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                result = true;
            }

            conn.disconnect();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return result;
    }
}
