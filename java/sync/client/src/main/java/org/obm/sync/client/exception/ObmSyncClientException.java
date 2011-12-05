package org.obm.sync.client.exception;

public class ObmSyncClientException extends RuntimeException {
	
	public ObmSyncClientException(String msg) {
		super(msg);
	}
	
	public ObmSyncClientException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public ObmSyncClientException(Throwable ex) {
		super(ex);
	}
}
