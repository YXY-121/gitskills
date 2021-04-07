package com.imooc.miaoshaproject.service;

import com.imooc.miaoshaproject.error.BusinessException;
import com.imooc.miaoshaproject.service.model.ItemModel;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.List;

/**
 * @author: yxy
 * Date: 2021/3/14
 * 描述:
 */
public interface ItemService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();
    public ItemModel getItemByIdInCache(Integer id);
    public ItemModel getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException, InterruptedException, RemotingException, MQClientException, MQBrokerException;

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount)throws BusinessException;

    List<ItemModel> listItemByCache();


    // 库存回滚
    boolean increaseStock(Integer itemId,Integer amount);


    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId,Integer amoumt) throws InterruptedException, RemotingException, MQClientException, MQBrokerException;

    //初始化库存流水
    String initStockLog(Integer itemId,Integer amount);

}
