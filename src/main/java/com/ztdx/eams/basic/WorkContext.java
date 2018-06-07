package com.ztdx.eams.basic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 工作上下文
 */
public class WorkContext {
    private static ThreadLocal<HttpServletRequest> requestLocal=new ThreadLocal<HttpServletRequest>();

    private static HttpServletRequest getRequest(){
        return requestLocal.get();
    }

    public static void setRequest(HttpServletRequest request){
        requestLocal.set(request);
    }

    public static HttpSession getSession(){
        return (HttpSession)(getRequest()).getSession();
    }
}
