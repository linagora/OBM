package org.obm.push.exception;


public class QuotaExceededException extends Exception {

	private int quota;
	private final byte[] loadedData; 
	
	public QuotaExceededException(String message, int quota, byte[] loadedData) {
		super(message);
		this.quota = quota;
		this.loadedData = loadedData;
	}

	public int getQuota() {
		return quota;
	}
	
	public byte[] getLoadedData() {
		return loadedData;
	}
	
}
