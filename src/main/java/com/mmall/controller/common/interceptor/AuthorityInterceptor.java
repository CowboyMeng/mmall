package com.mmall.controller.common.interceptor;

import com.github.pagehelper.StringUtil;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: Chou_meng
 * @Date: 2017/12/8
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        log.info("preHandle");

        // 请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) o;

        // 解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        // 解析参数，具体的key以及value是什么，我们打印日志
        StringBuffer requestParamBuffer = new StringBuffer();

        Map paramMap = httpServletRequest.getParameterMap();
        Iterator iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            // request参数的map，里面的value返回的是一个String[]
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        // 解决登录不进而一直循环的问题，即对特殊的请求url进行特殊处理。
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("权限拦截器拦截到请求,className:{},methodName:{}", className, methodName);
            // 如果拦截到的是登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            return true;
        }

        log.info("权限拦截器拦截到请求,className:{},methodName:{},param:{}", className, methodName, requestParamBuffer.toString());

        User user = null;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtil.isNotEmpty(loginToken)) {
            String userJson = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJson, User.class);
        }

        if (user == null || user.getRole().intValue() != Const.Role.ROLE_ADMIN) {
            httpServletResponse.reset(); // 这里要reset，否则会报getWritter() has already been called
            httpServletResponse.setCharacterEncoding("UTF-8"); //不设置会乱码
            httpServletResponse.setContentType("application/json;charset=UTF-8"); // 这里要设置返回值的类型，因为全部是json接口

            PrintWriter out = httpServletResponse.getWriter();

            // 上传由于富文本的控件要求，要特殊处理返回值，这里面区分是否登录以及是否有权限
            if (user == null) {
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", "false");
                    resultMap.put("msg", "请登录管理员");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
                }
            } else {
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", "false");
                    resultMap.put("msg", "无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("拦截器拦截，用户无权限操作")));
                }
            }

            out.flush();
            out.close(); // 这里要关闭

            return false;

        }

        // 该方法的返回值决定了是否进入相应的controller层中的方法
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.info("afterCompletion");
    }
}
