package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class JCEKSUtil {
  private static final String TYPE = "JCEKS";
  private static final KeyStore keyStore;
  static {
    try {
      keyStore = KeyStore.getInstance(TYPE);
      keyStore.load(null, null); // 初始化空库
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    } catch (CertificateException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   *
   * @param alias 别名
   * @param keyPassword 密钥密码
   * @param storePassword 密钥库密码
   * @throws Exception
   */
  public static void addKey(String alias, String keyPassword, String storePassword) throws Exception {
    SecretKey secretKey = AESUtil.generateKey(AESKeySizeEnum.K_256);
    keyStore.setKeyEntry(alias, secretKey, keyPassword.toCharArray(), null);

    String fileName = String.format("app-secrets-%s.jceks", alias);
    try (FileOutputStream fos = new FileOutputStream(fileName)) {
      keyStore.store(fos, storePassword.toCharArray()); // 密钥库密码
    }

    System.out.println("✅ AES 密钥已存入 JCEKS: " + fileName);
  }

  public static SecretKey loadKey(Path path, String alias, String keyPassword, String storePassword) throws Exception {
    KeyStore keyStore = KeyStore.getInstance(TYPE);
    try (FileInputStream fis = new FileInputStream(path.toFile())) {
      keyStore.load(fis, storePassword.toCharArray());
    }

    return (SecretKey) keyStore.getKey(alias, keyPassword.toCharArray());
  }

  public static void main(String[] args) {
    String alias = "my-key";
    String keyPassword = "key-password";
    String storePassword = "store-password";

    try {
      addKey(alias, keyPassword, storePassword);

      String fileName = String.format("app-secrets-%s.jceks", alias);
      Path path = Path.of(fileName);

      SecretKey key = loadKey(path, alias, keyPassword, storePassword);
      System.out.println("✅ 加载密钥成功: " + key.getAlgorithm());

      path.toFile().deleteOnExit();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
