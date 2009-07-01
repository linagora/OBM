package org.obm.caldav.server.exception;

/**
 * 
 * @author adrienp
 *
 */
public class AppendPropertyException extends CalDavException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3853898685469672383L;

	public AppendPropertyException(int httpStatusCode) {
		super(httpStatusCode);
	}

}
