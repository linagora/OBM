package org.obm.push.protocol.bean;

public enum AutodiscoverStatus {

	SUCCESS("1"),
	PROTOCOL_ERROR("2");
	
	private final String id;
	
	private AutodiscoverStatus(String id) {
		this.id = id;
	}

	/**
	 * 1 : Success. Because the Status element is only returned when the command encounters an error,
		the success status code is never included in a response message.<br>
	 * 2 : Protocol error
	 *
	 * @return status code that corresponds to the error
	 */
	public String asXmlValue() {
		return id;
	}
	
}
