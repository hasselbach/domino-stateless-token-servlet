package ch.hasselba.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * filter for incoming requests:
 * Static resources are accessed from the filesystem, everything else is processed by the servlet
 * 
 * @author Sven Hasselbach
 *
 */
public class StaticFilter implements Filter {

	final static String PATH_STATIC = "/static";
	final static String PATH_SERVLET = "/servlet";
	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    	HttpServletRequest req = (HttpServletRequest) request;
    	String path = req.getRequestURI().substring(req.getContextPath().length());
    	
    	if (path.startsWith( PATH_STATIC )) {
    		// forward static file requests to the filesystem
    	    chain.doFilter(request, response);
    	} else {
    		// forward everything else to the servlet
    	    request.getRequestDispatcher(PATH_SERVLET + path).forward(request, response);
    	}

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}