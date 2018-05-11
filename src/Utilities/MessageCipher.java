package Utilities;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
        return encrypt_data(message, password_group);
    }

    public static byte[] groupDecipher(byte[] message){
        return decrypt_data(message, password_group);
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

}
