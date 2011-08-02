package org.obm.push.exception;


public class FolderTypeNotFoundException extends ActiveSyncException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public FolderTypeNotFoundException() {
		super();
	}

	public FolderTypeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FolderTypeNotFoundException(String message) {
		super(message);
	}

	public FolderTypeNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
