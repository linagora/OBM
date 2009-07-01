package org.obm.caldav.server.exception;

/**
 * 
 * @author adrienp
 *
 */
public class AuthorizationException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2388282495295018733L;

	public AuthorizationException(String message){
		super(message);
	}
}
