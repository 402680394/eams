package com.ztdx.eams.basic;

import com.ztdx.eams.basic.exception.ApplicationException;
import com.ztdx.eams.basic.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class Interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws IOException {
        /*if (!httpServletRequest.getMethod().equals("OPTIONS")){
            //请求验证拦截
            if(!"/user/login".equals(httpServletRequest.getRequestURI())){
                UserCredential userCredential= (UserCredential) httpServletRequest.getSession().getAttribute("LOGIN_USER");
                if (null==userCredential) {
                    appendToResponse(httpServletRequest, httpServletResponse, new UnauthorizedException("请登录后进行操作"));
                }
            }
        }*/
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        if (e != null) {
            appendToResponse(httpServletRequest, httpServletResponse, e);
        }
    }

    private void appendToResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception exception) throws IOException {

        int errorCode = 500;
        if (exception instanceof ApplicationException) {
            errorCode = ((ApplicationException) exception).getCode();
        }
        String response = "{\"error\":{\"timestamp\":" + System.currentTimeMillis() + ",\"code\":" + errorCode + ",\"message\":\"" + exception.getMessage() + "\",\"path\":\"" + httpServletRequest.getServletPath() + "\"}}";

        String origin = httpServletRequest.getHeader("Origin");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpServletResponse.setHeader("Vary", "Origin");
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("Content-type", "application/json;charset=UTF-8");

        httpServletResponse.setStatus(errorCode);
        OutputStream os = httpServletResponse.getOutputStream();
        os.write(response.getBytes());
        os.flush();
        os.close();
    }
}
