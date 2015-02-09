package ch.hasselba.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.NotesException;
import lotus.domino.Session;
import ch.hasselba.servlet.StatelessToken;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.xsp.XSPNative;

public class TokenServlet extends HttpServlet implements Serializable {

	private static final long serialVersionUID = -1L;
	private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String URL_PATH_VALIDATE = "/validate/";
	private static final String URL_PATH_CREATE = "/create/";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";
	private static final String PARAM_TOKEN = "token";
	
	private final StatelessToken tokenHandler;
	private Session currentSession;

	public TokenServlet() {
		tokenHandler = new StatelessToken("I am a super secret salt!");
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException{
		String userName = null;
		String password = null;
		ServletOutputStream out = res.getOutputStream();
		
		try {
			 StringBuffer requestURL = req.getRequestURL();
			 	
			 // create a new token
			 if( requestURL.indexOf(URL_PATH_CREATE) != -1  ){
				if (req.getParameter(PARAM_USERNAME) == null || req.getParameter(PARAM_PASSWORD) == null ) {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
					
				userName = req.getParameter(PARAM_USERNAME);
				password = req.getParameter(PARAM_PASSWORD);
					
				// TODO: Validate the password
				// Check Names.nsf
				// If not OK, return SC_UNAUTHORIZED  
					
				res.setContentType( CONTENT_TYPE_JSON );
				String tokenStr = tokenHandler.createTokenForUser(userName);
				res.addHeader(AUTH_HEADER_NAME, tokenStr);
				out.println("{token: '" + tokenStr + "'}");
				return;
			}
			 
			 if( requestURL.indexOf( URL_PATH_VALIDATE ) != -1  ){
				 String token = null;
				 
				 if( req.getHeader(AUTH_HEADER_NAME) != null  )
					 token = req.getHeader(AUTH_HEADER_NAME);
				 
				 if( req.getParameter(PARAM_TOKEN) != null )
					 token = req.getParameter(PARAM_TOKEN);
				 
				 if( token == null ){
					 res.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				 }
				 
				 userName = getAuthentication(token);
				 if (userName == null)
					userName = "Anonymous";
				 
				 res.setContentType( CONTENT_TYPE_JSON );
				 Session s = createUserSession( userName );
				 out.println("{user: '" + s.getEffectiveUserName() + "'}");
				 return;
			 }
			 
			 // else: BAD REQUEST!
			 res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			 return;
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(out));
		} finally {
			out.close();
		}
	}

	/**
	 * adds header containing token to HTTP response
	 * 
	 * @param response
	 * 		a valid HttpServletResponse object
	 * @param userName
	 * 		a valid user name
	 * @throws NotesException
	 */
	public void addAuthentication(HttpServletResponse response, String userName) throws NotesException {
		response.addHeader(AUTH_HEADER_NAME, tokenHandler.createTokenForUser(userName));
	}

	/**
	 * extracts the user from a token String
	 * If token is invalid, it returns NULL
	 * 
	 * @param token
	 * 		the String containing a token
	 * @return
	 * 		String with the extracted username
	 */
	public String getAuthentication(final String token) {
		String user = null;
		if (token != null) {
			user = tokenHandler.parseUserFromToken(token);
		}
		return user;
	}

	/**
	 * create a new Domino session for the give username
	 * @param userName
	 * 		String containing the canonical username
	 * @return
	 * 		lotus.domino.Session for the given username
	 * @throws ServletException
	 */
	private Session createUserSession(final String userName)
			throws ServletException {
		try {
			long hList = NotesUtil.createUserNameList(userName);
			this.currentSession = XSPNative.createXPageSession(userName, hList,
					false, false);

			return this.currentSession;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.currentSession;
	}

}