package com.yinghe.whiteboardlib.callback;


import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.Callback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;

/**
 * Desc:出现回调类
 *
 * @author wang
 * @time 2017/4/11.
 */
public abstract class BeanCallBack<T> extends Callback<T> {
    @Override
    public T parseNetworkResponse(Response response, int id) throws Exception {
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            //如果用户写了泛型，就会进入这里，否者不会执行
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type beanType = parameterizedType.getActualTypeArguments()[0];

            String msg = response.body().string();
            if (beanType == String.class) {
                //如果是String类型，直接返回字符串
                return (T) msg;
            } else {
                //如果是 Bean List Map ，则解析完后返回
//                LogUtils.d("response-->\n" + msg);
                return new Gson().fromJson(msg, beanType);
            }
        } else {
            //如果没有写泛型，直接返回Response对象
            return (T) response;
        }
    }
}
