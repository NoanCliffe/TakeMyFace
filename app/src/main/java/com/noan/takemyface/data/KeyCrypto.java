package com.noan.takemyface.data;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyCrypto {
    private static final IvParameterSpec iv=new IvParameterSpec("ZQlUQJqpXKDKAKjh".getBytes());
    private static final SecretKeySpec sKeySpec=new SecretKeySpec("2NodmRYBL1cAtW3N".getBytes(), "AES");
    public static String orgPasswordEncrypt(String pwOrigin) {
        try {
            String sKey = "0725@pwdorgopenp";
            Cipher cipher = Cipher.getInstance("AES//ECB/PKCS7Padding");
            SecretKeySpec sKeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
            byte[] encrypted = cipher.doFinal(pwOrigin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
    public static String encryptToken(String token)
    {
        try {

            Cipher cipher = Cipher.getInstance("AES//CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec,iv);
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
    public static String decryptToken(String encryptedStr) throws IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance("AES//CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec,iv);
            byte[] encrypted = Base64.getDecoder().decode(encryptedStr);
            byte[] originalRaw = cipher.doFinal(encrypted);
            return new String(originalRaw,StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
}
