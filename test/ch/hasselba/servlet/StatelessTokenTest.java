package ch.hasselba.servlet;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class StatelessTokenTest {

	@Test 
	public final void testHMACChained(){
		StatelessToken token = new StatelessToken("TESTSALT");
		
		final String toTest = "userName";
		StringBuilder sb = new StringBuilder();
		sb.append( toTest );                           

		byte[] userBytes = sb.toString().getBytes();
		byte[] hash1 = token.createHmac(userBytes);
		byte[] hash2 = token.createHmac( Utils.fromBase64(Utils.toBase64(userBytes)) );

		assertTrue( "Hash1 is NULL!", hash1.length > 0 );
		assertTrue( "Hash2 is NULL!", hash2.length > 0 );
		assertTrue( "Both hashes are not equal!", Arrays.equals( hash1, hash2 ) );
		
	}
	@Test 
	public final void testCreateTokenForUser(){
		StatelessToken token = new StatelessToken("TESTSALT");
		
		final String toTest = "userName";
		
		final String token1 = token.createTokenForUser( toTest );
		final String token2 = token.createTokenForUser( toTest );

		assertTrue( "Token1 is NULL!",  token1.length() > 0 );
		assertTrue( "Token2 is NULL!", token2.length() > 0 );
		assertFalse( "Tokens are equal (while timestamps are different)!", token1.equals( token2 ) );
		
	}
	
	@Test
	public final void testToken() {
		StatelessToken token = new StatelessToken("TESTSALT");
		final String toTest = "CN=Sven Hasselbach/OU=Hasselba/O=CH";
		String testToken = token.createTokenForUser(toTest);
		String result = token.parseUserFromToken( testToken );
		
		assertTrue( "Token is null!", testToken.length() > 0 );
		assertTrue( "Result is null!", result.length() > 0 );
		assertTrue( "Token does not euals result!",toTest.equals(result) );
	}



}
