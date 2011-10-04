package fr.aliacom.obm.freebusy;

/**
 * Exception thrown when we attempt to retrieve user's free/busy status, and
 * this information is not public.
 */
public class PrivateFreeBusyException extends FreeBusyException {
	public PrivateFreeBusyException() {
		super();
	}

	public PrivateFreeBusyException(String message) {
		super(message);
	}
}
