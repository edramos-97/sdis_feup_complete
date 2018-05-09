package Utilities;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageCipher {

    private static String password_private = "default_private";
    private static String password_group = "default_group";

    public MessageCipher(){
        // TODO - get password/key

    }

    // TODO: add cipher of message body in toCharArry and parse, add cipher of everything based on key store

    public static byte[] privateCipher(byte[] message){
        return encrypt_data(message, password_private, FileHandler.CHUNK_SIZE);
    }

    public static byte[] privateDecipher(byte[] message){
        return decrypt_data(message, password_private, FileHandler.CHUNK_SIZE);
    }

    public static byte[] groupCipher(byte[] message){
        return encrypt_data(message, password_group, FileHandler.MAX_SIZE_MESSAGE);
    }

    public static byte[] groupDecipher(byte[] message){
        return decrypt_data(message, password_group, FileHandler.MAX_SIZE_MESSAGE);
    }


    private static byte[] encrypt_data(byte[] message, String password, int max_size){
        byte[] encrypted = new byte[max_size];
        try {

            SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"Blowfish");
            Cipher cipher=Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, secret_key_spec);
            encrypted = cipher.doFinal(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Encryption");
        }
        return encrypted;
    }

    private static byte[] decrypt_data(byte[] message, String password, int max_size){
        byte[] decrypted = new byte[max_size];
        try {
            SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, secret_key_spec);
            decrypted = cipher.doFinal(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Decryption");
        }
        return decrypted;
    }

}
