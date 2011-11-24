package org.obm.push.exception.activesync;

public class ServerItemNotFoundException extends ActiveSyncException {

	private String serverId;
	
	public ServerItemNotFoundException() {
		super();
	}
	
	public ServerItemNotFoundException(Throwable cause) {
		super(cause);
	}

	public ServerItemNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerItemNotFoundException(String serverId) {
		super();
		this.serverId = serverId;
	}

	public String getServerId() {
		return serverId;
	}
	
}
