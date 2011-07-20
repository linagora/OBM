package org.obm.push.backend;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obm.push.Device;
import org.obm.push.ItemChange;
import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendSession {

	private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

	private final Credentials credentials;
	private final String devId;
	private final Device device;
	private final String command;
	private final BigDecimal protocolVersion;
	private final Map<Integer, SyncState> lastClientSyncState;
	private final Map<Integer, Set<ItemChange>> unSynchronizedDeletedItemChangeByCollection;
	
	private String lastContinuationHandler;
	private String policyKey;

	private Map<Integer, SyncCollection> lastMonitored;
	private Map<String, String> lastSyncProcessedClientIds;

	public BackendSession(Credentials credentials, String devId, String command, Device device, BigDecimal protocolVersion) {
		super();
		this.credentials = credentials;
		this.devId = devId;
		this.command = command;
		this.device = device;
		this.protocolVersion = protocolVersion;
		this.unSynchronizedDeletedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.lastClientSyncState = new HashMap<Integer, SyncState>();
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

	public void setPolicyKey(String pKey) {
		this.policyKey = pKey;
	}

	public String getPolicyKey() {
		return policyKey;
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
	
	public void addUnSynchronizedDeletedItemChange(Integer collectionId, ItemChange change) {
		Set<ItemChange> deletes = unSynchronizedDeletedItemChangeByCollection.get(collectionId);
		if (deletes == null) {
			deletes = new HashSet<ItemChange>();
			unSynchronizedDeletedItemChangeByCollection.put(collectionId, deletes);
		}
		deletes.add(change);
	}

	public Set<ItemChange> getUnSynchronizedDeletedItemChange(
			Integer collectionId) {
		Set<ItemChange> ret = unSynchronizedDeletedItemChangeByCollection
				.get(collectionId);
		if (ret == null) {
			ret = new HashSet<ItemChange>();
		}
		return ret;
	}

	public SyncState getLastClientSyncState(Integer collectionId) {
		return lastClientSyncState.get(collectionId);
	}

	public void addLastClientSyncState(Integer collectionId, SyncState synckey) {
		lastClientSyncState.put(collectionId, synckey);
	}

	public void clear(Integer collectionId) {
		this.lastClientSyncState.remove(collectionId);
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
