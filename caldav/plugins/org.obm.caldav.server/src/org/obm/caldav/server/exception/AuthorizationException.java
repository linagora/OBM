package org.obm.caldav.server.exception;

import org.obm.caldav.server.StatusCodeConstant;

/**
 * 
 * @author adrienp
 *
 */
public class AuthorizationException extends CalDavException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2388282495295018733L;
	
	public AuthorizationException(){
		super(StatusCodeConstant.SC_FORBIDDEN);
	}

	public AuthorizationException(int httpStatusCode){
		super(httpStatusCode);
	}
}
