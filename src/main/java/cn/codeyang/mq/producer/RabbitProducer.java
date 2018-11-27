package cn.codeyang.mq.producer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yangzhongyang
 */
public class RabbitProducer {
    private static final String EXCHANGE_NAME = "exchange_demo2";
    private static final String ROUTING_KEY = "routing_demo2";
    private static final String QUEUE_NAME = "queue_demo2";
    private static final String IP_ADDRESS = "192.168.1.166";
    private static final int PORT = 5672;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(IP_ADDRESS);
        factory.setPort(PORT);

        //创建链接
        Connection connection = factory.newConnection();
        //创建信道
        Channel channel = connection.createChannel();

        //创建一个 type="direct"、持久化的、非自动删除的交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
        //创建一个持久化、非排他的、非自动删除的队列
        Map<String, Object> param = new HashMap<>();
        param.put("x-message-ttl", 6000);
        channel.queueDeclare(QUEUE_NAME, true, false, false, param);
        //将交换器与队列通过路由键绑定
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        //发送一条持久化的消息: hello world !
        String message = "Hello World";
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());



        channel.close();
        connection.close();

        TimeUnit.SECONDS.sleep(10);
        System.out.println("end...");
    }
}
