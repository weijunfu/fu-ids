#

## [1.0.31] - 2026-08-13

### Add
+ 新增无依赖 Java Nano ID 实现，支持安全随机、自定义字母表与长度

### Changed
+ Snowflake 不再读取配置文件，默认构造固定使用内置参数

## [1.0.30] - 2026-08-06

### Add
+ Snowflake 支持 application.yml / application.yaml 配置文件

### fix log
+ Snowflake 不会再因为缺少 application.properties 反复输出warn

## [1.0.29] - 2026-08-05

### fix 安全问题

## [1.0.28] - 2026-06-02

### Add ERP 安全解决方案
+ Java
  + ErpPayloadCryptoUtil
+ Web
  + fuids (npmjs)

### Fix 
+ 改 KeyManager：去掉内置默认 key，改为外部注入/KMS/密钥文件加载；校验 AES key 只能是 16 或 32 字节。
+ 收敛 AES 实现：EncryptedData 复用 AESUtil；增加版本、keyId、AAD；补充错误 key、篡改 nonce/tag、短密文、非法 key 长度测试。
+ 改 RSA：删除/废弃 encryptByPrivate 和 decryptByPublicKey；加密只保留公钥加密/私钥解密，并限制为包装 AES key；签名使用 sign/verify，新实现优先考虑 RSA-PSS。
+ 把 MD5 从安全章节移出或标记 @Deprecated，新增 SHA256Util / HmacSHA256Util，密码场景不要提供通用 MD5/SHA 工具，应使用 BCrypt/Argon2/PBKDF2。


## [1.0.27] - 2026-02-04

### Add
- 新增`FuField`注解，用于自定义处理字段
    - weight: 权重，默认为 0
    - pattern: 格式化，默认为空，如`yyyy-MM-dd HH:mm:ss`


## [1.0.25] - 2026-01-09

### Add
- 新增 `FuDate`类，用于处理日期和时间

## [1.0.23.1] - 2026-01-06

### Fix
- 修复`FuURL`类`toSlug`方法

## [1.0.23] - 2026-01-06

### Add
- 新增`FuURL`类,  用于统一处理URL
    + `FuURL.toSlug` 将标题字符串转换为URL友好的slug格式

## [1.0.21] - 2025-12-30

### Add
- 新增树形数据结构构建

## [1.0.19.1] - 2025-12-20

### Add
- `RSAUtil` 新增 `generateMapKeyPair` 方法

## [1.0.19] - 2025-12-19

### Fixed
- 优化雪花算法，支持时间戳自定义配置

### Add
- 添加`TimeUtil`工具类
