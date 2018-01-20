package common.wechat.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author LongerHsiao
 * @date 2018-01-14
 */
public class SHA1 {
    public static String sign(byte[] bytes) {
        String signature = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);
            signature = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return signature;
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String byteToStr(byte[] bytes) {
        StringBuffer strDigest = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            strDigest.append(byteToHexStr(bytes[i]));
        }
        return strDigest.toString();
    }

    /**
     * 将字节转换为十六进制字符串
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }
}
