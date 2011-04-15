package fr.aliacom.obm;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class ToolBox {

	public static ObmUser getDefaultObmUser(){
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(obmDomain);
		return obmUser;
	}
	
}
