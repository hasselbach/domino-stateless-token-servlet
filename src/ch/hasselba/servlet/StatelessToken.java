package ch.hasselba.servlet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StatelessToken {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String SEPARATOR_SPLITTER = "!";
	private Mac hmac;

	/**
	 * Stateless Token constructor
	 * 
	 * @param secretKey
	 *            the secret key (aka Salt) to initialize the token
	 */
	public StatelessToken(final String secretKey) {

		try {
			hmac = Mac.getInstance(HMAC_ALGORITHM);
			hmac.init(new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	/**
	 * creates a Hmac hash from the given byte Array
	 * @param content
	 * @return
	 */
	protected synchronized byte[] createHmac(byte[] content) {
		return hmac.doFinal(content);
	}
	
	/**
	 * parse a given token: Validate data, check the timestamp
	 * @param token
	 * 			A String containing the token
	 * @return
	 * 			the username in the token
	 */
	public String parseUserFromToken(String token) {

		 // split token into user data & hash
		 String[] particles = token.split( SEPARATOR_SPLITTER );
		 
		 // drop if data is missing
		 if (particles.length != 2 || particles[0].length() == 0 || particles[1].length() == 0) {
			 return null;
		 }
		 
		try {
			
			// decode from Base64
			final byte[] data = Utils.fromBase64( particles[0].getBytes() );
			final byte[] hash = Utils.fromBase64( particles[1].getBytes() );
				
			// TODO: There is a damn Bug with the encoding / decoding.
			// Only solution is to encode / decode the fresh Hmac hash to get the same result
			final byte[] hmac = Utils.fromBase64(Utils.toBase64(createHmac(data)));
			
			// Validate the hash & drop if not matching
			boolean validHash = Arrays.equals(hmac, hash);
			if ( !validHash ){
				return null;
			}
			
			
			// TODO: Hash is OK, but maybe too many elements??  Thinking about a validation...
			final String[] dataParticles = Utils.convertByteArrayToString( data ).split( SEPARATOR_SPLITTER );
			
			// get token timestamp
			Date tokenTimeStamp = Utils.getStringAsDate( dataParticles[0] );
			Date testDate = new Date();
			
			// Only process timestamps younger then now
			if ( testDate.getTime() >= tokenTimeStamp.getTime() ) {
				// TODO: MaxAge must be configurable. Now it is 1h max
				Calendar dt = Calendar.getInstance();
				dt.roll(Calendar.HOUR_OF_DAY, -1);
					
				if( dt.getTimeInMillis() < tokenTimeStamp.getTime() )
					return dataParticles[1]; // everything is ok, return the username
			}
					
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
				
		}
		return null;
	}


	/**
	 * creates a new token for a user#
	 * 
	 * @param userName
	 * 			a String with the user credentials
	 * @return
	 * 			a token String
	 */

	public String createTokenForUser(final String userName) {
		
		// get current timestamp
		final Calendar currentDate = Calendar.getInstance();
		final String dateNow = Utils.getDateAsString( currentDate.getTime() );
		
		// create user data part: token creation timestamp & usernam
		StringBuilder sb = new StringBuilder();
		sb.append( dateNow );
		sb.append( SEPARATOR_SPLITTER );
		sb.append( userName );
		
		// convert to byte array
		byte[] userBytes = sb.toString().getBytes();
		
		// create a Hmac hash
		byte[] hash = createHmac(userBytes);

		// generate the token
		// TODO: encrypt the user data part
		sb = new StringBuilder();
		sb.append( Utils.convertByteArrayToString(Utils.toBase64(userBytes)) );
		sb.append(SEPARATOR_SPLITTER);
		sb.append( Utils.convertByteArrayToString(Utils.toBase64(hash))   );
		
		return sb.toString();
	}
	
}
