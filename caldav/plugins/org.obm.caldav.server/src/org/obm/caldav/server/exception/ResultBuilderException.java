package org.obm.caldav.server.exception;

/**
 * 
 * @author adrienp
 *
 */
public class ResultBuilderException extends CalDavException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1367293928291623452L;
		
	public ResultBuilderException(int httpStatusCode){
		super(httpStatusCode);
	}
	
}
