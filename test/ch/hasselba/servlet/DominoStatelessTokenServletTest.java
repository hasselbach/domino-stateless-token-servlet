package ch.hasselba.servlet;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class DominoStatelessTokenServletTest {

	private WebDriver driver;
	private String baseUrl="http://localhost/token";
	private String user1Name = "TTestUser01";
	private String user1Password = "test01";
	private String user2Name = "TTestUser02";
	private String user2Password = "test02";

	private StringBuffer verificationErrors = new StringBuffer();
	
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	/**
	 * tests if the servlet returns a 400er if only opened directly
	 * @throws Exception
	 */
	public void testEmptyPath() throws Exception {
		driver.get(baseUrl);
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
	}

	@Test
	/**
	 * test if servlet response is 400 if parameters are missing
	 * @throws Exception
	 */
	public void testCreateNoParams() throws Exception {
		driver.get(baseUrl + "/create/");
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
	}
	
	
	@Test
	/**
	 * test if a required parameter is missed
	 * @throws Exception
	 */
	public void testCreateBadParams() throws Exception {
		driver.get(baseUrl + "/create/?username=" + this.user1Name);
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
		
		driver.get(baseUrl + "/create/?password=" + this.user1Password);
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
		
	}
	
	@Test
	/**
	 * test a valid token creation
	 * @throws Exception
	 */
	public void testCreateFail() throws Exception {
		driver.get(baseUrl + "/create/?username=" + this.user1Name + "&password=" + "WRONG");
		assertEquals("Error 401: Error reported: 401", driver.findElement(By.tagName("body")).getText());
		driver.get(baseUrl + "/create/?username=WRONG" + "&password=" + this.user1Password );
		assertEquals("Error 401: Error reported: 401", driver.findElement(By.tagName("body")).getText());
		driver.get(baseUrl + "/create/?username=WRONG" + "&password=WRONG" );
		assertEquals("Error 401: Error reported: 401", driver.findElement(By.tagName("body")).getText());
	}
	
	@Test
	/**
	 * test a valid token creation
	 * @throws Exception
	 */
	public void testCreateSuccess() throws Exception {
		driver.get(baseUrl + "/create/?username=" + this.user1Name + "&password=" + this.user1Password);
		String txt = driver.findElement(By.tagName("pre")).getText();
		assertEquals("{token:", txt.substring(0, 7));
		
		driver.get(baseUrl + "/create/?username=" + this.user2Name + "&password=" + this.user2Password);
		txt = driver.findElement(By.tagName("pre")).getText();
		assertEquals("{token:", txt.substring(0, 7));
	}
	
	@Test
	/**
	 * test if servlet response is 400 if parameters are missing for verifying
	 * @throws Exception
	 */
	public void testValidateNoParams() throws Exception {
		driver.get(baseUrl + "/validate/");
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
	}
	
	@Test
	/**
	 * test if a required parameter is missed
	 * @throws Exception
	 */
	public void testValidateBadParams() throws Exception {
		driver.get(baseUrl + "/validate/?token=");
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());
		
		driver.get(baseUrl + "/validate/?token=BADTOKEN");
		assertEquals("Error 400: Error reported: 400", driver.findElement(By.tagName("body")).getText());	
	}
	
	@Test
	/**
	 * test if a required parameter is missed
	 * @throws Exception
	 */
	public void testValidateSuccess() throws Exception {
		String token = null;
		String txt = null;
		
		driver.get(baseUrl + "/create/?username=" + this.user1Name + "&password=" + this.user1Password);
		txt = driver.findElement(By.tagName("pre")).getText();
		token = encodeToken(txt.substring(9, txt.lastIndexOf("'")));
		driver.get(baseUrl + "/validate/?token=" + token );
		assertEquals("{user: '" + this.user1Name  + "'}", driver.findElement(By.tagName("pre")).getText());
		
		driver.get(baseUrl + "/create/?username=" + this.user2Name + "&password=" + this.user2Password);
		txt = driver.findElement(By.tagName("pre")).getText();
		token = encodeToken(txt.substring(9, txt.lastIndexOf("'")));
		driver.get(baseUrl + "/validate/?token=" + token );
		
		assertEquals("{user: '" + this.user2Name + "'}", driver.findElement(By.tagName("pre")).getText());
	}
	@Test
	public void testValidateSuccess5000Times() throws Exception {
		String token1 = null;
		String token2 = null;
		String txt = null;
		
		driver.get(baseUrl + "/create/?username=" + this.user1Name + "&password=" + this.user1Password);
		txt = driver.findElement(By.tagName("pre")).getText();
		token1 = encodeToken(txt.substring(9, txt.lastIndexOf("'")));
		driver.get(baseUrl + "/create/?username=" + this.user2Name + "&password=" + this.user2Password);
		txt = driver.findElement(By.tagName("pre")).getText();
		token2 = encodeToken(txt.substring(9, txt.lastIndexOf("'")));

		for( int i=0; i<5000;i++){
			
			driver.get(baseUrl + "/validate/?token=" + token1 + "&norefresh=" + java.lang.System.currentTimeMillis() );
			assertEquals("{user: '" + this.user1Name  + "'}", driver.findElement(By.tagName("pre")).getText());
			
			
			driver.get(baseUrl + "/validate/?token=" + token2 + "&norefresh=" + java.lang.System.currentTimeMillis() );
			
			assertEquals("{user: '" + this.user2Name + "'}", driver.findElement(By.tagName("pre")).getText());
		}
	}
	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
	
	private String encodeToken( final String token ) throws UnsupportedEncodingException{
		return  URLEncoder.encode( token, "UTF-8" );
	}

}
