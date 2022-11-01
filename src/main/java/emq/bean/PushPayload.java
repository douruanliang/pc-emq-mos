package emq.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * mqtt 消息推送实体
 */
@Slf4j
@Setter
@Getter
public class PushPayload {

    //推送类型
    private int type;
    //内容
    private String body;

    public PushPayload(int type, String content) {
        this.type = type;
        this.body = content;
    }

    public static class Builder {
        //推送类型
        private int type;
        //内容
        private String body;

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        /* public Builder setMobile(String mobile) {
             this.mobile = mobile;
             return this;
         }
           public Builder setBadge(Integer badge) {
             this.badge = badge;
             return this;
         }

         public Builder setSound(String sound) {
             this.sound = sound;
             return this;
         }
         public Builder setTitle(String title) {
             this.title = title;
             return this;
         }
 */
        public Builder setContent(String content) {
            this.body = content;
            return this;
        }


        public PushPayload build() {
            return new PushPayload(type, body);
        }


    }


    public static Builder getPushPayloadBuilder() {
        return new Builder();
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.DisableCircularReferenceDetect);
    }




}
