/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.client.impl;

import javax.naming.NoPermissionException;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.exception.ContactNotFoundException;
import org.w3c.dom.Document;

public class SyncClientException {

	public void checkServerFaultException(Document doc) throws ServerFault {
		if (documentIsError(doc)) {
			throw new ServerFault( getErrorMessage(doc) );
		}
	}

	public void checkCreateContactException(Document doc) throws ServerFault, NoPermissionException {
		if (documentIsError(doc)) {
			throwNoPermissionException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkModifyContactException(Document doc) throws ContactNotFoundException, ServerFault, NoPermissionException {
		if (documentIsError(doc)) {
			throwContactNotFoundException(doc);
			throwNoPermissionException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkRemoveContactException(Document doc) throws ContactNotFoundException, ServerFault, NoPermissionException {
		if (documentIsError(doc)) {
			throwContactNotFoundException(doc);
			throwNoPermissionException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkContactNotFoundException(Document doc) throws ContactNotFoundException, ServerFault {
		if (documentIsError(doc)) {
			throwContactNotFoundException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkCreateEventException(Document doc) throws ServerFault, EventAlreadyExistException, NotAllowedException {
		if (documentIsError(doc)) {
			throwEventAlreadyExistException(doc);
			throwNotAllowedException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkEventNotFoundException(Document doc) throws ServerFault, EventNotFoundException, NotAllowedException {
		if (documentIsError(doc)) {
			throwEventNotFoundException(doc);
			throwNotAllowedException(doc);
			checkServerFaultException(doc);
		}
	}
	
	public void checkLoginExpection(Document doc) throws AuthFault {
		if (documentIsError(doc)) {
			throw new AuthFault(getErrorMessage(doc));
		}
	}
	
	public void checkNotAllowedException(Document doc) throws ServerFault, NotAllowedException {
		if (documentIsError(doc)) {
			throwNotAllowedException(doc);
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
	
	private void throwNotAllowedException(Document doc) throws NotAllowedException {
		String message = getErrorMessage(doc);
		String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
		if (NotAllowedException.class.getName().equals(type)) {
			throw new NotAllowedException(message);
		}
	}

	private void throwNoPermissionException(Document doc) throws NoPermissionException {
		String message = getErrorMessage(doc);
		String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
		if (NoPermissionException.class.getName().equals(type)) {
			throw new NoPermissionException(message);
		}
	}
	
	private boolean documentIsError(Document doc) {
		boolean isError = true;
		if (doc != null && doc.getDocumentElement() != null) {
			isError = doc.getDocumentElement().getNodeName().equals("error");
		}
		return isError;
	}
	
	private String getErrorMessage(Document doc) {
		return DOMUtils.getElementText(doc.getDocumentElement(), "message");
	}
	
}
