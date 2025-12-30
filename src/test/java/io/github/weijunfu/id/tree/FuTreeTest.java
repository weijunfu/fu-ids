package io.github.weijunfu.id.tree;

import io.github.weijunfu.id.json.FuJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FuTreeTest {

    private List<Menu> menuList;

    private List<FuTreeNode<Menu>> tree;

    @BeforeEach
    void setUp() {
        menuList = Arrays.asList(
                new Menu("1", "0", "首页", 1, "home", true),
                new Menu("2", "0", "产品", 2, "product", true),
                new Menu("3", "2", "手机", 1, "phone", true),
                new Menu("4", "2", "电脑", 2, "computer", true),
                new Menu("5", "3", "iPhone", 1, "iphone", true),
                new Menu("6", "3", "Android", 2, "android", true),
                new Menu("7", "4", "笔记本", 1, "laptop", false),
                new Menu("8", "4", "台式机", 2, "desktop", true)
        );

        // 测试包含扩展字段和label字段的树构建
        tree = FuTree.buildTree(
                menuList,
                "0",
                Menu::getId,
                Menu::getParentId,
                Menu::getSortOrder,
                Menu::getName,
                menu -> {
                    Map<String, Object> extensions = new HashMap<>();
                    extensions.put("icon", menu.getIcon());
                    extensions.put("visible", menu.getVisible());
                    return extensions;
                }
        );

    }

    @Test
    void testBuildTreeWithAllFeatures() {
        System.out.println(FuJson.toJson(tree));

        // 验证根节点数量
        assertEquals(2, tree.size());
    }
}