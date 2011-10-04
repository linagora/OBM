package org.obm.push.exception.activesync;


public class FolderTypeNotFoundException extends ActiveSyncException {
	
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
