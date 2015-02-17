package ch.hasselba.servlet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lotus.domino.Base;
import org.apache.commons.codec.binary.Base64;

public class Utils {
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS"); 
	
 
	/**
	 * decodes a BASE64 encoded byte array
	 * 
	 * @param toDecode
	 *            the byte array to decode
	 * @return the decoded byte array
	 */
	public static byte[] fromBase64(final byte[] toDecode) {
		return Base64.decodeBase64(toDecode);
	}

	/**
	 * encodes a byte array to a BASE64 encoded byte array
	 * 
	 * @param toEncode
	 *            the byte array to encode
	 * @return the encoded byte array
	 */
	public static byte[] toBase64(final byte[] toEncode) {
		return Base64.encodeBase64( toEncode );
	}
	
	/**
	 * converts a byte array to a String
	 * 
	 * @param toConvert
	 *   		the byte array to convert
	 * @return
	 * 			the converted String
	 */
	public static String convertByteArrayToString( final byte[] toConvert ){
		return new String( toConvert, 0 , toConvert.length );
	}
	
	/**
	 * converts java.util.Date to a String
	 * 
	 * @param toConvert
	 *   		the java.util.Date to convert
	 * @return
	 * 			the converted String
	 */
	public static String getDateAsString(final Date toConvert){
		return formatter.format(toConvert.getTime());
	}
	
	/**
	 * converts String to a java.util.Date
	 * 
	 * @param toConvert
	 *   		the String to convert
	 * @return
	 * 			the converted java.util.Date
	 */
	public static Date getStringAsDate(final String toConvert) {
		Date dt = null;
		try{
			dt = formatter.parse( toConvert );
		}catch( ParseException pe ){}
		
		return dt;
	}
	
	/**
	 * recycleDominoObjects helper method for recycling Domino objects
	 * 
	 * @param nObjects
	 *            the Domino objects to recycle
	 */
	public static void recycleDominoObjects(Base... nObjects) {
		for (Base nObject : nObjects) {
			if (nObject != null) {
				try {
					(nObject).recycle();
				} catch (Exception e) {}
			}
		}
	}
	
	
}
