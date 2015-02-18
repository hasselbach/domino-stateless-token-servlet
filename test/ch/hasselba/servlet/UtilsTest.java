package ch.hasselba.servlet;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import ch.hasselba.servlet.Utils;


public class UtilsTest {

	@Test
	public void testBase64Encode() {
		byte[] toEncode = new String("Test").getBytes();
		byte[] result = new String("VGVzdA==").getBytes();
		byte[] testResult = Utils.toBase64(toEncode);
		
		assertTrue( Arrays.equals(result, testResult));
	}
	
	@Test
	public void testBase64Decode() {
		byte[] toDecode = new String("VGVzdA==").getBytes();
		byte[] result = new String("Test").getBytes();
		byte[] testResult = Utils.fromBase64(toDecode);
		
		assertTrue(Arrays.equals(result, testResult));
		
		toDecode = new String("MjAxNS9GZWIvMDggMTI6MjA6MjEuQ049U3ZlbiBIYXNzZWxiYWNoL09VPUhhc3NlbGJhL089REU9").getBytes();
		result = new String("2015/Feb/08 12:20:21.CN=Sven Hasselbach/OU=Hasselba/O=DE=").getBytes();
		
		testResult = Utils.fromBase64(toDecode);
		assertTrue(Arrays.equals(result, testResult));
		
	}
	
	@Test
	public void testBase64Chained() {
		byte[] toEncode = new String("Test").getBytes();
		
		byte[] resultEncode = Utils.toBase64(toEncode);
		byte[] resultDecode = Utils.fromBase64(resultEncode);

		assertTrue(Arrays.equals(toEncode, resultDecode));
		
	}
	@Test
	public void testBase64ChainedAscii() {
		
		StringBuffer sb = new StringBuffer();
		for( int i=32; i<126; i++ ){
			sb.append( (char) i );
		}
		
		byte[] toEncode = sb.toString().getBytes();
		
		byte[] resultEncode = Utils.toBase64(toEncode);
		byte[] resultDecode = Utils.fromBase64(resultEncode);

		assertTrue(Arrays.equals(toEncode, resultDecode));
		
	}
	
	
	@Test
	public void testConvertByteArrayToString(){
		
		String toTest = new String("Test");
		byte[] toConvert = toTest.getBytes();
		String testResult = Utils.convertByteArrayToString( toConvert );
		assertTrue( toTest.equals( testResult ) );
		
		String failTest = new String("TestFail");
		testResult = Utils.convertByteArrayToString( toConvert );
		assertFalse( failTest.equals( testResult ) );
		
	}
	
	@Test
	public void testConvertByteArrayToStringAscii(){
		StringBuffer sb = new StringBuffer();
		for( int i=32; i<126; i++ ){
			sb.append( (char) i );
		}
		String toTest = sb.toString();
		byte[] toConvert = toTest.getBytes();
		String testResult = Utils.convertByteArrayToString( toConvert );
		assertTrue( toTest.equals( testResult ) );
	
		
	}
	
	@Test
	public void testDateConversionChained(){
		Calendar currentDate = Calendar.getInstance();
		Date toConvert = currentDate.getTime();
		String convertedStr = Utils.getDateAsString( toConvert );
		Date convertedDate = Utils.getStringAsDate( convertedStr );
		assertTrue( toConvert.equals(convertedDate)  );
	}

}
