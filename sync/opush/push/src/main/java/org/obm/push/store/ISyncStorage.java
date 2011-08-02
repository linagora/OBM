package org.obm.push.store;

import org.obm.push.bean.PIMDataType;

public interface ISyncStorage {

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
