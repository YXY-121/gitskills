package com.imooc.miaoshaproject.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.imooc.miaoshaproject.mq.MyProducer;
import com.imooc.miaoshaproject.service.ItemService;
import com.imooc.miaoshaproject.service.OrderService;
import com.imooc.miaoshaproject.error.BusinessException;
import com.imooc.miaoshaproject.error.EmBusinessError;
import com.imooc.miaoshaproject.response.CommonReturnType;
import com.imooc.miaoshaproject.service.model.OrderModel;
import com.imooc.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

/**
 * @author: yxy
 * Date: 2021/3/14
 * 描述:
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MyProducer producer;
    private ExecutorService executorService;
    private RateLimiter rateLimiter;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);

    }

    @Autowired
    private ItemService itemService;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId) throws BusinessException {

        UserModel userModel = null;
        String token=httpServletRequest.getParameterMap().get("token")[0];
        System.out.println(token);
       // Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

        //获取用户的登陆信息
        //UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
       userModel = (UserModel)redisTemplate.opsForValue().get("user_"+token);//登录后存到redis里
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

        //或者是存在header里 每次来就从加密的token里解绑并且拿到id 但是这样 还是要通过id去redis里查询= =、
//
//        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
//
        if(redisTemplate.hasKey("item_stock_zero"+itemId))
            throw  new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);



//        String token=httpServletRequest.getParameterMap().get("token")[0];
//        userModel = (UserModel)redisTemplate.opsForValue().get("user_"+token);//登录后存到redis里
//        if(userModel == null){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
//        }

        String stockLogId = itemService.initStockLog(itemId, amount);
        //j加入库存流水init状态

        if (!producer.transactionAsyncReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId)) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        }
/*        //拥塞窗口为20的等待队列，用来队列化泄洪
     Future<Object>future= executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw  new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw  new BusinessException(EmBusinessError.UNKNOWN_ERROR);

        }

    }*/
        return CommonReturnType.create(null);
    }
}
