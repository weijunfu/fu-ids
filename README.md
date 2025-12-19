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

### 3. Id View

```java
JsonMapper mapper = new JsonMapper();

IdView idView = new IdView(1L);
System.out.println(mapper.writeValueAsString(idView));  // {"id":"jR"}

IdsView idsView = new IdsView(List.of(1L, 2L, 3L));
System.out.println(mapper.writeValueAsString(idsView)); // {"ids":["jR","k5","l5"]}
```

### 4. 金额

```java
public class Amount implements Serializable {

  private static final long serialVersionUID = 291217L;

  // 默认保留两位小数
  @JsonAmount
  private Double amount1;

  // 保留3位小数
  @JsonAmount(precision = 3)
  private Double amount2;

}
```

### 5. 雪花算法

```java
// 从配置中读取值，否则使用默认值
Snowflake snowflake = IdUtil.getSnowflake();

// 自定义机器码、数据中心码，从配置中读取起始时间戳
Snowflake snowflake = IdUtil.getSnowflake(1, 5);

// 自定义机器码、数据中心码、起始时间戳
Snowflake snowflake = IdUtil.getSnowflake(1, 5, 1609459200000L);
```

文件配置
```properties
fu-ids.snowflake.epoch=1609459200000L   # 起始时间戳（2021-01-01 00:00:00 UTC）
fu-ids.snowflake.workerId=1             #工作节点ID
fu-ids.snowflake.datacenterId=1         # 数据中心ID
```

### MD5

```java
String input = "Hello, World!";
System.out.println("原始字符串: " + input);
System.out.println("MD5 哈希值: " + MD5Util.get(input));
System.out.println("MD5 哈希值(大写): " + MD5Util.get(input, Boolean.TRUE));
```
### 安全

#### AES

```java
String key = generateKey(AESKeySizeEnum.K_128); // 或 256
System.out.println("密钥 (Base64): " + key);

String plaintext = "Sensitive data: credit card = 1234-5678-9012-3456";
System.out.println("明文: " + plaintext);

String encrypted = encrypt(plaintext, key);
System.out.println("密文 (Base64): " + encrypted);

String decrypted = decrypt(encrypted, key);
System.out.println("解密后: " + decrypted);
System.out.println("一致: " + plaintext.equals(decrypted));

// 尝试篡改密文（会抛出异常）
try {
  String tampered = encrypted.substring(0, encrypted.length() - 4) + "AAAA";
  decrypt(tampered, key);
} catch (Exception e) {
  System.out.println("✅ 篡改检测成功！解密失败（预期行为）: " + e.getClass().getSimpleName());
}
```
