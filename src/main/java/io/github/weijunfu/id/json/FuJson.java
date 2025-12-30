package io.github.weijunfu.id.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

/**
 * Jackson JSON 工具类 - 完整封装
 */
public class FuJson {

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    static {
        // 基本配置
        DEFAULT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        DEFAULT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DEFAULT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        DEFAULT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

        // 日期时间处理
        DEFAULT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        DEFAULT_MAPPER.registerModule(new JavaTimeModule());
        DEFAULT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 值处理
        DEFAULT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 大小写不敏感
        DEFAULT_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    // ==================== 对象序列化 ====================

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return DEFAULT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonException("object to json fail", e);
        }
    }

    /**
     * 对象转字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        try {
            return DEFAULT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new JsonException("object to byte array fail", e);
        }
    }

    // ==================== 集合序列化 ====================

    /**
     * 集合转 JSON 字符串
     */
    public static <T> String collectionToJson(Collection<T> collection) {
        if (collection == null) {
            return "[]";
        }
        try {
            return DEFAULT_MAPPER.writeValueAsString(collection);
        } catch (JsonProcessingException e) {
            throw new JsonException("collection to json fail", e);
        }
    }

    /**
     * List 转 JSON 字符串
     */
    public static <T> String listToJson(List<T> list) {
        return collectionToJson(list);
    }

    /**
     * Set 转 JSON 字符串
     */
    public static <T> String setToJson(Set<T> set) {
        return collectionToJson(set);
    }

    /**
     * Map 转 JSON 字符串
     */
    public static <K, V> String mapToJson(Map<K, V> map) {
        if (map == null) {
            return "{}";
        }
        try {
            return DEFAULT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JsonException("Map to JSON fail", e);
        }
    }

    /**
     * 数组转 JSON 字符串
     */
    public static <T> String arrayToJson(T[] array) {
        if (array == null) {
            return "[]";
        }
        try {
            return DEFAULT_MAPPER.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            throw new JsonException("数组转JSON失败", e);
        }
    }

    // ==================== 反序列化 ====================

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转对象失败", e);
        }
    }

    /**
     * JSON 字符串转泛型对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转泛型对象失败", e);
        }
    }

    /**
     * JSON 字符串转 List
     */
    public static <T> List<T> jsonToList(String json, Class<T> elementType) {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        try {
            return DEFAULT_MAPPER.readValue(json,
                    DEFAULT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转List失败", e);
        }
    }

    /**
     * JSON 字符串转 Set
     */
    public static <T> Set<T> jsonToSet(String json, Class<T> elementType) {
        if (json == null || json.trim().isEmpty()) {
            return new HashSet<>();
        }
        try {
            return DEFAULT_MAPPER.readValue(json,
                    DEFAULT_MAPPER.getTypeFactory().constructCollectionType(Set.class, elementType));
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转Set失败", e);
        }
    }

    /**
     * JSON 字符串转 Map
     */
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return DEFAULT_MAPPER.readValue(json,
                    DEFAULT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType));
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转Map失败", e);
        }
    }

    /**
     * JSON 字符串转复杂 Map
     */
    public static <K, V> Map<K, V> jsonToMap(String json, TypeReference<Map<K, V>> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return DEFAULT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转复杂Map失败", e);
        }
    }

    /**
     * JSON 字符串转数组
     */
    public static <T> T[] jsonToArray(String json, Class<T> elementType) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return DEFAULT_MAPPER.readValue(json,
                    DEFAULT_MAPPER.getTypeFactory().constructArrayType(elementType));
        } catch (JsonProcessingException e) {
            throw new JsonException("JSON转数组失败", e);
        }
    }

    // ==================== 流操作 ====================

    /**
     * 从 InputStream 读取对象
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        try {
            return DEFAULT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new JsonException("从流读取JSON失败", e);
        }
    }

    /**
     * 从 InputStream 读取 List
     */
    public static <T> List<T> fromJsonToList(InputStream inputStream, Class<T> elementType) {
        try {
            return DEFAULT_MAPPER.readValue(inputStream,
                    DEFAULT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (IOException e) {
            throw new JsonException("从流读取JSON List失败", e);
        }
    }

    /**
     * 写入对象到 OutputStream
     */
    public static void writeJson(OutputStream outputStream, Object obj) {
        try {
            DEFAULT_MAPPER.writeValue(outputStream, obj);
        } catch (IOException e) {
            throw new JsonException("写入JSON到流失败", e);
        }
    }

    // ==================== 转换操作 ====================

    /**
     * 对象深度克隆（通过JSON序列化）
     */
    public static <T> T deepClone(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        String json = toJson(obj);
        return fromJson(json, clazz);
    }

    /**
     * 对象深度克隆（通过JSON序列化）泛型版本
     */
    public static <T> T deepClone(T obj, TypeReference<T> typeReference) {
        if (obj == null) {
            return null;
        }
        String json = toJson(obj);
        return fromJson(json, typeReference);
    }

    /**
     * 对象转换（通过JSON序列化）
     */
    public static <T, R> R convert(T source, Class<R> targetClass) {
        if (source == null) {
            return null;
        }
        String json = toJson(source);
        return fromJson(json, targetClass);
    }

    /**
     * Map 转对象
     */
    public static <T> T mapToObject(Map<?, ?> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return DEFAULT_MAPPER.convertValue(map, clazz);
    }

    /**
     * 对象转 Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        return DEFAULT_MAPPER.convertValue(obj,
                new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 对象转指定类型的 Map
     */
    public static <K, V> Map<K, V> objectToMap(Object obj, Class<K> keyType, Class<V> valueType) {
        if (obj == null) {
            return new HashMap<>();
        }
        return DEFAULT_MAPPER.convertValue(obj,
                DEFAULT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType));
    }

    // ==================== 高级操作 ====================

    /**
     * 提取 JSON 中指定路径的值
     */
    public static <T> T extractValue(String json, String path, Class<T> clazz) {
        try {
            JsonNode root = DEFAULT_MAPPER.readTree(json);
            JsonNode node = root.at(path);
            if (node.isMissingNode()) {
                return null;
            }
            return DEFAULT_MAPPER.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException("提取JSON值失败", e);
        }
    }

    /**
     * 提取 JSON 中指定路径的 List
     */
    public static <T> List<T> extractList(String json, String path, Class<T> elementType) {
        try {
            JsonNode root = DEFAULT_MAPPER.readTree(json);
            JsonNode node = root.at(path);
            if (!node.isArray()) {
                return new ArrayList<>();
            }

            List<T> result = new ArrayList<>();
            for (JsonNode element : node) {
                result.add(DEFAULT_MAPPER.treeToValue(element, elementType));
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new JsonException("提取JSON List失败", e);
        }
    }

    /**
     * 判断是否为有效 JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            DEFAULT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * JSON 压缩（去除空格和换行）
     */
    public static String compressJson(String json) {
        if (json == null) {
            return null;
        }
        return json.replaceAll("\\s+", "");
    }

    // ==================== 批量操作 ====================

    /**
     * 批量转换 List
     */
    public static <T, R> List<R> convertList(List<T> sourceList, Function<T, R> converter) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<R> result = new ArrayList<>(sourceList.size());
        for (T source : sourceList) {
            String json = toJson(source);
            R target = fromJson(json, new TypeReference<R>() {});
            result.add(target);
        }
        return result;
    }

    /**
     * 批量转换 List（指定目标类型）
     */
    public static <T, R> List<R> convertList(List<T> sourceList, Class<R> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        List<R> result = new ArrayList<>(sourceList.size());
        for (T source : sourceList) {
            result.add(convert(source, targetClass));
        }
        return result;
    }

    /**
     * 批量转换 Map
     */
    public static <K, V1, V2> Map<K, V2> convertMapValues(Map<K, V1> sourceMap, Class<V2> targetClass) {
        if (sourceMap == null || sourceMap.isEmpty()) {
            return new HashMap<>();
        }

        Map<K, V2> result = new HashMap<>(sourceMap.size());
        for (Map.Entry<K, V1> entry : sourceMap.entrySet()) {
            result.put(entry.getKey(), convert(entry.getValue(), targetClass));
        }
        return result;
    }

    // ==================== 自定义异常 ====================

    public static class JsonException extends RuntimeException {
        public JsonException(String message) {
            super(message);
        }

        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
