package io.github.weijunfu.id.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultFuTreeNode<T> implements FuTreeNode<T> {

    private T id;
    private T parentId;
    private T data;
    private String label; // 新增 label 字段
    private Integer sortOrder; // 新增排序字段
    private Map<String, Object> extensions = new HashMap<>(); // 扩展字段
    private List<FuTreeNode<T>> children = new ArrayList<>();

    // 添加带 label 的构造函数
    public DefaultFuTreeNode(T id, T parentId, String label, T data, Integer sortOrder) {
        this.id = id;
        this.parentId = parentId;
        this.data = data;
        this.sortOrder = sortOrder;
        this.label = label;
    }

    @Override
    public T getId() {
        return id;
    }

    @Override
    public T getParentId() {
        return parentId;
    }

    @Override
    public List<FuTreeNode<T>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<FuTreeNode<T>> children) {
        this.children = children;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Integer getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions != null ? extensions : new HashMap<>();
    }

    @Override
    public void addExtension(String key, Object value) {
        if (this.extensions == null) {
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
    }

    @Override
    public Object getExtension(String key) {
        return this.extensions != null ? this.extensions.get(key) : null;
    }
}
