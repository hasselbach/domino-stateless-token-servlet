package ch.hasselba.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;

import javax.servlet.ServletContext;
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

public class DominoStatelessTokenServlet extends HttpServlet implements Serializable {

	private static final long serialVersionUID = -1L;
	private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String URL_PATH_VALIDATE = "/validate/";
	private static final String URL_PATH_CREATE = "/create/";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";
	private static final String PARAM_DEBUGMODE = "debug";
	private static final String PARAM_TOKEN = "token";
	
	private String userNameBackend;
	private String secretSalt;
	private StatelessToken tokenHandler;
	private Session currentSession;
	
	
	public void init() throws ServletException{
		ServletContext ctx = getServletContext();
		userNameBackend = ctx.getInitParameter("UsernameBackend");
		secretSalt = ctx.getInitParameter("SecretSalt");
		tokenHandler = new StatelessToken( secretSalt );
		final String maxAgeStr = ctx.getInitParameter( "TokenMaxAge" );
		if( ! "".equals( maxAgeStr) ){
			final long maxAge = Long.parseLong( maxAgeStr );
			tokenHandler.setMaxAge( maxAge );
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		
		ServletOutputStream out = res.getOutputStream();		
 
		try {
			 StringBuffer requestURL = req.getRequestURL();
			 	
			 // create a new token
			 if( requestURL.indexOf(URL_PATH_CREATE) != -1  ){
				this.doCreate(req, res, out);
				return;
			 }
			 
			 // validate a token
			 if( requestURL.indexOf( URL_PATH_VALIDATE ) != -1  ){
				 this.doValidate(req, res, out);
				 return;
			 }
			 
			 // else: BAD REQUEST!
			 res.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(out));
		} finally {
			Utils.recycleDominoObjects( this.currentSession );
			out.close();
		}
	
	}
	
	/**
	 * validate a token
	 * 
	 * @param req
	 * 		HttpServletRequest
	 * @param res
	 * 		HttpServletResponse
	 * @param out
	 * 		ServletOutputStream
	 * 
	 * @throws IOException
	 * @throws ServletException
	 * 
	 */
	private void doValidate( HttpServletRequest req, HttpServletResponse res, ServletOutputStream out  ) throws IOException, ServletException{

		final String debugmode = req.getParameter(PARAM_DEBUGMODE);
		
		
		String token = null;
		 
		if( req.getHeader(AUTH_HEADER_NAME) != null  )
			token = req.getHeader(AUTH_HEADER_NAME);
		 
		 if( debugmode != null ){
			if( req.getParameter(PARAM_TOKEN) != null )
				token = req.getParameter(PARAM_TOKEN);
		 }
		 
		 if( token == null ){
			 res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			 return;
		 }

		 final String userName = getAuthentication(token);
		 if (userName != null){
			 
			 Session session = null;
			 
			 res.setContentType( CONTENT_TYPE_JSON );
			 
			 try{
				 session = createUserSession( userName );
				 out.println("{username: '" + session.getEffectiveUserName() + "'}");
			 } catch (NotesException e) {
				e.printStackTrace();
			}finally{
				Utils.recycleDominoObjects( session );
			 }
			 
		 }else{
			 res.setContentType( CONTENT_TYPE_JSON );
			 out.println("{username: 'Anonymous'}");
			 return; 
		 }
	}
	/**
	 * creates a new token if username / password are valid
	 * 
	 * @param req
	 * 		HttpServletRequest
	 * @param res
	 * 		HttpServletResponse
	 * @param out
	 * 		ServletOutputStream
	 * 
	 * @throws IOException
	 * @throws ServletException
	 * 
	 */
	private void doCreate( HttpServletRequest req, HttpServletResponse res, ServletOutputStream out  ) throws IOException, ServletException{
		
		if (req.getParameter(PARAM_USERNAME) == null || req.getParameter(PARAM_PASSWORD) == null ) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		final String userName = req.getParameter(PARAM_USERNAME);
		final String password = req.getParameter(PARAM_PASSWORD);
		final String debugmode = req.getParameter(PARAM_DEBUGMODE);
		
		if(this.validatePassword( userName, password)){
			
			String tokenStr = tokenHandler.createTokenForUser(userName);
			res.addHeader(AUTH_HEADER_NAME, tokenStr);
			if( debugmode != null ){
				res.setContentType( CONTENT_TYPE_JSON );
				out.println("{token: '" + tokenStr + "'}");
			}

		}else{
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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
	private Session createUserSession(final String userName )
			throws ServletException {
			Session session = null;
		try {
			long hList = NotesUtil.createUserNameList(userName);
			session = XSPNative.createXPageSession(userName, hList,
					false, false);

			return session;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return session;
	}

	/**
	 * validate the given username / password aaginst the HTTP password of Domino
	 * Requires a valid Domino user with read access for the NAB
	 * 
	 * @param userName
	 * 			the username
	 * @param password
	 * 			the plain passwrd
	 * @return
	 * 			true, if username / password is correct
	 * @throws ServletException
	 */
	private boolean validatePassword( final String userName, final String password) throws ServletException{
		Session session = null;
		try {

			// create a new backend session
			session = createUserSession( userNameBackend );
			
			// check the backend's password
			Vector<?> result = session.evaluate( "@NameLookup([TrustedOnly] ;\"" + userName + "\"; \"HTTPPassword\");" );
			String hashedPwd = result.firstElement().toString() ;
			if( "".equals( hashedPwd ) )
				return false;
			
			// validate the password
			if( session.verifyPassword( password, hashedPwd  ) == true )
				return true;
			
		} catch (NotesException e) {
			e.printStackTrace();
		} finally{
			Utils.recycleDominoObjects( session );
		}
		return false;
	}

	
}