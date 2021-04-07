package com.imooc.miaoshaproject.error;

/**
 * @author: yxy
 * Date: 2021/3/12
 * 描述:
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);


}
