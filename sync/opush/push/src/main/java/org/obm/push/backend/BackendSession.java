package org.obm.push.backend;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obm.push.Device;
import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

public class BackendSession {

	private final Credentials credentials;
	private final String devId;
	private final Device device;
	private final String command;
	private final BigDecimal protocolVersion;
	
	private String lastContinuationHandler;

	private Map<Integer, SyncCollection> lastMonitored;
	private Map<String, String> lastSyncProcessedClientIds;

	public BackendSession(Credentials credentials, String devId, String command, Device device, BigDecimal protocolVersion) {
		super();
		this.credentials = credentials;
		this.devId = devId;
		this.command = command;
		this.device = device;
		this.protocolVersion = protocolVersion;
		this.lastMonitored = new HashMap<Integer, SyncCollection>();
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
	
	public Map<Integer, SyncCollection> getLastMonitoredById() {
		return lastMonitored;
	}

	public Collection<SyncCollection> getLastMonitored() {
		return lastMonitored.values();
	}

	public void setLastMonitored(Map<Integer, SyncCollection> lastMonitored) {
		this.lastMonitored = lastMonitored;
	}

	public String getLastContinuationHandler() {
		return lastContinuationHandler;
	}

	public void setLastContinuationHandler(String lastContinuationHandler) {
		this.lastContinuationHandler = lastContinuationHandler;
	}

	public Map<String, String> getLastSyncProcessedClientIds() {
		return lastSyncProcessedClientIds;
	}

	public void setLastSyncProcessedClientIds(
			Map<String, String> lastSyncProcessedClientIds) {
		this.lastSyncProcessedClientIds = lastSyncProcessedClientIds;
	}

	public void setLastMonitored(Set<SyncCollection> toMonitor) {
		this.lastMonitored.clear();
		for(SyncCollection col : toMonitor){
			this.lastMonitored.put(col.getCollectionId(), col);
		}
	}

	public Credentials getCredentials() {
		return credentials;
	}

}
