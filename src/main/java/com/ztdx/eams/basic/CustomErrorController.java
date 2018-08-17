package com.ztdx.eams.basic;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomErrorController extends BasicErrorController {
    public CustomErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        this(errorAttributes, errorProperties, Collections.emptyList());
    }

    public CustomErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> body = super.error(request);

        Map<String, Object> map = new HashMap<>();
        map.put("system_timestamp", body.getBody().get("timestamp"));
        map.put("system_code", body.getStatusCodeValue());
        map.put("system_status", body.getBody().get("status"));
        if (body.getStatusCode() == HttpStatus.FORBIDDEN) {
            map.put("system_message", "禁止访问");
        }else if (body.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            map.put("system_message", "没有认证");
        }else{
            map.put("system_message", body.getBody().get("message"));
        }
        map.put("system_path", body.getBody().get("path"));
        map.put("system_exception", body.getBody().get("exception"));
        map.put("system_trace", body.getBody().get("trace"));

        return new ResponseEntity<>(map, body.getHeaders(), body.getStatusCode());
    }
}
