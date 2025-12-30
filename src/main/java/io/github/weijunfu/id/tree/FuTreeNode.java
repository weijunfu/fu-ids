package io.github.weijunfu.id.tree;

import java.util.List;
import java.util.Map;

public interface FuTreeNode<T> {

    T getId();

    T getParentId();

    // 新增 label 字段相关方法
    String getLabel(); // 获取显示标签
    void setLabel(String label); // 设置显示标签

    List<FuTreeNode<T>> getChildren();

    void setChildren(List<FuTreeNode<T>> children);

    // 新增排序相关方法
    Integer getSortOrder(); // 获取排序值

    void setSortOrder(Integer sortOrder); // 设置排序值

    // 新增扩展字段相关方法
    Map<String, Object> getExtensions(); // 获取扩展字段映射

    void setExtensions(Map<String, Object> extensions); // 设置扩展字段映射

    void addExtension(String key, Object value); // 添加单个扩展字段

    Object getExtension(String key); // 获取特定扩展字段

    default <E> E getExtension(String key, Class<E> type) {
        Object value = getExtension(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    default String getStringExtension(String key) {
        return getExtension(key, String.class);
    }

    default Integer getIntegerExtension(String key) {
        return getExtension(key, Integer.class);
    }

    default Boolean getBooleanExtension(String key) {
        return getExtension(key, Boolean.class);
    }
}
