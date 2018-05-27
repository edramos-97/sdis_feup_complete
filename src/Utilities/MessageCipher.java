package Utilities;

import Executables.Peer;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;


public class MessageCipher {

    private static String password_private = "default_private!";
    private static String password_group = "default_group!!!";


    // private static SecureRandom rnd = new SecureRandom();
    private static IvParameterSpec iv;


    public MessageCipher(){
        // TODO - get password/key

    }

    // TODO: add cipher of message body in toCharArry and parse, add cipher of everything based on key store

    public static byte[] privateCipher(byte[] message, String fileID){
        return encrypt_data(message, fileID);
    }

    public static byte[] privateDecipher(byte[] message, String fileID){
        return decrypt_data(message, fileID);
    }

    /*public static byte[] groupCipher(byte[] message){
        return encrypt_data_salted(message, password_group);
    }

    public static byte[] groupDecipher(byte[] message){
        return decrypt_data_salted(message, password_group);
    }*/


    private static byte[] encrypt_data(byte[] message, String fileID){
        byte[] encrypted = new byte[message.length];
        try {
            //SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
            Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, Peer.privateKeyStore, new IvParameterSpec(fileID.substring(0,16).getBytes()));
            encrypted = cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Encryption");
        }
        return encrypted;
    }

    private static byte[] decrypt_data(byte[] message, String fileID){
        byte[] decrypted = new byte[message.length];
        try {
            //SecretKeySpec secret_key_spec = new SecretKeySpec(password.getBytes(),"AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, Peer.privateKeyStore, new IvParameterSpec(fileID.substring(0,16).getBytes()));
            decrypted = cipher.doFinal(message);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error In Decryption");
        }
        return decrypted;
    }

    /*private static byte[] encrypt_data_salted(byte[] message, String password){
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

    private static byte[] decrypt_data_salted(byte[] message, String password){
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
    }*/
}
