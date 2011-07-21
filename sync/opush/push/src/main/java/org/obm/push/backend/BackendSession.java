package org.obm.push.backend;

import java.math.BigDecimal;

import org.obm.push.Device;
import org.obm.push.impl.Credentials;

public class BackendSession {

	private final Credentials credentials;
	private final String devId;
	private final Device device;
	private final String command;
	private final BigDecimal protocolVersion;

	public BackendSession(Credentials credentials, String devId, String command, Device device, BigDecimal protocolVersion) {
		super();
		this.credentials = credentials;
		this.devId = devId;
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
		return devId;
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

}
