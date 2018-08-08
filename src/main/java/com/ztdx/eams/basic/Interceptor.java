package com.ztdx.eams.basic;

import com.ztdx.eams.basic.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Component
public class Interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        /*if (!httpServletRequest.getMethod().equals("OPTIONS")){
            //请求验证拦截
            if(!"/user/login".equals(httpServletRequest.getRequestURI())){
                UserCredential userCredential= (UserCredential) httpServletRequest.getSession().getAttribute("LOGIN_USER");
                if (null==userCredential) {
                    appendToResponse(httpServletRequest, httpServletResponse, new UnauthorizedException("请登录后进行操作"));
                }
            }
        }*/
        WorkContext.setRequest(httpServletRequest);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        
        if (e != null) {
            appendToResponse(httpServletRequest, httpServletResponse, e);
            //状态码是200，且请求方式不为get
        } else if (!httpServletRequest.getRequestURL().toString().endsWith("/user/login")
                && !(httpServletRequest.getRequestURL().toString().endsWith("/entry") && httpServletRequest.getMethod().equals("POST"))) {
            if (httpServletResponse.getStatus() == 200 && !httpServletRequest.getMethod().equals("GET")) {
                StringBuilder response = new StringBuilder();
                response.append("{\"message\":\"successful\"}");
                OutputStream os = httpServletResponse.getOutputStream();
                os.write(response.toString().getBytes());
                os.flush();
                os.close();
            }
        }
    }

    @Value("${debug}")
    private boolean debug;

    private void appendToResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception exception) throws IOException {

        int errorCode = 500;

        String message;
        if (exception instanceof ApplicationException) {
            errorCode = ((ApplicationException) exception).getCode();
            message = exception.getMessage();
        } else if (exception instanceof AccessDeniedException) {
            errorCode = 403;
            message = "您没有权限，请联系管理员";
        } else {
            message = "系统忙，请稍后重试";
        }

        message = message.replaceAll("\"", "'");

        StringBuilder response = new StringBuilder();
        response.append("{\"error\":{\"timestamp\":");
        response.append(System.currentTimeMillis());
        response.append(",\"code\":");
        response.append(errorCode);
        response.append(",\"message\":\"");
        response.append(message);
        response.append("\",\"path\":\"");
        response.append(httpServletRequest.getServletPath());
        if (debug) {
            response.append("\",\"class\":\"");
            response.append(exception.getClass().toString());
            response.append("\",\"stackTrace\":\"");
            response.append(Arrays.toString(exception.getStackTrace()));
            response.append("\",\"exception\":\"");
            response.append(exception.getMessage());
        }
        response.append("\"}}");

        String origin = httpServletRequest.getHeader("Origin");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpServletResponse.setHeader("Vary", "Origin");
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("Content-type", "application/json;charset=UTF-8");

        httpServletResponse.setStatus(200);
        OutputStream os = httpServletResponse.getOutputStream();
        os.write(response.toString().getBytes());
        os.flush();
        os.close();
    }
}
