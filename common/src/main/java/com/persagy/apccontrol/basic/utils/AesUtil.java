package com.persagy.apccontrol.basic.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author shaohongbo
 * @date 2023年03月27日 22:08
 */
public class AesUtil {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 算法/模式/补码方式
     */
    private static final String ALGORITHM_PROVIDER = "AES/ECB/PKCS5Padding";

    private static final String UNIQUE_KEY = "jfdjk670qEH5lm3b";

    /**
     * 加密
     *
     * @param src       原内容
     * @param uniqueKey 唯一key
     * @return
     * @throws Exception
     */
    public static String encrypt(String src, String uniqueKey) throws Exception {
        byte[] key = uniqueKey.getBytes();
        SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM_PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherBytes = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
        return byteToHexString(cipherBytes);
    }

    public static String encrypt(String src) throws Exception {
        return encrypt(src, UNIQUE_KEY);
    }

    /**
     * 解密
     *
     * @param enc       加密内容
     * @param uniqueKey 唯一key
     * @return
     * @throws Exception
     */
    public static String decrypt(String enc, String uniqueKey) throws Exception {
        byte[] key = uniqueKey.getBytes();
        SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM_PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] hexBytes = hexStringToBytes(enc);
        byte[] plainBytes = cipher.doFinal(hexBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    public static String decrypt(String enc) throws Exception {
        return decrypt(enc, UNIQUE_KEY);
    }

    /**
     * 将byte数组转换为16进制字符串
     *
     * @param src
     * @return
     */
    private static String byteToHexString(byte[] src) {
        return Hex.encodeHexString(src);
    }

    /**
     * 将16进制字符串转换为byte数组
     *
     * @param hexString
     * @return
     */
    private static byte[] hexStringToBytes(String hexString) throws DecoderException {
        return Hex.decodeHex(hexString.toCharArray());
    }

    /**
     * AES加密、解密测试方法
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // 唯一key作为密钥
            String uniqueKey = "jfdjk670qEH5lm3b";
            // 加密前数据，此处数据与前端数据一致(加密内容包含有时间戳)，请注意查看前端加密前数据
            //String src = "{\"name\":\"何丽\",\"mobile\":\"15757099671\",\"idCard\":\"42230119780801112X\"}";
            String src = "";
            System.out.println("密钥:" + uniqueKey);
            System.out.println("原字符串:" + src);
            // 加密
            String encrypt = encrypt(src, uniqueKey);
            System.out.println("加密：" + encrypt);
            // 解密
            String aa = "ab98b6ef43d7c492389b7eebc7ee7e6d31f8d8d94474ee5c86591d6bf656b6399e0e104c6c1691764951b52c6d55dc8ea589d68b92e80f3d941daf2d135be649a0a664d2b8cdd6e8114d030206d572fc";
            String decrypt = decrypt(aa, uniqueKey);
            System.out.println("解密：" + decrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
