package com.mob41.pushbullet.hash;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Random;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import sun.misc.*;

public class AES {
	
	private static final String ALGO = "AES";
    private static byte[] keyValue = new byte[16];
    
    public static String getRandomByte(){
    	Random rand = new Random();
    	byte[] key = new byte[16];
    	rand.nextBytes(key);
    	return Base64.encodeBase64String(key);
    }
    
    public static String getRandom(int amount){
    	Random rand = new Random();
    	byte[] key = new byte[amount];
    	rand.nextBytes(key);
    	return Base64.encodeBase64String(key);
    }
    
    
    public static byte[] decode(String buffer) throws Exception {
    	return Base64.decodeBase64(buffer);
    }
    
    public static String encrypt(String Data, String salt) throws Exception {
    	keyValue = decode(salt);
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.encodeBase64String(encVal);
        return encryptedValue;
    }

    public static String decrypt(String encryptedData, String salt) throws Exception {
    	keyValue = decode(salt);
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.decodeBase64(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
}
	
}
