package org.obm.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncPermsConfigurationService extends AbstractConfigurationService{

	private static final String BLACKLIST_USERS_PARAMS = "blacklist.users";
	
	private static final String ALLOW_UNKNOWN_PDA_PARAMS = "allow.unknown.pda";
	
	@Inject
	protected SyncPermsConfigurationService() {
		super("/etc/opush/sync_perms.ini");
	}

	public String getBlackListUser(){
		return getStringValue(BLACKLIST_USERS_PARAMS);
	}
	
	public Boolean allowUnknownPdaToSync(){
		return getBooleanValue(ALLOW_UNKNOWN_PDA_PARAMS);
	}
	
}
