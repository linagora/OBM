package org.obm.push.exception.activesync;

public class ServerItemNotFoundException extends ActiveSyncException {

	private Integer serverItemId;
	
	public ServerItemNotFoundException() {
		super();
	}
	
	public ServerItemNotFoundException(Integer collectionId) {
		super();
		this.serverItemId = collectionId;
	}

	public ServerItemNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerItemNotFoundException(String message) {
		super(message);
	}

	public ServerItemNotFoundException(Throwable cause) {
		super(cause);
	}

	public Integer getServerItemId() {
		return serverItemId;
	}
	
}
