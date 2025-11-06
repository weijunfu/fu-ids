# fu-ids

> 基于Hashids实现的一个轻量级的加密ID工具库


## 安装

```xml
<dependency>
    <groupId>io.github.weijunfu</groupId>
    <artifactId>fu-ids</artifactId>
    <version>{last version}</version>
</dependency>

```

## 使用场景

### 1. 序列化 & 反序列化
> 支持两种序列化和反序列化方式：
> 1. 直接在字段上使用`FuIdSerializer` 和 `FuIdDeserializer`
> 2. 使用复合式注解`@JsonLong`
```java
public class Student implements Serializable {

  // 1.在字段上，直接使用 FuIdSerializer 和 FuIdDeserializer
  @JsonSerialize(using = FuIdSerializer.class)
  @JsonDeserialize(using = FuIdDeserializer.class)
  private Long id;

  // 2. JsonLong 为组合式注解，相当于使用了@JsonSerialize(using = FuIdSerializer.class)和@JsonDeserialize(using = FuIdDeserializer.class)
  @JsonLong
  private Long idSchool;

  private String name;

  // 数组、集合
  // 3. @JsonContentLong 为组合式主注解，相当于同时使用了@JsonSerialize(contentUsing = FuIdSerializer.class)和 @JsonDeserialize(contentUsing =  FuIdDeserializer.class)
  @JsonContentLong
  private Long[] nums;

  // Map<Long, ?>
  // 4.@JsonKeyLong 为组合式注解，相当于同时使用了@JsonSerialize(keyUsing = FuIdKeySerializer.class)和@JsonDeserialize(keyUsing =  FuIdKeyDeserializer.class)
  @JsonKeyLong
  private Map<Long, String> others;

}
```

### 2. 传参
```java
@RestController
@RequestMapping("/product")
public class SiteProductController {

  // 查询分类
  @GetMapping("/category/{id}")
  public AjaxResult getCategory(@PathVariable("id") FuId id) {

    // 使用 getValue方法获取Long类型值，注意 空值
    Long categoryId = id.getValue();

    return AjaxResult.success();
  }
}
```
