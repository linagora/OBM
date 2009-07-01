package org.obm.caldav.server.exception;

import org.obm.caldav.server.StatusCodeConstant;

/**
 * 
 * @author adrienp
 *
 */
public class CalDavException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7956416583930247999L;
	
	private int httpStatusCode;
	
	public CalDavException(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	
	public String getMessage(){
		return StatusCodeConstant.getStatusMessage(httpStatusCode);
	}

}
