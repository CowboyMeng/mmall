package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;

/**
 * @Author: Chou_meng
 * @Date: 2017/11/27
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 序列化：对象的所有字段全部列入
        // 四个选项：
        // NON_NULL 非null字段序列化
        // NON_EMPTY    非empty字段序列化
        // NON_DEFAULT  非默认值字段序列化
        // ALWAYS   全部字段序列化
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        // 序列化：取消默认日期转换成timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 序列化：忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        // 序列化和反序列化：所有的日期格式都统一为以下的格式，即：yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        // 反序列时 忽略在json字符串中存在，但是在java对象中不存在对应属性的情况，防止错误。
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error", e);
            return null;
        }

    }

    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error", e);
            return null;
        }

    }

    // 针对单/多泛型序列化解决方案
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    // 针对多泛型反序列化解决方案一
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T) str : (T) objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    // 针对多泛型反序列化解决方案二
    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }


    public static void main(String[] args) {

//        TestPojo testPojo = new TestPojo();
//        testPojo.setId(1);
//        testPojo.setName("name");
//        {"id":1,"name":"name"}
//        String testPojoJson = JsonUtil.obj2String(testPojo);
//        User user = new User();
//        user.setId(1);
//        user.setUsername("choumeng");
//        user.setCreateTime(new Date());
//
//        String json = JsonUtil.obj2StringPretty(user);
//        log.info("json: {}", json);

//        User user2 = new User();
//        user2.setId(2);
//        user2.setUsername("zhoumeng");
//
//        String result1 = JsonUtil.obj2String(user);
//        String result2 = JsonUtil.obj2StringPretty(user);
//        User jsonUser1 = JsonUtil.string2Obj(result1, User.class);
//        User jsonUser2 = JsonUtil.string2Obj(result2, User.class);
//
//        log.info("result1: {}", result1);
//        log.info("result2: {}", result2);
//        log.info("jsonUser1: {}", jsonUser1.toString());
//        log.info("jsonUser2: {}", jsonUser2);
//
//        List<User> userList = Lists.newArrayList();
//        userList.add(user);
//        userList.add(user2);
//        String resultList = JsonUtil.obj2StringPretty(userList);
//        log.info("resultList: {}", resultList);
//
//        List<User> list1 = JsonUtil.string2Obj(resultList, new TypeReference<List<User>>() {
//        });
//
//        List<User> list2 = JsonUtil.string2Obj(resultList, List.class, User.class);

    }

}
