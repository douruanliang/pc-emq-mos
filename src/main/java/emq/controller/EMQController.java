package emq.controller;

import emq.bean.BaseResponse;
import emq.server.MqttConnectJob;
import emq.server.MqttPushServer;
import emq.util.QosType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


@RestController
public class EMQController {

    private ExecutorService service = Executors.newCachedThreadPool();

    private static final Logger log = LoggerFactory.getLogger(EMQController.class);

    /**
     * @param uid
     * @param type
     * @param body
     * @return
     */
    @PostMapping("/sendMessage")
    public BaseResponse sendMessage(
            @RequestParam(value = "uid", required = true) String uid,
            @RequestParam(value = "type", required = false) int type,
            @RequestParam(value = "body", required = false) String body) {
        //这里将消息异步处理  使用futuretask，或者使用rabbimq进行异步处理或者spring的异步机制进行处理
        log.info(uid + type + body);
        FutureTask futureTask = new FutureTask(() -> {
            MqttPushServer.getInstance().publish(type, false, "user/" + uid, body);
            return true;
        });

        if (service != null) {
            service.submit(futureTask);
        }

        String msg = "";
        try {
            Boolean result = (Boolean) futureTask.get();
            if (result) {
                msg = "消息发送成功";

            } else {
                msg = "消息推送异常";
            }
            System.out.println(msg);
        } catch (Exception e) {
            msg = "消息推送异常";
            e.printStackTrace();
        }
        return new BaseResponse(0, msg);
    }


    @PostMapping("/sendGroupMessage")
    public BaseResponse sendGroupMessage(
            @RequestParam(value = "gid", required = true) String gid,
            @RequestParam(value = "type", required = false) int type,
            @RequestParam(value = "body", required = false) String body) {


        System.out.println("---sendGroupMessage--" + gid + type + body);
        //这里将消息异步处理  使用futuretask，或者使用rabbimq进行异步处理或者spring的异步机制进行处理
        FutureTask futureTask = new FutureTask(() -> {
            MqttPushServer.getInstance().publish(QosType.QOS_AT_LEAST_ONCE.getNumber(), false, "group/" + gid, body);
            return true;
        });

        if (service != null) {
            service.submit(futureTask);
        }

        String msg = "";
        try {
            Boolean result = (Boolean) futureTask.get();
            if (result) {
                msg = "群消息发送成功";

            } else {
                msg = "群消息推送异常";
            }
            System.out.println(msg);
        } catch (Exception e) {
            msg = "群消息推送异常";
            e.printStackTrace();
        }
        return new BaseResponse(0, msg);
    }


}
