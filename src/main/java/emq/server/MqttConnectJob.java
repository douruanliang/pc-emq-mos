package emq.server;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttConnectJob implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MqttConnectJob.class);
    public static final int RETRY_COUNT = 5;

    private int retryCount;
    @Override
    public void run() {

        boolean success = false;
        Exception exception = null;
        for (retryCount=0; retryCount < RETRY_COUNT && !success; retryCount++) {
            try {
               /* if (!LoginManager.getInstance().isLogin()) {
                    Log.d(TAG,"can not find login info, so abandon doConnect!");
                    break;
                }*/
                doConnect();
                success = true;
            } catch (Exception e) {
                //Log.e(TAG,String.format("mqtt connect fail, retry count is %d", retryCount));
                exception = e;
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                success = false;
                if (e instanceof MqttException &&
                        ((MqttException) e).getReasonCode() == MqttException.REASON_CODE_FAILED_AUTHENTICATION) {
                    break;
                }

            }
        }
        if (!success) {
            if (exception != null) {
              System.out.println(String.format("connect : %s failed!! reason is %s", exception.toString()));
            }
        }
    }

    private void doConnect() throws MqttException {
        MqttPushServer.getInstance().reconnect();
    }
}
