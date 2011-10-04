package fr.aliacom.obm.freebusy;

import org.obm.sync.calendar.FreeBusyRequest;

/**
 * Returns the availability of a user for a given period of time.
 */
public abstract interface FreeBusyProvider {

	public abstract String findFreeBusyIcs(FreeBusyRequest fbr) throws FreeBusyException;

}