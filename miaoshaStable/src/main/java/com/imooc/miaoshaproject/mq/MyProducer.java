package com.imooc.miaoshaproject.mq;


import com.alibaba.fastjson.JSON;
import com.imooc.miaoshaproject.dao.StockLogDOMapper;
import com.imooc.miaoshaproject.dataobject.StockLogDO;
import com.imooc.miaoshaproject.error.BusinessException;
import com.imooc.miaoshaproject.service.ItemService;
import com.imooc.miaoshaproject.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * @author: yxy
 * Date: 2021/3/18
 * Time: 21:48
 * 描述:
 */
@Component
public class MyProducer {
    private DefaultMQProducer producer;
    @Value("${mq.nameserver.addr}")
    private  String nameAddr;
    @Value("${mq.topicname}")
    private  String topicname;

   private TransactionMQProducer transactionMQProducer;
    @Autowired
    private OrderService orderService;

    @Autowired
    ItemService itemService;

    @Autowired
    StockLogDOMapper stockLogDOMapper;


    @PostConstruct
    public void init(){
        //做mq producer 的初始化
        producer =new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.setCreateTopicKey("AUTO_CREATE_TOPIC_KEY");
        try {

            transactionMQProducer=new TransactionMQProducer("producer_group_transaction");
            transactionMQProducer.setNamesrvAddr(nameAddr);
            transactionMQProducer.setTransactionListener(new TransactionListener() {
                @Override
                public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                    Integer itemId=(Integer) ((Map)arg).get("itemId");
                    Integer userId=(Integer) ((Map)arg).get("userId");
                    Integer promoId=(Integer) ((Map)arg).get("promoId");
                    Integer amount=(Integer) ((Map)arg).get("amount");
                    String stockLogId=(String) ((Map)arg).get("stockLogId");
                    try {
                        //这里执行无误 consumer才会进行第二步：在数据库减库存
                        orderService.createOrder(userId,itemId,promoId,amount,stockLogId);
                    } catch (BusinessException e) {
                        e.printStackTrace();
                        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                        stockLogDO.setStatus(3);
                        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }

                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                @Override
                public LocalTransactionState checkLocalTransaction(MessageExt msgs) {
                    //判断redis里是否扣减成功
                    String jsonString=new String(msgs.getBody());
                    Map<String,Object> map= JSON.parseObject(jsonString, Map.class);
                    Integer itemid= (Integer) map.get("itemId");
                    Integer amount= (Integer) map.get("amount");
                    String stockLogId=(String) map.get("stockLogId");
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    if(stockLogDO==null){
                        return LocalTransactionState.UNKNOW;
                    }
                    if(stockLogDO.getStatus().intValue()==2){
                        return LocalTransactionState.COMMIT_MESSAGE;
                    }
                    else if(stockLogDO.getStatus().intValue()==1){
                        return LocalTransactionState.UNKNOW;
                    }
                    System.out.println("checkLocal");
                    //根据itemid和amount 判断缓存里是否扣减  检查流水 增加交易转台
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            });
            transactionMQProducer.start();
            producer.start();

        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

   public boolean transactionAsyncReduceStock(Integer userId,Integer promoId,Integer itemId,Integer amount,String stockLogId){
       //用来减库存
        Map<String,Object>bodyMap=new HashMap<>();
       bodyMap.put("itemId",itemId);
       bodyMap.put("amount",amount);

        //用来生成订单
       Map<String,Object>argMap=new HashMap<>();
       argMap.put("itemId",itemId);
       argMap.put("amount",amount);
       argMap.put("userId",userId);
       argMap.put("promoId",promoId);
       argMap.put("stockLogId",stockLogId);
       //投送消息给consume ，这个消息是prepared 然后回调
       Message message=new Message(topicname,"increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
       TransactionSendResult transactionSendResult=null;
       try {
           //发出去先是prepare状态 prere状态的消息被pa存储在messagebroker里 -》 executeLocalTransaction
           //后面确认后才发送 执行真正的事情-》checkLocalTransaction
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message, argMap);
           System.out.println("已经发送生成订单信息");
       } catch (MQClientException e) {

           return false;
       }
       if(transactionSendResult.getLocalTransactionState()==LocalTransactionState.ROLLBACK_MESSAGE)
           return false;
       else if(transactionSendResult.getLocalTransactionState()==LocalTransactionState.COMMIT_MESSAGE) {
           return true;
       }
       return false;

    }




    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId,Integer amount) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        Map<String,Object>bodyMap=new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        Message message=new Message(topicname,"increase", JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));

              producer.send(message);
        System.out.println("发送扣减信息");
        return true;
    }
}
