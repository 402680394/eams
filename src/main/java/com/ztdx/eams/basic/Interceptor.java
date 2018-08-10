package com.ztdx.eams.basic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class Interceptor implements HandlerInterceptor {

    private ErrorAttributes errorAttributes;

    public Interceptor(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        WorkContext.setRequest(httpServletRequest);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        WebRequest webRequest = new ServletWebRequest(httpServletRequest);
        if (((HandlerMethod) o).getMethod().getReturnType().getName().equals("void")
        && errorAttributes.getError(webRequest) == null) {
            appendToResponse(httpServletRequest, httpServletResponse, null);
        }
    }

    @Value("${debug}")
    private boolean debug;

    private void appendToResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception exception) throws IOException {

        int errorCode;
        String message;
        errorCode = 200;
        message = "success";

        StringBuilder response = new StringBuilder();
        response.append("{\"timestamp\":");
        response.append(System.currentTimeMillis());
        response.append(",\"code\":");
        response.append(errorCode);
        response.append(",\"status\":");
        response.append(errorCode);
        response.append(",\"message\":\"");
        response.append(message);
        response.append("\",\"path\":\"");
        response.append(httpServletRequest.getServletPath());
        response.append("\",\"data\":{}}");

        httpServletResponse.setStatus(200);
        OutputStream os = httpServletResponse.getOutputStream();
        os.write(response.toString().getBytes());
        os.flush();
        os.close();
    }
}
