package Utilities;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

public final class VolatileDatabase {

    public static ConcurrentHashMap<String, ProtocolStateMachine> database = new ConcurrentHashMap<>();

    private VolatileDatabase(){

    }

    // from:
    // https://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
    public static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance( "SHA-256" );

        // Change this to UTF-16 if needed
        md.update( input.getBytes( StandardCharsets.UTF_8 ) );
        byte[] digest = md.digest();

        return String.format( "%064x", new BigInteger( 1, digest ) );
    }

}
