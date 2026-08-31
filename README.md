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
// 获取线程安全的默认单例：workerId=1、datacenterId=1、epoch=1609459200000L
Snowflake snowflake = IdUtil.getSnowflake();

// 自定义机器码、数据中心码，epoch 使用默认值
Snowflake snowflake = IdUtil.getSnowflake(1, 5);

// 自定义机器码、数据中心码、起始时间戳
Snowflake snowflake = new Snowflake(1, 5, 1609459200000L);
```

默认配置通过 `Snowflake.getInstance()` 延迟初始化为单例，`IdUtil.getSnowflake()` 和 `IdUtil.getSnowflakeNextId()` 均复用该实例。
Snowflake 不读取 `application.yml`、`application.yaml` 或 `application.properties`。需要修改节点参数时，请通过构造函数显式传入并复用同一实例。

### 6. Nano ID

Nano ID 使用 `SecureRandom` 生成不可预测的 URL 安全字符串。默认长度为 21，包含约 126 bit 随机性。

```java
// 默认生成 21 位 URL 安全 ID
String id = IdUtil.getNanoId();

// 指定长度
String shortId = IdUtil.getNanoId(12);

// 自定义字母表和长度
String hexId = IdUtil.getNanoId("0123456789abcdef", 16);

// 也可以通过 io.github.weijunfu.nanoid.NanoId 直接调用
String directId = NanoId.randomNanoId();
```

自定义字母表必须包含 1 至 256 个不重复字符。实现使用拒绝采样，避免直接取模造成字符分布不均。

### 兼容性哈希（非安全）

#### MD5（Deprecated）

> `MD5Util` 仅保留给历史兼容、非安全校验用途。不要用于密码、签名、防篡改、Token 或任何安全完整性校验。

```java
String input = "Hello, World!";
System.out.println("原始字符串: " + input);
System.out.println("MD5 哈希值: " + MD5Util.get(input));
System.out.println("MD5 哈希值(大写): " + MD5Util.get(input, Boolean.TRUE));
```
### 安全

#### 密码存储

不要使用 `MD5Util`、`SHA256Util` 或 `HmacSHA256Util` 直接处理密码。

密码存储应使用 BCrypt、Argon2 或 PBKDF2，并为每个密码使用独立随机 salt 和合理成本参数。

#### SHA-256 / HMAC-SHA256

`SHA256Util` 适合非密码摘要；`HmacSHA256Util` 适合带密钥的消息认证。

```java
String digest = SHA256Util.digestToHex("Hello, World!");

String hmacKey = HmacSHA256Util.generateKeyToString();
String signature = HmacSHA256Util.signToBase64("message", hmacKey);
boolean valid = HmacSHA256Util.verifyBase64("message", signature, hmacKey);
```

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

### Tree
> 构建树形结构
```java
FuTree.buildTree(
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
```

## 许可证

本项目依据 [PolyForm Noncommercial License 1.0.0](LICENSE.md) 提供，仅允许该许可证定义的非商业用途。
商业用途必须事先取得单独书面许可，具体参见 [商业授权说明](COMMERCIAL-LICENSE.md)。
