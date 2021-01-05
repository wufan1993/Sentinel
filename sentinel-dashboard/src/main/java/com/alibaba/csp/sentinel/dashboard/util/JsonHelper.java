package com.alibaba.csp.sentinel.dashboard.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 我本非凡
 * Date:2020-12-23
 * Time:16:12:44
 * Description:JsonHelper.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public class JsonHelper {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        // 解决实体未包含字段反序列化时抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 对于空的对象转json的时候不抛出错误
        //mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 允许属性名称没有引号
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }


    public static String toJson(Object o) {
        String asString = null;
        try {
            asString = mapper.writeValueAsString(o);
            return asString;
        } catch (JsonProcessingException e) {
            //log.error("序列化失败", e);
            throw new RuntimeException("序列化失败", e);
        }
    }



    /**
     * 获取泛型的Collection Type
     * @param collectionClass 泛型的Collection
     * @param elementClasses 元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static <T> List<T> fromJsonArray(String str, Class<T> clazz) {
        List<T> t;
        try {
            JavaType javaType = getCollectionType(ArrayList.class, clazz);

            t = mapper.readValue(str, javaType);
            return t;
        } catch (IOException e) {
            //log.error("序列化失败", e);
            throw new RuntimeException("序列化失败", e);
        }
    }


    public static <T> T fromJson(String str, Class<T> clazz) {
        T t;
        try {
            t = mapper.readValue(str, clazz);
            return t;
        } catch (IOException e) {
            //log.error("序列化失败", e);
            throw new RuntimeException("序列化失败", e);
        }
    }

}
