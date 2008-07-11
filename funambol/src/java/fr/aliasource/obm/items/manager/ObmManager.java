package fr.aliasource.obm.items.manager;

import java.util.List;
import java.util.Map;

import org.obm.sync.auth.AccessToken;

import fr.aliasource.funambol.utils.Helper;

public abstract class ObmManager {
	
	protected AccessToken token;
	
	protected Map  updatedRest = null;
	protected Map  added		= null;
	protected List deletedRest	= null;
	
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
	
	public String[] extractKeys(Map map) {
		return Helper.setToTab(map.keySet());
	}
}
