package emq.server;

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


    /**
     * 初始化MqttClient
     */
    public void init() {
        System.out.println("--mqttClient-- init");
        //初始化连接设置对象
        mqttConnectOptions = new MqttConnectOptions();
        //true可以安全地使用内存持久性作为客户端断开连接时清除的所有状态
        mqttConnectOptions.setCleanSession(true);
        //设置连接超时
        mqttConnectOptions.setConnectionTimeout(10);
        //自动重连
        //mqttConnectOptions.setAutomaticReconnect(true);
        String clientId = "server";
        //设置账号密码
        mqttConnectOptions.setUserName(clientId);
        mqttConnectOptions.setPassword(clientId.toCharArray());
        //    设置持久化方式
        memoryPersistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(PropertiesUtil.MQTT_HOST, clientId, memoryPersistence);
            //设置连接和回调
            if (!mqttClient.isConnected()) {
//            创建连接
                try {
                    System.out.println("--mqttClient-- 创建连接");
                    mqttClient.connect(mqttConnectOptions);
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } catch (MqttException e) {
            e.printStackTrace();
            System.out.println("--mqttClient-- MqttException" + e.getMessage());
        }

        System.out.println("--mqttClient-- 连接成功否：" + mqttClient.isConnected());

        if (mqttClient.isConnected()) {
            try {
                //添加回调方法1
                mqttClient.setCallback(new PushCallback());

                mqttClient.subscribe("$SYS/broker/clients/expired");

                subscribe("$SYS/broker/clients/connected");

            } catch (Exception e) {
                System.out.println("--mqttClient--" + e.getMessage());
                e.printStackTrace();
            }
        }

    }


    /**
     * @param type
     * @param retained MQTT客户端向服务器发布(PUBLISH)消息时，
     *                 可以设置保留消息(Retained Message)标志。保留消息(Retained Message)会驻留在消息服务器，后来的订阅者订阅主题时仍可以接收该消息。
     * @param topic
     * @param body     这是一个 json - string
     */
    public void publish(int type, boolean retained, String topic, String body) {

        if (null != mqttClient) {
            if (!mqttClient.isConnected()) {
                try {
                    reconnect();
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
            MqttMessage message = new MqttMessage();
            message.setQos(QosType.QOS_AT_LEAST_ONCE.getNumber());
            message.setRetained(retained);
            message.setPayload(body.getBytes());
            MqttTopic mTopic = mqttClient.getTopic(topic);
            if (null == mTopic) {
                System.out.println("topic not exist");
            }
            MqttDeliveryToken token;

            synchronized (this){
                try {
                    assert mTopic != null;
                    token = mTopic.publish(message);
                    token.waitForCompletion(1000L);
                } catch (MqttException e) {
                    System.out.println("MqttException" + e.getMessage());
                    e.printStackTrace();
                }
            }

        }
    }

    public void reconnect() throws MqttException {
        System.out.println("重连 reconnect");
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
                mqttClient.disconnect();;
                mqttClient.connect(mqttConnectOptions);
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

}
