package fr.aliacom.obm.common.calendar;

import org.obm.sync.auth.ServerFault;

public class ResourceNotFoundException extends ServerFault {

	public ResourceNotFoundException(String s) {
		super(s);
	}

}
