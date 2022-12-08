package emq.server;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PushCallback implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(PushCallback.class);
    @Override
    public void connectionLost(Throwable throwable) {
        log.error("mqtt connectionLost 连接断开，5S之后尝试重连: {}", throwable.getMessage());
       // MsgJobManager.getInstance().addJob(new MqttConnectJob());
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("==========deliveryComplete={}==========", token.isComplete());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //服务端不用关心，客户端的业务
        log.info("接收消息主题 : " + topic);
        log.info("接收消息内容 : " + new String(message.getPayload()));
        // subscribe后得到的消息会执行到这里面
    }
}
