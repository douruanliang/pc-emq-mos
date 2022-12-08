package emq.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configuration
public class MqttProviderConfig {
    @Value("${spring.mqtt.username}")
    public String username;

    @Value("${spring.mqtt.password}")
    public String password;

    @Value("${spring.mqtt.host}")
    public String hostUrl;

    @Value("${spring.mqtt.client.id}")
    public String clientId;

    @Value("${spring.mqtt.default.topic}")
    public String defaultTopic;

}
