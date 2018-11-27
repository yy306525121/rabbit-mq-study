#acmq消息模型
![AMQP协议模型](img.codeyang.cn/15433066397613.jpg)

# RabbitMQ中的Exchange类型
 * RoutingKey: 路由键。生产者将消息发给交换器 的时候， 一般会 指定 一个 RoutingKey，用 来指定这个消息的路由规则，而这个 RoutingKey 需要与交换器类型和绑定键 (BindingKey) 联 合使用才能最终生效。
 * Bining: 绑定。 RabbitMQ 中通过绑定将交换器与队列关联起来，在绑定的时候 一般会指定一个绑定键 (BindingKey)，这样 RabbitMQ就知道如何正确地将消息路由到队列了

## mandatory参数 vs immediate参数:
### mandatory
* 当mandatory设置为`true`时，交换器无法根据自身的类型和路由键找到队列时，将会调用`Basic.Return` 命令将消息返回给生产者。 当mandatory设置为`false`时,出现上述情况时则消息直接被丢弃
PS.
``` 
channel.basicPublish(EXCHANGE_NAME, "", true, MessageProperties.PERSISTENT_TEXT_PLAIN, "mandatory test".getBytes());
```

* 生产者可以通过调用 `channel.addReturnListener`来添加`ReturnListener`监听器实现来获取没有被正确路由的消息
```
channel.basicPublish(EXCHANGE_NAME, "", true, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body);
                System.out.println("这条消息没有被正确路由： " + message);
            }
        });
```

### immediate（3.0版本开始不支持，建议采用TTL和DLX的方法替代)
* 当immediate为 `true`时,如果交换器在将消息路由到队列时发现队列上并不存在任何消费者，那么这条消息将不会存入队列, 并将消息通过`Basic.Reture` 返回给生产者

## 过期时间（TTL）
设置过期时间有两种方式, 如果两种方法一起使用，过期时间以两者之间比较小的数值为准
* 通过队列属性设置
* 对消息本身进行单独设置

### 通过队列属性设置消息的TTL
需要在`channel.queueDeclare`方法中加入`x-message-ttl（毫秒）`参数
```
channel.exchangeDeclare(EXCHANGE_NAME, "direct", true, false, null);
//创建一个持久化、非排他的、非自动删除的队列
Map<String, Object> param = new HashMap<>();
param.put("x-message-ttl", 6000);
channel.queueDeclare(QUEUE_NAME, true, false, false, param);
```

如果不设置TTL表示此消息不会过期，如果将TTL设置为0，则表示除非此时可以直接将消息投递到消费者，否者该消息会被立即丢弃
### 针对每条消息设置TTL
```
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .expiration("6000")
                .build();
channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, properties, message.getBytes());
```
### 设置队列的TTL
队列的x-expires不能设置为0
```
 Map<String, Object> param = new HashMap<>();
param.put("x-expires", 18000);
channel.queueDeclare(QUEUE_NAME, false, false, false, param);
```

## 死信队列
DLX，全称为 Dead-Letter-Exchange，可以称之为死信交换器，也有人称之为死信邮箱。当 消息在一个队列中变成死信 (dead message) 之后，它能被重新被发送到另一个交换器中，这个 交换器就是 DLX，绑定 DLX 的队列就称之为死信队列。
消息编程死信一般是由于一下几种情况
* 消息被拒绝（Basic.Reject/Basic.Nack)，并且设置requeue参数为false
* 消息过期
* 队列达到最大长度

