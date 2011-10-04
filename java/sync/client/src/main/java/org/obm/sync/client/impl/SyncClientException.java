package org.obm.sync.client.impl;

import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;

public class SyncClientException {

	public void checkServerFaultException(Document doc) throws ServerFault {
		if (documentIsError(doc)) {
			throw new ServerFault( getErrorMessage(doc) );
		}
	}

	public void checkContactNotFoundException(Document doc) throws ContactNotFoundException, ServerFault {
		if (documentIsError(doc)) {
			throwContactNotFoundException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkEventAlreadyExistException(Document doc) throws ServerFault, EventAlreadyExistException {
		if (documentIsError(doc)) {
			throwEventAlreadyExistException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkEventNotFoundException(Document doc) throws ServerFault, EventNotFoundException {
		if (documentIsError(doc)) {
			throwEventNotFoundException(doc);
			checkServerFaultException(doc);
		}
	}
	
	private void throwContactNotFoundException(Document doc) throws ContactNotFoundException {
		String message = getErrorMessage(doc);
		String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
		if (ContactNotFoundException.class.getName().equals(type)) {
			throw new ContactNotFoundException(message);
		}
	}
	
	private void throwEventAlreadyExistException(Document doc) throws EventAlreadyExistException {
		String message = getErrorMessage(doc);
		String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
		if (EventAlreadyExistException.class.getName().equals(type)) {
			throw new EventAlreadyExistException(message);
		}
	}
	
	private void throwEventNotFoundException(Document doc) throws EventNotFoundException {
		String message = getErrorMessage(doc);
		String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
		if (EventNotFoundException.class.getName().equals(type)) {
			throw new EventNotFoundException(message);
		}
	}

	private boolean documentIsError(Document doc) {
		return doc.getDocumentElement().getNodeName().equals("error");
	}
	
	private String getErrorMessage(Document doc) {
		return DOMUtils.getElementText(doc.getDocumentElement(), "message");
	}
	
}
