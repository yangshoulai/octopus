package com.octopus.sample.music;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.json.JSONUtil;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/23
 */
public class EncryptUtil {

  private static final String KEY_SEEDS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private static String AES_IV = "0102030405060708";

  public static String createAesEncryptKey(int size) {
    StringBuilder key = new StringBuilder();
    for (int i = 0; i < size; i++) {
      int pos = (int) Math.floor(Math.random() * KEY_SEEDS.length());
      key.append(KEY_SEEDS.charAt(pos));
    }
    return key.toString();
  }

  public static String aesEncrypt(String content, String secretKey) {
    SecretKey key =
        SecureUtil.generateKey(
            SymmetricAlgorithm.AES.getValue(), secretKey.getBytes(StandardCharsets.UTF_8));
    AES aes =
        new AES(
            Mode.CBC,
            Padding.PKCS5Padding,
            key,
            new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8)));
    return aes.encryptBase64(content);
  }

  /**
   * RSA 公钥假面
   *
   * @param content 原始明文
   * @return 密文
   */
  public static String rasPublicKeyEncrypt(String content) {
    BigInteger biText =
        new BigInteger(1, StrUtil.reverse(content).getBytes(StandardCharsets.UTF_8));
    String RSA_EXPONENT = "010001";
    String RSA_MODULES = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
    return biText
        .modPow(
            new BigInteger(1, HexUtil.decodeHex(RSA_EXPONENT)),
            new BigInteger(1, HexUtil.decodeHex(RSA_MODULES)))
        .toString(16);
  }

  public static Map<String, String> getEncryptParams(Map<String, Object> params) {
    String secKey = createAesEncryptKey(16);
    String AES_KEY = "0CoJUm6Qyw8W8jud";
    String encText = aesEncrypt(aesEncrypt(JSONUtil.toJsonStr(params), AES_KEY), secKey);
    String encKey = rasPublicKeyEncrypt(secKey);
    return MapUtil.builder("params", encText).put("encSecKey", encKey).build();
  }
}
