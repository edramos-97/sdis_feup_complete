package Utilities;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
import javax.crypto.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.PBEKeySpec;
import java.util.Arrays;


public class MessageCipher {

    private static String password_private = "default_private!";
    private static String password_group = "default_group!!!";


    // private static SecureRandom rnd = new SecureRandom();
    private static IvParameterSpec iv = new IvParameterSpec(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});

    public MessageCipher(){
        // TODO - get password/key

    }

    // TODO: add cipher of message body in toCharArry and parse, add cipher of everything based on key store

    public static byte[] privateCipher(byte[] message){
        return encrypt_data(message, password_private);
    }

    public static byte[] privateDecipher(byte[] message){
        return decrypt_data(message, password_private);
    }

    public static byte[] groupCipher(byte[] message){
        return encrypt_data_salted(message, password_group);
    }

    public static byte[] groupDecipher(byte[] message){
        return decrypt_data_salted(message, password_group);
    }


    private static byte[] encrypt_data(byte[] message, String password){
        byte[] encrypted = new byte[message.length];
        try {

            SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
            Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secret_key_spec, iv);
            encrypted = cipher.doFinal(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Encryption");
        }
        return encrypted;
    }

    private static byte[] decrypt_data(byte[] message, String password){
        byte[] decrypted = new byte[message.length];
        try {
            SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secret_key_spec, iv);
            decrypted = cipher.doFinal(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Decryption");
        }
        return decrypted;
    }

    private static byte[] deriveKey(String p, byte[] s, int i, int l) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), s, i, l);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(ks).getEncoded();
    }

    private static byte[] encrypt_data_salted(byte[] message, String password){

        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
 
        // Generate 160 bit Salt for Encryption Key
        byte[] esalt = new byte[20]; r.nextBytes(esalt);
        // Generate 128 bit Encryption Key
        byte[] dek = deriveKey(password, esalt, 10000, 128);

        // Perform Encryption
        SecretKeySpec eks = new SecretKeySpec(dek, "AES");
        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
        byte[] es = c.doFinal(message);

        // Generate 160 bit Salt for HMAC Key
        byte[] hsalt = new byte[20]; r.nextBytes(hsalt);
        // Generate 160 bit HMAC Key
        byte[] dhk = deriveKey(password, hsalt, 10000, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(hks);
        byte[] hmac = m.doFinal(es);

        // Construct Output as "ESALT + HSALT + CIPHERTEXT + HMAC"
        byte[] os = new byte[40 + es.length + 32];
        System.arraycopy(esalt, 0, os, 0, 20);
        System.arraycopy(hsalt, 0, os, 20, 20);
        System.arraycopy(es, 0, os, 40, es.length);
        System.arraycopy(hmac, 0, os, 40 + es.length, 32);

        return os;

        // byte[] encrypted = new byte[message.length];
        // try {

        //     SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
        //     Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
        //     cipher.init(Cipher.ENCRYPT_MODE, secret_key_spec, iv);
        //     encrypted = cipher.doFinal(message);

        // } catch (Exception e) {
        //     e.printStackTrace();
        //     System.out.println("Error In Encryption");
        // }
        // return encrypted;
    }

    private static byte[] decrypt_data_salted(byte[] message, String password){

        
        // Check Minimum Length (ESALT (20) + HSALT (20) + HMAC (32))
        if (message.length > 72) {
        // Recover Elements from String
        byte[] esalt = Arrays.copyOfRange(message, 0, 20);
        byte[] hsalt = Arrays.copyOfRange(message, 20, 40);
        byte[] es = Arrays.copyOfRange(message, 40, message.length - 32);
        byte[] hmac = Arrays.copyOfRange(message, message.length - 32, message.length);

        // Regenerate HMAC key using Recovered Salt (hsalt)
        byte[] dhk = deriveKey(password, hsalt, 10000, 160);

        // Perform HMAC using SHA-256
        SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(hks);
        byte[] chmac = m.doFinal(es);

        // Compare Computed HMAC vs Recovered HMAC
        if (MessageDigest.isEqual(hmac, chmac)) {
            // HMAC Verification Passed
            // Regenerate Encryption Key using Recovered Salt (esalt)
            byte[] dek = deriveKey(password, esalt, 10000, 128);

            // Perform Decryption
            SecretKeySpec eks = new SecretKeySpec(dek, "AES");
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            c.init(Cipher.DECRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
            byte[] s = c.doFinal(es);

            // Return our Decrypted String
            return s;
            }
        }
        System.out.println("Error In Decryption");
        return null;


        // byte[] decrypted = new byte[message.length];
        // try {
        //     SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
        //     Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        //     cipher.init(Cipher.DECRYPT_MODE, secret_key_spec, iv);
        //     decrypted = cipher.doFinal(message);

        // } catch (Exception e) {
        //     e.printStackTrace();
        //     System.out.println("Error In Decryption");
        // }
        // return decrypted;
    }

}
