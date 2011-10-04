package fr.aliacom.obm.freebusy;

public class FreeBusyException extends Exception {
	public FreeBusyException() {
		super();
	}

	public FreeBusyException(String message) {
		super(message);
	}

	public FreeBusyException(Throwable t) {
		super(t);
	}

	public FreeBusyException(String message, Throwable t) {
		super(message, t);
	}
}
