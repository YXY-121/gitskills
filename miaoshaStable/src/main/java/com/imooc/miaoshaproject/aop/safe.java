package com.imooc.miaoshaproject.aop;

import com.imooc.miaoshaproject.error.BusinessException;
import com.imooc.miaoshaproject.error.EmBusinessError;
import com.imooc.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: yxy
 * Date: 2021/4/6
 * Time: 10:06
 * 描述:
 */
//@Aspect
@Component
public class safe {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    //@Pointcut("execution(public * com.imooc.miaoshaproject.controller.OrderController.*.*(..))")
    public void isLogin(){

    }
  //  @Before("isLogin()")
    public void before() throws BusinessException {
        String token=httpServletRequest.getParameterMap().get("token")[0];
        System.out.println(token);
        // Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

        //获取用户的登陆信息
        //UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get("user_"+token);//登录后存到redis里
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
    }
}
