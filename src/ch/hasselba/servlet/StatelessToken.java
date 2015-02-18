package ch.hasselba.servlet;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StatelessToken {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String SEPARATOR_SPLITTER = "!";
	private Mac hmac;
	private static final long DEFAULT_MAXAGE = 3600 * 1000;
	private long maxAge = DEFAULT_MAXAGE;
	
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

	
	public void setMaxAge( final long maxAge ){
		this.maxAge = maxAge;
	}
	
	public long getMaxAge(){
		return this.maxAge;
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
				
			// create the validation Hmac
			final byte[] hmac = createHmac(data);
			
			// Validate the hash & drop if not matching
			boolean validHash = Arrays.equals(hmac, hash);
			if ( !validHash ){
				return null;
			}
			
			// Split data into TimeStamp & Username
			final String[] dataParticles = Utils.convertByteArrayToString( data ).split( SEPARATOR_SPLITTER );
			if( dataParticles.length != 2 )
				return null;
			
			// get token timestamp
			long timestampToken = Long.parseLong(dataParticles[0] );
			long timestampNow = System.currentTimeMillis();
			
			// Only process timestamps younger then now
			if ( timestampNow >= timestampToken) {
			
				if( timestampToken < (timestampNow + maxAge) )
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
		final long timestamp = System.currentTimeMillis();
		
		// create user data part: token creation timestamp & username
		StringBuilder sb = new StringBuilder();
		sb.append( timestamp );
		sb.append( SEPARATOR_SPLITTER );
		sb.append( userName );
		
		// convert to byte array
		final byte[] userBytes = sb.toString().getBytes();
		
		// create a Hmac hash
		final byte[] hash = createHmac(userBytes);

		// generate the token
		// TODO: encrypt the user data part ??
		sb = new StringBuilder();
		sb.append( Utils.convertByteArrayToString(Utils.toBase64(userBytes)) );
		sb.append(SEPARATOR_SPLITTER);
		sb.append( Utils.convertByteArrayToString(Utils.toBase64(hash))   );
		
		return sb.toString();
	}
	
}
