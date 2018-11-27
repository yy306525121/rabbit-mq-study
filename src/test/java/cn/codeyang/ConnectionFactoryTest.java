package cn.codeyang;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author yangzhongyang
 */
public class ConnectionFactoryTest {
    public static final String EXCHANGE_NAEM = "persistExchange";
    public static final String QUEUE_NAME = "persistQueue";
    public static final String ROUTING_KEY = "persistRouting";


    @Test
    public void testConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.166");
        factory.setPort(5672);
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        //创建一个 type="direct"、持久化的、非自动删除的交换器
        channel.exchangeDeclare(EXCHANGE_NAEM, "direct", true, false, null);
        //创建一个持久化、非排他的、非自动删除的队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        //将交换器与队列通过路由键绑定
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAEM, ROUTING_KEY);
        //发送一条持久化的消息: hello world !
        String message = "Hello World";
        channel.basicPublish(EXCHANGE_NAEM, ROUTING_KEY, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        channel.close();
        connection.close();


        System.out.println("end...");
    }
}