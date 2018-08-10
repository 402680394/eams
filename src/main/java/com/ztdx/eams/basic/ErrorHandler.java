package com.ztdx.eams.basic;

import com.ztdx.eams.basic.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
    @Value("${debug}")
    private boolean debug;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> defaultErrorHandler(HttpServletRequest httpServletRequest, Exception exception) {
        int errorCode;
        String message;

        errorCode = ((ApplicationException) exception).getCode();
        message = exception.getMessage();
        message = message.replaceAll("\"", "'");

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());
        result.put("code", errorCode);
        result.put("status", 200);
        result.put("message", message);
        if (debug){
            result.put("path", httpServletRequest.getServletPath());
            result.put("trace", Arrays.toString(exception.getStackTrace()));
            result.put("exception", exception.getClass().getName());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
