package fr.aliasource.obm.items.manager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.obm.wauth.AccessToken;

public abstract class ObmManager {
	
	protected AccessToken token;
	
	protected Map  updatedRest = null;
	protected Map  added		= null;
	protected List deletedRest	= null;
	
	protected boolean syncReceived = false;
	
	protected String restrictTest = "";
	protected int restrictions = 1; //default restrict private
	
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
