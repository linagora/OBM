package fr.aliasource.funambol.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Helper extends Helper {

	private static final char hexDigits[] = {
        '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	public static String encryptPassword(String password) {
		String result = "";
		
		byte[] dataout = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            dataout = md.digest(password.getBytes());
            result = bytesToHex(dataout);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
		
		return result;
	}
	
	/**
     * Converts the byte array in a hex string representation
     */
    public static String bytesToHex(byte[] b) {
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < b.length; i++) {
            buf.append(byteToHex(b[i]));
        }
        return buf.toString();
    }


    // --------------------------------------------------------- Private methods

    /**
     * Returns hex String representation of byte b
     *
     * @param b byte
     * @return String
     */
    private static String byteToHex(byte b) {
        char[] array = {hexDigits[(b >> 4) & 0x0f], hexDigits[b & 0x0f]};
        return new String(array);
    }
}
