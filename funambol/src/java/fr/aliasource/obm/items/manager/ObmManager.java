package fr.aliasource.obm.items.manager;

import java.util.List;
import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;

import fr.aliasource.funambol.utils.Helper;

public abstract class ObmManager {
	
	protected AccessToken token;
	
	
	protected boolean syncReceived = false;
	
	protected String restrictTest = "";
	protected int restrictions = Helper.RESTRICTS_DEFAULT;
	
	public abstract void initRestriction(int restrictions);

	public int getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(int restrictions) {
		this.restrictions = restrictions;
	}
	
}
