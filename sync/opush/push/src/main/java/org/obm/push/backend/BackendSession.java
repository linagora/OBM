package org.obm.push.backend;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.obm.push.ItemChange;
import org.obm.push.store.FilterType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendSession {

	private static final Logger logger = LoggerFactory
			.getLogger(BackendSession.class);

	private String loginAtDomain;
	private String password;
	private String devId;
	private String devType;
	private String command;
	private Properties hints;
	private Map<Integer, Date> updatedSyncDate;
	private Map<Integer, Set<ItemChange>> unSynchronizedItemChangeByCollection;
	private Map<Integer, Set<ItemChange>> unSynchronizedDeletedItemChangeByCollection;
	private Map<Integer, SyncState> lastClientSyncState;
	private Map<Integer, FilterType> lastFilterTypeByCollectionId;
	private Sync lastSync;

	private String lastContinuationHandler;

	private double protocolVersion;

	private String policyKey;

	private Map<Integer, SyncCollection> lastMonitored;

	private Map<String, String> lastSyncProcessedClientIds;

	public BackendSession(String loginAtDomain, String password, String devId,
			String devType, String command) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.command = command;
		this.unSynchronizedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.unSynchronizedDeletedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.lastClientSyncState = new HashMap<Integer, SyncState>();
		this.updatedSyncDate = new HashMap<Integer, Date>();
		this.lastMonitored = new HashMap<Integer, SyncCollection>();
		this.lastFilterTypeByCollectionId = new HashMap<Integer, FilterType>();
		loadHints();
	}

	public void setHint(String key, boolean value) {
		hints.put(key, value);
	}

	public boolean checkHint(String key, boolean defaultValue) {
		if (!hints.containsKey(key)) {
			return defaultValue;
		} else {
			return "true".equals(hints.get(key));
		}
	}

	private void loadHints() {
		hints = new Properties();
		try {
			InputStream in = BackendSession.class.getClassLoader()
					.getResourceAsStream("hints/" + devType + ".hints");
			hints.load(in);
			in.close();
			logger.info("Loaded hints for " + devType);
		} catch (Throwable e) {
			logger.warn("could not load hints for device type " + devType);
		}
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public void setLoginAtDomain(String loginAtDomain) {
		this.loginAtDomain = loginAtDomain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Date getUpdatedSyncDate(Integer collectionId) {
		return updatedSyncDate.get(collectionId);
	}

	public void addUpdatedSyncDate(Integer collectionId, Date updatedSyncDate) {
		logger.info("addUpdatedSyncDate [" + collectionId + ", " + updatedSyncDate.toString() + " ]");
		this.updatedSyncDate.put(collectionId, updatedSyncDate);
	}

	public void setProtocolVersion(double parseInt) {
		this.protocolVersion = parseInt;
	}

	public double getProtocolVersion() {
		return protocolVersion;
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

	public Set<ItemChange> getUnSynchronizedItemChange(Integer collectionId) {
		Set<ItemChange> ret = unSynchronizedItemChangeByCollection
				.get(collectionId);
		if (ret == null) {
			ret = new HashSet<ItemChange>();
		}
		return ret;
	}

	public void addUnSynchronizedItemChange(Integer collectionId,
			ItemChange change) {
		Set<ItemChange> changes = unSynchronizedItemChangeByCollection
				.get(collectionId);
		if (changes == null) {
			changes = new HashSet<ItemChange>();
			unSynchronizedItemChangeByCollection.put(collectionId, changes);
		}
		changes.add(change);
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

	public void clearAll() {
		this.updatedSyncDate = new HashMap<Integer, Date>();
		this.unSynchronizedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.lastClientSyncState = new HashMap<Integer, SyncState>();
	}

	public void clear(Integer collectionId) {
		this.updatedSyncDate.remove(collectionId);
		this.unSynchronizedItemChangeByCollection.remove(collectionId);
		this.lastClientSyncState.remove(collectionId);
	}
	
	public Sync getLastSync() {
		return lastSync;
	}

	public void setLastSync(Sync lastSync) {
		this.lastSync = lastSync;
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

	public FilterType getLastFilterType(Integer collectionId) {
		return lastFilterTypeByCollectionId.get(collectionId);
	}

	public void addLastFilterType(Integer collectionId, FilterType filter) {
		this.lastFilterTypeByCollectionId.put(collectionId, filter);
	}
}
