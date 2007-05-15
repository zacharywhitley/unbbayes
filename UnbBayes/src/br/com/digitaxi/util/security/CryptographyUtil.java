package br.com.digitaxi.util.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public final class CryptographyUtil {

	private static final String ALGORITHM_MD5 = "MD5";
	private static final String ALGORITHM_SHA = "SHA";
	
	private CryptographyUtil() {
	}
	
	public static String encodeToMD5(String message) {
		return encode(message, ALGORITHM_MD5);
	}

	public static String encodeToSHA(String message) {
		return encode(message, ALGORITHM_SHA);
	}

	public static String encode(String message, String algorithm) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(message.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			return hash.toString(16);
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}
	
	public static void main(String[] args) {   
        try {   
            byte[] t = encrypt("teste2");   
            System.out.println(new String(t).length());   
            System.out.println(new String(t));   
            System.out.println(decrypt(t));   
        } catch (Exception x) {   
            x.printStackTrace();   
        }   
    }   
       
    /**  
     * Encrypt/decrypt a String.  
     * @param dir true for encrypting, false for decrypting  
     * @param password the password should be at least 8 chars long  
     * @param text the text to encrypted/decrypted  
     * @return the decrypted/encrypted text  
     */  
    private static byte[] crypto(boolean dir, byte[] password, byte[] text) {   
        try {
        	DESKeySpec keySpec = new DESKeySpec(password);   
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");   
            Cipher cipher = Cipher.getInstance("DES");   
            cipher.init(dir ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,    
                        factory.generateSecret(keySpec));   
            return cipher.doFinal(text);           	
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }   
       
    public static byte[] encrypt(String text) {   
        return crypto(true, "teste123".getBytes(), text.getBytes());   
    }   
       
    public static String decrypt(byte[] text) {   
        return new String(crypto(false, "teste123".getBytes(), text));   
    } 
    
    public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}