package org.obm.push.store;

import java.sql.SQLException;

import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;

public interface ISyncStorage {

	void updateState(String loginAtDomain, String devId, Integer collectionId, SyncState state) throws SQLException;

	SyncState findStateForKey(String syncKey);

	Long findLastHearbeat(String loginAtDomain, String deviceId) throws SQLException;

	void updateLastHearbeat(String loginAtDomain, String deviceId, long hearbeat) throws SQLException;

	/**
	 * Stores device informations for the given user. Returns <code>true</code>
	 * if the device is allowed to synchronize.
	 */
	boolean initDevice(String loginAtDomain, String deviceId, String deviceType);

	PIMDataType getDataClass(String collectionId);
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId);
	
}
