package org.obm.push.exception;

import org.obm.push.store.ActiveSyncException;

public class XMLValidationException extends ActiveSyncException{

	public XMLValidationException() {
		super();
	}

	public XMLValidationException(Exception e) {
		super(e);
	}

}
