package mq.consumer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yangzhongyang
 */
public class RabbitConsumer {
    private static final String QUEUE_NAME = "queue_demo2";
    private static final String IP_ADDRESS = "192.168.1.166";
    private static final int PORT = 5672;


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        Address[] addresses = {new Address(IP_ADDRESS, PORT)};

        ConnectionFactory factory = new ConnectionFactory();
        //这里链接方式与生产者略有不同
        Connection connection = factory.newConnection(addresses);
        final Channel channel = connection.createChannel();
        //设置客户端最多接收未被ACK的消息个数
        channel.basicQos(64);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("Recv message: " + new String(body));
//                super.handleDelivery(consumerTag, envelope, properties, body);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(QUEUE_NAME, consumer);
        //等待回调函数执行完毕之后， 关闭资源
        TimeUnit.SECONDS.sleep(5);
        channel.close();
        connection.close();


    }
}
