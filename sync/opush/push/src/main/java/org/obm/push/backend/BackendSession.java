package org.obm.push.backend;

import java.math.BigDecimal;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;

public class BackendSession {

	private final Credentials credentials;
	private final Device device;
	private final String command;
	private final BigDecimal protocolVersion;

	public BackendSession(Credentials credentials, String command, Device device, BigDecimal protocolVersion) {
		super();
		this.credentials = credentials;
		this.command = command;
		this.device = device;
		this.protocolVersion = protocolVersion;
	}

	public boolean checkHint(String key, boolean defaultValue) {
		return device.checkHint(key, defaultValue);
	}

	public String getLoginAtDomain() {
		return credentials.getLoginAtDomain();
	}

	public String getPassword() {
		return credentials.getPassword();
	}

	public String getDevId() {
		return device.getDevId();
	}

	public String getDevType() {
		return device.getDevType();
	}

	public String getCommand() {
		return command;
	}

	public BigDecimal getProtocolVersion() {
		return this.protocolVersion;
	}
	
	public Credentials getCredentials() {
		return credentials;
	}
	
	public Device getDevice(){
		return device;
	}

}
