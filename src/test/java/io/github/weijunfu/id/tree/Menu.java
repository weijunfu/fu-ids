package io.github.weijunfu.id.tree;

public class Menu {

    private String id;
    private String parentId;
    private String name;
    private Integer sortOrder;
    private String icon;
    private Boolean visible;

    public Menu(String id, String parentId, String name, Integer sortOrder, String icon, Boolean visible) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.sortOrder = sortOrder;
        this.icon = icon;
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}

