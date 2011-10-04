package org.obm.push.bean;

import java.math.BigDecimal;

import com.google.common.base.Objects;

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

	@Override
	public final int hashCode(){
		return Objects.hashCode(credentials, device, command, protocolVersion);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof BackendSession) {
			BackendSession that = (BackendSession) object;
			return Objects.equal(this.credentials, that.credentials)
				&& Objects.equal(this.device, that.device)
				&& Objects.equal(this.command, that.command)
				&& Objects.equal(this.protocolVersion, that.protocolVersion);
		}
		return false;
	}

	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
			.add("credentials", credentials)
			.add("device", device)
			.add("command", command)
			.add("protocolVersion", protocolVersion)
			.toString();
	}
	
}
