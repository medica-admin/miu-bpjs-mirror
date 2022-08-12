package controllers;

import blazing.chain.LZSEncoding;
import play.Logger;
import play.shaded.oauth.org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class decriptBpjs {

    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
//    private static final String key = "57A1781643775830";
    private static final String key = "254717eDF57A1781643775830";

    private static final String initVector = "encryptionIntVec";

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            System.out.println(skeySpec.getEncoded().length);
            System.out.println(skeySpec.getEncoded());
            System.out.println(skeySpec.getFormat());
            System.out.println(skeySpec.getAlgorithm());

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static AesKeySpec generateKey(String key) throws NoSuchPaddingException, NoSuchAlgorithmException,InvalidAlgorithmParameterException, InvalidKeyException,BadPaddingException, IllegalBlockSizeException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] _hashKey = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        byte[] _hashIv = new byte[16];
        for (int i = 0; i < 16; i++) {
            _hashIv[i] = _hashKey[i];
        }

        AesKeySpec aesKeySpec = new AesKeySpec();
        SecretKeySpec _key = new SecretKeySpec(_hashKey, "AES");
        IvParameterSpec _iv = new IvParameterSpec(_hashIv);

        aesKeySpec.setKey(_key);
        aesKeySpec.setIv(_iv);
        return aesKeySpec;
    }

    public static String encrypt(String cipherText, SecretKeySpec key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return Base64.getEncoder().encodeToString(cipher.doFinal(cipherText.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String cipherText, SecretKeySpec key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(plainText);
        }catch (Exception e){
            return e.getClass().getName() +": "+ e.getMessage();
        }

    }


    public static void main(String[] args) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String key = "253416fQ55ED0231660141095";
        AesKeySpec aes = generateKey(key);
        String data = "1LAwKe+NkEh9qhvp+ZMhQcnLc2oEKEJyE1cpnY7jN1GMEfGKI8XIZ4OSJi8LjArigWw2wQKKsKe3TIqjc1XfFqprl/ElewBIPbAVe5ZxBySr+vJbXXhdBE2lXabhc64pY4p56SwaP5RJXsXFPipCPA==";

        String text = "irvan wahyudi";
        String compressedText = LZString.compressToEncodedURIComponent(text);
        String encryptedString = encrypt(compressedText, aes.getKey(), aes.getIv());
        String decryptedString = decrypt(data, aes.getKey(), aes.getIv());
        String decompressedText = LZString.decompressFromEncodedURIComponent(decryptedString);

        System.out.println("Response : " + decompressedText);



    }

}