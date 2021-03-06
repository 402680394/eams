package com.ztdx.eams.basic.params;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JsonParamResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper jsonMapper;

    public JsonParamResolver(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(JsonParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        JsonNode jsonNode = getJsonNode(nativeWebRequest);
        JsonParam jsonParam = methodParameter.getParameterAnnotation(JsonParam.class);

        JsonNode paramNode;
        if (!StringUtils.isEmpty(jsonParam.path())){
            paramNode = getSubJsonNode(jsonNode, jsonParam.path());
        }else {
            paramNode = jsonNode.get(methodParameter.getParameterName());
        }

        if (paramNode == null) {
            // 基础类型验证
            if (methodParameter.getParameterType().isPrimitive()) {
                throw new Exception(methodParameter.getParameterName() + " 格式不正确");
            }
            return null;
        }
        return jsonMapper.treeToValue(paramNode, methodParameter.getParameterType());
    }

    private JsonNode getSubJsonNode(JsonNode jsonNode, String path) {
        String[] list = path.split("\\.");
        for (String nodeStr : list) {
            if (jsonNode == null){
                return null;
            }
            Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)]");

            Matcher matcher = pattern.matcher(nodeStr);
            if (matcher.find()){
                String fieldName = matcher.group(1);
                if (!jsonNode.hasNonNull(fieldName)){
                    return null;
                }
                jsonNode = jsonNode.get(fieldName);

                String indexStr = matcher.group(2);
                int index = Integer.parseInt(indexStr);

                jsonNode = jsonNode.get(index);
            }else {
                jsonNode = jsonNode.get(nodeStr);
            }
        }
        return jsonNode;
    }

    private JsonNode getJsonNode(NativeWebRequest nativeWebRequest) throws Exception {

        Object value = nativeWebRequest.getAttribute("JsonNode", RequestAttributes.SCOPE_REQUEST);
        if (value != null) {
            return (JsonNode) value;
        }

        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(httpServletRequest).getInputStream(), "utf-8"));
        StringBuilder requestBuilder = new StringBuilder("");
        String temp;
        while ((temp = br.readLine()) != null) {
            requestBuilder.append(temp);
        }
        String request = requestBuilder.toString().trim();
        if (StringUtils.isEmpty(request)) {
            throw new InvalidArgumentException("请求的Json数据为空异常");
        }
        JsonNode jsonNode = jsonMapper.readTree(request);
        nativeWebRequest.setAttribute("JsonNode", jsonNode, RequestAttributes.SCOPE_REQUEST);
        return jsonNode;
    }


}
