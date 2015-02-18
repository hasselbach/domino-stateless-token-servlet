package ch.hasselba.servlet;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class StatelessTokenTest {
	
	/**
	 * test if Base64 Encoding / Decoding does not destroy
	 * a Hmac Hash
	 */
	@Test
	public final void testHMACChained() {
		StatelessToken token = new StatelessToken("TESTSALT");
		
		final String toTest = "userName";
		StringBuilder sb = new StringBuilder();
		sb.append(toTest);

		final byte[] userBytes = sb.toString().getBytes();
		final byte[] hash1 = token.createHmac(userBytes);
		byte[] hash2 = token.createHmac(Utils.fromBase64(Utils
				.toBase64(userBytes)));

		assertTrue("Hash1 is NULL!", hash1.length > 0);
		assertTrue("Hash2 is NULL!", hash2.length > 0);
		assertTrue("Hashes are not equal!", Arrays.equals(hash1, hash2));

	}

	/**
	 * Test if hashes with different timestamps are different
	 */
	@SuppressWarnings("static-access")
	@Test
	public final void testCreateTokenForUser() {
		StatelessToken token = new StatelessToken("TESTSALT");

		final String toTest = "userName";
		final String token1 = token.createTokenForUser(toTest);
		
		assertTrue("Token1 is NULL!", token1.length() > 0);
		
		// sleep 1ms for a different timestamp
		try {
			Thread.currentThread().sleep(1);
		} catch (InterruptedException e) {}
		
		final String token2 = token.createTokenForUser(toTest);
		assertTrue("Token2 is NULL!", token2.length() > 0);
		
		assertFalse("Tokens are equal (while timestamps are different)!",
				token1.equals(token2));

	}

	/**
	 * test if a hierarchical name works
	 */
	@Test
	public final void testToken() {
		StatelessToken token = new StatelessToken("TESTSALT");
		final String toTest = "CN=Sven Hasselbach/OU=Hasselba/O=CH";
		final String testToken = token.createTokenForUser(toTest);
		final String result = token.parseUserFromToken(testToken);

		assertTrue("Token is null!", testToken.length() > 0);
		assertTrue("Result is null!", result.length() > 0);
		assertTrue("Token does not euals result!", toTest.equals(result));
	}
	
	/**
	 * test length of tokens
	 */
	@Test
	public final void testTokenLength() {

		StatelessToken token = new StatelessToken("TESTSALT");
		String toTest = "";
		String testToken = null;
		String result = null;
		
		for (int i = 0; i < 512; i++) {
			toTest = toTest + "X";
			testToken = token.createTokenForUser(toTest);
			result = token.parseUserFromToken(testToken);

			assertTrue("Token is null in round " + i + "!",
					testToken.length() > 0);
			assertTrue("Result is null in round" + i + "!", result.length() > 0);
			assertTrue("Token does not equals result in round " + i + "!",
					toTest.equals(result));
		}
	}

	/**
	 * test ASCII codes 0-128 as token characters
	 */
	@Test
	public final void testTokenLengthChars() {
		
		StatelessToken token = new StatelessToken("TESTSALT");
		
		String toTest = "";
		String testToken = null;
		String result = null;
		char chr = 0;

		for (int i = 0; i < 512; i++) {
			// skip the split char
			if (i % 128 != 33) {
				chr = (char) (i % 128);
			}

			toTest = toTest + chr;
			testToken = token.createTokenForUser(toTest);
			result = token.parseUserFromToken(testToken);

			assertTrue("Token is null in round " + i + "!",
					testToken.length() > 0);
			assertTrue("Result is null in round" + i + "!", result.length() > 0);
			assertTrue("Token does not equals result in round " + i + "!",
					toTest.equals(result));
		}
	}

	/**
	 * test different salt lengths
	 * from 1 - 512 bytes
	 */
	@Test
	public final void testTokenSaltLength() {

		StatelessToken token = null;
		String testSalt = "";
		String testToken = null;
		String result = null;

		final String tokenData = "ABCDEFHIJKLMNOPQRSTUVWXYZ0123456789";

		for (int i = 0; i < 512; i++) {
			testSalt = testSalt + "X";
			token = new StatelessToken(testSalt);

			testToken = token.createTokenForUser(tokenData);
			result = token.parseUserFromToken(testToken);

			assertTrue("Token is null in round " + i + "!",
					testToken.length() > 0);
			assertTrue("Result is null in round" + i + "!", result.length() > 0);
			assertTrue("Token does not equals result in round " + i + "!",
					tokenData.equals(result));
		}
	}

	/**
	 * test different salt lengths // ASCII Codes
	 */
	@Test
	public final void testTokenSaltLengthChars() {

		StatelessToken token = null;
		String testSalt = "";
		String testToken = null;
		String result = null;

		final String tokenData = "ABCDEFHIJKLMNOPQRSTUVWXYZ0123456789";

		for (int i = 0; i < 512; i++) {
			testSalt = testSalt + (char) (i % 255);
			token = new StatelessToken(testSalt);

			testToken = token.createTokenForUser(tokenData);
			result = token.parseUserFromToken(testToken);

			assertTrue("Token is null in round " + i + "!",
					testToken.length() > 0);
			assertTrue("Result is null in round" + i + "!", result.length() > 0);
			assertTrue("Token does not euals result in round " + i + "!",
					tokenData.equals(result));
		}
	}

}
