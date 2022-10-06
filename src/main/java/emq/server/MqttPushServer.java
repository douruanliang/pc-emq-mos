package emq.server;

import emq.bean.PushPayload;
import emq.util.PropertiesUtil;
import emq.util.QosType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPushServer {

    private MqttClient mqttClient;
    private static volatile MqttPushServer mqttPushClient = null;

    private static MemoryPersistence memoryPersistence = null;
    private static MqttConnectOptions mqttConnectOptions = null;
    //重连次数
    private int reConnTimes;

    public int getReConnTimes() {
        return this.reConnTimes;
    }

    public void setReConnTimes(int reConnTimes) {
        if (this.isConnected()) {
            reConnTimes = 0;
        }

        this.reConnTimes = reConnTimes;
    }

    public int getMaxReconnTimes() {
        return PropertiesUtil.MQTT_MAXRECONNECTTIMES;
    }

    public int getReconnInterval() {
        return PropertiesUtil.MQTT_RECONNINTERVAL;
    }

    public static MqttPushServer getInstance() {

        if (null == mqttPushClient) {
            synchronized (MqttPushServer.class) {
                if (null == mqttPushClient) {
                    mqttPushClient = new MqttPushServer();
                }
            }

        }
        return mqttPushClient;

    }

    private MqttPushServer() {
        init();
    }


    public void init() {
        System.out.println("--mqtt-- init");
        //初始化连接设置对象
        mqttConnectOptions = new MqttConnectOptions();
        //初始化MqttClient
        if (null != mqttConnectOptions) {
//            true可以安全地使用内存持久性作为客户端断开连接时清除的所有状态
            mqttConnectOptions.setCleanSession(true);
//            设置连接超时
            mqttConnectOptions.setConnectionTimeout(10);

            //设置账号密码
            //    mqttConnectOptions.setUserName(username);
            //    mqttConnectOptions.setPassword(password.toCharArray());
            //    设置持久化方式

            String clientId = PropertiesUtil.MQTT_CLIENTID;
            memoryPersistence = new MemoryPersistence();
            if (null != memoryPersistence && null != clientId) {
                try {
                    mqttClient = new MqttClient(PropertiesUtil.MQTT_HOST, clientId, memoryPersistence);
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {

            }
        } else {
            System.out.println("mqttConnectOptions对象为空");
        }
        //设置连接和回调
        if (null != mqttClient) {
            if (!mqttClient.isConnected()) {
//            创建连接
                try {
                    System.out.println("创建连接");
                    mqttClient.connect(mqttConnectOptions);
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } else {
            System.out.println("mqttClient为空");
        }

        System.out.println("连接成功否："+mqttClient.isConnected());

        if (mqttClient.isConnected()) {
            try {
                //添加回调方法1
                mqttClient.setCallback(new PushCallback());
            } catch (Exception e) {
                System.out.println("--mqttClient--"+ e.getMessage());
                e.printStackTrace();
            }
        }

    }


    /**
     * @param type
     * @param retained MQTT客户端向服务器发布(PUBLISH)消息时，
     *                 可以设置保留消息(Retained Message)标志。保留消息(Retained Message)会驻留在消息服务器，后来的订阅者订阅主题时仍可以接收该消息。
     * @param topic
     * @param body 这是一个 json - string
     */
    public void publish(int type, boolean retained, String topic, String body) {
        if (null != mqttClient && mqttClient.isConnected()) {

            System.out.println("---mTopic--" +topic);
            System.out.println("---publish--{ isConnected:}" + mqttClient.isConnected());
            System.out.println("---body--{id:}" + body);

           /* PushPayload pushMessage = PushPayload.getPushPayloadBuilder()
                    .setContent(body)
                    .setType(type)
                    .build();*/

            MqttMessage message = new MqttMessage();
            message.setQos(QosType.QOS_AT_LEAST_ONCE.getNumber());
            message.setRetained(retained);
            message.setPayload(body.getBytes());

            MqttTopic mTopic = mqttClient.getTopic(topic);
            if (null == mTopic) {
                // log.error("topic not exist");
                System.out.println("topic not exist");
            }
            MqttDeliveryToken token;
            try {
                token = mTopic.publish(message);
                /*if (!token.isComplete()){
                    System.out.println("消息发布成功");
                }*/
                token.waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            reConnect();
        }
    }

    private void reConnect() {
        System.out.println("重连 reConnect");
        if (null != mqttClient) {
            if (!mqttClient.isConnected()) {
                if (null != mqttConnectOptions) {
                    try {
                        mqttClient.connect(mqttConnectOptions);
                    } catch (MqttException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("mqttConnectOptions is null");
                }
            } else {
                System.out.println("mqttClient is null or connect");
            }
        } else {
            init();
        }
    }


    /**
     * 订阅某个主题，qos默认为0
     *
     * @param topic
     */
    public void subscribe(String topic) {
        subscribe(topic, 0);
    }

    /**
     * 订阅某个主题
     *
     * @param topic
     * @param qos
     */
    public void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //    订阅主题
    public void subTopic(String topic) {
        if (null != mqttClient && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic, 1);
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("mqttClient is error");
        }
    }

    //    清空主题
    public void cleanTopic(String topic) {
        if (null != mqttClient && !mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("mqttClient is error");
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }


    //    关闭连接
    public void closeConnect() {
        //关闭存储方式
        if (null != memoryPersistence) {
            try {
                memoryPersistence.close();
            } catch (MqttPersistenceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("memoryPersistence is null");
        }

//        关闭连接
        if (null != mqttClient) {
            if (mqttClient.isConnected()) {
                try {
                    mqttClient.disconnect();
                    mqttClient.close();
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                System.out.println("mqttClient is not connect");
            }
        } else {
            System.out.println("mqttClient is null");
        }
    }
   /*public static void main(String[] args) throws Exception {
        String kdTopic = "demo/topics";
        PushPayload pushMessage = PushPayload.getPushPayloadBuider()
                .setContent("designModel")
                .bulid();
        MqttPushServer.getInstance().publish(0, false, kdTopic, pushMessage);

    }*/
}
