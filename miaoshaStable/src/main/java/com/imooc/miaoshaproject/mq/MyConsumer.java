package com.imooc.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import com.imooc.miaoshaproject.dao.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author: yxy
 * Date: 2021/3/19
 * Time: 0:08
 * 描述:
 */
@Component
public class MyConsumer {
    private DefaultMQPushConsumer consumer;
    @Value("${mq.nameserver.addr}")
    private  String nameAddr;
    @Value("${mq.topicname}")
    private  String topicname;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @PostConstruct
    public void init() throws MQClientException {
        consumer=new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicname,"*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
               //实现库存真正到1数据库内扣减的逻辑
                Message msg=msgs.get(0);
                String jsonString=new String(msg.getBody());
                Map<String,Object> map= JSON.parseObject(jsonString, Map.class);
                Integer itemid= (Integer) map.get("itemId");
                Integer amount= (Integer) map.get("amount");
                itemStockDOMapper.decreaseStock(itemid,amount);
                System.out.println("已经收到");

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
