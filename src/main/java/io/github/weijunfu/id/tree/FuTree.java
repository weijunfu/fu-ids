package io.github.weijunfu.id.tree;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FuTree {

    /**
     * 将List转换为树形结构（支持扩展字段和label字段）
     *
     * @param list 原始列表
     * @param rootId 根节点ID
     * @param idGetter ID获取函数
     * @param parentIdGetter 父ID获取函数
     * @param sortOrderGetter 排序获取函数
     * @param labelGetter 标签获取函数
     * @param extensionGetter 扩展字段获取函数
     * @param <T> 元素类型
     * @return 树形结构列表
     */
    public static <T> List<FuTreeNode<T>> buildTree(
            List<T> list,
            String rootId,
            Function<T, String> idGetter,
            Function<T, String> parentIdGetter,
            Function<T, Integer> sortOrderGetter,
            Function<T, String> labelGetter,
            Function<T, Map<String, Object>> extensionGetter) {

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, FuTreeNode<T>> nodeMap = new ConcurrentHashMap<>();
        Map<String, List<FuTreeNode<T>>> childrenMap = new ConcurrentHashMap<>();

        // 第一次遍历：创建所有节点并建立映射
        for (T item : list) {
            String id = idGetter.apply(item);
            String parentId = parentIdGetter.apply(item);
            Integer sortOrder = sortOrderGetter != null ? sortOrderGetter.apply(item) : 0;
            String label = labelGetter != null ? labelGetter.apply(item) : null; // 获取 label
            Map<String, Object> extensions = extensionGetter != null ? extensionGetter.apply(item) : new HashMap<>();

            FuTreeNode<T> node = new DefaultFuTreeNode(id, parentId, label, item, sortOrder);
            node.setExtensions(extensions);
            nodeMap.put(id, node);
        }

        // 第二次遍历：建立父子关系
        for (T item : list) {
            String id = idGetter.apply(item);
            String parentId = parentIdGetter.apply(item);
            FuTreeNode<T> node = nodeMap.get(id);

            if (isRootNode(parentId, rootId)) {
                continue;
            } else {
                FuTreeNode<T> parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    List<FuTreeNode<T>> parentChildren = childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>());
                    parentChildren.add(node);
                }
            }
        }

        // 第三次遍历：设置子节点并排序
        for (Map.Entry<String, List<FuTreeNode<T>>> entry : childrenMap.entrySet()) {
            FuTreeNode<T> parent = nodeMap.get(entry.getKey());
            if (parent != null) {
                List<FuTreeNode<T>> sortedChildren = entry.getValue().stream()
                        .sorted(Comparator.comparing(FuTreeNode::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
                parent.setChildren(sortedChildren);
            }
        }

        // 返回根节点列表并排序
        return list.stream()
                .filter(item -> isRootNode(parentIdGetter.apply(item), rootId))
                .map(item -> nodeMap.get(idGetter.apply(item)))
                .sorted(Comparator.comparing(FuTreeNode::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }


    /**
     * 使用TreeNode接口实现的List转树形结构
     */
    public static <T extends FuTreeNode<T>> List<T> buildTreeFromNodes(List<T> list, String rootId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用Map建立ID到节点的映射，提高查找效率
        Map<T, T> nodeMap = new HashMap<>(list.size());
        for (T node : list) {
            nodeMap.put(node.getId(), node);
        }

        // 清空所有节点的children，重新构建关系
        for (T node : list) {
            node.setChildren(new ArrayList<>());
        }

        // 存储根节点
        List<T> rootNodes = new ArrayList<>();

        // 构建树形结构
        for (T node : list) {
            T parentId = node.getParentId();
            if (isRootNode(parentId, rootId)) {
                // 如果是根节点，直接添加到结果中
                rootNodes.add(node);
            } else {
                // 如果不是根节点，找到父节点并添加到父节点的children中
                T parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    // 如果找不到父节点，也作为根节点处理
                    rootNodes.add(node);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 判断是否为根节点
     */
    private static <T> boolean isRootNode(T parentId, T rootId) {
        // 判断父节点ID和根节点ID是否都为空，如果都为空则返回true
        if (Objects.isNull(parentId) && Objects.isNull(rootId)) {
            return true;
        }

        // 检查父节点ID与根节点ID是否同时存在且相等，如果相等则返回true表示为根节点
        if(Objects.nonNull(parentId) && Objects.nonNull(rootId) && parentId.equals(rootId)) {
            return true;
        }

        return false;
    }

    private FuTree() {}
}
